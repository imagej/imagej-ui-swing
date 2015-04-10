/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2015 Board of Regents of the University of
 * Wisconsin-Madison, Broad Institute of MIT and Harvard, and Max Planck
 * Institute of Molecular Cell Biology and Genetics.
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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import net.imagej.updater.FilesCollection;
import net.imagej.updater.UpdateSite;
import net.imagej.updater.UploaderService;
import net.imagej.updater.util.UpdaterUtil;
import net.imagej.util.MediaWikiClient;
import net.miginfocom.swing.MigLayout;

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
	protected JButton addNewSite, addPersonalSite, remove, close;

	public SitesDialog(final UpdaterFrame owner, final FilesCollection files)
	{
		super(owner, "Manage update sites");
		updaterFrame = owner;
		this.files = files;

		sites = new ArrayList<UpdateSite>(files.getUpdateSites(true));

		final Container contentPane = getContentPane();
		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.PAGE_AXIS));

		tableModel = new DataModel();
		table = new JTable(tableModel) {

			@Override
			public void valueChanged(final ListSelectionEvent e) {
				super.valueChanged(e);
				remove.setEnabled(getSelectedRow() > 0);
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
					default:
						updaterFrame.log.error("Whoa! Column " + column + " is not handled!");
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
		tableModel.setColumnWidths();
		scrollpane = new JScrollPane(table);
		scrollpane.setPreferredSize(new Dimension(tableModel.tableWidth, 400));
		contentPane.add(scrollpane);

		final JPanel buttons = new JPanel();
		addPersonalSite = SwingTools.button("Add my site", "Add my personal update site", this, buttons);
		addNewSite = SwingTools.button("Add", "Add", this, buttons);
		remove = SwingTools.button("Remove", "Remove", this, buttons);
		remove.setEnabled(false);
		close = SwingTools.button("Close", "Close", this, buttons);
		contentPane.add(buttons);

		getRootPane().setDefaultButton(close);
		escapeCancels(this);
		pack();
		addNewSite.requestFocusInWindow();
		setLocationRelativeTo(owner);
	}

	private static String wrapToolTip(final String description, final String maintainer) {
		if (description == null) return null;
		return  "<html><p width='400'>" + description.replaceAll("\n", "<br />")
			+ (maintainer != null ? "</p><p>Maintainer: " + maintainer + "</p>": "")
			+ "</p></html>";
	}

	protected String getUpdateSiteName(int row) {
		return sites.get(row).getName();
	}

	protected UpdateSite getUpdateSite(int row) {
		return sites.get(row);
	}

	private void addNew() {
		add(new UpdateSite(makeUniqueSiteName("New"), "", "", "", null, null, 0l));
	}

	private final static String PERSONAL_SITES_URL = "http://sites.imagej.net/";

	private void addPersonalSite() {
		final PersonalSiteDialog dialog = new PersonalSiteDialog();
		final String user = dialog.name;
		if (user == null) return;
		final String url = PERSONAL_SITES_URL + user;
		final UpdateSite site = new UpdateSite(makeUniqueSiteName("My Site"), url, "webdav:" + user, "", null, null, 0l);
		add(site);
		activateUpdateSite(site);
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
		final Set<String> names = new HashSet<String>();
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

	@Override
	public void actionPerformed(final ActionEvent e) {
		final Object source = e.getSource();
		if (source == addNewSite) addNew();
		else if (source == addPersonalSite) addPersonalSite();
		else if (source == remove) delete(table.getSelectedRow());
		else if (source == close) {
			dispose();
		}
	}

	protected class DataModel extends DefaultTableModel {

		protected int tableWidth;
		protected int[] widths = { 20, 150, 280, 125, 125 };
		protected String[] headers = { "Active", "Name", "URL", "Host",
			"Directory on Host" };

		public void setColumnWidths() {
			final TableColumnModel columnModel = table.getColumnModel();
			for (int i = 0; i < tableModel.widths.length && i < getColumnCount(); i++)
			{
				final TableColumn column = columnModel.getColumn(i);
				column.setPreferredWidth(tableModel.widths[i]);
				column.setMinWidth(tableModel.widths[i]);
				tableWidth += tableModel.widths[i];
			}
		}

		@Override
		public int getColumnCount() {
			return 5;
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
			if (col == 1) return getUpdateSiteName(row);
			final UpdateSite site = getUpdateSite(row);
			if (col == 0) return Boolean.valueOf(site.isActive());
			if (col == 2) return site.getURL();
			if (col == 3) return site.getHost();
			if (col == 4) return site.getUploadDirectory();
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

	private class PersonalSiteDialog extends JDialog implements ActionListener {
		private String name;
		private JLabel userLabel, realNameLabel, emailLabel, passwordLabel;
		private JTextField userField, realNameField, emailField;
		private JPasswordField passwordField;
		private JButton cancel, okay;

		public PersonalSiteDialog() {
			super(SitesDialog.this, "Add Personal Site");
			setLayout(new MigLayout("wrap 2"));
			add(new JLabel("<html>" +
					"<style type='text/css'>p { text-indent: 10px; }</style>" +
					"<h2>Personal update site setup</h2>" +
					"<p width=400>For security reasons, personal update sites are associated with a ImageJ Wiki account. " +
					"Please provide the account name of your ImageJ Wiki account.</p>" +
					"<p width=400>If your personal update site was not yet initialized, you can initialize it in this dialog.</p>" +
					"<p width=400>You can register a ImageJ Wiki account here if  you do not have one yet.</p></html>"), "span 2");
			userLabel = new JLabel("ImageJ Wiki account");
			add(userLabel);
			userField = new JTextField();
			userField.setColumns(30);
			add(userField);
			realNameLabel = new JLabel("Real Name");
			add(realNameLabel);
			realNameField = new JTextField();
			realNameField.setColumns(30);
			add(realNameField);
			emailLabel = new JLabel("Email");
			add(emailLabel);
			emailField = new JTextField();
			emailField.setColumns(30);
			add(emailField);
			passwordLabel = new JLabel("Password");
			add(passwordLabel);
			passwordField = new JPasswordField();
			passwordField.setColumns(30);
			add(passwordField);
			final JPanel panel = new JPanel();
			cancel = new JButton("Cancel");
			cancel.addActionListener(this);
			panel.add(cancel);
			okay = new JButton("OK");
			okay.addActionListener(this);
			panel.add(okay);
			add(panel, "span 2, right");
			setWikiAccountFieldsEnabled(false);
			setChangePasswordEnabled(false);
			pack();
			final KeyAdapter keyListener = new KeyAdapter() {

				@Override
				public void keyPressed(final KeyEvent e) {
					if (e.getKeyCode() == KeyEvent.VK_ESCAPE) dispose();
					else if (e.getKeyCode() == KeyEvent.VK_ENTER) actionPerformed(new ActionEvent(okay, -1, null));
				}

			};
			userField.addKeyListener(keyListener);
			realNameField.addKeyListener(keyListener);
			emailField.addKeyListener(keyListener);
			passwordField.addKeyListener(keyListener);
			cancel.addKeyListener(keyListener);
			okay.addKeyListener(keyListener);
			setModal(true);
			setVisible(true);
		}

		private void setWikiAccountFieldsEnabled(final boolean enabled) {
			realNameLabel.setEnabled(enabled);
			realNameField.setEnabled(enabled);
			emailLabel.setEnabled(enabled);
			emailField.setEnabled(enabled);
			if (enabled) realNameField.requestFocusInWindow();
		}

		private void setChangePasswordEnabled(final boolean enabled) {
			passwordLabel.setEnabled(enabled);
			passwordField.setEnabled(enabled);
			if (enabled) passwordField.requestFocusInWindow();
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == cancel) {
				dispose();
				return;
			} else if (e.getSource() == okay) {
				String newName = userField.getText();
				if (!MediaWikiClient.isCapitalized(newName)) {
					newName = MediaWikiClient.capitalize(newName);
					userField.setText(newName);
				}
				if ("".equals(newName)) {
					error("Please provide a ImageJ Wiki account name!");
					return;
				}
				if (validURL(PERSONAL_SITES_URL + newName)) {
					name = newName;
					dispose();
					return;
				}

				// create a ImageJ Wiki user if needed
				final MediaWikiClient wiki = new MediaWikiClient();
				try {
					if (!wiki.userExists(newName)) {
						if (realNameLabel.isEnabled()) {
							final String realName = realNameField.getText();
							final String email = emailField.getText();
							if ("".equals(realName) || "".equals(email)) {
								error("<html><p width=400>Please provide your name and email address to register an account on the ImageJ Wiki!</p></html>");
							} else {
								if (wiki.createUser(newName, realName, email, "Wants a personal site")) {
									setWikiAccountFieldsEnabled(false);
									setChangePasswordEnabled(true);
									info("<html><p width=400>An email with the activation code was sent. " +
											"Please provide your ImageJ Wiki password after activating the account.</p></html>");
								} else {
									error("<html><p width=400>There was a problem creating the user account!</p></html>");
								}
							}
						} else {
							setWikiAccountFieldsEnabled(true);
							error("<html><p width=400>Please provide your name and email address to register an account on the ImageJ Wiki</p></html>");
						}
						return;
					}
					else if (!passwordField.isEnabled()) {
						setChangePasswordEnabled(true);
						error("<html><p width=400>Please type in your the password for your account on the Fiji/ImageJ Wiki</p></html>");
						return;
					}

					// initialize the personal update site
					final String password = new String(passwordField.getPassword());
					if (!wiki.login(newName, password)) {
						error("Could not log in (incorrect password?)");
						return;
					}
					if (!wiki.changeUploadPassword(password)) {
						error("Could not initialize the personal update site");
						return;
					}
					wiki.logout();
					this.name = newName;
					dispose();
				} catch (IOException e2) {
					updaterFrame.log.error(e2);
					error("<html><p width=400>There was a problem contacting the ImageJ Wiki: " + e2 + "</p></html>");
					return;
				}
			}
		}
	}

}
