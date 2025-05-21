/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2025 ImageJ developers.
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

import net.imagej.updater.*;
import net.imagej.updater.util.Platforms;
import org.scijava.Context;
import org.scijava.app.AppService;
import org.scijava.launcher.Java;
import org.scijava.launcher.Versions;
import org.scijava.log.LogService;
import org.scijava.log.Logger;
import org.scijava.ui.ApplicationFrame;
import org.scijava.ui.DialogPrompt;
import org.scijava.ui.UIService;
import org.scijava.ui.UserInterface;
import org.scijava.widget.UIComponent;
import org.xml.sax.SAXException;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import java.awt.Window;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Absurdly complex logic for helping users transition
 * safely from the old trio of ImageJ/Fiji/Java-8 update sites to the new
 * Fiji-latest site.
 *
 * @author Curtis Rueden
 */
class LauncherMigrator {

	private static final List<String> ARM32 = Arrays.asList("aarch32", "arm32");
	private static final List<String> ARM64 = Arrays.asList("aarch64", "arm64");
	private static final List<String> X32 =
		Arrays.asList("i386", "i486", "i586", "i686", "x86-32", "x86_32", "x86");
	private static final List<String> X64 =
		Arrays.asList("amd64", "x86-64", "x86_64", "x64");
	private static final boolean OS_WIN, OS_MACOS, OS_LINUX;
	private static final String OS, ARCH;
	private static final String NEW_FIJI_SITE = "Fiji-latest";

	static {
		OS = System.getProperty("os.name");
		OS_WIN = OS.toLowerCase().contains("windows");
		OS_MACOS = OS.toLowerCase().contains("mac");
		OS_LINUX = OS.toLowerCase().contains("linux");
		String osArch = System.getProperty("os.arch").toLowerCase();
		if (ARM32.contains(osArch)) ARCH = "arm32";
		else if (ARM64.contains(osArch)) ARCH = "arm64";
		else if (X32.contains(osArch)) ARCH = "x32";
		else if (X64.contains(osArch)) ARCH = "x64";
		else ARCH = osArch;
	}

	private AppService appService;
	private UIService uiService;
	private LogService log;

	LauncherMigrator(Context ctx) {
		if (ctx == null) return;
		appService = ctx.getService(AppService.class);
		uiService = ctx.getService(UIService.class);
		log = ctx.getService(LogService.class);
	}

	/**
	 * Figures out what's going on with the application's launch situation.
	 * <ul>
	 * <li>If the Fiji-latest site is not active, call
	 *   {@link #switchToFijiLatest(FilesCollection)}.</li>
	 * <li>Do nothing if Fiji-latest already active, or launched in some other creative way.</li>
	 * </ul>
	 */
	void checkLaunchStatus() {
		// Check whether *either* launcher (old or new) launched the app.
		// Both launchers set one of these telltale properties.
		boolean launcherUsed =
			System.getProperty("ij.executable") != null ||
				System.getProperty("fiji.executable") != null;
		if (!launcherUsed) return; // Program was launched in some creative way.

		FilesCollection files = new FilesCollection(log, ImageJUpdater.getAppDirectory());

		// Initialize the FilesCollection
		try {
			files.tryLoadingCollection();
		}
		catch (SAXException | ParserConfigurationException e) {
			throw new RuntimeException(e);
		}

		// If the new single Fiji update site is not active, proceed to upgrade
		boolean fijiSiteActive = false;
		// TODO update to account for European mirror (when it exists)
		for (UpdateSite site : files.getUpdateSites(false)) {
			if (site.getURL().equals("https://sites.imagej.net/Fiji/")) {
				fijiSiteActive = true;
				break;
			}
		}
		if (!fijiSiteActive) {
			// TODO remove this opt-in env var check for complete rollout
			if (System.getenv("UPGRADE_IMAGEJ") != null) switchToFijiLatest(files);
		}
	}

	/**
	 * Warns users to update any shortcuts to reference the new launcher.
	 * Currently, this is recommended to be done manually.
	 * <p>
	 * The macOS-specific {@code Contents/Info.plist} is handled separately
	 * by the Updater, as part of the switch to the new update site.
	 * </p>
	 * <p>
	 * Linux users need to change any {@code .desktop} files.
	 * </p>
	 * <p>
	 * Windows is particularly complicated, because:
	 * A) we don't know where the user put their Fiji shortcuts&mdash;they
	 * could be in the Start menu, on the desktop, or elsewhere and pinned
	 * to the taskbar; and B) the .lnk file format is an opaque binary
	 * format and I'm not going to code up something to hack it just for
	 * this transitional migration code that will be obsolete in a few months.
	 * </p>
	 * <p>
	 * Note that launchers will be renamed with the {@code .backup} extension in
	 * case there are any missed shortcuts, so that launch fails fast at the
	 * OS level rather than potentially exploding at the application level; see
	 * {@link #queueRestart(Path, String, Path)}
	 * </p>
	 */
	private void warnAboutShortcuts(Path oldExePath, String newExePath) {
		uiService.showDialog(
				"As part of this update, the Fiji launcher is being upgraded\n" +
								"to a completely new version. Therefore any shortcuts referring\n" +
								"to the old launcher will need to be updated.\n" +
								"(e.g. start menu entries, taskbar pins, desktop shortcuts, etc...)\n\n" +
								"Old launcher path:\n" +
								oldExePath + "\n\n" +
								"New launcher path:\n" +
								newExePath,
				"Reminder: update shortcuts!",
				DialogPrompt.MessageType.WARNING_MESSAGE);
	}

		/**
		 * Inform the user about the pros and cons of to the latest update site.
		 * Then, if they should elect to switch, upgrades the managed Java to the
		 * recommended one and finally relaunches with the new launcher.
		 */
	private void switchToFijiLatest(FilesCollection files) {

		// Check whether the user has silenced the launcher upgrade prompt.
		String prefKey = "skipFijiLatestUpgradePrompt";
		Preferences prefs = Preferences.userNodeForPackage(getClass());
		boolean skipPrompt = prefs.getBoolean(prefKey, false);
		if (skipPrompt) {
			// User previously said to "never ask again".
			if (log != null) log.debug("Skipping launcher upgrade due to user preference");
			return;
		}

		// Define the title of the application.
		// Note: I wanted to use the AppService for this.
		// But the net.imagej.app.TopLevelImageJApp in net.imagej:imagej
		// has higher priority than the sc.fiji.app.FijiApp in sc.fiji:fiji,
		// so appService.getApp().getTitle() does not yield the needed string.
		// In practice, the only launcher migration path we are supporting
		// with this code is ImageJ-<platform> -> fiji-<platform>, nothing else.
		String appTitle = "Fiji";

		// Discern the application base directory.
		File appDir = appService == null ?
			ImageJUpdater.getAppDirectory() :
			appService.getApp().getBaseDirectory();
		if (appDir == null) {
			if (log != null) log.debug("Cannot glean base directory");
			return;
		}
		appDir = appDir.getAbsoluteFile();
		String appSlug = appTitle.toLowerCase();
		File configDir = appDir.toPath().resolve("config").resolve("jaunch").toFile();
		File jarDir = appDir.toPath().resolve("jars").toFile();

		// Test whether the new launcher is likely to work on this system.
		String nljv;
		try {
			nljv = probeJavaVersion(appDir, jarDir, appSlug);
			if (log != null) log.debug("Java from new launcher BEFORE: " + nljv);
		}
		catch (IOException exc) {
			// Something bad happened invoking the new launcher.
			// We inform the user and ask for a bug report, then give up.
			askForBugReport(log, appTitle, appSlug, exc);
			return;
		}
		catch (UnsupportedOperationException exc) {
			// No new executable launcher is available for this platform.
			// We give up, because there is nothing to switch over to.
			if (log != null) log.debug(exc);
			return;
		}

		// OK, we've gotten far enough that it's time to ask the
		// user whether they want to make the switch.

		String message = "<html>" +
			"<style>" +
			"table {" +
			"border-top: 1px solid gray;" +
			"border-bottom: 1px solid gray;" +
			"}" +
			"tr th { font-size: large }" +
			"tr td { background-color: #eeeeee }" +
			"tr.odd td { background-color: #dddddd }" +
			".shiny { color: #5599aa; font-weight: bold }" +
			"</style>" +
			"<center><span class=\"shiny\">&#x2728;</span>" +
			" Heads up: " + appTitle + " is receiving some major updates under the hood! " +
			"<span class=\"shiny\">&#x2728;</span></center>" +
			"<br><p>You are currently running the <b>stable</b> version of " + appTitle +
			", but you now have the option to switch<br>to the <b>latest</b> version. " +
			"To help you decide, here is a table summarizing the differences:</p>" +
			"<br><center><table>" +
			"<tr><th>Feature</th>                           <th>" + appTitle + " Stable</th>         <th>" + appTitle + " Latest</th></tr>" +
			"<tr class=\"odd\"><td>Stability</td>           <td>More</td>                            <td>Less</td></tr>" +
			"<tr              ><td>Java version</td>        <td>OpenJDK 8</td>                       <td>OpenJDK 21</td></tr>" +
			"<tr class=\"odd\"><td>Launcher</td>            <td>ImageJ Launcher (deprecated)</td>    <td>Jaunch</td></tr>" +
			"<tr              ><td>Executable</td>          <td>ImageJ-*(.exe)</td>                  <td>fiji-*-*(.exe)</td></tr>" +
			"<tr class=\"odd\"><td>Core update site(s)</td> <td>ImageJ+Fiji+Java-8</td>              <td>sites.imagej.net/Fiji</td></tr>" +
			"<tr              ><td>Apple silicon?</td>      <td>Emulated/x86 mode</td>               <td>Native!</td></tr>" +
			"<tr class=\"odd\"><td>Receives updates?</td>   <td>Security only</td>                   <td>Latest features</td></tr>" +
			"<tr              ><td>Minimum Windows</td>     <td>Windows XP</td>                      <td>Windows 10</td></tr>" +
			"<tr class=\"odd\"><td>Minimum macOS</td>       <td>Mac OS X 10.8 'Mountain Lion'</td>   <td>macOS 11 'Big Sur'</td></tr>" +
			"<tr              ><td>Minimum Ubuntu</td>      <td>Ubuntu 12.04 'Precise Pangolin'</td> <td>Ubuntu 22.04 'Jammy Jellyfish'</td></tr>" +
			"</table></center><br>" +
			"In short: updating to Latest will let you <i>continue receiving updates</i>, " +
			"but because it is still<br>new and less well tested, it also " +
			"<b><i>might break your " + appTitle + " installation or favorite plugins</i></b>.<br>" +
			"<br>How would you like to proceed?";

		int optionType = JOptionPane.DEFAULT_OPTION;
		int messageType = JOptionPane.QUESTION_MESSAGE;
		ImageIcon icon = new ImageIcon(appDir.toPath()
			.resolve("images").resolve("icon.png").toString());
		int w = icon.getIconWidth();
		int maxWidth = 120;
		if (w > maxWidth) {
			icon = new ImageIcon(icon.getImage().getScaledInstance(
				maxWidth, icon.getIconHeight() * maxWidth / w,
				java.awt.Image.SCALE_SMOOTH));
		}
		String yes = "<html><center>Update to Latest!<br>※\\(^o^)/※</center>";
		String no = "<html><center>Keep stable for now<br>⊹╰(~ʟ~)╯⊹</center>";
		String never = "<html><center>Stable, and never ask again<br>୧/0益0\\୨</center>";
		Object[] options = {yes, no, never};
		Window parent = getApplicationWindow();
		int rval = JOptionPane.showOptionDialog(parent, message,
			appTitle, optionType, messageType, icon, options, no);
		if (rval != 0) {
			// User did not opt in to the upgrade.
			if (rval == 2) prefs.putBoolean(prefKey, true); // never ask again!
			return;
		}

		// Here, the user has agreed to switch to the new Fiji update site.
		// We need to be very careful. Users on Apple silicon hardware are likely
		// to be running with Rosetta (x86 emulation mode) rather than in native
		// ARM64 mode. As such, they probably do not have *any* ARM64 version of
		// Java installed, not even Java 8, much less Java 21+.
		//
		// Therefore, we are going to trigger the new app-launcher's Java upgrade
		// logic now, rather than relying on it to trigger conditionally upon
		// restart with the new launcher -- because that restart could potentially
		// fail due to lack of available Java installations, especially on macOS.
		// In order to trigger it successfully, we need to set various properties:

		File appConfigFile = new File(configDir, appSlug + ".toml");
		List<String> lines;
		try {
			lines = Files.readAllLines(appConfigFile.toPath());
		}
		catch (IOException exc) {
			log.debug(exc);
			// Couldn't read from the config file.
			lines = new ArrayList<>();
		}

		// Remember if old launcher was used
		boolean oldLauncherUsed = System.getProperty("scijava.app.name") == null;

		setPropertyIfNull("scijava.app.name", appTitle);
		setPropertyIfNull("scijava.app.directory", appDir.getPath());

		Path splashImage = appDir.toPath().resolve("images").resolve("icon.png");
		setPropertyIfNull("scijava.app.splash-image", splashImage.toString());

		extractAndSetProperty("scijava.app.java-links", lines,
			"https://downloads.imagej.net/java/jdk-urls.txt");
		extractAndSetProperty("scijava.app.java-version-minimum", lines, "8");
		// NB: do not extract the recommended version here because on the old trio
		// of update sites this is likely 8
		extractAndSetProperty("scijava.app.config-file", lines,
			new File(configDir, appSlug + ".cfg").getPath());

		String platform = Platforms.current();
		setPropertyIfNull("scijava.app.java-platform", platform);
		setPropertyIfNull("scijava.app.java-root",
			appDir.toPath().resolve("java").resolve(platform).toString());

		// NB: we ALWAYS want to set this explicitly, even if it is already set.
		// That way we will upgrade regardless of which launcher we're using
		System.setProperty("scijava.app.java-version-recommended", "21");

		// Now that the properties are set, we can decide whether to upgrade Java.
		if (nljv == null || Versions.compare(nljv, Java.recommendedVersion()) < 0) {
			// The new launcher did not find a good-enough Java in our test above,
			// so we now ask the app-launcher to download and install such a Java.
			Java.upgrade(Java.isHeadless(), false);

			// And now we test whether the new launcher finds the new Java.
			try {
				nljv = probeJavaVersion(appDir, jarDir, appSlug);
				if (log != null) log.debug("Java from new launcher AFTER: " + nljv);
			}
			catch (IOException | UnsupportedOperationException exc) {
				// Something bad happened invoking the new launcher. This is especially
				// bad because it worked before running the Java upgrade, but now fails.
				// Bummer. We inform the user and ask for a bug report, then give up.
				askForBugReport(log, appTitle, appSlug, exc);
				return;
			}

			if (nljv == null || Versions.compare(nljv, Java.recommendedVersion()) < 0) {
				// The new launcher is not using a good-enough Java after upgrading!
				if (log != null) {
					Path cfgPath = appDir.toPath().resolve(
						Paths.get("config", "jaunch", appSlug + ".cfg"));
					log.warn("Congratulations on upgrading Java.\n\tUnfortunately, " +
						"the Java version chosen by the new launcher after the upgrade " +
						"is " + nljv + ", which is still less than the recommended Java " +
						"version of " + Java.recommendedVersion() + ".\n\tThis should not " +
						"be the case of course; it seems to be a bug.\n\tWould you please " +
						"visit https://forum.image.sc/ and report this problem?\n\tClick " +
						"'New Topic', choose 'Usage & Issues' category, and use tag '" +
						appSlug + "'.\n\tTo fix it locally for now, you can try editing the " +
						cfgPath + " file by hand to point to a newer Java installation.");
				}
				return;
			}
		}

		// All looks good!
		// Switch update sites, and then we can finally relaunch safely with the
		// new launcher.
		migrateUpdateSites(files);

		Path appPath = appDir.toPath();
		try {
			File exeFile = exeFile(appSlug, appDir);
			String exePath = exeFile.getCanonicalPath();
			Path originalExe;
			Path oldExe;
			Path backupExe;
			if (OS_WIN) {
				String arch = "win64";
				if (ARCH.equals("x32")) {
					arch = "win32";
				}
				originalExe = appPath.resolve("ImageJ-" + arch + ".exe");
				oldExe = appPath.resolve("ImageJ-" + arch + ".old.exe");
				backupExe = appPath.resolve( "ImageJ-" + arch + ".backup.exe");
			} else if (OS_LINUX) {
				originalExe = appPath.resolve("ImageJ-linux64");
				oldExe = appPath.resolve("ImageJ-linux64.old");
				backupExe = appPath.resolve( "ImageJ-linux64.backup");
			} else if (OS_MACOS) {
				originalExe = appPath.resolve("Contents").resolve("MacOS").resolve("ImageJ-macosx");
				oldExe = appPath.resolve("Contents").resolve("MacOS").resolve("ImageJ-macosx.old");
				backupExe = appPath.resolve("Contents").resolve("MacOS").resolve("ImageJ-macosx.backup");
			} else {
				throw new RuntimeException("Unknown operating system");
			}

			// Back up the previous executable to a .backup version
			Files.copy(oldExe, backupExe);

			if (oldLauncherUsed) {
				warnAboutShortcuts(originalExe, exePath);
				queueRestart(appPath, exePath, oldExe);
			} else {
				queueRestart(appPath, exePath, exeFile.toPath());
			}

			appService.getContext().dispose();
			System.exit(0);
		}
		catch (IOException exc) {
			askForBugReport(log, appTitle, appSlug, exc);
		}
	}

	/**
	 * Disable the old ImageJ/Java-8/Fiji sites (and their mirrors) and turn on
	 * the new Fiji site.
	 */
	private void migrateUpdateSites(FilesCollection files) {
		// TODO detect if the Europe mirrors were enabled and if so enable the
		// corresponding Fiji-latest mirror

		// List of all sites to disable
		final List<String> siteList = new ArrayList<>();
		for (String site : new String[]{"Java-8", "ImageJ", "Fiji"}) {
			for (String suffix : new String[]{"", " (Europe mirror)"}) {
				siteList.add(site + suffix);
			}
		}

		// List of the file object names associated with disabled sites
		final Set<String> legacyFiles = new HashSet<>();

		File ijDir = ImageJUpdater.getAppDirectory();
		try {
			// TODO this logic may need to change once Fiji-latest is public
			// Add the new Fiji update site
			files.addUpdateSite(NEW_FIJI_SITE, "https://sites.imagej.net/Fiji/", null,
					null, 0l);

			// Deactivate the old update site trio
			for (String siteName : siteList) {
				UpdateSite site = files.getUpdateSite(siteName, false);
				if (site != null) {
					// Record all file names that were associated with this site
					for (FileObject file : files.forUpdateSite(siteName, true)) {
						legacyFiles.add(file.getFilename());
					}
					// Disable the site
					files.deactivateUpdateSite(site);
				}
			}

			// Persist the changes and refresh the FileCollection
			files.write();
			files.reloadCollectionAndChecksum(new ProgressDialog(null, "Updating..."));

			// Stage files for removal from all the sites we disabled
			for (FileObject file : files.values()) {
				if (legacyFiles.contains(file.getFilename())) {
					switch (file.getStatus()) {
						case LOCAL_ONLY:
						case OBSOLETE:
						case OBSOLETE_MODIFIED:
							// Remove obsolete and now local-only files
							file.stageForUninstall(files);
							break;
					}
				}
			}

			// Ensure any file from the new Fiji site is staged appropriately
			for (FileObject file : files.forUpdateSite(NEW_FIJI_SITE)) {
				final FileObject.Status status = file.getStatus();
				switch (status) {
					case LOCAL_ONLY:
					case OBSOLETE:
					case OBSOLETE_MODIFIED:
						// Remove obsolete and now local-only files
						file.stageForUninstall(files);
						break;
					case INSTALLED:
						// Nothing to do
						break;
					default:
						// Try to update the file
						// NB: Must force for files recognized as "modified" to be updated,
						// which would be any file that IS on the new site but checksums
						// differently
						if (!file.stageForUpdate(files, true)) {
							log.warn("Skipping " + file.filename + " with status " + file.getStatus());
						}
				}
			}

			// Try automatically resolving any conflicts by updating files as needed
			Conflicts conflicts = new Conflicts(files);
			for (Conflicts.Conflict c : conflicts.getConflicts(false)) {
				for (Conflicts.Resolution r : c.getResolutions()) {
					if (r.getDescription().startsWith("Update")) {
						r.resolve();
						break;
					}
				}
			}

			// Double-check that there are no remaining problems
			final ResolveDependencies resolver = new ResolveDependencies(null, files);
			if (!resolver.resolve()) {
				//FIXME abort
			}

			// Stage the changes in the update folder
			Installer i = new Installer(files, null);
			i.start();
		}
		catch (IOException | SAXException | TransformerConfigurationException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Create a process to monitor the specified path ({@code checkExe}) until it
	 * is no longer locked by the system. Then, launch the new executable
	 * ({@code launchExe}).
	 */
	private static void queueRestart(Path appDir,
			String launchExe, Path checkExe) throws IOException
	{
		// We can't actually remove the previous executable while this process is
		// running, since it was used to launch this JVM. So we need to start a
		// sub-process that will continually try to delete the file until it
		// succeeds.
		final int checkIntervalMs = 500;
		final int numTries = 30;
		ProcessBuilder pb;
		String pathToCheck = checkExe.toFile().getAbsolutePath();

		if (OS_WIN) {
			String processToCheck = checkExe.toFile().getName();
			processToCheck = processToCheck.substring(0, processToCheck.lastIndexOf('-'));
			// Windows implementation using PowerShell
			String scriptContent = String.join("\n",
					"$tries = 0; ",
					"while ((Test-Path '" + pathToCheck + "') -and ($tries -lt " + numTries + ")) { ",
					"   if (Get-Process -Name \"" + processToCheck + "*\" -ErrorAction SilentlyContinue) { ",
					"       Write-Host \"Attempt $tries of " + numTries + " - File is locked.\"; ",
					"   } else { ",
					"       break; ",
					"   }",
					"   $tries++;",
					"   if ($tries -eq " + numTries + ") { Write-Host 'Max attempts reached. Exiting.'; break; }",
					"   Start-Sleep -Milliseconds " + checkIntervalMs,
					"}",
					"Start-Process -FilePath " + launchExe,
					"Start-Sleep -Seconds 5");

			// Write the script to a temporary file
			Path tempScript = Files.createTempFile("check_lock_", ".ps1");
			Files.write(tempScript, scriptContent.getBytes());

			// Run the script using ProcessBuilder
			tempScript = tempScript.toAbsolutePath();
			pb = new ProcessBuilder(
					"powershell.exe",
					"-Command",
					"& '" + tempScript + "'; " +
					"Remove-Item -Path '" + tempScript + "' -Force"
			);

			pb.redirectOutput(new File("NUL"));
		} else {
			// Unix/Linux/Mac implementation using bash
			String scriptContent = String.join("\n",
					"#!/bin/bash",
					"tries=0",
					"while [ -f \"" + pathToCheck + "\" ] && [ $tries -lt " + numTries + " ]; do",
					"   if ! lsof " + pathToCheck + " >/dev/null; then",
					"      echo \"File is not locked.\"",
					"      break",
					"   else " +
					"      tries=$((tries+1))",
					"      echo \"Attempt $tries of " + numTries + " - File is locked\"",
					"      if [ $tries -eq " + numTries + " ]; then",
					"         echo \"Max attempts reached. Exiting.\"",
					"         break",
					"      fi",
					"   fi",
					"   sleep " + (checkIntervalMs / 1000.0) ,
					"done",
					launchExe + " &"
			);

			// Write the script to a temporary file
			Path tempScript = Files.createTempFile("check_lock_", ".sh");
			Files.write(tempScript, scriptContent.getBytes());

			// Make the script executable
			tempScript.toFile().setExecutable(true);

			// Run the script using ProcessBuilder
			tempScript = tempScript.toAbsolutePath();
			pb = new ProcessBuilder(
					"bash",
					"-c",
					tempScript + " ; rm -f " + tempScript);

			// Redirect output to /dev/null (needed for Mac, fails on Windows)
			pb.redirectOutput(new File("/dev/null"));
		}

		// Write error output to a timestamped file in a "logs" subdir
		File logsDir = appDir.resolve("logs").toFile();
		if (!logsDir.exists()) {
			logsDir.mkdir();
		}

		String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
		File errFile = new File(logsDir, "restart-err-" + timestamp + ".log");
		pb.redirectError(errFile);

		// Redirect process output (optional - for debugging)
//		pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
//		pb.redirectError(ProcessBuilder.Redirect.INHERIT);

		// Start the process
		Process process = pb.start();
	}

	/** Implores the user to report a bug relating to update site switch-over. */
	private void askForBugReport(
		Logger log, String appTitle, String appSlug, Exception exc)
	{
		if (log == null) return;
		log.error("Argh! " + appTitle + "'s fancy new launcher is not " +
			"working on your system! It might be a bug in the new launcher, " +
			"or your operating system may be too old to support it. Would you " +
			"please visit https://forum.image.sc/ and report this problem? " +
			"Click 'New Topic', choose 'Usage & Issues' category, and use tag '" +
			appSlug + "'. Please copy+paste the technical information below " +
			"into your report. Thank you!\n\n" +
			"* os.name=" + System.getProperty("os.name") + "\n" +
			"* os.arch=" + System.getProperty("os.arch") + "\n" +
			"* os.version=" + System.getProperty("os.version") + "\n", exc);
	}

	/**
	 * Invokes the new native launcher, to make sure all is working as intended.
	 *
	 * @return
	 *   The version of Java discovered and used by the native launcher, or else
	 *   {@code null} if either no valid Java installation is discovered or the
	 *   discovered installation emitted no {@code java.version} property value.
	 * @throws IOException
	 *   If executing the native launcher or reading its output fails.
	 * @throws UnsupportedOperationException
	 *   If no executable native launcher is available for this system platform.
	 */
	private static String probeJavaVersion(
		File appDir, File jarDir, String appPrefix) throws IOException
	{
		// 1. Find and validate the new launcher and helper files.

		File exeFile = exeFile(appPrefix, appDir);
		if (!jarDir.isDirectory()) {
			throw new UnsupportedOperationException("Launcher jar directory is missing: " + jarDir);
		}

		// 2. Run it.
		List<String> output;
		int exitCode;
		try {
			File propsOut = File.createTempFile("props", ".txt");
			propsOut.deleteOnExit();
			Process p = new ProcessBuilder(exeFile.getPath(),
					"-Djava.class.path=" + jarDir.getPath(), "--main-class",
					"net.imagej.ui.swing.updater.PropsProbe",
					propsOut.getAbsolutePath())
				.redirectErrorStream(true).start();
			output = collectProcessOutput(p, propsOut);
			exitCode = p.exitValue();
		}
		catch (InterruptedException exc) {
			throw new IOException(exc);
		}

		// 3. Analyze the output.

		String noJavas = "No matching Java installations found.";
		if (!output.isEmpty() && output.get(0).startsWith(noJavas)) return null;

		// Note: We check the exit code below *after* detecting the lack of Java
		// installations, because in the above case, that exit code is also non-zero
		// (20 as of this writing), and we want to return -1, not throw IOException.
		if (exitCode != 0) {
			throw new IOException("Launcher exited with non-zero value: " + exitCode);
		}

		String propKey = "java.version=";
		return output.stream()
			.filter(line -> line.startsWith(propKey))
			.map(line -> line.substring(propKey.length()))
			.findFirst().orElse(null);
	}

	private static File exeFile(String appPrefix, File appDir) {
		// Determine the right executable path for the new launcher.
		String exe;
		if (OS_WIN) exe = appPrefix + "-windows-" + ARCH + ".exe";
		else if (OS_MACOS) exe = "Fiji.app/Contents/MacOS/" + appPrefix + "-macos-" + ARCH;
		else if (OS_LINUX) exe = appPrefix + "-linux-" + ARCH;
		else throw new UnsupportedOperationException("Unsupported OS: " + OS);

		// Do some sanity checks to make sure we can actually run it.
		File exeFile = new File(appDir, exe);
		if (!exeFile.isFile()) {
			throw new UnsupportedOperationException("Launcher is missing: " + exe);
		}
		if (!exeFile.canExecute()) {
			// Weird -- program is not executable like it should be. Try to fix it.
			//noinspection ResultOfMethodCallIgnored
			exeFile.setExecutable(true);
		}
		if (!exeFile.canExecute()) {
			throw new UnsupportedOperationException("Launcher is not executable: " + exeFile);
		}

		return exeFile;
	}

	private static void setPropertyIfNull(String name, String value) {
		if (System.getProperty(name) == null) System.setProperty(name, value);
	}

	private static void extractAndSetProperty(
		String name,
		List<String> lines,
		String fallbackValue)
	{
		// No, the following replacement does not escape all problematic regex
		// characters. But the properties we're working with here are only
		// alphameric with dot separators, so it's OK. Hooray for pragmatism!
		String escaped = name.replaceAll("\\.", "\\\\.");
		Pattern p = Pattern.compile(".*'-D" + escaped + "=['\"]?(.*?)['\"]?,$");
		String value = fallbackValue;
		for (String line : lines) {
			Matcher m = p.matcher(line);
			if (m.matches()) {
				value = m.group(1);
				break;
			}
		}
		if (value != null) setPropertyIfNull(name, value);
	}

	/**
	 * Annoying code to collect lines from a short-running that writes to the
	 * specified file.
	 */
	private static List<String> collectProcessOutput(Process p, File outFile)
		throws IOException, InterruptedException
	{
		boolean completed = p.waitFor(15, TimeUnit.SECONDS);
		if (!completed) {
			p.destroyForcibly();
			throw new IOException("Process took too long to complete.");
		}
		return Files.readAllLines(outFile.toPath());
	}

	/** Annoying code to discern the AWT/Swing main application frame, if any. */
	private Window getApplicationWindow() {
		if (uiService == null) return null;
		return uiService.getVisibleUIs().stream()
			.map(this::getApplicationWindow)
			.filter(Objects::nonNull)
			.findFirst().orElse(null);
	}

	private Window getApplicationWindow(UserInterface ui) {
		ApplicationFrame appFrame = ui.getApplicationFrame();
		if (appFrame instanceof Window) return (Window) appFrame;
		if (appFrame instanceof UIComponent) {
			Object component = ((UIComponent<?>) appFrame).getComponent();
			if (component instanceof Window) return (Window) component;
		}
		return null;
	}
}
