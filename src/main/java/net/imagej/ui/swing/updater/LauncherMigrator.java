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

import net.imagej.updater.util.UpdaterUtil;
import org.scijava.Context;
import org.scijava.app.AppService;
import org.scijava.launcher.Java;
import org.scijava.launcher.Versions;
import org.scijava.log.LogService;
import org.scijava.log.Logger;
import org.scijava.ui.ApplicationFrame;
import org.scijava.ui.UIService;
import org.scijava.ui.UserInterface;
import org.scijava.widget.UIComponent;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import java.awt.Window;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Absurdly complex logic for helping users transition
 * safely from the old ImageJ launcher to the new one.
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
	private Logger log;

	LauncherMigrator(Context ctx) {
		if (ctx == null) return;
		appService = ctx.getService(AppService.class);
		uiService = ctx.getService(UIService.class);
		log = ctx.getService(LogService.class);
	}

	/**
	 * Figures out what's going on with the application's launch situation.
	 * <ul>
	 * <li>If launched with the old ImageJ launcher, call
	 *   {@link #switchToNewLauncher()}.</li>
	 * <li>If launched with the new Jaunch launcher, call
	 *   {@link #migrateShortcuts()} and {@link #migrateUpdateSite()}.</li>
	 * <li>If launched in some other creative way, do nothing.</li>
	 * </ul>
	 */
	void checkLaunchStatus() {
		// Check whether *either* launcher (old or new) launched the app.
		// Both launchers set one of these telltale properties.
		boolean launcherUsed =
			System.getProperty("ij.executable") != null ||
				System.getProperty("fiji.executable") != null;
		if (!launcherUsed) return; // Program was launched in some creative way.

		// Check if the old launcher launched the app.
		// The old launcher does not set the scijava.app.name property.
		boolean oldLauncherUsed = System.getProperty("scijava.app.name") == null;
		if (oldLauncherUsed) switchToNewLauncher();
		else {
			migrateShortcuts();
			migrateUpdateSite();
		}
	}

	/**
	 * Updates platform-specific shortcuts to reference the new launcher,
	 * including {@code .desktop} files on Linux.
	 * <p>
	 * The macOS-specific {@code Contents/Info.plist} is handled separately
	 * by the Updater, as part of the switch to the new update site; see
	 * {@link #migrateUpdateSite()}.
	 * </p>
	 * <p></p>
	 * Windows users are advised to update their shortcuts by hand, because:
	 * A) we don't know where the user put their Fiji shortcuts&mdash;they
	 * could be in the Start menu, on the desktop, or elsewhere and pinned
	 * to the taskbar; and B) the .lnk file format is an opaque binary
	 * format and I'm not going to code up something to hack it just for
	 * this transitional migration code that will be obsolete in a few months.
	 * </p>
	 * <p>
	 * Finally, old launchers are renamed with {@code .backup} extension in
	 * case there are any missed shortcuts, so that launch fails fast at the
	 * OS level rather than potentially exploding at the application level.
	 * </p>
	 */
	private void migrateShortcuts() {
		// If no old launchers are present, assume we already did this.
		// FIXME: Which launcher(s) should we look for? All platforms? Or current only?

		// Fix links within Linux .desktop files.
		// ~/.local/share/applications
		// ~/.local/share/applications/wine/Programs
		// /usr/share/applications
		// /usr/local/share/applications
		// /var/lib/flatpak/exports/share/applications
		// START HERE: Scan all .desktop files? Or only ImageJ2.desktop and Fiji.desktop? Or...?
		// Completely rewrite them? By calling what code? Can we reuse the code that generated
		// them in the first place? Where is that code? The only code I could find that generates
		// one of these files is fiji/fiji/scripts/Plugins/Utilities/Create_Desktop_Icon.bsh,
		// but it's outdated. So what is making these files these days??

		if (OS_WIN) {
			// FIXME: Warn user to update any shortcuts!
		}

		//
	}

	/**
	 * Checks whether the application is running with a
	 * sufficient Java version, and if so, transitions it to the new core update site.
	 */
	private void migrateUpdateSite() {
		throw new UnsupportedOperationException("migrateUpdateSite unimplemented"); //FIXME
	}

		/**
		 * Checks whether this installation has the new launcher available, and if so,
		 * clues in the user, informing them about the pros and cons of switching.
		 * Then, if they should elect to switch, upgrades the managed Java to the
		 * recommended one and finally relaunches with the new launcher.
		 */
	private void switchToNewLauncher() {
		// Check whether the user has silenced the launcher upgrade prompt.
		String prefKey = "skipLauncherUpgradePrompt";
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

		// Test whether the new launcher is likely to work on this system.
		String nljv;
		try {
			nljv = probeJavaVersion(appDir, configDir, appSlug);
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
		// user whether they want to upgrade to the new launcher.

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
			", but you now have the option to switch<br>to the <b>future</b> version. " +
			"To help you decide, here is a table summarizing the differences:</p>" +
			"<br><center><table>" +
			"<tr><th>Feature</th>                           <th>" + appTitle + " stable</th>         <th>" + appTitle + " Future</th></tr>" +
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
			"<tr class=\"odd\"><td>Java 3D version</td>     <td>1.6.0-scijava-2 (buggier)</td>       <td>1.7.x (less buggy)</td></tr>" +
			"<tr              ><td>ImgLib2 version</td>     <td>6.1.0 (released 2023-03-07)</td>     <td>7.1.2 (released 2024-09-03)</td></tr>" +
			"</table></center><br>" +
			"In short: updating to Future will let you <i>continue receiving updates</i>, " +
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
		String yes = "<html><center>Update to Future!<br>※\\(^o^)/※</center>";
		String no = "<html><center>Keep stable for now<br>⊹╰(~ʟ~)╯⊹</center>";
		String never = "<html><center>Stable, and never ask again<br>୧/0益0\\୨</center>";
		Object[] options = {yes, no, never};
		Window parent = getApplicationWindow();
		int rval = JOptionPane.showOptionDialog(parent, message,
			appTitle, optionType, messageType, icon, options, no);
		if (rval != 0) {
			// User did not opt in to the launcher upgrade.
			if (rval == 2) prefs.putBoolean(prefKey, true); // never ask again!
			return;
		}

		// Here, the user has agreed to switch to the new launcher. At this point
		// we need to be very careful. Users on Apple silicon hardware are likely
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

		setPropertyIfNull("scijava.app.name", appTitle);
		setPropertyIfNull("scijava.app.directory", appDir.getPath());

		Path splashImage = appDir.toPath().resolve("images").resolve("icon.png");
		setPropertyIfNull("scijava.app.splash-image", splashImage.toString());

		extractAndSetProperty("scijava.app.java-links", lines,
			"https://downloads.imagej.net/java/jdk-urls.txt");
		extractAndSetProperty("scijava.app.java-version-minimum", lines, "8");
		extractAndSetProperty("scijava.app.java-version-recommended", lines, "21");
		extractAndSetProperty("scijava.app.config-file", lines,
			new File(configDir, appSlug + ".cfg").getPath());

		String platform = UpdaterUtil.getPlatform();
		// FIXME: I think the macOS platform will be wrong here. It needs -arm64 suffix!
		setPropertyIfNull("scijava.app.java-platform", platform);
		setPropertyIfNull("scijava.app.java-root",
			appDir.toPath().resolve("java").resolve(platform).toString());

		// Now that the properties are set, we can decide whether to upgrade Java.
		if (nljv == null || Versions.compare(nljv, Java.recommendedVersion()) < 0) {
			// The new launcher did not find a good-enough Java in our test above,
			// so we now ask the app-launcher to download and install such a Java.
			Java.upgrade();

			// And now we test whether the new launcher finds the new Java.
			try {
				nljv = probeJavaVersion(appDir, configDir, appSlug);
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

		// All looks good! We can finally relaunch safely with the new launcher.
		File exeFile = exeFile(appSlug, appDir);
		try {
			Process p = new ProcessBuilder(exeFile.getPath()).start();
			boolean terminated = p.waitFor(500, TimeUnit.MILLISECONDS);
			if (terminated || !p.isAlive()) {
				askForBugReport(log, appTitle, appSlug,
					new RuntimeException("New launcher terminated unexpectedly"));
				return;
			}
			// New process seems to be up and running; we are done. Whew!
			System.exit(0);
		}
		catch (IOException | InterruptedException exc) {
			askForBugReport(log, appTitle, appSlug, exc);
		}
	}

	/** Implores the user to report a bug relating to new launcher switch-over. */
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
		File appDir, File configDir, String appPrefix) throws IOException
	{
		// 1. Find and validate the new launcher and helper files.

		File exeFile = exeFile(appPrefix, appDir);
		if (!configDir.isDirectory()) {
			throw new UnsupportedOperationException("Launcher config directory is missing: " + configDir);
		}
		File propsClass = new File(configDir, "Props.class");
		if (!propsClass.isFile()) {
			throw new UnsupportedOperationException("Launcher helper program is missing: " + propsClass);
		}

		// 2. Run it.

		List<String> output;
		int exitCode;
		try {
			Process p = new ProcessBuilder(exeFile.getPath(),
				"-Djava.class.path=" + configDir.getPath(), "--main-class", "Props")
				.redirectErrorStream(true).start();
			output = collectProcessOutput(p);
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
		else if (OS_MACOS) exe = "Contents/MacOS/" + appPrefix + "-macos-" + ARCH;
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

	/** Annoying code to collect stdout lines from a short-running process. */
	private static List<String> collectProcessOutput(Process p)
		throws IOException, InterruptedException
	{
		boolean completed = p.waitFor(5, TimeUnit.SECONDS);
		p.exitValue();
		if (!completed) {
			p.destroyForcibly();
			throw new IOException("Process took too long to complete.");
		}
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
			return reader.lines().collect(Collectors.toList());
		}
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
