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
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
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
import org.scijava.command.CommandService;
import org.scijava.log.LogService;
import org.scijava.platform.PlatformService;
import org.scijava.plugin.Parameter;
import org.scijava.prefs.PrefService;

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
public class OpViewer extends JFrame implements DocumentListener {

	public static final int DEFAULT_WINDOW_WIDTH = 800;
	public static final int DEFAULT_WINDOW_HEIGHT = 700;
	public static final int COLUMN_MARGIN = 5;
	public static final String WINDOW_HEIGHT = "op.viewer.height";
	public static final String WINDOW_WIDTH = "op.viewer.width";
	public static final String NO_NAMESPACE = "default namespace";

	// Sizing fields
	private int[] widths;

	// Child elements
	private JTextField prompt;
	private JXTreeTable treeTable;
	private OpTreeTableModel model;
	private JLabel successLabel = null;

	// Icons
	private ImageIcon opFail;
	private ImageIcon opSuccess;

	// Caching TreePaths
	private Set<TreePath> expandedPaths;

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
	private CommandService commandService;

	public OpViewer(final Context context) {
		super("Viewing available Ops...");
		context.inject(this);

		expandedPaths = new HashSet<>();

		// Load the frame size
		loadPreferences();

		model = new OpTreeTableModel();
		widths = new int[model.getColumnCount()];

		// Populate the nodes
		createNodes(model.getRoot());

		treeTable = new JXTreeTable(model);
		treeTable.setColumnMargin(COLUMN_MARGIN);

		// Allow rows to be selected
		treeTable.setRowSelectionAllowed(true);

		// Expand the top row
		treeTable.expandRow(0);

		// Update dimensions
		final Dimension dims = new Dimension(getSize());
		int preferredWidth = 0;
		for (int i : widths)
			preferredWidth += (i + COLUMN_MARGIN);
		dims.setSize(preferredWidth, DEFAULT_WINDOW_HEIGHT);
		setPreferredSize(dims);

		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		for (int i = 0; i < model.getColumnCount(); i++) {
			treeTable.getColumn(i).setPreferredWidth(widths[i]);
		}

		// Build search panel
		prompt = new JTextField("", 20);
		final JLabel label = new JLabel("Filter Ops:  ");
		final JPanel panel = new JPanel();
		panel.add(label);
		panel.add(prompt);

		panel.add(new JSeparator(SwingConstants.VERTICAL));

		// Build buttons
		final JButton runButton = new JButton(new ImageIcon(getClass().getResource("/icons/opbrowser/play.png")));
		final JButton snippetButton = new JButton(new ImageIcon(getClass().getResource("/icons/opbrowser/paperclip.png")));
		final JButton wikiButton = new JButton(new ImageIcon(getClass().getResource("/icons/opbrowser/globe.png")));

		runButton.setToolTipText("Run the Op selected below");
		runButton.addActionListener(new RunButtonListener());

		snippetButton.setToolTipText("Copy the selected code snippet to your clipboard (e.g. for use in the script editor)");
		snippetButton.addActionListener(new SnippetButtonListener());

		wikiButton.setToolTipText("Learn more about ImageJ Ops");
		wikiButton.addActionListener(new WikiButtonListener());

		panel.add(runButton);
		panel.add(snippetButton);

		panel.add(new JSeparator(SwingConstants.VERTICAL));

		panel.add(wikiButton);

		panel.add(new JSeparator(SwingConstants.VERTICAL));

		// These icons are used for visual feedback after clicking a button
		opFail = new ImageIcon(getClass().getResource("/icons/opbrowser/redx.png"));
		opSuccess = new ImageIcon(getClass().getResource("/icons/opbrowser/greencheck.png"));
		successLabel = new JLabel(opSuccess);
		successLabel.setVisible(false);

		panel.add(successLabel);

		prompt.getDocument().addDocumentListener(this);

		// Add panel
		add(panel, BorderLayout.NORTH);

		// Add table and make it scrollable
		add(new JScrollPane(treeTable, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED), BorderLayout.CENTER);

		try {
			if (SwingUtilities.isEventDispatchThread()) {
				pack();
			}
			else {
				SwingUtilities.invokeAndWait(new Runnable() {

					@Override
					public void run() {
						pack();
					}
				});
			}
		}
		catch (final Exception ie) {
			/* ignore */
		}

		setLocationRelativeTo(null); // center on screen
		requestFocus();
	}

	/**
	 * Load any preferences saved via the {@link PrefService}, such as window
	 * width and height.
	 */
	public void loadPreferences() {
		final Dimension dim = getSize();

		// If a dimension is 0 then use the default dimension size
		if (0 == dim.width) {
			dim.width = DEFAULT_WINDOW_WIDTH;
		}
		if (0 == dim.height) {
			dim.height = DEFAULT_WINDOW_HEIGHT;
		}

		setPreferredSize(new Dimension(prefService.getInt(WINDOW_WIDTH, dim.width),
			prefService.getInt(WINDOW_HEIGHT, dim.height)));
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
		final OpTreeTableNode parent = new OpTreeTableNode("ops", "# @OpService ops;",
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

			CommandInfo cInfo;

			if (selectedNode == null || (cInfo = selectedNode.getCommandInfo()) == null) {
				statusService.clearStatus();
				statusService.showStatus("No Op selected or Op name not found");
				setFail();
			} else {
				commandService.run(cInfo, true);
				setPass();
			}
		}
	}

	/**
	 * Abstract helper class for button {@link ActionListener}s
	 */
	private abstract class OpsViewerButtonListener implements ActionListener {
		public OpTreeTableNode getSelectedNode() {
			final int row = treeTable.getSelectedRow();
			if (row < 0) return null;

			final TreePath path = treeTable.getPathForRow(row);
			return (OpTreeTableNode) path.getPath()[path.getPathCount()-1];
		}

		public void setPass() {
			successLabel.setVisible(true);
			successLabel.setIcon(opSuccess);
		}

		public void setFail() {
			successLabel.setVisible(true);
			successLabel.setIcon(opFail);
		}
	}
}
