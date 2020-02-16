/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2020 ImageJ developers.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

package net.imagej.ui.swing.updater;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import net.imagej.ui.swing.updater.ViewOptions.Option;
import net.imagej.updater.*;
import net.imagej.updater.Conflicts.Conflict;
import net.imagej.updater.util.*;

import org.scijava.app.StatusService;
import org.scijava.command.CommandService;
import org.scijava.event.ContextDisposingEvent;
import org.scijava.event.EventHandler;
import org.scijava.log.LogService;
import org.scijava.log.Logger;
import org.scijava.plugin.Menu;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.util.AppUtils;

import javax.swing.*;

/**
 * The Updater. As a command.
 * <p>
 * Incidentally, this class can be used as an out-of-ImageJ entry point to the
 * updater, as it does not *require* a StatusService to run.
 * 
 * @author Johannes Schindelin
 */
@Plugin(type = UpdaterUI.class, menu = { @Menu(label = "Help"),
	@Menu(label = "Update...") })
public class ImageJUpdater implements UpdaterUI {
	private UpdaterFrame main;

	@Parameter
	private StatusService statusService;

	@Parameter
	private LogService log;

	@Parameter
	private UploaderService uploaderService;

	@Parameter
	private CommandService commandService;

	private final static String UPDATER_UPDATING_THREAD_NAME = "Updating the Updater itself!";

	@Override
	public void run() {

		if (errorIfDebian()) return;

		if (log == null) {
			log = UpdaterUtil.getLogService();
		}

		if (errorIfNetworkInaccessible(log)) return;

		String imagejDirProperty = System.getProperty("imagej.dir");
		final File imagejRoot = imagejDirProperty != null ? new File(imagejDirProperty) :
			AppUtils.getBaseDirectory("ij.dir", FilesCollection.class, "updater");
		final FilesCollection files = new FilesCollection(log, imagejRoot);

		UpdaterUserInterface.set(new SwingUserInterface(log, statusService));

		if (!areWeUpdatingTheUpdater() && new File(imagejRoot, "update").exists()) {
			if (!UpdaterUserInterface.get().promptYesNo("It is suggested that you restart ImageJ, then continue the update.\n"
					+ "Alternately, you can attempt to continue the upgrade without\n"
					+ "restarting, but ImageJ might crash.\n\n"
					+ "Do you want to try it?",
					"Restart required to finalize update"))
				return;
			try {
				new Installer(files, null).moveUpdatedIntoPlace();
			} catch (IOException e) {
				log.debug(e);
				UpdaterUserInterface.get().error("Could not move files into place: " + e);
				return;
			}
		}
		UpdaterUtil.useSystemProxies();
		Authenticator.setDefault(new SwingAuthenticator());

		SwingTools.invokeOnEDT(new Runnable() {
			@Override
			public void run() {
				main = new UpdaterFrame(log, uploaderService, files);
			}
		});

		main.setEasyMode(true);
		Progress progress = main.getProgress("Starting up...");

		try {
			files.tryLoadingCollection();
			HTTPSUtil.checkHTTPSSupport(log);
			if(!HTTPSUtil.supportsHTTPS()) {
				main.warn("Your Java might be too old to handle updates via HTTPS. This is a security risk!\n" +
						"Please download a recent version of this software.\n");
			}
			refreshUpdateSites(files);
			String warnings = files.reloadCollectionAndChecksum(progress);
			main.checkWritable();
			main.addCustomViewOptions();
			if (!warnings.equals("")) main.warn(warnings);
			final List<Conflict> conflicts = files.getConflicts();
			if (conflicts != null && conflicts.size() > 0 &&
					!new ConflictDialog(main, "Conflicting versions") {
						private static final long serialVersionUID = 1L;

						@Override
						protected void updateConflictList() {
							conflictList = conflicts;
						}
					}.resolve())
				return;
		}
		catch (final UpdateCanceledException e) {
			main.error("Canceled");
			return;
		}
		catch (final Exception e) {
			log.error(e);
			String message;
			if (e instanceof UnknownHostException) message =
				"Failed to lookup host " + e.getMessage();
			else message = "There was an error reading the cached metadata: " + e;
			main.error(message);
			return;
		}

		if (!areWeUpdatingTheUpdater() && Installer.isTheUpdaterUpdateable(files, commandService)) {
			try {
				// download just the updater
				Installer.updateTheUpdater(files, main.getProgress("Installing the updater..."), commandService);
			}
			catch (final UpdateCanceledException e) {
				main.error("Canceled");
				return;
			}
			catch (final IOException e) {
				main.error("Installer failed: " + e);
				return;
			}

			// make a class path using the updated files
			final List<URL> classPath = new ArrayList<>();
			for (FileObject component : Installer.getUpdaterFiles(files, commandService, false)) {
				final File updated = files.prefixUpdate(component.getFilename(false));
				if (updated.exists()) try {
					classPath.add(updated.toURI().toURL());
					continue;
				} catch (MalformedURLException e) {
					log.error(e);
				}
				final String name = component.getLocalFilename(false);
				File file = files.prefix(name);
				try {
					classPath.add(file.toURI().toURL());
				} catch (MalformedURLException e) {
					log.error(e);
				}
			}
			try {
				log.info("Trying to install and execute the new updater");
				final URL[] urls = classPath.toArray(new URL[classPath.size()]);
				URLClassLoader remoteClassLoader = new URLClassLoader(urls, getClass().getClassLoader().getParent());
				Class<?> runnable = remoteClassLoader.loadClass(ImageJUpdater.class.getName());
				final Thread thread = new Thread((Runnable)runnable.newInstance());
				thread.setName(UPDATER_UPDATING_THREAD_NAME);
				thread.start();
				thread.join();
				return;
			} catch (Throwable t) {
				log.error(t);
			}

			main.info("Please restart ImageJ and call Help>Update to continue with the update");
			return;
		}

		try {
			final String missingUploaders = main.files.protocolsMissingUploaders(main.getUploaderService(), main.getProgress(null));
			if (missingUploaders != null) {
				main.warn(missingUploaders);
			}
		} catch (final IllegalArgumentException e) {
			e.printStackTrace();
		}

		main.setLocationRelativeTo(null);
		main.setVisible(true);
		main.requestFocus();

		files.markForUpdate(false);
		main.setViewOption(Option.UPDATEABLE);
		if (files.hasForcableUpdates()) {
			main.warn("There are locally modified files!");
			if (files.hasUploadableSites() && !files.hasChanges()) {
				main.setViewOption(Option.LOCALLY_MODIFIED);
				main.setEasyMode(false);
			}
		}
		else if (!files.hasChanges()) main.info("Your ImageJ is up to date!");

		main.updateFilesTable();
	}

	private void refreshUpdateSites(FilesCollection files)
			throws InterruptedException, InvocationTargetException
	{
		List<URLChange>
				changes = AvailableSites.initializeAndAddSites(files, (Logger) log);
		if(ReviewSiteURLsDialog.shouldBeDisplayed(changes)) {
			ReviewSiteURLsDialog dialog = new ReviewSiteURLsDialog(main, changes);
			SwingUtilities.invokeAndWait(() -> dialog.setVisible(true));
			if(dialog.isOkPressed())
				AvailableSites.applySitesURLUpdates(files, changes);
		}
		else
			AvailableSites.applySitesURLUpdates(files, changes);
	}

	@EventHandler
	private void onEvent(final ContextDisposingEvent e) {
		if (main != null && main.isDisplayable()) main.dispose();
	}

	protected boolean overwriteWithUpdated(final FilesCollection files,
		final FileObject file)
	{
		File downloaded = files.prefix("update/" + file.filename);
		if (!downloaded.exists()) return true; // assume all is well if there is no updated file
		final File jar = files.prefix(file.filename);
		if (!jar.delete() && !moveOutOfTheWay(jar)) return false;
		if (!downloaded.renameTo(jar)) return false;
		for (;;) {
			downloaded = downloaded.getParentFile();
			if (downloaded == null) return true;
			final String[] list = downloaded.list();
			if (list != null && list.length > 0) return true;
			// dir is empty, remove
			if (!downloaded.delete()) return false;
		}
	}

	/**
	 * This returns true if this seems to be the Debian packaged version of
	 * ImageJ, or false otherwise.
	 */

	public static boolean isDebian() {
		final String debianProperty = System.getProperty("fiji.debian");
		return debianProperty != null && debianProperty.equals("true");
	}

	/**
	 * If this seems to be the Debian packaged version of ImageJ, then produce an
	 * error and return true. Otherwise return false.
	 */
	public static boolean errorIfDebian() {
		// If this is the Debian / Ubuntu packaged version, then
		// insist that the user uses apt-get / synaptic instead:
		if (isDebian()) {
			String message = "You are using the Debian packaged version of ImageJ.\n";
			message +=
				"You should update ImageJ with your system's usual package manager instead.";
			UpdaterUserInterface.get().error(message);
			return true;
		}
		return false;
	}

	/**
	 * If there is no network connection, then produce an error and return true.
	 * Otherwise return false.
	 */
	public static boolean errorIfNetworkInaccessible(final LogService log) {
		try {
			testNetworkConnection();
		}
		catch (final SecurityException | IOException exc) {
			final String msg = exc.getMessage();
			String friendlyError = "Cannot connect to the Internet.";
			if (msg != null && msg.indexOf(
				"Address family not supported by protocol family: connect") >= 0)
			{
				friendlyError += "" + //
					"\n-----------------------------------------------------------" + //
					"\n* Check your computer for spyware called RelevantKnowledge" + //
					"\n* Try disabling your antivirus software temporarily" + //
					"\n* Try disabling IPv6 temporarily" + //
					"\n* See also http://forum.imagej.net/t/5070" + //
					"\n-----------------------------------------------------------";
			}
			friendlyError += "" + //
				"\nDo you have a network connection?" + //
				"\nAre your proxy settings correct?";
			UpdaterUserInterface.get().error(friendlyError);
			if (log != null) log.error(exc);
			return true;
		}
		return false;
	}

	/**
	 * Check whether we can connect to the Internet. If we cannot connect, we will
	 * not be able to update.
	 * 
	 * @throws IOException if anything goes wrong.
	 */
	private static void testNetworkConnection() throws IOException {
		// NB: Remember initial static state, to be reset afterward.
		final boolean followRedirects = HttpURLConnection.getFollowRedirects();

		try {
			HttpURLConnection.setFollowRedirects(false);
			final URL url = new URL("http://imagej.net/");
			final URLConnection urlConn = url.openConnection();
			if (!(urlConn instanceof HttpURLConnection)) {
				throw new IOException("Unexpected connection type: " + //
						urlConn.getClass().getName());
			}
			final HttpURLConnection httpConn = (HttpURLConnection) urlConn;

			// Perform some sanity checks.
			final int code = httpConn.getResponseCode();
			if (code != 301) {
				throw new IOException("Unexpected response code: " + code);
			}
			final String message = httpConn.getResponseMessage();
			if (!"Moved Permanently".equals(message)) {
				throw new IOException("Unexpected response message: " + message);
			}
			final long length = httpConn.getContentLengthLong();
			if (length < 250 || length > 500) {
				throw new IOException("Unexpected response length: " + length);
			}

			// Header looks reasonable; now let's check the content to be sure.
			final byte[] content = new byte[(int) length];
			try (final DataInputStream din = //
					new DataInputStream(httpConn.getInputStream()))
			{
				din.readFully(content);
			}
			final String s = new String(content, "UTF-8");
			if (!s.matches("(?s).*<html>.*" +
					"<head>.*<title>301 Moved Permanently</title>.*</head>.*" + //
					"<body>.*<h1>Moved Permanently</h1>.*" + //
					"<a href=\"http://imagej.net/Welcome\">" + //
					".*</body></html>.*"))
			{
				throw new IOException("Unexpected response:\n" + s);
			}
		}
		finally {
			// NB: Reset static state back to previous.
			if (followRedirects != HttpURLConnection.getFollowRedirects()) {
				HttpURLConnection.setFollowRedirects(followRedirects);
			}
		}
	}

	protected static boolean moveOutOfTheWay(final File file) {
		if (!file.exists()) return true;
		File backup = new File(file.getParentFile(), file.getName() + ".old");
		if (backup.exists() && !backup.delete()) {
			final int i = 2;
			for (;;) {
				backup = new File(file.getParentFile(), file.getName() + ".old" + i);
				if (!backup.exists()) break;
			}
		}
		return file.renameTo(backup);
	}

	private boolean areWeUpdatingTheUpdater() {
		return UPDATER_UPDATING_THREAD_NAME.equals(Thread.currentThread().getName());
	}

	public static void main(String[] args) {
		new ImageJUpdater().run();
	}
}
