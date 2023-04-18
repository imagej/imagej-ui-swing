/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2023 ImageJ developers.
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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.RowSorter.SortKey;
import javax.swing.SortOrder;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import net.imagej.updater.Diff.Mode;
import net.imagej.updater.FileObject;
import net.imagej.updater.FileObject.Action;
import net.imagej.updater.FileObject.Status;
import net.imagej.updater.FilesCollection;
import net.imagej.updater.FilesCollection.DependencyMap;
import net.imagej.updater.FilesUploader;
import net.imagej.updater.GroupAction;
import net.imagej.updater.Installer;
import net.imagej.updater.UploaderService;
import net.imagej.updater.action.InstallOrUpdate;
import net.imagej.updater.action.KeepAsIs;
import net.imagej.updater.action.Uninstall;
import net.imagej.updater.util.Progress;
import net.imagej.updater.util.UpdateCanceledException;
import net.imagej.updater.util.UpdaterUserInterface;
import net.imagej.updater.util.UpdaterUtil;

import org.scijava.Context;
import org.scijava.log.LogService;

/**
 * TODO
 * 
 * @author Johannes Schindelin
 */
@SuppressWarnings("serial")
public class UpdaterFrame extends JFrame implements TableModelListener,
	ListSelectionListener
{

	protected LogService log;
	private UploaderService uploaderService;
	protected FilesCollection files;

	protected JTextField searchTerm;
	protected JPanel searchPanel;
	protected ViewOptions viewOptions;
	protected JPanel viewOptionsPanel;
	protected JPanel chooseLabel;
	protected FileTable table;
	protected JLabel fileSummary;
	protected JPanel summaryPanel;
	protected FileDetails fileDetails;
	protected JPanel bottomPanel, easyBottomPanel;
	protected JButton applyOrUpload, cancel, easy, easy2, updateSites;
	protected boolean easyMode;

	// For developers
	protected JButton showChanges, rebuildButton;
	protected boolean canUpload;
	private CollapsibleLeftRightSplitPane splitPane;

	public UpdaterFrame(final LogService log,
		final UploaderService uploaderService, final FilesCollection files)
	{
		super("ImageJ Updater");
		//setPreferredSize(new Dimension((easyMode) ? 700 : 900, 560));

		this.log = log;
		this.uploaderService = uploaderService;
		this.files = files;

		// make sure that sezpoz finds the classes when triggered from the EDT
		final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
		SwingTools.invokeOnEDT(() -> {
			if (contextClassLoader != null)
				Thread.currentThread().setContextClassLoader(contextClassLoader);
		});

		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(final WindowEvent e) {
				quit();
			}
		});

		// ======== Start: LEFT PANEL ========
		final JPanel leftPanel = new JPanel();
		GridBagLayout gb = new GridBagLayout();
		leftPanel.setLayout(gb);
		GridBagConstraints c = Utils.gbConstraints();

		searchTerm = new JTextField();
		searchTerm.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void changedUpdate(final DocumentEvent e) {
				updateFilesTable();
			}

			@Override
			public void removeUpdate(final DocumentEvent e) {
				updateFilesTable();
			}

			@Override
			public void insertUpdate(final DocumentEvent e) {
				updateFilesTable();
			}
		});
		searchPanel = SwingTools.labelComponentRigid("Search:", searchTerm);
		gb.setConstraints(searchPanel, c);
		leftPanel.add(searchPanel);

		Component box = Utils.vSpace();
		c.gridy = 1;
		gb.setConstraints(box, c);
		leftPanel.add(box);

		viewOptions = new ViewOptions();
		viewOptions.addActionListener(e -> updateFilesTable());

		viewOptionsPanel =
			SwingTools.labelComponentRigid("View Options:", viewOptions);
		c.gridy = 2;
		gb.setConstraints(viewOptionsPanel, c);
		leftPanel.add(viewOptionsPanel);

		box = Utils.vSpace();
		c.gridy = 3;
		gb.setConstraints(box, c);
		leftPanel.add(box);

		// Create labels to annotate table
		chooseLabel =
			SwingTools.label("Please choose what you want to install/uninstall:",
				null);
		c.gridy = 4;
		gb.setConstraints(chooseLabel, c);
		leftPanel.add(chooseLabel);

		box = Utils.vSpace();
		c.gridy = 5;
		gb.setConstraints(box, c);
		leftPanel.add(box);

		// Label text for file summaries
		fileSummary = new JLabel();
		summaryPanel = SwingTools.horizontalPanel();
		summaryPanel.add(Utils.hSpace());
		summaryPanel.add(fileSummary);
		summaryPanel.add(Box.createHorizontalGlue());

		// Create the file table and set up its scrollpane
		table = new FileTable(this);
		table.getSelectionModel().addListSelectionListener(this);
		final JScrollPane fileListScrollpane = new JScrollPane(table);
		fileListScrollpane.getViewport().setBackground(table.getBackground());

		c.gridy = 6;
		c.weightx = 1;
		c.weighty = 1;
		c.fill = GridBagConstraints.BOTH;
		gb.setConstraints(fileListScrollpane, c);
		leftPanel.add(fileListScrollpane);

		box = Utils.vSpace();
		c.gridy = 7;
		c.weightx = 0;
		c.weighty = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		gb.setConstraints(box, c);
		leftPanel.add(box);
		// ======== End: LEFT PANEL ========

		// ======== Start: RIGHT PANEL ========
		final JPanel rightPanel = new JPanel();
		gb = new GridBagLayout();
		rightPanel.setLayout(gb);
		c = Utils.gbConstraints();

		updateSites = manageButton();
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.NORTHEAST;
		gb.setConstraints(updateSites, c);
		rightPanel.add(updateSites);

		box = Utils.vSpace();
		c.gridy = 1;
		gb.setConstraints(box, c);
		rightPanel.add(box);

		easy = easyButton();
		Utils.equalizeDimensions(easy, updateSites);
		c.gridy = 2;
		gb.setConstraints(easy, c);
		rightPanel.add(easy);

		box = Utils.vSpace();
		c.gridy = 3;
		gb.setConstraints(box, c);
		rightPanel.add(box);

		final JPanel dummyLabel = SwingTools.label("", null);
		c.gridy = 4;
		gb.setConstraints(dummyLabel, c);
		rightPanel.add(dummyLabel);

		box = Utils.vSpace();
		c.gridy = 5;
		gb.setConstraints(box, c);
		rightPanel.add(box);

		fileDetails = new FileDetails(this);
		fileDetails.setBackground(table.getBackground());
		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.addTab("Details", null, SwingTools.scrollPane(fileDetails, -1, -1, null),
				"Individual file information");

		c.gridy = 6;
		c.weightx = 1;
		c.weighty = 1;
		c.anchor = GridBagConstraints.NORTHWEST;
		c.fill = GridBagConstraints.BOTH;
		gb.setConstraints(tabbedPane, c);
		rightPanel.add(tabbedPane);

		box = Utils.vSpace();
		c.gridy = 7;
		c.weightx = 0;
		c.weighty = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		gb.setConstraints(box, c);
		rightPanel.add(box);
//		rightPanel.setMaximumSize(new Dimension((int) (leftPanel.getPreferredSize().getWidth() * .25),
//				rightPanel.getMaximumSize().height));
		// ======== End: RIGHT PANEL ========

		// ======== Start: TOP PANEL (LEFT + RIGHT) ========
		splitPane = new CollapsibleLeftRightSplitPane(leftPanel, rightPanel);
		final JPanel topPanel = SwingTools.horizontalPanel();
		topPanel.add(splitPane);
		topPanel.setBorder(Utils.defaultBorder());
		// ======== End: TOP PANEL (LEFT + RIGHT) ========

		easyBottomPanel = SwingTools.horizontalPanel();
		easyBottomPanel.add(manageButton());
		easyBottomPanel.add(Utils.hSpace());
		easyBottomPanel.add(easy2 = easyButton());
		easyBottomPanel.add(Utils.hSpace());
		easyBottomPanel.setVisible(easyMode);

		bottomPanel = SwingTools.horizontalPanel();
		bottomPanel.setBorder(Utils.defaultBorder());
		bottomPanel.add(easyBottomPanel);
		bottomPanel.add(new FileActionButton(new KeepAsIs()));
		bottomPanel.add(Utils.hSpace());
		bottomPanel.add(new FileActionButton(new InstallOrUpdate()));
		bottomPanel.add(Utils.hSpace());
		bottomPanel.add(new FileActionButton(new Uninstall()));
		bottomPanel.add(Utils.hSpace());
		showChanges =
				SwingTools.button("Diff",
					"Show the differences to the uploaded version", e -> new Thread() {

						@Override
						public void run() {
							for (final FileObject file : table.getSelectedFiles()) try {
								final DiffFile diff = new DiffFile(files, file, Mode.LIST_FILES);
								diff.setLocationRelativeTo(UpdaterFrame.this);
								diff.setVisible(true);
							} catch (MalformedURLException e) {
								files.log.error(e);
								UpdaterUserInterface.get().error("There was a problem obtaining the remote version of " + file.getLocalFilename(true));
							}
						}
					}.start(), bottomPanel);
		bottomPanel.add(Box.createHorizontalGlue());
		// Button to start actions
		applyOrUpload =
			SwingTools.button("Apply Changes", "Start installing/uninstalling/uploading files",
				e -> {
					if (files.hasUploadOrRemove()) {
						new Thread() {

							@Override
							public void run() {
								try {
									upload();
								}
								catch (final InstantiationException e) {
									log.error(e);
									error("Could not upload (possibly unknown protocol)");
								}
							}
						}.start();
					}
					else if (files.hasChanges()) {
						applyChanges();
					}
				}, bottomPanel);
		enableApplyOrUpload();

		bottomPanel.add(Utils.hSpace());
		cancel = SwingTools.button("Close", "Exit Updater [Esc]", e -> quit(), bottomPanel);
		// ======== End: BOTTOM PANEL ========

		getContentPane().setLayout(
			new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
		getContentPane().add(topPanel);
		getContentPane().add(summaryPanel);
		getContentPane().add(bottomPanel);

		getRootPane().setDefaultButton(applyOrUpload);
		table.getModel().addTableModelListener(this);
		pack();

		SwingTools.addAccelerator(cancel, (JComponent) getContentPane(), cancel
			.getActionListeners()[0], KeyEvent.VK_ESCAPE, 0);
	}

	@Override
	public void setVisible(final boolean visible) {
		if (!SwingUtilities.isEventDispatchThread()) {
			try {
				SwingUtilities.invokeAndWait(() -> setVisible(visible));
			} catch (final InterruptedException e) {
				// ignore
			} catch (final InvocationTargetException e) {
				log.error(e);
			}
			return;
		}
		showOrHide();
		super.setVisible(visible);
		if (visible) {
			UpdaterUserInterface.get().addWindow(this);
			applyOrUpload.requestFocusInWindow();
		}
	}

	@Override
	public void dispose() {
		UpdaterUserInterface.get().removeWindow(this);
		super.dispose();
	}

	public Progress getProgress(final String title) {
		return new ProgressDialog(this, title);
	}

	/**
	 * Sets the context class loader if necessary.
	 *
	 * If the current class cannot be found by the current Thread's context
	 * class loader, we should tell the Thread about the class loader that
	 * loaded this class.
	 */
	private void setClassLoaderIfNecessary() {
		ClassLoader thisLoader = getClass().getClassLoader();
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		for (; loader != null; loader = loader.getParent()) {
			if (thisLoader == loader) return;
		}
		Thread.currentThread().setContextClassLoader(thisLoader);
	}

	/** Gets the uploader service associated with this updater frame. */
	public UploaderService getUploaderService() {
		if (uploaderService == null) {
			setClassLoaderIfNecessary();
			final Context context = new Context(UploaderService.class);
			uploaderService = context.getService(UploaderService.class);
		}

		return uploaderService;
	}

	@Override
	public void valueChanged(final ListSelectionEvent event) {
		filesChanged();
	}

	List<FileActionButton> fileActions = new ArrayList<>();

	private class FileActionButton extends JButton implements ActionListener {

		private GroupAction goal;

		public FileActionButton(final GroupAction goal) {
			super(goal.toString());
			this.goal = goal;
			addActionListener(this);
			fileActions.add(this);
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			if (table.isEditing()) table.editingCanceled(null);
			for (final FileObject file : table.getSelectedFiles()) {
				goal.setAction(files, file);
				table.fireFileChanged(file);
			}
			filesChanged();
		}

		public void enableIfValid() {
			boolean enable = true;
			int count = 0;

			for (final FileObject file : table.getSelectedFiles()) {
				count++;
				if (!goal.isValid(files, file)) {
					enable = false;
					break;
				}
			}
			setText(goal.getLabel(files, table.getSelectedFiles()));
			setEnabled(count > 0 && enable);
		}
	}

	public void addCustomViewOptions() {
		viewOptions.clearCustomOptions();

		final Collection<String> names = files.getUpdateSiteNames(false);
		if (names.size() > 1) for (final String name : names)
			viewOptions.addCustomOption("View files of the '" + name + "' site",
				files.forUpdateSite(name));
	}

	public void setViewOption(final ViewOptions.Option option) {
		SwingTools.invokeOnEDT(() -> {
			viewOptions.setSelectedItem(option);
			updateFilesTable();
		});
	}

	public void updateFilesTable() {
		SwingTools.invokeOnEDT(() -> {
			Iterable<FileObject> view = viewOptions.getView(table);
			final Set<FileObject> selected = new HashSet<>();
			for (final FileObject file : table.getSelectedFiles())
				selected.add(file);
			table.clearSelection();

			final String search = searchTerm.getText().trim();
			if (!search.equals("")) view = FilesCollection.filter(search, view);

			// Directly update the table for display
			table.setFiles(view);
			for (int i = 0; i < table.getRowCount(); i++)
				if (selected.contains(table.getFile(i))) table.addRowSelectionInterval(i,
					i);
		});
	}

	public void applyChanges() {
		final ResolveDependencies resolver = new ResolveDependencies(this, files);
		if (!resolver.resolve()) return;
		new Thread() {

			@Override
			public void run() {
				install();
			}
		}.start();
	}

	private void quit() {
		if (files.hasChanges()) {
			if (!SwingTools.showQuestion(this, "Quit?",
				"You have specified changes. Are you sure you want to quit?")) return;
		}
		else try {
			files.write();
		}
		catch (final Exception e) {
			error("There was an error writing the local metadata cache: " + e);
		}
		dispose();
	}

	public void setEasyMode(final boolean easyMode) {
		this.easyMode = easyMode;
		showOrHide();
		if (isVisible()) repaint();
	}

	protected void showOrHide() {
		// make sure that *some* files are shown in advanced mode
		if (!easyMode && table.getRowCount() == 0 &&
			viewOptions.getSelectedItem() == ViewOptions.Option.UPDATEABLE)
		{
			viewOptions.setSelectedItem(ViewOptions.Option.ALL);
			final List<SortKey> keys = new ArrayList<>();
			keys.add(new SortKey(FileTable.ACTION_COLUMN, SortOrder.ASCENDING));
			keys.add(new SortKey(FileTable.SITE_COLUMN, SortOrder.ASCENDING));
			table.getRowSorter().setSortKeys(keys);
		}

		for (final FileActionButton action : fileActions) {
			action.setVisible(!easyMode);
		}
		searchPanel.setVisible(!easyMode);
		viewOptionsPanel.setVisible(!easyMode);
		chooseLabel.setVisible(!easyMode);
		summaryPanel.setVisible(!easyMode);
		splitPane.setRightPaneVisible(!easyMode);
		easyBottomPanel.setVisible(easyMode);
		final boolean uploadable = !easyMode && files.hasUploadableSites();
		if (showChanges != null) showChanges.setVisible(!easyMode);
		if (rebuildButton != null) rebuildButton.setVisible(uploadable);

		easy.setText(easyMode ? "Advanced Mode" : "Easy Mode");
		easy2.setText(easyMode ? "Advanced Mode" : "Easy Mode");
	}

	public void toggleEasyMode() {
		setEasyMode(!easyMode);
	}

	public void install() {
		final Installer installer =
			new Installer(files, getProgress("Installing..."));
		try {
			installer.start();
			updateFilesTable();
			filesChanged();
			files.write();
			info("Updated successfully.  Please restart ImageJ!");
			dispose();
		}
		catch (final UpdateCanceledException e) {
			// TODO: remove "update/" directory
			error("Canceled");
			installer.done();
		}
		catch (final Exception e) {
			log.error(e);
			// TODO: remove "update/" directory
			error("Installer failed: " + e);
			installer.done();
		}
	}

	private Thread filesChangedWorker;

	public synchronized void filesChanged() {
		if (filesChangedWorker != null) return;

		filesChangedWorker = new Thread() {

			@Override
			public void run() {
				filesChangedWorker();
				synchronized (UpdaterFrame.this) {
					filesChangedWorker = null;
				}
			}
		};
		SwingUtilities.invokeLater(filesChangedWorker);
	}

	private void filesChangedWorker() {
		// TODO: once this is editable, make sure changes are committed
		fileDetails.reset();
		for (final FileObject file : table.getSelectedFiles())
			fileDetails.showFileDetails(file);
		if (fileDetails.getDocument().getLength() > 0 &&
			table.areAllSelectedFilesUploadable()) fileDetails
			.setEditableForDevelopers();

		for (final FileActionButton button : fileActions)
			button.enableIfValid();

		if (showChanges != null) showChanges.setEnabled(table.getSelectedFiles().iterator().hasNext());

		enableApplyOrUpload();
		cancel.setText(files.hasChanges() || files.hasUpdateSitesChanges() ? "Cancel" : "Close");

		int install = 0, uninstall = 0, upload = 0;
		long bytesToDownload = 0, bytesToUpload = 0;

		for (final FileObject file : files)
			switch (file.getAction()) {
				case INSTALL:
				case UPDATE:
					install++;
					bytesToDownload += file.filesize;
					break;
				case UNINSTALL:
					uninstall++;
					break;
				case UPLOAD:
					upload++;
					bytesToUpload += file.filesize;
					break;
				default:
			}
		int implicated = 0;
		final DependencyMap map = files.getDependencies(true);
		for (final FileObject file : map.keySet()) {
			implicated++;
			bytesToUpload += file.filesize;
		}
		String text = "";
		if (install > 0) text +=
			" Install/update: " + install + (implicated > 0 ? "+" + implicated : "") +
				" (" + sizeToString(bytesToDownload) + ")";
		if (uninstall > 0) text += " uninstall: " + uninstall;
		if (files.hasUploadableSites() && upload > 0) text +=
			" upload: " + upload + " (" + sizeToString(bytesToUpload) + ")";
		fileSummary.setText(text);

	}

	protected final static String[] units = { "B", "kB", "MB", "GB", "TB" };

	public static String sizeToString(long size) {
		int i;
		for (i = 1; i < units.length && size >= 1l << (10 * i); i++); // do nothing
		if (--i == 0) return "" + size + units[i];
		// round
		size *= 100;
		size >>= (10 * i);
		size += 5;
		size /= 10;
		return "" + (size / 10) + "." + (size % 10) + units[i];
	}

	@Override
	public void tableChanged(final TableModelEvent e) {
		filesChanged();
	}

	// checkWritable() is guaranteed to be called after Checksummer ran
	public void checkWritable() {
		if (UpdaterUtil.isProtectedLocation(files.prefix(""))) {
			error("<html><p width=400>Windows' security model for the directory '" + files.prefix("") + "' is incompatible with the ImageJ updater.</p>" +
				"<p>Please install ImageJ into a user-writable directory, e.g. onto the Desktop.</p></html>");
			return;
		}
		String list = null;
		for (final FileObject object : files) {
			final File file = files.prefix(object);
			if (!file.exists() || file.canWrite()) continue;
			if (list == null) list = object.getFilename();
			else list += ", " + object.getFilename();
		}
		if (list != null) UpdaterUserInterface.get().info(
			"WARNING: The following files are set to read-only: '" + list + "'",
			"Read-only files");
	}

	void markUploadable() {
		canUpload = true;
		enableApplyOrUpload();
	}

	void enableApplyOrUpload() {
		if (files.hasUploadOrRemove()) {
			applyOrUpload.setEnabled(true);
			applyOrUpload.setText("Apply Changes (Upload)");
		}
		else {
			applyOrUpload.setText("Apply Changes");
			applyOrUpload.setEnabled(files.hasChanges());
		}
	}

	protected void upload() throws InstantiationException {
		final ResolveDependencies resolver =
			new ResolveDependencies(this, files, true);
		if (!resolver.resolve()) return;

		final String errors = files.checkConsistency();
		if (errors != null) {
			error(errors);
			return;
		}

		final List<String> possibleSites =
			new ArrayList<>(files.getSiteNamesToUpload());
		if (possibleSites.size() == 0) {
			error("Huh? No upload site?");
			return;
		}
		String updateSiteName;
		if (possibleSites.size() == 1) updateSiteName = possibleSites.get(0);
		else {
			updateSiteName =
				SwingTools.getChoice(this, possibleSites,
					"Which site do you want to upload to?", "Update site");
			if (updateSiteName == null) return;
		}
		final FilesUploader uploader = new FilesUploader(uploaderService, files, updateSiteName, getProgress(null));

		Progress progress = null;
		try {
			if (!uploader.login()) return;
			progress = getProgress("Uploading...");
			uploader.upload(progress);
			for (final FileObject file : files.toUploadOrRemove())
				if (file.getAction() == Action.UPLOAD) {
					file.markUploaded();
					file.setStatus(Status.INSTALLED);
				}
				else {
					file.markRemoved(files);
				}
			updateFilesTable();
			canUpload = false;
			files.write();
			info("Uploaded successfully.");
			enableApplyOrUpload();
			dispose();
		}
		catch (final UpdateCanceledException e) {
			// TODO: teach uploader to remove the lock file
			error("Canceled");
			if (progress != null) progress.done();
		}
		catch (final Throwable e) {
			UpdaterUserInterface.get().handleException(e);
			error("Upload failed: " + e);
			if (progress != null) progress.done();
		}
	}

	protected boolean initializeUpdateSite(final String url,
		final String sshHost, final String uploadDirectory)
		throws InstantiationException
	{
		final FilesUploader uploader =
			FilesUploader.initialUploader(uploaderService, url, sshHost, uploadDirectory, getProgress(null));
		Progress progress = null;
		try {
			if (!uploader.login()) return false;
			progress = getProgress("Initializing Update Site...");
			uploader.upload(progress);
			// JSch needs some time to finalize the SSH connection
			try {
				Thread.sleep(1000);
			}
			catch (final InterruptedException e) { /* ignore */}
			return true;
		}
		catch (final UpdateCanceledException e) {
			if (progress != null) progress.done();
		}
		catch (final Throwable e) {
			UpdaterUserInterface.get().handleException(e);
			if (progress != null) progress.done();
		}
		return false;
	}

	public void error(final String message) {
		SwingTools.showMessageBox(this, message, JOptionPane.ERROR_MESSAGE);
	}

	public void warn(final String message) {
		SwingTools.showMessageBox(this, message, JOptionPane.WARNING_MESSAGE);
	}

	public void info(final String message) {
		SwingTools.showMessageBox(this, message, JOptionPane.INFORMATION_MESSAGE);
	}

	JButton easyButton() {
		return SwingTools.button("Easy Mode", "Toggle between simplified and advanced view", e -> toggleEasyMode(),
				null);
	}

	JButton manageButton() {
		 return SwingTools.button("Manage Update Sites",
					"Manage subscriptions of update sites for updating and uploading",
					e -> new SitesDialog(UpdaterFrame.this, UpdaterFrame.this.files).setVisible(true), null);
	}

	private class CollapsibleLeftRightSplitPane extends JSplitPane {

		static final double DEF_DIVIDER_LOCATION = .85d;
		final int defDividerSize;
		int lastVisibleDividerLocation;
		private Dimension rightComponentPreferredSize;

		CollapsibleLeftRightSplitPane(final Component leftComponent, final Component rightComponent) {
			super(HORIZONTAL_SPLIT, leftComponent, rightComponent);
			defDividerSize = getDividerSize();
			setDividerLocation(DEF_DIVIDER_LOCATION);
			lastVisibleDividerLocation = getDividerLocation();
			setResizeWeight(.90); // right component should minimally change on resize
		}

		void setResizable(final boolean resizable) {
			if (resizable) {
				setDividerSize(defDividerSize);
			} else {
				setDividerSize(0);
			}
			setEnabled(resizable);
			//((BasicSplitPaneUI) getUI()).getDivider().setEnabled(resizable);
		}

		void setRightPaneVisible(final boolean visible) {
			if (visible) {
				setDividerLocation(lastVisibleDividerLocation);
				getRightComponent().setPreferredSize(rightComponentPreferredSize);
			} else {
				lastVisibleDividerLocation = getDividerLocation();
				rightComponentPreferredSize = getRightComponent().getPreferredSize();
				setDividerLocation(1d);
				getRightComponent().setPreferredSize(new Dimension(0,0));
			}
			getRightComponent().setVisible(visible);
			setResizable(visible);
			if (isVisible()) pack();
		}
	}

	private static class Utils {

		static Border defaultBorder() {
			return BorderFactory.createEmptyBorder(5, 10, 10, 5);
		}

		static Component hSpace() {
			return Box.createRigidArea(new Dimension(10, 0));
		}

		static Component vSpace() {
			return Box.createRigidArea(new Dimension(0, 5));
		}

		static GridBagConstraints gbConstraints() {
			return new GridBagConstraints(//
					0, 0, // x, y
					9, 1, // rows, cols
					0, 0, // weightx, weighty
					GridBagConstraints.NORTHWEST, // anchor
					GridBagConstraints.HORIZONTAL, // fill
					new Insets(0, 0, 0, 0), // insets
					0, 0 // ipadx, ipady
			);
		}

		static void equalizeDimensions(final JComponent source, final JComponent target) {
			source.setMinimumSize(target.getMinimumSize());
			source.setPreferredSize(target.getPreferredSize());
			source.setMaximumSize(target.getMaximumSize());
		}
	}

}
