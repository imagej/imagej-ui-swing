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

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

import javax.swing.AbstractAction;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;

import net.imagej.updater.FilesCollection;
import net.imagej.updater.URLChange;
import net.imagej.updater.UpdateSite;
import net.imagej.updater.UploaderService;
import net.imagej.updater.util.AvailableSites;
import net.imagej.updater.util.HTTPSUtil;
import net.imagej.updater.util.UpdaterUtil;

import org.scijava.log.Logger;
import org.scijava.ui.swing.StaticSwingUtils;

/**
 * The dialog in which the user can choose which update sites to follow.
 * 
 * @author Johannes Schindelin
 */
@SuppressWarnings("serial")
public class SitesDialog extends JDialog implements ActionListener {

	protected UpdaterFrame updaterFrame;
	protected FilesCollection files;
	protected List<UpdateSite> sites;

	protected DataModel tableModel;
	protected JTable table;
	protected JScrollPane scrollpane;
	protected JButton addNewSite, remove, close, checkForUpdates;
	private JTextField searchTerm;
	private boolean searchDescription, searchURL;
	private TableRowSorter<DataModel> sorter;

	public SitesDialog(final UpdaterFrame owner, final FilesCollection files)
	{
		super(owner, "Manage Update Sites");
		updaterFrame = owner;
		this.files = files;

		sites = new ArrayList<>(files.getUpdateSites(true));

		final Container contentPane = getContentPane();
		contentPane.setLayout(new GridBagLayout());

		tableModel = new DataModel();
		table = new JTable(tableModel) {

			@Override
			public void valueChanged(final ListSelectionEvent e) {
				super.valueChanged(e);
				remove.setEnabled(!getSelectionModel().isSelectionEmpty());
			}

			@Override
			public boolean isCellEditable(final int row, final int column) {
				return column >= 0 && column < getColumnCount() && row >= 0 && row < getRowCount();
			}

			@Override
			public TableCellEditor getCellEditor(final int row, final int column) {
				if (column == 0) return super.getCellEditor(row, column);
				final JTextField field = new JTextField();
				return new DefaultCellEditor(field) {
					@Override
					public boolean stopCellEditing() {
						if (row >= sites.size()) {
							// In case of stopping after a row has been removed, properly stop editing
							return super.stopCellEditing();
						}
						String value = field.getText();
						if ((column == 2 || column == 4) && !value.equals("") && !value.endsWith("/")) {
							value += "/";
						}
						if (column == 1) {
							if (value.equals(getUpdateSiteName(row))) return super.stopCellEditing();
							if (files.getUpdateSite(value, true) != null) {
								error("Update site '" + value + "' exists already!");
								return false;
							}
						} else if (column == 2) {
							if ("/".equals(value)) value = "";
							final UpdateSite site = getUpdateSite(row);
							if (value.equals(site.getURL())) return super.stopCellEditing();
							if(!HTTPSUtil.supportsURLProtocol(value)) {
								if(showYesNoQuestion("Convert HTTPS URL to HTTP?",
										"Your installation cannot handle secure communication (HTTPS).\n" +
												"Please download a recent version of this software.\n\n" +
												"Do you want to use the insecure URL of this update site (HTTP)?")) {
									value = HTTPSUtil.userSiteConvertToHTTP(value);
									field.setText(value);
								} else return false;
							}
							if (validURL(value)) {
								site.setURL(value);
								boolean wasActive = site.isActive();
								activateUpdateSite(site);
								if (!wasActive && site.isActive()) tableModel.rowChanged(row);
							} else {
								if (site.getHost() == null || site.getHost().equals("")) {
									error("URL does not refer to an update site: " + value + "\n"
										+ "If you want to initialize that site, you need to provide upload information first.");
									return false;
								}
								if (!showYesNoQuestion("Initialize upload site?",
										"It appears that the URL\n"
										+ "\t" + value + "\n"
										+ "is not (yet) valid. "
										+ "Do you want to initialize it (host: "
										+ site.getHost() + "; directory: "
										+ site.getUploadDirectory() + ")?"))
									return false;
								if (!initializeUpdateSite(site.getName(),
										value, site.getHost(), site.getUploadDirectory()))
									return false;
							}
						} else if (column == 3) {
							final UpdateSite site = getUpdateSite(row);
							if (value.equals(site.getHost())) return super.stopCellEditing();
							final int colon = value.indexOf(':');
							if (colon > 0) {
								final String protocol = value.substring(0, colon);
								final UploaderService uploaderService = updaterFrame.getUploaderService();
								if (null == uploaderService.installUploader(protocol, files, updaterFrame.getProgress(null))) {
									error("Unknown upload protocol: " + protocol);
									return false;
								}
							}
						} else if (column == 4) {
							final UpdateSite site = getUpdateSite(row);
							if (value.equals(site.getUploadDirectory())) return super.stopCellEditing();
						}
						updaterFrame.enableApplyOrUpload();
						return super.stopCellEditing();
					}
				};
			}

			@Override
			public void setValueAt(final Object value, final int row, final int column)
			{
				if (row < sites.size()) {
					final UpdateSite site = getUpdateSite(row);
					if (column == 0) {
						if (Boolean.TRUE.equals(value)) {
							if (column == 0 || column == 2) {
								activateUpdateSite(site);
							}
						} else {
							deactivateUpdateSite(site);
						}
					} else {
						final String string = (String)value;
						// if the name changed, or if we auto-fill the name from the URL
						switch (column) {
						case 1:
							final String name = site.getName();
							if (name.equals(string)) return;
							files.renameUpdateSite(name, string);
							break;
						case 2:
							if (site.getURL().equals(string)) return;
							boolean active = site.isActive();
							if (active) deactivateUpdateSite(site);
							site.setURL(string);
							if (active && validURL(string)) activateUpdateSite(site);
							break;
						case 3:
							if (string.equals(site.getHost())) return;
							site.setHost(string);
							break;
						case 4:
							if (string.equals(site.getUploadDirectory())) return;
							site.setUploadDirectory(string);
							break;
						case 5:
							// do nothing: description column
							break;
						default:
							updaterFrame.log.error("Whoa! Column " + column + " is not handled!");
						}
					}
				}
				files.setUpdateSitesChanged(true);
			}

			@Override
			public Component prepareRenderer(TableCellRenderer renderer,int row, int column) {
				Component component = super.prepareRenderer(renderer, row, column);
				if (component instanceof JComponent) {
					final UpdateSite site = getUpdateSite(row);
					if (site != null) {
						JComponent jcomponent = (JComponent) component;
						jcomponent.setToolTipText(wrapToolTip(site.getDescription(), site.getMaintainer()));
					}
				}
			    return component;
			}
		};
		table.setColumnSelectionAllowed(false);
		table.setRowSelectionAllowed(true);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		// row filtering
		sorter = new TableRowSorter<>(tableModel);
		table.setRowSorter(sorter);
		setSearchDescription(true);
		searchTerm = new JTextField();
		searchTerm.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void changedUpdate(final DocumentEvent e) {
				filterTable();
			}

			@Override
			public void removeUpdate(final DocumentEvent e) {
				filterTable();
			}

			@Override
			public void insertUpdate(final DocumentEvent e) {
				filterTable();
			}
		});

		// Add all components to dialog
		final JPanel labeledSearchField = SwingTools.labelComponentRigid(" Search:", searchTerm);
		scrollpane = new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		final JPanel buttons = new JPanel();
		final GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 1;
		c.fill = GridBagConstraints.BOTH;
		c.weighty = 0;
		contentPane.add(labeledSearchField, c);
		c.gridx++;
		c.weightx = 0;
		contentPane.add(searchOptionsButton(), c);
		c.gridwidth = 2;
		c.gridx = 0;
		c.gridy++;
		c.weighty = 1;
		contentPane.add(scrollpane, c);
		c.gridy++;
		c.weighty = 0;
		contentPane.add(buttons, c);

		// Adjust table size, column widths and scrollbars
		tableModel.setColumnWidths();
		scrollpane.setPreferredSize(new Dimension(tableModel.tableWidth, 12 * table.getRowHeight()));
		contentPane.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(final ComponentEvent e) {
				if (table.getPreferredSize().width < getWidth()) {
					// unlikely to happen given current column widths
					table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
				} else {
					table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
				}
			}
		});

		addNewSite = SwingTools.button("Add Unlisted Site", "Add a new entry for a site not listed", this, buttons);
		remove = SwingTools.button("Remove", "Remove highlighted site from list", this, buttons);
		remove.setEnabled(false);
		checkForUpdates = SwingTools.button("Validate URLs", "Check whether update sites are using outdated URLs", this, buttons);
		close = SwingTools.button("Apply and Close", "Confirm activations and dismiss [ESC]", this, buttons);

		getRootPane().setDefaultButton(close);
		escapeCancels(this);
		pack();
		addNewSite.requestFocusInWindow();
		setLocationRelativeTo(owner);
	}

	private JButton searchOptionsButton() {
		final JPopupMenu popup = new JPopupMenu();
		final JCheckBoxMenuItem jcbmi0 = new JCheckBoxMenuItem("Search Site Names", true);
		jcbmi0.setToolTipText("Site names are always searched");
		jcbmi0.setEnabled(false); // dummy checkbox: Site names are always searched
		popup.add(jcbmi0);
		final JCheckBoxMenuItem jcbmi1 = new JCheckBoxMenuItem("Search Site URLs", isSearchURL());
		jcbmi1.addItemListener(e -> {
			setSearchURL(jcbmi1.isSelected());
			filterTable();
		});
		popup.add(jcbmi1);
		final JCheckBoxMenuItem jcbmi2 = new JCheckBoxMenuItem("Search Site Descriptions", isSearchDescription());
		jcbmi2.addItemListener(e -> {
			setSearchDescription(jcbmi2.isSelected());
			filterTable();
		});
		popup.add(jcbmi2);
		final JButton options = optionsButton(searchTerm);
		options.addActionListener(e -> popup.show(options, options.getWidth() / 2, options.getHeight() / 2));
		return options;
	}

	private JButton optionsButton(final JComponent main) {
		final JButton b = new JButton("⋮");
		b.setToolTipText("Search options");
		final float factor = .5f;
		final Insets insets = b.getMargin();
		if (insets != null)
			b.setMargin(new Insets((int) (insets.top * factor), (int) (insets.left * factor),
					(int) (insets.bottom * factor), (int) (insets.right * factor)));
		b.setPreferredSize(new Dimension(b.getPreferredSize().width, (int) main.getPreferredSize().getHeight()));
		b.setMaximumSize(new Dimension(b.getMaximumSize().width, (int) main.getPreferredSize().getHeight()));
		return b;
	}

	private boolean isSearchDescription() {
		return searchDescription;
	}

	private void setSearchDescription(final boolean searchDescription) {
		this.searchDescription = searchDescription;
	}

	private boolean isSearchURL() {
		return searchURL;
	}

	private void setSearchURL(final boolean searchURL) {
		this.searchURL = searchURL;
	}

	private void filterTable() {
		final List<Integer> cols = new ArrayList<>();
		cols.add(1); // Name column
		if (isSearchURL()) cols.add(2); // URL column
		if (isSearchDescription()) cols.add(5); // Description column
		final String query = Pattern.quote(searchTerm.getText());
		SwingTools.invokeOnEDT(() -> {
			try {
				sorter.setRowFilter(RowFilter.regexFilter("(?i)" + query,
						cols.stream().mapToInt(i -> i).toArray()));
			} catch (final java.util.regex.PatternSyntaxException e) {
				// do nothing if expression doesn't parse
				return;
			}
		});
	}

	private static String inlineSynopsis(final UpdateSite site) {
		if (site == null) return "";
		final StringBuilder sb = new StringBuilder();
		String s = site.getDescription();
		if (s != null) sb.append(s.replace("\n", " "));
		s = site.getMaintainer();
		if (s != null) sb.append(" Maintainer:").append(s);
		return  sb.toString();
	}

	private static String wrapToolTip(final String description, final String maintainer) {
		if (description == null) return null;
		return  "<html><p width='500'>" + description.replaceAll("\n", "<br />")
			+ (maintainer != null ? "</p><p>Maintainer: " + maintainer + "</p>": "")
			+ "</p></html>";
	}

	/*
	 * returns the name of the update site associated with the _viewed_ row by
	 * mapping its index to the table model
	 */
	protected String getUpdateSiteName(int row) {
		return getUpdateSite(row).getName();
	}

	/*
	 * returns the update site associated with the _viewed_ row by mapping its index
	 * to the table model
	 */
	protected UpdateSite getUpdateSite(int row) {
		// table model does not change but the table _display_ does change during
		// filtering so we need to look up the proper index of displayed rows
		return sites.get(table.convertRowIndexToModel(row));
	}

	private void addNew() {
		searchTerm.setText(""); // Reset filtering so that displayed rows match row model
		table.requestFocusInWindow();
		add(new UpdateSite(makeUniqueSiteName("New"), "", "", "", null, null, 0l));

		table.changeSelection( table.getRowCount()-1, 2, false, false);

		if (table.editCellAt(table.getRowCount()-1, 2))
		{
			Component editor = table.getEditorComponent();
			editor.requestFocusInWindow();
		}
	}

	private void add(final UpdateSite site) {
		final int row = sites.size();
		files.addUpdateSite(site);
		sites.add(site);
		tableModel.rowsChanged();
		tableModel.rowChanged(row);
		table.setRowSelectionInterval(row, row);
		StaticSwingUtils.scrollToBottom(scrollpane);
	}

	private String makeUniqueSiteName(final String prefix) {
		final Set<String> names = new HashSet<>();
		for (final UpdateSite site : sites) names.add(site.getName());
		if (!names.contains(prefix)) return prefix;
		for (int i = 2; ; i++) {
			if (!names.contains(prefix + "-" + i)) return prefix + "-" + i;
		}
	}

	protected void delete(final int row) {
		final UpdateSite site = getUpdateSite(row);
		final String name = site.getName();
		if (!showYesNoQuestion("Remove " + name + "?",
				"Do you really want to remove the site '" + name + "' from the list?\n"
				+ "URL: " + getUpdateSite(row).getURL()))
			return;
		files.removeUpdateSite(site.getName());
		sites.remove(row);
		tableModel.rowChanged(row);

		// Properly stop cell editing
		TableCellEditor cellEditor = table.getCellEditor();
		if (cellEditor != null) {
			cellEditor.stopCellEditing();
		}

	}

	private void deactivateUpdateSite(final UpdateSite site) {
		int count = files.deactivateUpdateSite(site);
		if (count > 0) {
			info("" +
			count + (count == 1 ? " file is" : " files are") +
			" installed from the site '" +
			site.getName() +
			"' and will be updated/uninstalled\n");
			updaterFrame.updateFilesTable();
		}
	}

	private void updateAvailableUpdateSites() {
		new Thread(() -> {
			List<URLChange>
					changes = AvailableSites.initializeAndAddSites(files, (Logger) null);
			boolean reviewChanges = ReviewSiteURLsDialog.shouldBeDisplayed(changes);
			AtomicBoolean changesApproved = new AtomicBoolean(!reviewChanges);
			try {
				SwingUtilities.invokeAndWait(() -> {
					ReviewSiteURLsDialog dialog = new ReviewSiteURLsDialog(null, changes);
					dialog.setVisible(true);
					changesApproved.set(dialog.isOkPressed());
				});
			} catch (InterruptedException | InvocationTargetException e) {
				e.printStackTrace();
			}
			if(changesApproved.get()) {
				searchTerm.setText(""); // Reset filtering
				AvailableSites.applySitesURLUpdates(files, changes);
			}
			tableModel.rowsChanged(0, tableModel.getRowCount()-1);
		}).start();

	}

	@Override
	public void setVisible(boolean b) {
		if (b)
			searchTerm.requestFocusInWindow();
		super.setVisible(b);
	}

	@Override
	public void actionPerformed(final ActionEvent e) {
		final Object source = e.getSource();
		if (source == addNewSite) addNew();
		else if (source == remove) delete(table.getSelectedRow());
		else if (source == checkForUpdates) updateAvailableUpdateSites();
		else if (source == close) {
			dispose();
		}
	}

	protected class DataModel extends DefaultTableModel {

		protected int tableWidth;
		protected String[] headers = { "Active", "Name", "URL", "Host", "Directory on Host", "Description" };
		private String[] canonicalRows = { "Active", "Fuzzy logic and artificial neural",
				"sites.imagej.net/Fiji-Legacy/", "webdav:User", "/path", " Large description with maintainer name" };

		public void setColumnWidths() {
			table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF); // otherwise horizontal scrollbar is not displayed
			final TableColumnModel columnModel = table.getColumnModel();
			final FontMetrics fm = table.getFontMetrics(table.getFont());
			for (int i = 0; i < tableModel.headers.length && i < getColumnCount(); i++) {
				final TableColumn column = columnModel.getColumn(i);
				column.setPreferredWidth(fm.stringWidth(canonicalRows[i]));
				tableWidth += column.getPreferredWidth();
			}
		}

		@Override
		public int getColumnCount() {
			return 6;
		}

		@Override
		public String getColumnName(final int column) {
			return headers[column];
		}

		@Override
		public Class<?> getColumnClass(final int column) {
			return column == 0 ? Boolean.class : String.class;
		}

		@Override
		public int getRowCount() {
			return sites.size();
		}

		@Override
		public Object getValueAt(final int row, final int col) {
			if (col == 1) return sites.get(row).getName();
			final UpdateSite site = sites.get(row);
			if (col == 0) return Boolean.valueOf(site.isActive());
			if (col == 2) return site.getURL();
			if (col == 3) return site.getHost();
			if (col == 4) return site.getUploadDirectory();
			if (col == 5) return inlineSynopsis(site);
			return null;
		}

		public void rowChanged(final int row) {
			rowsChanged(row, row + 1);
		}

		public void rowsChanged() {
			rowsChanged(0, sites.size());
		}

		public void rowsChanged(final int firstRow, final int lastRow) {
			// fireTableChanged(new TableModelEvent(this, firstRow, lastRow));
			fireTableChanged(new TableModelEvent(this));
		}
	}

	protected boolean validURL(String url) {
		if (!url.endsWith("/"))
			url += "/";
		try {
			return files.util.getLastModified(new URL(url
					+ UpdaterUtil.XML_COMPRESSED)) != -1;
		} catch (MalformedURLException e) {
			updaterFrame.log.error(e);
			return false;
		}
	}

	protected boolean activateUpdateSite(final UpdateSite updateSite) {
		try {
			files.activateUpdateSite(updateSite, updaterFrame.getProgress(null));
		} catch (final Exception e) {
			e.printStackTrace();
			error("Not a valid URL: " + updateSite.getURL());
			return false;
		}
		updaterFrame.filesChanged();
		return true;
	}

	protected boolean initializeUpdateSite(final String siteName,
			String url, final String host, String uploadDirectory) {
		if (!url.endsWith("/"))
			url += "/";
		if (!uploadDirectory.endsWith("/"))
			uploadDirectory += "/";
		boolean result;
		try {
			result = updaterFrame.initializeUpdateSite(url, host,
					uploadDirectory) && validURL(url);
		} catch (final InstantiationException e) {
			updaterFrame.log.error(e);
			result = false;
		}
		if (result)
			info("Initialized update site '" + siteName + "'");
		else
			error("Could not initialize update site '" + siteName + "'");
		return result;
	}

	@Override
	public void dispose() {
		table.editCellAt(0,0);
		super.dispose();
		updaterFrame.updateFilesTable();
		updaterFrame.enableApplyOrUpload();
		updaterFrame.addCustomViewOptions();
	}

	public void info(final String message) {
		SwingTools.showMessageBox(this, message, JOptionPane.INFORMATION_MESSAGE);
	}

	public void error(final String message) {
		SwingTools.showMessageBox(this, message, JOptionPane.ERROR_MESSAGE);
	}

	public boolean showYesNoQuestion(final String title, final String message) {
		return SwingTools.showYesNoQuestion(this, title, message);
	}

	public static void escapeCancels(final JDialog dialog) {
		dialog.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
			KeyStroke.getKeyStroke("ESCAPE"), "ESCAPE");
		dialog.getRootPane().getActionMap().put("ESCAPE", new AbstractAction() {

			@Override
			public void actionPerformed(final ActionEvent e) {
				dialog.dispose();
			}
		});
	}
}
