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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;

import net.imagej.updater.FileObject;
import net.imagej.updater.FileObject.Action;
import net.imagej.updater.FileObject.Status;
import net.imagej.updater.FilesCollection;
import net.imagej.updater.GroupAction;
import net.imagej.updater.UpdateSite;

/**
 * This class's role is to be in charge of how the Table should be displayed.
 * 
 * @author Johannes Schindelin
 */
@SuppressWarnings("serial")
public class FileTable extends JTable {

	protected UpdaterFrame updaterFrame;
	protected FilesCollection files;
	protected List<FileObject> row2file;
	private FileTableModel fileTableModel;
	protected Font plain, bold;

	final static int NAME_COLUMN = 0;
	final static int ACTION_COLUMN = 1;
	final static int SITE_COLUMN = 2;

	public FileTable(final UpdaterFrame updaterFrame) {
		this.updaterFrame = updaterFrame;
		files = updaterFrame.files;
		row2file = new ArrayList<>();
		for (final FileObject file : files) {
			row2file.add(file);
		}

		// Set appearance of table
		setShowGrid(false);
		setIntercellSpacing(new Dimension(0, 0));
		setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		setRequestFocusEnabled(false);

		// set up the table properties and other settings
		setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		setCellSelectionEnabled(true);
		setColumnSelectionAllowed(false);
		setRowSelectionAllowed(true);
		final JPopupMenu popupMenu = new JPopupMenu();
		popupMenu.addPopupMenuListener(new PopupMenuListener() {

			@Override
			public void popupMenuWillBecomeVisible(final PopupMenuEvent arg0) {
				popupMenu.removeAll();
				populatePopupMenu(getSelectedFiles(), popupMenu);
			}

			@Override
			public void popupMenuWillBecomeInvisible(final PopupMenuEvent arg0) {
				popupMenu.removeAll();
			}

			@Override
			public void popupMenuCanceled(PopupMenuEvent arg0) {
				// do nothing
			}

		});
		setComponentPopupMenu(popupMenu);

		fileTableModel = new FileTableModel(files);
		setModel(fileTableModel);
		getModel().addTableModelListener(this);
		setColumnWidths(250, 100, 80);
		TableRowSorter<FileTableModel> sorter =
			new TableRowSorter<>(fileTableModel);
		sorter.setComparator(ACTION_COLUMN, new Comparator<FileObject.Action>() {

			@Override
			public int compare(Action o1, Action o2) {
				return o1.toString().compareTo(o2.toString());
			}

		});
		setRowSorter(sorter);

		setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {

			@Override
			public Component getTableCellRendererComponent(final JTable table,
				final Object value, final boolean isSelected, final boolean hasFocus,
				final int row, final int column)
			{
				final Component comp =
					super.getTableCellRendererComponent(table, value, isSelected,
						hasFocus, row, column);
				setStyle(comp, row, column);
				return comp;
			}
		});
	}

	/*
	 * This sets the font to bold when the user selected an action for
	 * this file, or when it is locally modified.
	 *
	 * It also warns loudly when the file is obsolete, but locally
	 * modified.
	 */
	protected void
		setStyle(final Component comp, final int row, final int column)
	{
		if (plain == null) {
			plain = comp.getFont();
			bold = plain.deriveFont(Font.BOLD);
		}
		final FileObject file = getFile(row);
		if (file == null) return;
		comp.setFont(file.actionSpecified() || file.isLocallyModified() ? bold
			: plain);
		comp.setForeground(file.getStatus() == Status.OBSOLETE_MODIFIED ? Color.red
			: Color.black);
	}

	private void setColumnWidths(final int nameColumnWidth,
		final int actionColumnWidth, final int siteColumnWidth)
	{
		final TableColumn nameColumn = getColumnModel().getColumn(NAME_COLUMN);
		final TableColumn actionColumn = getColumnModel().getColumn(ACTION_COLUMN);
		final TableColumn siteColumn = getColumnModel().getColumn(SITE_COLUMN);

		nameColumn.setPreferredWidth(nameColumnWidth);
		nameColumn.setMinWidth(nameColumnWidth);
		nameColumn.setResizable(false);
		actionColumn.setPreferredWidth(actionColumnWidth);
		actionColumn.setMinWidth(actionColumnWidth);
		actionColumn.setResizable(true);
		siteColumn.setPreferredWidth(siteColumnWidth);
		siteColumn.setMinWidth(siteColumnWidth);
		siteColumn.setResizable(true);
	}

	public FilesCollection getAllFiles() {
		return files;
	}

	public void setFiles(final Iterable<FileObject> files) {
		fileTableModel.setFiles(files);
	}

	@Override
	public TableCellEditor getCellEditor(final int row, final int col) {
		final FileObject file = getFile(row);

		// As we follow FileTableModel, 1st column is filename
		if (col == NAME_COLUMN) return super.getCellEditor(row, col);
		final Set<GroupAction> actions = files.getValidActions(Collections.singleton(file));
		return new DefaultCellEditor(new JComboBox<>(actions.toArray()));
	}

	private void populatePopupMenu(final Iterable<FileObject> selected,
		final JPopupMenu menu)
	{
		int count = 0;
		for (final GroupAction action : files.getValidActions(selected)) {
			final JMenuItem item = new JMenuItem(action.getLabel(files, selected));
			item.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(final ActionEvent e) {
					for (final FileObject file : selected) {
						action.setAction(files, file);
						fireFileChanged(file);
					}
				}
			});
			menu.add(item);
			count++;
		}
		if (count == 0) {
			final JMenuItem noActions = new JMenuItem("<No common actions>");
			noActions.setEnabled(false);
			menu.add(noActions);
		}
	}

	public FileObject getFile(final int viewRow) {
		return getFileFromModel(convertRowIndexToModel(viewRow));
	}

	private FileObject getFileFromModel(final int modelRow) {
		return fileTableModel.rowToFile.get(modelRow);
	}

	public Iterable<FileObject> getSelectedFiles() {
		return getSelectedFiles(-1);
	}

	public Iterable<FileObject> getSelectedFiles(final int fallbackRow) {
		int[] rows = getSelectedRows();
		if (fallbackRow >= 0 && getFile(fallbackRow) != null &&
			(rows.length == 0 || indexOf(rows, fallbackRow) < 0)) rows =
			new int[] { fallbackRow };
		final FileObject[] result = new FileObject[rows.length];
		for (int i = 0; i < rows.length; i++)
			result[i] = getFile(rows[i]);
		return Arrays.asList(result);
	}

	protected int indexOf(final int[] array, final int value) {
		for (int i = 0; i < array.length; i++) {
			if (array[i] == value) return i;
		}
		return -1;
	}

	public boolean areAllSelectedFilesUploadable() {
		if (getSelectedRows().length == 0) return false;
		for (final FileObject file : getSelectedFiles())
			if (!file.isUploadable(updaterFrame.files, true)) return false;
		return true;
	}

	public boolean chooseUpdateSite(final FilesCollection files,
		final FileObject file)
	{
		final List<String> list = new ArrayList<>();
		for (final String name : files.getUpdateSiteNames(false)) {
			final UpdateSite site = files.getUpdateSite(name, true);
			if (site.isUploadable()) list.add(name);
		}
		if (list.size() == 0) {
			error("No upload site available");
			return false;
		}
		if (list.size() == 1 &&
			list.get(0).equals(FilesCollection.DEFAULT_UPDATE_SITE))
		{
			file.updateSite = FilesCollection.DEFAULT_UPDATE_SITE;
			return true;
		}
		final String updateSite =
			SwingTools.getChoice(updaterFrame, list,
				"To which upload site do you want to upload " + file.filename + "?",
				"Upload site");
		if (updateSite == null) return false;
		file.updateSite = updateSite;
		return true;
	}

	protected class FileTableModel extends AbstractTableModel {

		private FilesCollection files;
		protected Map<FileObject, Integer> fileToRow;
		protected List<FileObject> rowToFile;

		public FileTableModel(final FilesCollection files) {
			this.files = files;
		}

		public void setFiles(final Iterable<FileObject> files) {
			setFiles(this.files.clone(files));
		}

		public void setFiles(final FilesCollection files) {
			this.files = files;
			fileToRow = null;
			rowToFile = null;
			updateMappings();
			fireTableChanged(new TableModelEvent(fileTableModel));
		}

		@Override
		public int getColumnCount() {
			return 3; // Name of file, status, update site
		}

		@Override
		public Class<?> getColumnClass(final int columnIndex) {
			switch (columnIndex) {
				case NAME_COLUMN:
				case SITE_COLUMN:
					return String.class; // filename / update site
				case ACTION_COLUMN:
					return FileObject.Action.class; // status/action
				default:
					return Object.class;
			}
		}

		@Override
		public String getColumnName(final int column) {
			switch (column) {
				case NAME_COLUMN:
					return "Name";
				case ACTION_COLUMN:
					return "Status/Action";
				case SITE_COLUMN:
					return "Update Site";
				default:
					throw new Error("Column out of range");
			}
		}

		public FileObject getEntry(final int rowIndex) {
			return rowToFile.get(rowIndex);
		}

		@Override
		public int getRowCount() {
			return files.size();
		}

		@Override
		public Object getValueAt(final int row, final int column) {
			updateMappings();
			if (row < 0 || row >= files.size()) return null;
			final FileObject file = rowToFile.get(row);
			switch (column) {
			case NAME_COLUMN:
				return file.getFilename(true);
			case ACTION_COLUMN:
				return file.getAction();
			case SITE_COLUMN:
				return file.updateSite;
			}
			throw new RuntimeException("Unhandled column: " + column);
		}

		@Override
		public boolean isCellEditable(final int rowIndex, final int columnIndex) {
			return columnIndex == ACTION_COLUMN;
		}

		@Override
		public void setValueAt(final Object value, final int row, final int column)
		{
			if (column == ACTION_COLUMN) {
				final GroupAction action = (GroupAction) value;
				final FileObject file = getFileFromModel(row);
				action.setAction(files, file);
				fireFileChanged(file);
			}
		}

		public void fireRowChanged(final int row) {
			fireTableRowsUpdated(row, row);
		}

		public void fireFileChanged(final FileObject file) {
			updateMappings();
			final Integer row = fileToRow.get(file);
			if (row != null) fireRowChanged(row.intValue());
		}

		protected void updateMappings() {
			if (fileToRow != null) return;
			fileToRow = new HashMap<>();
			rowToFile = new ArrayList<>();
			// the table may be sorted, and we need the model's row
			int i = 0;
			for (final FileObject f : files) {
				fileToRow.put(f, new Integer(i++));
				rowToFile.add(f);
			}
		}
	}

	public void fireFileChanged(final FileObject file) {
		fileTableModel.fireFileChanged(file);
	}

	protected void error(final String message) {
		SwingTools.showMessageBox(updaterFrame, message, JOptionPane.ERROR_MESSAGE);
	}
}
