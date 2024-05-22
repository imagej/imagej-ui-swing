/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2024 ImageJ developers.
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

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.*;
import java.util.*;
import java.util.concurrent.ExecutionException;

import net.imagej.ui.swing.updater.ViewOptions.Option;
import net.imagej.updater.*;
import net.imagej.updater.Conflicts.Conflict;
import net.imagej.updater.util.*;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.scijava.app.StatusService;
import org.scijava.command.CommandService;
import org.scijava.download.Download;
import org.scijava.download.DownloadService;
import org.scijava.event.ContextDisposingEvent;
import org.scijava.event.EventHandler;
import org.scijava.io.location.LocationService;
import org.scijava.log.LogService;
import org.scijava.log.Logger;
import org.scijava.plugin.Menu;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.ui.DialogPrompt;
import org.scijava.ui.UIService;
import org.scijava.util.AppUtils;
import org.scijava.util.PropertiesHelper;

import javax.swing.*;

import static org.scijava.ui.DialogPrompt.MessageType.QUESTION_MESSAGE;
import static org.scijava.ui.DialogPrompt.OptionType.YES_NO_OPTION;

/**
 * The Updater. As a command.
 * <p>
 * Incidentally, this class can be used as an out-of-ImageJ entry point to the
 * updater, as it does not *require* a StatusService to run.
 *
 * @author Johannes Schindelin
 */
@Plugin(type = UpdaterUI.class, menu = { @Menu(label = "Help"), @Menu(
	label = "Update...") })
public class ImageJUpdater implements UpdaterUI {

	private UpdaterFrame main;

	@Parameter
	private StatusService statusService;

	@Parameter
	private DownloadService downloadService;

	@Parameter
	private LocationService locationService;

	@Parameter
	private LogService log;

	@Parameter
	private UIService uiService;

	@Parameter
	private UploaderService uploaderService;

	@Parameter
	private CommandService commandService;

	private final static String UPDATER_UPDATING_THREAD_NAME =
		"Updating the Updater itself!";

	@Override
	public void run() {

		if (errorIfDebian()) return;

		if (log == null) {
			log = UpdaterUtil.getLogService();
		}

		if (errorIfNetworkInaccessible(log)) return;

		String imagejDirProperty = System.getProperty("imagej.dir");
		final File imagejRoot = imagejDirProperty != null ? new File(
			imagejDirProperty) : AppUtils.getBaseDirectory("ij.dir",
				FilesCollection.class, "updater");

		// -- Check for HTTPs support in Java --
		HTTPSUtil.checkHTTPSSupport(log);
		if (!HTTPSUtil.supportsHTTPS()) {
			main.warn(
				"Your Java might be too old to handle updates via HTTPS. This is a security risk!\n" +
					"Please download a recent version of this software.\n");
		}

		// check if there is a new Java update available
		updateJavaIfNecessary(imagejRoot);

		// -- Determine which files are governed by the updater --
		final FilesCollection files = new FilesCollection(log, imagejRoot);

		UpdaterUserInterface.set(new SwingUserInterface(log, statusService));

		if (!areWeUpdatingTheUpdater() && new File(imagejRoot, "update").exists()) {
			if (!UpdaterUserInterface.get().promptYesNo(
				"It is suggested that you restart ImageJ, then continue the update.\n" +
					"Alternately, you can attempt to continue the upgrade without\n" +
					"restarting, but ImageJ might crash.\n\n" + "Do you want to try it?",
				"Restart required to finalize update")) return;
			try {
				new Installer(files, null).moveUpdatedIntoPlace();
			}
			catch (IOException e) {
				log.debug(e);
				UpdaterUserInterface.get().error("Could not move files into place: " +
					e);
				return;
			}
		}
		UpdaterUtil.useSystemProxies();
		Authenticator.setDefault(new SwingAuthenticator());

		SwingTools.invokeOnEDT(() -> main = new UpdaterFrame(log, uploaderService,
			files));

		main.setEasyMode(true);
		Progress progress = main.getProgress("Starting up...");

		try {
			files.tryLoadingCollection();
			refreshUpdateSites(files);
			main.updateFilesTable();
			String warnings = files.reloadCollectionAndChecksum(progress);
			main.checkWritable();
			main.addCustomViewOptions();
			if (!warnings.equals("")) main.warn(warnings);
			final List<Conflict> conflicts = files.getConflicts();
			if (conflicts != null && conflicts.size() > 0 && !new ConflictDialog(main,
				"Conflicting Versions")
			{

				private static final long serialVersionUID = 1L;

				@Override
				protected void updateConflictList() {
					conflictList = conflicts;
				}
			}.resolve()) return;
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

		if (!areWeUpdatingTheUpdater() && Installer.isTheUpdaterUpdateable(files,
			commandService))
		{
			try {
				// download just the updater
				Installer.updateTheUpdater(files, main.getProgress(
					"Installing the updater..."), commandService);
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
			for (FileObject component : Installer.getUpdaterFiles(files,
				commandService, false))
			{
				final File updated = files.prefixUpdate(component.getFilename(false));
				if (updated.exists()) try {
					classPath.add(updated.toURI().toURL());
					continue;
				}
				catch (MalformedURLException e) {
					log.error(e);
				}
				final String name = component.getLocalFilename(false);
				File file = files.prefix(name);
				try {
					classPath.add(file.toURI().toURL());
				}
				catch (MalformedURLException e) {
					log.error(e);
				}
			}
			try {
				log.info("Trying to install and execute the new updater");
				final URL[] urls = classPath.toArray(new URL[classPath.size()]);
				URLClassLoader remoteClassLoader = new URLClassLoader(urls, getClass()
					.getClassLoader().getParent());
				Class<?> runnable = remoteClassLoader.loadClass(ImageJUpdater.class
					.getName());
				final Thread thread = new Thread((Runnable) runnable.newInstance());
				thread.setName(UPDATER_UPDATING_THREAD_NAME);
				thread.start();
				thread.join();
				return;
			}
			catch (Throwable t) {
				log.error(t);
			}

			main.info(
				"Please restart ImageJ and call Help>Update to continue with the update");
			return;
		}

		try {
			final String missingUploaders = main.files.protocolsMissingUploaders(main
				.getUploaderService(), main.getProgress(null));
			if (missingUploaders != null) {
				main.warn(missingUploaders);
			}
		}
		catch (final IllegalArgumentException e) {
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

	/**
	 * Helper method to download and extract the appropriate JDK for this platform
	 * to the corresponding ImageJ java subdirectory.
	 */
	private boolean updateJava(final Map<String, String> jdkVersions,
		final File imagejRoot)
	{
		// Download and unzip the new JDK
		final String platform = UpdaterUtil.getPlatform();
		final String jdkUrl = jdkVersions.get(platform);
		final String jdkName = jdkUrl.substring(jdkUrl.lastIndexOf("/") + 1);
		final File jdkDir = new File(imagejRoot + File.separator + "java" +
			File.separator + platform);

		if (!jdkDir.exists() && !jdkDir.mkdirs()) {
			log.error("Unable to create platform Java directory: " + jdkDir);
			return false;
		}

		// Download the JDK
		final File jdkDlLoc = new File(jdkDir.getAbsolutePath() + File.separator +
			jdkName);
		jdkDlLoc.deleteOnExit();
		try {
			log.debug("Downloading " + locationService.resolve(jdkUrl) + " to " +
				locationService.resolve(jdkDlLoc.toURI()));
			Download download = downloadService.download(locationService.resolve(
				jdkUrl), locationService.resolve(jdkDlLoc.toURI()));
			download.task().waitFor();
		}
		catch (URISyntaxException | ExecutionException | InterruptedException e) {
			log.error(e);
			return false;
		}

		String javaLoc = jdkDlLoc.getAbsolutePath();
		int extensionLength = 0;

		// Extract the JDK
		if (jdkDlLoc.toString().endsWith("tar.gz")) {
			try (FileInputStream fis = new FileInputStream(jdkDlLoc);
					GzipCompressorInputStream gzIn = new GzipCompressorInputStream(fis);
					TarArchiveInputStream tarIn = new TarArchiveInputStream(gzIn))
			{
				doExtraction(jdkDir, tarIn);
				extensionLength = 7;
			}
			catch (IOException e) {
				log.error(e);
				return false;
			}
		}
		else if (jdkDlLoc.toString().endsWith("zip")) {
			try (FileInputStream fis = new FileInputStream(jdkDlLoc);
					ZipArchiveInputStream zis = new ZipArchiveInputStream(fis))
			{
				doExtraction(jdkDir, zis);
				extensionLength = 4;
			}
			catch (IOException e) {
				log.error(e);
				return false;
			}
		}

		// Notify user of success
		uiService.showDialog("Java version updated!" +
			" Please restart to take advantage of the new Java.",
			DialogPrompt.MessageType.INFORMATION_MESSAGE);

		// Update the app configuration file to use the newly downloaded JDK
		javaLoc = javaLoc.substring(0, javaLoc.length() - extensionLength);
		String exeName = System.getProperty("ij.executable");
		if (exeName != null && !exeName.trim().isEmpty()) {
			exeName = exeName.substring(exeName.lastIndexOf(File.separator));
			exeName = exeName.substring(0, exeName.indexOf("-"));
			final File appCfg = new File(imagejRoot + File.separator + "config" +
				File.separator + "jaunch" + File.separator + exeName + ".cfg");
			Map<String, String> appProps = appCfg.exists() ? PropertiesHelper.get(
				appCfg) : new HashMap<>();
			appProps.put("jvm.app-configured", javaLoc);
			PropertiesHelper.put(appProps, appCfg);
		}
		return true;
	}

	/**
	 * Helper method to extract an archive
	 */
	private void doExtraction(final File jdkDir, final ArchiveInputStream tarIn)
		throws IOException
	{
		ArchiveEntry entry;
		while ((entry = tarIn.getNextEntry()) != null) {
			if (entry.isDirectory()) {
				new File(jdkDir, entry.getName()).mkdirs();
			}
			else {
				byte[] buffer = new byte[1024];
				File outputFile = new File(jdkDir, entry.getName());
				OutputStream fos = new FileOutputStream(outputFile);
				int len;
				while ((len = tarIn.read(buffer)) != -1) {
					fos.write(buffer, 0, len);
				}
				fos.close();
			}
		}
	}

	/**
	 * Helper method that checks the remote JDK list and compares to a locally
	 * cached version. If the remote list is newer an available Java update is
	 * indicated. If the user agrees, the new JDK is downloaded and extracted to
	 * the appropriate directory.
	 */
	private void updateJavaIfNecessary(final File imagejRoot) {
		final File configDir = new File(imagejRoot.getAbsolutePath() +
			File.separator + "config" + File.separator + "jaunch");
		final File jdkUrlConf = new File(configDir.getAbsolutePath() +
			File.separator + "jdk-urls.cfg");
		final String modifiedKey = "LAST_MODIFIED";
		final String jdkUrl = "https://downloads.imagej.net/java/jdk-urls.txt";
		long lastModifiedRemote;

		// Get the last modified time on the remote JDK list
		try {
			HttpURLConnection connection = (HttpURLConnection) new URL(jdkUrl)
				.openConnection();
			connection.setRequestMethod("HEAD");
			lastModifiedRemote = connection.getLastModified();
		}
		catch (IOException e) {
			log.error("Unable to read remote JDK list", e);
			return;
		}

		// Make the config dir if it doesn't already exist
		if (!configDir.exists() && !configDir.mkdirs()) {
			log.error("Unable to create configuration directory: " + configDir);
			return;
		}

		// Check if we've already cached a local version of the JDK list
		if (jdkUrlConf.exists()) {
			// check when the remote was last modified
			Map<String, String> jdkVersionProps = PropertiesHelper.get(jdkUrlConf);
			if (lastModifiedRemote == 0) { // 0 means "not provided"
				log.error("No modification date found in jdk-urls.txt");
				return;
			}
			long lastModifiedLocal = Long.parseLong(jdkVersionProps.getOrDefault(
				modifiedKey, "0"));

			// return if up to date
			if (lastModifiedLocal == lastModifiedRemote) return;

			// Otherwise delete the conf file and re-download
			jdkUrlConf.delete();
		}

		// Download the new properties file
		try {
			Download dl = downloadService.download(locationService.resolve(jdkUrl),
				locationService.resolve(jdkUrlConf.toURI()));
			dl.task().waitFor();
		}
		catch (URISyntaxException e) {
			log.error("Failed to download the remote JDK url list: bad URI");
			return;
		}
		catch (ExecutionException | InterruptedException e) {
			log.error(
				"Failed to download the remote JDK url list: download task failed");
			return;
		}

		// Inject the last modification date to the JDK list
		Map<String, String> jdkUrlMap = PropertiesHelper.get(jdkUrlConf);
		jdkUrlMap.put(modifiedKey, Long.toString(lastModifiedRemote));

		// Ask the user if they would like to proceed with a Java update
		DialogPrompt.Result result = uiService.showDialog(
			"A newer version of Java is recommended.\n" +
				"Downloading this may take longer than normal updates, but will " +
				"eventually be required for continued updates.\n" +
				"Would you like to update now?", QUESTION_MESSAGE, YES_NO_OPTION);

		// Do the update, if desired
		if (result == DialogPrompt.Result.YES_OPTION && updateJava(jdkUrlMap,
			imagejRoot))
		{
			// Store the current url list if we updated Java
			PropertiesHelper.put(jdkUrlMap, jdkUrlConf);
		}
	}

	private void refreshUpdateSites(FilesCollection files)
		throws InterruptedException, InvocationTargetException
	{
		List<URLChange> changes = AvailableSites.initializeAndAddSites(files,
			(Logger) log);
		if (ReviewSiteURLsDialog.shouldBeDisplayed(changes)) {
			ReviewSiteURLsDialog dialog = new ReviewSiteURLsDialog(main, changes);
			SwingUtilities.invokeAndWait(() -> dialog.setVisible(true));
			if (dialog.isOkPressed()) AvailableSites.applySitesURLUpdates(files,
				changes);
		}
		else AvailableSites.applySitesURLUpdates(files, changes);
	}

	@EventHandler
	private void onEvent(final ContextDisposingEvent e) {
		if (main != null && main.isDisplayable()) main.dispose();
	}

	protected boolean overwriteWithUpdated(final FilesCollection files,
		final FileObject file)
	{
		File downloaded = files.prefix("update/" + file.filename);
		if (!downloaded.exists()) return true; // assume all is well if there is no
																						// updated file
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
		return UPDATER_UPDATING_THREAD_NAME.equals(Thread.currentThread()
			.getName());
	}

	public static void main(String[] args) {
		new ImageJUpdater().run();
	}
}
