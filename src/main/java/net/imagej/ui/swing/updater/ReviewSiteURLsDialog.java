/*-
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

import net.imagej.updater.URLChange;
import net.imagej.updater.UpdateSite;
import net.imagej.updater.util.HTTPSUtil;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import static net.imagej.ui.swing.updater.SitesDialog.escapeCancels;

/**
 * The dialog in which updated URLs of available update sites will be shown
 * and the user can chose to accept these updates.
 *
 * @author Deborah Schmidt
 */
public class ReviewSiteURLsDialog extends JDialog implements ActionListener {

	List< URLChange > urlChanges;

	private UpdatableTableDataModel tableModel;
	private JTable table;
	JButton cancel, submit, ok;
	JRadioButton updateAll;
	JRadioButton keepAll;
	JRadioButton manualChoice;
	JCheckBox stopAsking;
	private ButtonGroup generalChoiceGroup = new ButtonGroup();
	private final static int nameCol = 0, urlCol = 1;
	private final static String appendOld = " (keep URL)", appendNew = " (update URL)";
	private boolean okPressed;

	/**
	 * @param owner The frame that this dialog will be placed relative to
	 * @param urlChanges The urlChanges
	 */
	public ReviewSiteURLsDialog(final UpdaterFrame owner, final List< URLChange > urlChanges)
	{
		super(owner, "Changes to available update sites", ModalityType.DOCUMENT_MODAL);

		this.urlChanges = urlChanges;

		final Container contentPane = getContentPane();
		contentPane.setLayout(new MigLayout("fill, gap 0"));

		this.urlChanges.forEach( change -> change.setApproved(change.isRecommended()));

		if( this.urlChanges.size() > 0) {
			setMinimumSize(new Dimension(900, 0));
			contentPane.add(createHeader(), "span, grow");
			contentPane.add(createUpdateSiteScrollPane(), "newline, span, grow, push");
			contentPane.add(createButtonsPanel(), "dock south");
			updateGeneralChoices();
		} else {
			setMinimumSize(new Dimension(0, 0));
			contentPane.add(createOkPanel(), "dock south");
			contentPane.add(createOkIcon(), "w 70px!");
			contentPane.add(createEverythingIsFineMessage());
		}

		escapeCancels(this);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		pack();
		setLocationRelativeTo(owner);
	}

	static boolean shouldBeDisplayed(final List<URLChange> changes ) {
		for(URLChange change : changes) {
			if(change.isRecommended() && !change.isApproved()) {
				return true;
			}
		}
		return false;
	}

	boolean isOkPressed() {
		return okPressed;
	}

	private Component createEverythingIsFineMessage() {
		JEditorPane text = createHTMLText("<html><h2>Software package sources up to date</h2>" +
				"No updated URLs found for activated update sites.</html>");
		text.setBorder(BorderFactory.createEmptyBorder(10, 10, 20, 25));
		text.setMinimumSize(new Dimension(0,0));
		return text;
	}

	private Component createOkPanel() {
		JPanel panel = new JPanel();
		ok = new JButton("OK");
		panel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.darkGray));
		panel.add(ok);
		ok.addActionListener(this);
		getRootPane().setDefaultButton(ok);
		return panel;
	}

	private Component createHeader() {
		JPanel panel = new JPanel();
		panel.setLayout(new MigLayout("fillx, ins 5px", "[][]push[]"));
		panel.add(createUpdatesAvailableMessage(), "span, wrap, wmax 600px");
		panel.add(createAttentionIcon(), "w 100px!");
		panel.add(createGeneralChoices(), "wrap, w 300px!, bottom, left");
		if(HTTPSUtil.supportsHTTPS()) {
			panel.add(createHTTPSInfo(), "wmax 250px, dock east");
		}
		return panel;
	}

	private static Component createAttentionIcon() {
//		JLabel label = new JLabel("?", SwingConstants.CENTER);
//		label.setFont(new Font(label.getFont().getName(), Font.BOLD, 84));
//		label.setForeground(new Color(220,220,220));
		return new JLabel(javax.swing.UIManager.getIcon("OptionPane.warningIcon"));
	}

	private static Component createOkIcon() {
		JLabel label = new JLabel(":-)", SwingConstants.CENTER);
		label.setFont(new Font(label.getFont().getName(), Font.BOLD, 32));
		label.setForeground(new Color(114, 200, 218));
		return label;
//		return new JLabel(javax.swing.UIManager.getIcon("OptionPane.informationIcon"));
	}

	private static Component createUpdatesAvailableMessage() {
		return createHTMLText("<html><h2>Updated software package sources</h2>" +
				"<p>Please review the following update site URL changes.<br/>" +
				"If you have never heard of update sites," +
				" just click <b>OK</b> at the bottom.</p></html>");
	}

	private static Component createHTTPSInfo() {
		// TODO remove note in Updater V2
		JEditorPane text = createHTMLText("<html><h3>ImageJ is improving<br/>data security!</h3>" +
				"From now on ImageJ is supposed to update more securely via HTTPS. " +
				"Therefore addresses of update sites currently in use by your ImageJ installation " +
				"need to be updated.</html>");
		text.setBackground(new Color(250,250,250));
		text.setBorder(BorderFactory.createEmptyBorder(25,15,25,25));
		String bodyRule = "body { color: #404042; }";
		((HTMLDocument)text.getDocument()).getStyleSheet().addRule(bodyRule);
		text.setOpaque(true);
		return text;
	}

	private static JEditorPane createHTMLText(String text) {
		JEditorPane component =
				new JEditorPane(new HTMLEditorKit().getContentType(), text);
		component.setEditable(false);
		component.setOpaque(false);
		Font font = UIManager.getFont("Label.font");
		String bodyRule = "body { font-family: " + font.getFamily() + "; " +
				"font-size: " + font.getSize() + "pt; }";
		((HTMLDocument)component.getDocument()).getStyleSheet().addRule(bodyRule);
		return component;
	}

	private Component createGeneralChoices() {
		JPanel generalChoices = new JPanel();
		generalChoices.setLayout(new MigLayout("flowy, insets n 0 n n", "", ""));
		updateAll = createGeneralChoiceButton("Update all URLs (recommended)");
		keepAll = createGeneralChoiceButton("Keep the current URLs");
		manualChoice = createGeneralChoiceButton("Adjust manually:");
		manualChoice.setFont(manualChoice.getFont().deriveFont(manualChoice.getFont().getStyle() & ~Font.BOLD));
		generalChoices.add(updateAll);
		generalChoices.add(keepAll);
		generalChoices.add(manualChoice);
		return generalChoices;
	}

	private JRadioButton createGeneralChoiceButton(String name) {
		JRadioButton btn = new JRadioButton(name);
		generalChoiceGroup.add(btn);
		btn.addActionListener(this);
		return btn;
	}

	private Component createButtonsPanel() {
		final JPanel buttons = new JPanel();
		buttons.setLayout(new MigLayout("", "[]push[][]", ""));
		stopAsking = new JCheckBox("Remember to not update these URLs");
		buttons.add(stopAsking);
		cancel = SwingTools.button("Cancel", "Do not update software package sources", this, buttons);
		submit = SwingTools.button("OK", "Continue with updated software package sources", this, buttons);
		getRootPane().setDefaultButton(submit);
		return buttons;
	}

	private Component createUpdateSiteScrollPane() {
		JScrollPane scrollPane = new JScrollPane(createUpdatableSitesTable());
		scrollPane.setPreferredSize(new Dimension(tableModel.tableWidth, 150));
		scrollPane.setMinimumSize(new Dimension(0,0));
		return scrollPane;
	}

	private JTable createUpdatableSitesTable() {
		tableModel = new UpdatableTableDataModel();
		table = new UpdatableSitesTable(tableModel);
		table.setColumnSelectionAllowed(false);
		table.setRowSelectionAllowed(false);
		table.setRowHeight((int) (table.getRowHeight()*1.5));
		table.setShowVerticalLines(false);
		((DefaultTableCellRenderer)table.getTableHeader().getDefaultRenderer())
				.setHorizontalAlignment(JLabel.LEFT);
		table.setDefaultRenderer(URLChange.class, new URLRenderer());
		TableColumn urlColumn = table.getColumnModel().getColumn(urlCol);
		urlColumn.setCellEditor(new URLComboBoxEditor());
		tableModel.setColumnWidths();
		return table;
	}

	private static String wrapToolTip(final String description, final String maintainer) {
		if (description == null) return null;
		return  "<html><p width='400'>" + description.replaceAll("\n", "<br />")
				+ (maintainer != null ? "</p><p>Maintainer: " + maintainer + "</p>": "")
				+ "</p></html>";
	}

	private String getUpdateSiteName(int row) {
		return getUpdateSite( row ).getName();
	}

	private UpdateSite getUpdateSite(int row) {
		return getUrlChange(row).updateSite();
	}

	private URLChange getUrlChange(int row) {
		return urlChanges.get(row);
	}

	private void updateGeneralChoices() {
		int updated = 0;
		int kept = 0;
		for(URLChange change : urlChanges ) {
			if(change.isApproved()) {
				updated++;
			} else {
				kept++;
			}
		}
		if(kept == 0) {
			updateAll.setSelected(true);
		}else {
			if(updated == 0) {
				keepAll.setSelected(true);
			} else {
				manualChoice.setSelected(true);
			}
		}
		stopAsking.setEnabled(kept > 0);
	}

	private void submitAndDispose() {
		okPressed = true;
		if(stopAsking.isSelected()) {
			// stop trying to update the URLs
			urlChanges.forEach(change -> {
				if(!change.isApproved()) {
					change.updateSite().setKeepURL(true);
				}
			});
		}
		super.dispose();
	}

	private void keepAll() {
		urlChanges.forEach(change -> change.setApproved(false));
		stopAsking.setEnabled(true);
		updateTable();
	}

	private void updateAll() {
		urlChanges.forEach(change -> change.setApproved(true));
		stopAsking.setEnabled(false);
		updateTable();
	}

	private void updateTable() {
		tableModel.fireTableRowsUpdated(0, tableModel.getRowCount()-1);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource().equals(ok)) super.dispose();
		else if(e.getSource().equals(cancel)) dispose();
		else if(e.getSource().equals(submit)) submitAndDispose();
		else if(e.getSource().equals(updateAll)) updateAll();
		else if(e.getSource().equals(keepAll)) keepAll();
	}

	@Override
	public void dispose() {
		//reset choices, don't save available updated URLs and continue update
		urlChanges.forEach(change -> change.setApproved(false));
		super.dispose();
	}

	private class UpdatableTableDataModel extends DefaultTableModel {

		int tableWidth;
		int[] widths = { 130, 350 };
		String[] headers = { "Name", "Choice" };

		void setColumnWidths() {
			final TableColumnModel columnModel = table.getColumnModel();
			for (int i = 0; i < tableModel.widths.length && i < getColumnCount(); i++)
			{
				final TableColumn column = columnModel.getColumn(i);
				column.setPreferredWidth(tableModel.widths[i]);
				column.setMinWidth(tableModel.widths[i]);
				tableWidth += tableModel.widths[i];
			}
			columnModel.getColumn(nameCol).setMaxWidth(tableModel.widths[nameCol]);
		}

		@Override
		public int getColumnCount() {
			return 2;
		}

		@Override
		public String getColumnName(final int column) {
			return headers[column];
		}

		@Override
		public Class<?> getColumnClass(final int column) {
			if(column != nameCol) return URLChange.class;
			return String.class;
		}

		@Override
		public int getRowCount() {
			return urlChanges.size();
		}

		@Override
		public Object getValueAt(final int row, final int col) {
			if (col == nameCol) return getUpdateSiteName(row);
			if (col == urlCol) return getUrlChange(row);
			return null;
		}
	}

	private String keepChoiceString(URLChange site) {
		return "<html><b>" + site.updateSite().getURL() + "</b>" + appendOld;
	}

	private String updateChoiceString(URLChange site) {
		return  "<html><b>" + site.getNewURL() + "</b>" + appendNew;
	}

	private class URLComboBoxEditor extends DefaultCellEditor {

		private final JComboBox<String> box = new JComboBox<>();
		private final TableCellEditor editor = new DefaultCellEditor(box);

		URLComboBoxEditor() {
			super(new JComboBox());
			box.addPopupMenuListener(new PopupMenuListener() {
				@Override
				public void popupMenuWillBecomeVisible(PopupMenuEvent e) {}
				@Override
				public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {stopCellEditing();}
				@Override
				public void popupMenuCanceled(PopupMenuEvent e) {stopCellEditing();}
			});
			box.setFont(box.getFont().deriveFont(box.getFont().getStyle() & ~Font.BOLD));
		}

		@Override
		public Object getCellEditorValue() {
			return editor.getCellEditorValue();
		}

		@Override
		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
			URLChange change = getUrlChange(row);
			box.removeAllItems();
			box.addItem(keepChoiceString( change ));
			box.addItem(updateChoiceString( change ));
			box.setSelectedIndex( change.isApproved() ? 1 : 0);
			return editor.getTableCellEditorComponent(table, value, isSelected, row, column);
		}
	}

	private class UpdatableSitesTable extends JTable {
		UpdatableSitesTable(UpdatableTableDataModel tableModel) {
			super(tableModel);
		}

		@Override
		public void valueChanged(final ListSelectionEvent e) {
			super.valueChanged(e);
		}

		@Override
		public boolean isCellEditable(final int row, final int column) {
			return column > 0;
		}

		@Override
		public void setValueAt(final Object value, final int row, final int column)
		{
			URLChange change = getUrlChange(row);
			if(column == urlCol) {
				if(value.equals(keepChoiceString( change ))) {
					change.setApproved(false);
				}
				if(value.equals(updateChoiceString( change ))) {
					change.setApproved(true);
				}
				tableModel.fireTableRowsUpdated(row, row);
				updateGeneralChoices();
			}
		}

		@Override
		public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
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
	}

	private class URLRenderer extends JLabel implements TableCellRenderer {

		URLPanel panel = new URLPanel();

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
		                                               boolean hasFocus, int rowIndex, int vColIndex) {
			if(value == null) return null;
			panel.update( (URLChange) value );
			return panel;
		}
	}

	private class URLPanel extends JPanel {

		JLabel urlLabel, choiceLabel;

		URLPanel() {
			setLayout(new MigLayout("fill"));
			setOpaque(false);
			urlLabel = new JLabel();
			choiceLabel = new JLabel();
			choiceLabel.setFont(choiceLabel.getFont().deriveFont(choiceLabel.getFont().getStyle() & ~Font.BOLD));
			choiceLabel.setBorder(BorderFactory.createEmptyBorder(0,0,0,10));
			add(urlLabel, "dock west");
			add(choiceLabel, "dock east");
		}

		public void update(URLChange change ) {
			urlLabel.setText( change.isApproved() ? change.getNewURL() : change.updateSite().getURL());
			choiceLabel.setText( change.isApproved() ? appendNew : appendOld );
		}

	}
}
