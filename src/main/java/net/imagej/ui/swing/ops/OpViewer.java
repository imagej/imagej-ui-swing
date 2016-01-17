/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2014 - 2015 Board of Regents of the University of
 * Wisconsin-Madison, University of Konstanz and Brian Northan.
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

package net.imagej.ui.swing.ops;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.script.ScriptException;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.WindowConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.tree.TreePath;

import net.imagej.ops.Namespace;
import net.imagej.ops.Op;
import net.imagej.ops.OpInfo;
import net.imagej.ops.OpService;
import net.imagej.ops.OpUtils;

import org.jdesktop.swingx.JXTreeTable;
import org.scijava.Context;
import org.scijava.app.StatusService;
import org.scijava.command.CommandInfo;
import org.scijava.log.LogService;
import org.scijava.module.ModuleItem;
import org.scijava.platform.PlatformService;
import org.scijava.plugin.Parameter;
import org.scijava.prefs.PrefService;
import org.scijava.script.ScriptService;
import org.scijava.thread.ThreadService;

/**
 * A scrollable tree view of all discovered {@link Op} implementations. The goal
 * of this class is to make it easy to discover the available {@code Ops}, their
 * associated {@link Namespace}, and specific signatures available for these
 * ops.
 * <p>
 * {@code Ops} are sorted with {@code Namespaces} as the top level, then
 * {@code Op} name, finally with {@code Op} signatures as the leaves.
 * </p>
 *
 * @author Mark Hiner <hinerm@gmail.com>
 */
@SuppressWarnings("serial")
public class OpViewer extends JFrame implements DocumentListener, ActionListener {

	public static final int DETAILS_WINDOW_WIDTH = 400;
	public static final int MAIN_WINDOW_HEIGHT = 700;
	public static final int COLUMN_MARGIN = 5;
	public static final int HIDE_COOLDOWN = 1500;
	public static final String WINDOW_HEIGHT = "op.viewer.height";
	public static final String WINDOW_WIDTH = "op.viewer.width";
	public static final String NO_NAMESPACE = "(global)";

	// Sizing fields
	private int[] widths;

	// Child elements
	private JTextField prompt;
	private JXTreeTable treeTable;
	private OpTreeTableModel model;
	private JLabel successLabel = null;
	private JEditorPane textPane;
	private JScrollPane detailsPane;
	private JButton toggleDetailsButton;

	// Icons
	private ImageIcon opFail;
	private ImageIcon opSuccess;
	private ImageIcon expandDetails;
	private ImageIcon hideDetails;

	// Caching TreePaths
	private Set<TreePath> expandedPaths;

	// For hiding the successLabel
	private Timer timer;
	private ActionListener taskPerformer;

	@Parameter
	private StatusService statusService;

	@Parameter
	private OpService opService;

	@Parameter
	private PrefService prefService;

	@Parameter
	private LogService logService;

	@Parameter
	private PlatformService platformService;

	@Parameter
	private ScriptService scriptService;

	@Parameter
	private ThreadService threadService;

	public OpViewer(final Context context) {
		super("Viewing available Ops...   [shift + L]");
		context.inject(this);

		// Load the frame size
//		loadPreferences();

		initialize();
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);


		// Build search panel
		// Use flow layout to avoid resizing when showing/hiding the status buttons
		final JPanel topBar = buildTopPanel();
		add(topBar, BorderLayout.NORTH);

		// Build the tree table
		buildTreeTable();
		add(new JScrollPane(treeTable, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED), BorderLayout.CENTER);

		// Build the details pane
		buildDetailsPane();
		add(detailsPane, BorderLayout.EAST);

		// Build the bottom panel
		final JPanel panelBottom = buildBottomPanel();
		add(panelBottom, BorderLayout.SOUTH);

		// Update dimensions
		updateDimensions();


		pack();

		setLocationRelativeTo(null); // center on screen
		requestFocus();
	}

	/**
	 * 
	 */
	private void updateDimensions() {
		final Dimension dims = new Dimension(getSize());

		// Update preferred width of main window based
		// on discovered widths
		int preferredWidth = getPreferredMainWidth();

		// Update preferred width to account for the
		// details pane
		preferredWidth += DETAILS_WINDOW_WIDTH;

		// Set preferred width of main window
		dims.setSize(preferredWidth, MAIN_WINDOW_HEIGHT);
		setPreferredSize(dims);

		// Set column widths in the main table
		for (int i = 0; i < model.getColumnCount(); i++) {
			treeTable.getColumn(i).setPreferredWidth(widths[i]);
		}
	}

	/**
	 * 
	 */
	private void initialize() {
		expandedPaths = new HashSet<>();
		model = new OpTreeTableModel();
		widths = new int[model.getColumnCount()];
		taskPerformer = new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent evt) {
				successLabel.setVisible(false);
			}
		};
		timer = new Timer(HIDE_COOLDOWN, taskPerformer);
	}

	/**
	 * 
	 */
	private void buildTreeTable() {
		// Populate the nodes
		createNodes(model.getRoot());

		// per-cell tool-tips
		treeTable = new JXTreeTable(model) {
			// Adapted from:
			// http://stackoverflow.com/a/21281257/1027800
			@Override
			public String getToolTipText(final MouseEvent e) {
				String tip = "";
				final Point p = e.getPoint();
				final int rowIndex = rowAtPoint(p);
				final int colIndex = columnAtPoint(p);

				try {
					final OpTreeTableNode n = getNodeAtRow(rowIndex);
					if (n != null) {
						switch (colIndex)
						{
						case 0:
							String name;
							if (rowIndex == 0) name = "all available ops";
							else name = n.getName();
							if (rowIndex > 0 && n.getCodeCall().isEmpty())
								name += " namespace";
							return name;
						case 1:
							return n.getCodeCall();
						case 2:
							return n.getReferenceClass();
						}
					}
				} catch (RuntimeException e1) {
					// catch null pointer exception if mouse is over an empty
					// line
				}

				return tip;
			}
		};

		// Double-click to copy cell contents
		// See:
		// http://stackoverflow.com/a/25918436/1027800
		treeTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					final Point p = e.getPoint();
					final int rowIndex = treeTable.rowAtPoint(p);
					final int colIndex = treeTable.columnAtPoint(p);
					final OpTreeTableNode n = getNodeAtRow(rowIndex);

					if (n != null) {
						String text;
						switch (colIndex) {
						case 0:
							text = n.getName();
							break;
						case 1:
							text = n.getCodeCall();
							break;
						case 2:
							text = n.getReferenceClass();
							break;
						default:
							text = "";
						}

						if (text.isEmpty()) {
							setSuccessIcon(opFail);
						} else {
							final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
							clipboard.setContents(new StringSelection(text), null);
							setSuccessIcon(opSuccess);
						}
						queueHide();
					}
				}
			}
		});

		treeTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(final ListSelectionEvent event) {
				final OpTreeTableNode n = getNodeAtRow(treeTable.getSelectedRow());
				if (n != null) {
					final String newText = n.getReferenceClass();
					if (!newText.isEmpty())
						textPane.setText(n.getReferenceClass());
				}
			}
		});

		// Space the columns slightly
		treeTable.setColumnMargin(COLUMN_MARGIN);

		// Allow rows to be selected
		treeTable.setRowSelectionAllowed(true);

		// Expand the top row
		treeTable.expandRow(0);
	}

	private int getPreferredMainWidth() {
		int preferredWidth = 0;
		for (int i : widths)
			preferredWidth += (i + COLUMN_MARGIN);

		return preferredWidth;
	}

	/**
	 */
	private JPanel buildTopPanel() {
		prompt = new JTextField("", 20);
		final JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		final JLabel searchLabel = new JLabel("Filter Ops:  ");
		topPanel.add(Box.createRigidArea(new Dimension(5, 0)));
		topPanel.add(searchLabel);
		topPanel.add(prompt);

		topPanel.add(Box.createRigidArea(new Dimension(20, 0)));

		// Build buttons
		final JButton runButton = new JButton(new ImageIcon(getClass().getResource("/icons/opbrowser/play.png")));
		final JButton snippetButton = new JButton(new ImageIcon(getClass().getResource("/icons/opbrowser/paperclip.png")));
		final JButton wikiButton = new JButton(new ImageIcon(getClass().getResource("/icons/opbrowser/globe.png")));
		runButton.setPreferredSize(new Dimension(32, 32));
		snippetButton.setPreferredSize(new Dimension(32, 32));
		wikiButton.setPreferredSize(new Dimension(32, 32));

		runButton.setToolTipText("Run the selected Op");
		runButton.addActionListener(new RunButtonListener());

		snippetButton.setToolTipText(
				"<html>Copy the selected code snippet to your clipboard.<br />"
						+ "You can also double-click a cell to copy its contents.</html>");
		snippetButton.addActionListener(new SnippetButtonListener());

		wikiButton.setToolTipText("Learn more about ImageJ Ops");
		wikiButton.addActionListener(new WikiButtonListener());

		topPanel.add(runButton);
		topPanel.add(Box.createRigidArea(new Dimension(7, 0)));
		topPanel.add(snippetButton);
		topPanel.add(Box.createRigidArea(new Dimension(7, 0)));
		topPanel.add(wikiButton);

		topPanel.add(Box.createRigidArea(new Dimension(10, 0)));

		// These icons are used for visual feedback after clicking a button
		opFail = new ImageIcon(getClass().getResource("/icons/opbrowser/redx.png"));
		opSuccess = new ImageIcon(getClass().getResource("/icons/opbrowser/greencheck.png"));
		successLabel = new JLabel(opSuccess);
		successLabel.setPreferredSize(new Dimension(20, 20));
		successLabel.setVisible(false);

		topPanel.add(successLabel);

		prompt.getDocument().addDocumentListener(this);	

		setPanelToMainWidth(topPanel);

		return topPanel;
	}

	/**
	 * 
	 */
	private void buildDetailsPane() {
		textPane = new JEditorPane("text/html", "Select an Op for more information");
		textPane.setEditable(false);

		detailsPane = new JScrollPane(textPane, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		detailsPane.setPreferredSize(new Dimension(DETAILS_WINDOW_WIDTH, MAIN_WINDOW_HEIGHT));
	}

	/**
	 */
	private JPanel buildBottomPanel() {
		final JPanel panelBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		hideDetails = new ImageIcon(getClass().getResource("/icons/opbrowser/arrow_left.png"));
		expandDetails = new ImageIcon(getClass().getResource("/icons/opbrowser/arrow_right.png"));
		toggleDetailsButton = new JButton(hideDetails);
		toggleDetailsButton.setPreferredSize(new Dimension(32, 32));
		toggleDetailsButton.setToolTipText("Show / Hide Details");
		toggleDetailsButton.addActionListener(this);
		panelBottom.add(toggleDetailsButton);
		setPanelToMainWidth(panelBottom);
		return panelBottom;
	}

	private void setPanelToMainWidth(final JPanel panel) {
		final Dimension dims = new Dimension(panel.getPreferredSize());
		dims.setSize(getPreferredMainWidth(), dims.getHeight());
		panel.setPreferredSize(dims);
	}

	/**
	 * Load any preferences saved via the {@link PrefService}, such as window
	 * width and height.
	 */
	public void loadPreferences() {
		final Dimension dim = getSize();

		// If a dimension is 0 then use the default dimension size
		if (0 == dim.width) {
			dim.width = getPreferredMainWidth();

			dim.width += DETAILS_WINDOW_WIDTH;
		}
		if (0 == dim.height) {
			dim.height = MAIN_WINDOW_HEIGHT;
		}

		setPreferredSize(new Dimension(prefService.getInt(WINDOW_WIDTH, dim.width),
			prefService.getInt(WINDOW_HEIGHT, dim.height)));
	}

	@Override
	public void pack() {
		// Pack
		try {
			if (SwingUtilities.isEventDispatchThread()) {
				super.pack();
			}
			else {
				SwingUtilities.invokeAndWait(new Runnable() {

					@Override
					public void run() {
						OpViewer.super.pack();
					}
				});
			}
		}
		catch (final Exception ie) {
			logService.error(ie);
		}
	}

	@Override
	public void actionPerformed(final ActionEvent e) {
		if (e.getSource() == toggleDetailsButton) {
			int newWidth = getWidth();
			if (detailsPane.isVisible()) {
				newWidth -= detailsPane.getWidth();
				// provide a small margin when hiding
				detailsPane.setVisible(false);
				toggleDetailsButton.setIcon(expandDetails);
			} else {
				detailsPane.setVisible(true);
				newWidth += detailsPane.getPreferredSize().getWidth();
				toggleDetailsButton.setIcon(hideDetails);
			}

				setSize(newWidth, getHeight());
		}
	}

	// -- DocumentListener methods --

	@Override
	public void insertUpdate(final DocumentEvent e) {
		filterOps(e);
	}

	@Override
	public void removeUpdate(final DocumentEvent e) {
		filterOps(e);
	}

	@Override
	public void changedUpdate(final DocumentEvent e) {
		filterOps(e);
	}

	private void filterOps(final DocumentEvent e) {
		final Document doc = e.getDocument();
		try {
			final String text = doc.getText(0, doc.getLength());

			if (text == null || text.isEmpty()) {
				treeTable.setTreeTableModel(model);
				restoreExpandedPaths();
			}
			else {
				cacheExpandedPaths();
				OpTreeTableModel tempModel = new OpTreeTableModel();
				createNodes(tempModel.getRoot(), text);
				treeTable.setTreeTableModel(tempModel);

				treeTable.expandAll();
			}
		} catch (final BadLocationException exc) {
			logService.error(exc);
		}
	}

	/**
	 * Expand all cached TreePaths and clear the cache.
	 */
	private void restoreExpandedPaths() {
		for (final TreePath path : expandedPaths) {
			treeTable.expandPath(path);
		}

		expandedPaths.clear();
	}

	/**
	 * If they are not already cached, check which paths are expanded and
	 * cache them for future restoration.
	 */
	private void cacheExpandedPaths() {
		// If paths have already been cached we don't need to do anything.
		// Paths are only cached when filtering, and restored when done
		// filtering.
		if (!expandedPaths.isEmpty()) return;

		// Find and cache the expanded paths
		for (int i=0; i<treeTable.getRowCount(); i++) {
			if (treeTable.isExpanded(i))
				expandedPaths.add(treeTable.getPathForRow(i));
		}
	}

	// -- Helper methods --

	/**
	 * Helper method to populate the {@link Op} nodes. Ops without a valid name
	 * will be skipped. Ops with no namespace will be put in a
	 * {@link #NO_NAMESPACE} category.
	 */
	private void createNodes(final OpTreeTableNode root) {
		createNodes(root, null);
	}

	/**
	 * As {@link #createNodes(OpTreeTableNode)} with an option filter to restrict
	 * nodes matched.
	 */
	private void createNodes(final OpTreeTableNode root, final String filter) {
		final String filterLC = filter == null ? "" : filter.toLowerCase();
		final OpTreeTableNode parent = new OpTreeTableNode("ops", "# @OpService ops",
						"net.imagej.ops.OpService");
		root.add(parent);

		// Map namespaces and ops to their parent tree node
		final Map<String, OpTreeTableNode> namespaces =
			new HashMap<>();

		final Map<String, OpTreeTableNode> ops =
			new HashMap<>();

		// Iterate over all ops
		for (final OpInfo info : opService.infos()) {
			final String namespace = getName(info.getNamespace(), NO_NAMESPACE);

			// Get the namespace node for this Op
			final OpTreeTableNode nsCategory = getCategory(parent, namespaces,
				namespace);

			final String opName = getName(info.getSimpleName(), info.getName());

			if (!opName.isEmpty()) {
				// get the general Op node for this Op
				final OpTreeTableNode opCategory = getCategory(nsCategory, ops,
					opName);

				final String delegateClass = info.cInfo().getDelegateClassName();
				//NB: FILTERING here
				final String opClass = info.cInfo().getAnnotation().type().getSimpleName().toLowerCase();
				final String simpleDelegate = delegateClass
						.substring(delegateClass.lastIndexOf("."), delegateClass.length()).toLowerCase();
				if (filterLC.isEmpty() || opClass.contains(filterLC) || simpleDelegate.contains(filterLC)) {
					final String simpleName = OpUtils.simpleString(info.cInfo());
					final String codeCall = OpUtils.opCall(info.cInfo());

					updateWidths(widths, simpleName, codeCall, delegateClass);

					// Create a leaf node for this particular Op's signature
					final OpTreeTableNode opSignature = new OpTreeTableNode(
							simpleName, codeCall, delegateClass);

					opSignature.setCommandInfo(info.cInfo());

					opCategory.add(opSignature);
				}
			}
		}

		pruneEmptyNodes(root);
	}

	/**
	 * Recursively any node that a) has no children, and b) has no "ReferenceClass" field
	 *
	 * @return true if this node should be removed from the child list.
	 */
	private boolean pruneEmptyNodes(final OpTreeTableNode node) {
		boolean removeThis = node.getCodeCall().isEmpty();
		final List<OpTreeTableNode> preservedChildren = new ArrayList<>();

		for (final OpTreeTableNode child : node.getChildren()) {
			if (!pruneEmptyNodes(child)) preservedChildren.add(child);
		}

		node.getChildren().retainAll(preservedChildren);

		removeThis &= node.getChildren().isEmpty();

		return removeThis;
	}

	/**
	 * Helper method to update the widths array to track the longest strings in each column.
	 */
	private void updateWidths(int[] colWidths, String... colContents) {
		
		for (int i=0; i<Math.min(colWidths.length, colContents.length); i++) {
			colWidths[i] = Math.max(colWidths[i], colContents[i].length());
		}
	}

	/**
	 * Helper method to get a properly formatted name. {@code name} is tried
	 * first, then {@code backupName} if needed (i.e. {@code name} is {@code null}
	 * or empty).
	 * <p>
	 * The resulting string is trimmed and set to lowercase.
	 * </p>
	 */
	private String getName(String name, final String backupName) {
		if (name == null || name.isEmpty()) name = backupName;

		return name == null ? "" : name.toLowerCase().trim();
	}

	/**
	 * Helper method to retrieved a map category with the specified name. If the
	 * category does not exist yet, it's created, added to the map, and added as a
	 * child to the parent tree node.
	 */
	private OpTreeTableNode getCategory(
		final OpTreeTableNode parent,
		final Map<String, OpTreeTableNode> ops,
		final String categoryName)
	{
		OpTreeTableNode nsCategory = ops.get(categoryName);
		if (nsCategory == null) {
			nsCategory = new OpTreeTableNode(categoryName);
			parent.add(nsCategory);
			ops.put(categoryName, nsCategory);
		}

		return nsCategory;
	}

	private OpTreeTableNode getSelectedNode() {
		final int row = treeTable.getSelectedRow();
		if (row < 0) return null;

		return getNodeAtRow(row);
	}

	private OpTreeTableNode getNodeAtRow(final int row) {
		final TreePath path = treeTable.getPathForRow(row);
		return path == null ? null : (OpTreeTableNode) path.getPath()[path.getPathCount() - 1];
	}

	public void setSuccessIcon(final ImageIcon icon) {
		successLabel.setVisible(true);
		successLabel.setIcon(icon);
	}

	private void queueHide() {
		synchronized(timer) {
			timer.stop();
			timer = new Timer(HIDE_COOLDOWN, taskPerformer);
			timer.start();
		}
	}

	/**
	 * Button action listener to open the ImageJ Ops wiki page
	 */
	private class WikiButtonListener extends OpsViewerButtonListener {
		@Override
		public void actionPerformed(final ActionEvent e) {
			try {
				platformService.open(new URL("http://imagej.net/ImageJ_Ops"));
			} catch (final IOException exc) {
				logService.error(exc);
			}
		}
	}

	/**
	 * Button action listener to copy selected row's code snippet to clipboard
	 */
	private class SnippetButtonListener extends OpsViewerButtonListener {
		@Override
		public void actionPerformed(final ActionEvent e) {
			final OpTreeTableNode selectedNode = getSelectedNode();

			String codeCall;

			if (selectedNode == null || (codeCall = selectedNode.getCodeCall()).isEmpty()) {
				statusService.clearStatus();
				statusService.showStatus("No code to copy (is an Op row selected?)");
				setFail();
			} else {
				statusService.showStatus("Op copied to clipboard!");
				final StringSelection stringSelection = new StringSelection(codeCall);
				final Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
				clpbrd.setContents(stringSelection, null);
				setPass();
			}
		}
	}

	/**
	 * Button action listener to run the selected row's code snippet via the
	 * {@link OpService}.
	 */
	private class RunButtonListener extends OpsViewerButtonListener {
		@Override
		public void actionPerformed(final ActionEvent e) {
			final OpTreeTableNode selectedNode = getSelectedNode();

			if (selectedNode == null || selectedNode.getCommandInfo() == null) {
				statusService.clearStatus();
				statusService.showStatus("No Op selected or Op name not found");
				setFail();
			} else {
				try {
					final String script = makeScript(selectedNode);
					scriptService.run("op_browser.py", script, true);
				} catch (IOException | ScriptException | NoSuchFieldException | SecurityException
						| InstantiationException | IllegalAccessException | ClassNotFoundException exc) {
					logService.error(exc);
				}
				setPass();
			}
		}

		private String makeScript(final OpTreeTableNode node) throws NoSuchFieldException, SecurityException, InstantiationException, IllegalAccessException, ClassNotFoundException {
			final CommandInfo cInfo = node.getCommandInfo();
			final StringBuffer sb = new StringBuffer();
			sb.append("# @OpService ops\n");
			int i = 1;
			for (final ModuleItem<?> in : cInfo.inputs()) {
				if (in.isRequired()) {
					sb.append("# @");
					sb.append(in.getType().getName());
					sb.append(" p");
					sb.append(i++);
					sb.append("\n");
				}
			}

			sb.append("ops.run(\"");
			sb.append(OpUtils.getOpName(cInfo));
			sb.append("\"");

			i = 1;
			for (final ModuleItem<?> in : cInfo.inputs()) {
				if (in.isRequired()) {
					sb.append(", p");
					sb.append(i++);
				}
			}
			sb.append(")\n");

			return sb.toString();
		}
	}

	/**
	 * Abstract helper class for button {@link ActionListener}s
	 */
	private abstract class OpsViewerButtonListener implements ActionListener {

		public void setPass() {
			setSuccessIcon(opSuccess);
			queueHide();
		}

		public void setFail() {
			setSuccessIcon(opFail);
			queueHide();
		}
	}
}
