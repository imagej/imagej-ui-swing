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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.script.ScriptException;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
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
import net.imglib2.img.Img;
import net.miginfocom.swing.MigLayout;

import org.ahocorasick.trie.Emit;
import org.ahocorasick.trie.Trie;
import org.jdesktop.swingx.JXTreeTable;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import org.scijava.Context;
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
public class OpFinder extends JFrame implements DocumentListener, ActionListener {

	public static final int DETAILS_WINDOW_WIDTH = 400;
	public static final int MAIN_WINDOW_HEIGHT = 700;
	public static final int COLUMN_MARGIN = 5;
	public static final int HIDE_COOLDOWN = 1500;
	public static final String WINDOW_HEIGHT = "op.viewer.height";
	public static final String WINDOW_WIDTH = "op.viewer.width";
	public static final String NO_NAMESPACE = "(global)";
	public static final String BASE_JAVADOC_URL = "http://javadoc.imagej.net/ImageJ/";

	// Simple mode
	private boolean simple = true;
	private ModeButton modeButton;
	private JLabel searchLabel;
	private boolean autoToggle = true;
	private Set<Class<?>> simpleInputs;

	// Off-EDT work
	private FilterRunner lastFilter;
	private HTMLFetcher lastHTMLReq;

	// Sizing fields
	private int[] widths;

	// Child elements
	private JTextField searchField;
	private JXTreeTable treeTable;
	private JLabel successLabel = null;
	private JEditorPane textPane;
	private JScrollPane detailsPane;
	private JButton toggleDetailsButton;
	private JPanel mainPane;
	private JSplitPane splitPane;
	private JProgressBar progressBar;
	private OpTreeTableModel advModel;
	private OpTreeTableModel smplModel;

	// Icons
	private ImageIcon opFail;
	private ImageIcon opSuccess;
	private ImageIcon expandDetails;
	private ImageIcon hideDetails;

	// Caching TreePaths
	private Set<TreePath> advExpandedPaths;
	private Set<TreePath> smplExpandedPaths;

	// Caching web elements
	private Map<String, String> elementsMap;

	// Cache tries for matching
	private Map<Trie, OpTreeTableNode> advTries;
	private Map<Trie, OpTreeTableNode> smplTries;

	// For hiding the successLabel
	private Timer successTimer;
	private Timer progressTimer;

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

	public OpFinder(final Context context) {
		super("Viewing available Ops...   [shift + L]");
		context.inject(this);

		initialize();
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		//NB top panel defines column count
		mainPane = new JPanel(new MigLayout("", "[][][][][][][grow, right]","[grow]"));

		// Build search panel
		buildTopPanel();

		// Build the tree table
		buildTreeTable();

		// Build the details pane
		buildDetailsPane();

		// Build the bottom panel
		buildBottomPanel();

		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, mainPane, null);
		add(splitPane);
	}

	/**
	 * 
	 */
	private void initialize() {
		advExpandedPaths = new HashSet<>();
		smplExpandedPaths = new HashSet<>();
		elementsMap = new HashMap<>();
		advTries = new HashMap<>();
		smplTries = new HashMap<>();
		advModel = new OpTreeTableModel(false);
		smplModel = new OpTreeTableModel(true);
		widths = new int[advModel.getColumnCount()];

		buildSimpleInputs();

		successTimer = new Timer(HIDE_COOLDOWN,  new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent evt) {
				successLabel.setVisible(false);
			}
		});

		progressTimer = new Timer(HIDE_COOLDOWN,  new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent evt) {
				progressBar.setVisible(false);
			}
		});
	}

	/**
	 * TODO
	 */
	private void buildSimpleInputs() {
		simpleInputs = new HashSet<>();
		simpleInputs.add(Img.class);
	}

	/**
	 * 
	 */
	private void buildTreeTable() {
		// Populate the nodes
		createNodes();

		// per-cell tool-tips
		treeTable = new JXTreeTable(simple ? smplModel : advModel) {
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
							{
								final OpTreeTableNode firstChild = n.getChildren().get(0);
								if (firstChild != null && firstChild.getChildren().isEmpty()) {
									// If a child of this node is a leaf then this node
									// is an Op node
									name += " op";
								}
								// Otherwise this is a namespace
								else name += " namespace";
							}
							return name;
						default: return (String) treeTable.getValueAt(rowIndex, colIndex);
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
						final String text = treeTable.getValueAt(rowIndex, colIndex).toString();

						if (text.isEmpty()) {
							copyFail();
						} else {
							final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
							clipboard.setContents(new StringSelection(text), null);
							copyPass();
						}
						successTimer.restart();
					}
				}
			}
		});

		treeTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(final ListSelectionEvent event) {
				final OpTreeTableNode n = getNodeAtRow(treeTable.getSelectedRow());
				if (n != null && detailsPane.isVisible()) {
					String newText = n.getReferenceClass();
					if (!newText.isEmpty()) {
						newText = newText.replaceAll("\\.", "/");
						if (newText.contains("$")) {
							// For nested classes, replace $ with a URL-safe '.'
							final String suffix = newText.substring(newText.lastIndexOf("$"));
							newText = newText.replace(suffix, "%2E" + suffix.substring(1));
						}
						final String requestedClass = newText;
						final StringBuilder sb = new StringBuilder();
						sb.append(BASE_JAVADOC_URL);
						sb.append(newText);
						sb.append(".html");
						final String url = sb.toString();

						synchronized (elementsMap) {
							if (elementsMap.containsKey(url)) {
								textPane.setText(elementsMap.get(url));
								scrollToTop();
							} else {
								if (lastHTMLReq != null && !lastHTMLReq.isDone())
									lastHTMLReq.stop();

								lastHTMLReq = new HTMLFetcher(sb, url, requestedClass);
								threadService.run(lastHTMLReq);
							}
						}
					}
				}
			}


		});

		// Space the columns slightly
		treeTable.setColumnMargin(COLUMN_MARGIN);

		// Allow rows to be selected
		treeTable.setRowSelectionAllowed(true);

		// Expand the top row
		treeTable.expandRow(0);
		final int preferredWidth = getPreferredMainWidth();
		mainPane.add(
				new JScrollPane(treeTable, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
						ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED),
				"span, wrap, grow, w " + preferredWidth/2 + ":" + preferredWidth + ", h " + MAIN_WINDOW_HEIGHT);
	}

	/**
	 * TODO
	 */
	private void scrollToTop() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				// scroll to 0,0
				detailsPane.getVerticalScrollBar().setValue(0);
				detailsPane.getHorizontalScrollBar().setValue(0);
			}
		});
	}

	/**
	 * TODO
	 */
	private int getPreferredMainWidth() {
		int preferredWidth = 0;
		for (int i : widths)
			preferredWidth += (i + COLUMN_MARGIN);

		return preferredWidth;
	}

	/**
	 * TODO
	 */
	private void buildTopPanel() {
		final int searchWidth = 160;
		searchField = new JTextField(searchWidth);
		searchLabel = new JLabel();
		searchLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		mainPane.add(searchLabel, "w 145!");
		mainPane.add(searchField, "w " + searchWidth + "!");

		// Build buttons
		modeButton = new ModeButton();
		final JButton runButton = new JButton(new ImageIcon(getClass().getResource("/icons/opbrowser/play.png")));
		final JButton snippetButton = new JButton(new ImageIcon(getClass().getResource("/icons/opbrowser/paperclip.png")));
		final JButton wikiButton = new JButton(new ImageIcon(getClass().getResource("/icons/opbrowser/globe.png")));

		runButton.setToolTipText("Run the selected Op");
		runButton.addActionListener(new RunButtonListener());

		snippetButton.setToolTipText(
				"<html>Copy the selected code snippet to your clipboard.<br />"
						+ "You can also double-click a cell to copy its contents.</html>");
		snippetButton.addActionListener(new CopyButtonListener());

		wikiButton.setToolTipText("Learn more about ImageJ Ops");
		wikiButton.addActionListener(new WikiButtonListener());

		mainPane.add(modeButton, "w 145!, h 32!, gapleft 15");
		mainPane.add(runButton, "w 32!, h 32!, gapleft 15");
		mainPane.add(snippetButton, "w 32!, h 32!");
		mainPane.add(wikiButton, "w 32!, h 32!");

		// These icons are used for visual feedback after clicking a button
		opFail = new ImageIcon(getClass().getResource("/icons/opbrowser/redx.png"));
		opSuccess = new ImageIcon(getClass().getResource("/icons/opbrowser/greencheck.png"));
		successLabel = new JLabel();
		successLabel.setHorizontalTextPosition(SwingConstants.LEFT);
		successLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		successLabel.setVisible(false);

		mainPane.add(successLabel, "h 20!, w 155!, wrap");

		searchField.getDocument().addDocumentListener(this);
	}

	/**
	 * TODO
	 */
	private void buildDetailsPane() {
		textPane = new JEditorPane("text/html", "Select an Op for more information");
		textPane.setEditable(false);

		detailsPane = new JScrollPane(textPane, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		detailsPane.setPreferredSize(new Dimension(DETAILS_WINDOW_WIDTH, MAIN_WINDOW_HEIGHT));
		detailsPane.setVisible(false);
	}

	/**
	 * TODO
	 */
	private void buildBottomPanel() {
		progressBar = new JProgressBar(SwingConstants.HORIZONTAL, 0, 100);
		mainPane.add(progressBar, "w 100!");
		progressBar.setVisible(false);

		hideDetails = new ImageIcon(getClass().getResource("/icons/opbrowser/arrow_left.png"));
		expandDetails = new ImageIcon(getClass().getResource("/icons/opbrowser/arrow_right.png"));
		toggleDetailsButton = new JButton(expandDetails);
		toggleDetailsButton.setToolTipText("Show / Hide Details");
		toggleDetailsButton.addActionListener(this);
		mainPane.add(toggleDetailsButton, "span, align right, w 32!, h 32!");
	}

	/**
	 * TODO
	 */
	private void setProgress(final int progress) {
		progressBar.setVisible(true);
		progressBar.setValue(progress);
		if (progress >= progressBar.getMaximum())
			progressTimer.restart();
		else progressTimer.stop();
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
						OpFinder.super.pack();
					}
				});
			}
		}
		catch (final Exception ie) {
			logService.error(ie);
		}
	}

	/**
	 * TODO
	 */
	@Override
	public void actionPerformed(final ActionEvent e) {
		if (e.getSource() == toggleDetailsButton) {
			autoToggle = false;
			toggleDetails();
		}
	}

	/**
	 * TODO
	 */
	private void toggleDetails() {
		if (detailsPane == null) return;
		final boolean hide = detailsPane.isVisible();

		if (hide) {
			detailsPane.setPreferredSize(detailsPane.getSize());
			splitPane.remove(detailsPane);
			detailsPane.setVisible(false);
			toggleDetailsButton.setIcon(expandDetails);
		} else {
			detailsPane.setVisible(true);
			splitPane.add(detailsPane);
			toggleDetailsButton.setIcon(hideDetails);
		}

		// Prevent left side from resizing
		Component lc = splitPane.getLeftComponent();
		lc.setPreferredSize(lc.getSize());

		pack();
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

	/**
	 * TODO
	 */
	private void filterOps(final DocumentEvent e) {
		final Document doc = e.getDocument();
		filterOps(doc);
	}

	/**
	 * TODO
	 */
	private void filterOps(final Document doc) {
		try {
			final String text = doc.getText(0, doc.getLength());

			if (lastFilter != null) {
				lastFilter.stop();
			}

			if (text == null || text.isEmpty()) {
				treeTable.setTreeTableModel(simple ? smplModel : advModel);
				restoreExpandedPaths(simple, true);
			} else {
				cacheExpandedPaths(simple);
				lastFilter = new FilterRunner(text);
				threadService.run(lastFilter);
			}
		} catch (final BadLocationException exc) {
			logService.error(exc);
		}
	}

	/**
	 * Expand all cached TreePaths
	 *TODO
	 */
	private void restoreExpandedPaths(final boolean isSimple, final boolean clearCache) {
		final Set<TreePath> paths = isSimple ? smplExpandedPaths : advExpandedPaths;

		if (paths.isEmpty()) {
			// expand top row by default
			treeTable.expandRow(0);
		}
		else {
			for (final TreePath path : paths) {
				treeTable.expandPath(path);
			}
		}

		if (clearCache) {
			paths.clear();
		}

	}

	/**
	 * If they are not already cached, check which paths are expanded and
	 * cache them for future restoration.
	 */
	private void cacheExpandedPaths(final boolean isSimple) {
		final Set<TreePath> paths = isSimple ? smplExpandedPaths : advExpandedPaths;

		// Find and cache the expanded paths
		for (int i=0; i<treeTable.getRowCount(); i++) {
			if (treeTable.isExpanded(i))
				paths.add(treeTable.getPathForRow(i));
		}
	}

	// -- Helper methods --

	private class FilterRunner extends InterruptableRunner {
		private String text;

		public FilterRunner(final String text){
			this.text = text;
		}

		@Override
		public void run() {
			OpTreeTableModel tempModel = new OpTreeTableModel(simple);
			final OpTreeTableNode filtered = applyFilter(text.toLowerCase(Locale.getDefault()),
					simple ? smplTries : advTries);

			if (filtered == null) return;

			tempModel.getRoot().add(filtered);

			if (poll()) return;

			try {
				SwingUtilities.invokeAndWait(new Runnable() {

					@Override
					public void run() {
						// Don't update AWT stuff off the EDT
						treeTable.setTreeTableModel(tempModel);

						treeTable.expandAll();
					}
				});
			} catch (InvocationTargetException | InterruptedException exc) {
				logService.error(exc);
			}
		}

		/**
		 * TODO
		 */
		private OpTreeTableNode applyFilter(final String filter, final Map<Trie, OpTreeTableNode> tries) {

			// add to this guy..
			final OpTreeTableNode parent = new OpTreeTableNode("ops", "# @OpService ops", "net.imagej.ops.OpService");

			final Map<Integer, List<OpTreeTableNode>> scoredOps = new HashMap<>();
			final List<Integer> keys = new ArrayList<>();

			// How many top scores to keep
			// A higher score means more "fuzziness"
			int keep = 1;
			int count = 0;
			double nextProgress = 0.05;

			// For each Op, parse the filter text
			// Each fragment scores ((2 * length) - 1)
			for (final Trie trie : tries.keySet()) {
				count++;
				if (((double)count  / tries.keySet().size()) >= nextProgress) {
					if (poll()) return null;
					setProgress((int)(nextProgress * 100));
					nextProgress += 0.05;
				}

				final Collection<Emit> parse = trie.parseText(filter);
				int score = 0;
				for (Emit e : parse)
					score += ((2 * e.getKeyword().length()) - 1);

				// get the positional index of this key
				final int pos = -(Collections.binarySearch(keys, score) + 1);

				// Same value as another score
				if (scoredOps.containsKey(score)) {
					scoredOps.get(score).add(tries.get(trie));
					if (!keys.contains(score))
						keys.add(score, pos);
				} else {
					// If we haven't filled our score quota yet
					// we can freely add this score.
					if (keys.size() < keep || pos > 0) {
						final List<OpTreeTableNode> ops = new ArrayList<>();
						ops.add(tries.get(trie));
						scoredOps.put(score, ops);
						keys.add(pos, score);
						// If we are bumping a score, remove the lowest
						// key
						if (keys.size() > keep) {
							scoredOps.remove(keys.remove(0));
						}
					}
				}
			}

			final List<OpTreeTableNode> children = parent.getChildren();

			for (int i = keys.size() - 1; i >= 0; i--) {
				final Integer key = keys.get(i);
				for (final OpTreeTableNode node : scoredOps.get(key))
					children.add(node);
			}

			setProgress(100);

			return parent;
		}
	}

	/**
	 * Helper method to populate the {@link Op} nodes. Ops without a valid name
	 * will be skipped. Ops with no namespace will be put in a
	 * {@link #NO_NAMESPACE} category.
	 */
	private void createNodes() {
		final OpTreeTableNode advParent = new OpTreeTableNode("ops", "# @OpService ops",
						"net.imagej.ops.OpService");
		final OpTreeTableNode smplParent = new OpTreeTableNode("ops", "# @OpService ops",
						"net.imagej.ops.OpService");
		advModel.getRoot().add(advParent);
		smplModel.getRoot().add(smplParent);

		// Map namespaces and ops to their parent tree node
		final Map<String, OpTreeTableNode> advNamespaces =
			new HashMap<>();
		final Map<String, OpTreeTableNode> smplNamespaces =
			new HashMap<>();
		final Set<String> smplOps = new HashSet<>();

		// Iterate over all ops
		for (final OpInfo info : opService.infos()) {

			final String opName = getName(info.getSimpleName(), info.getName());

			if (!opName.isEmpty()) {
				final String namespacePath = getName(info.getNamespace(),NO_NAMESPACE);
				final String pathToOp = namespacePath + "." + opName;

				// Build the node path to this op.
				// There is one node per namespace.
				// Then a general Op type node, the leaves of which are the actual
				// implementations.
				final OpTreeTableNode advOpType = buildOpHierarchy(advParent, advNamespaces, pathToOp);
				final OpTreeTableNode smplOpType = buildOpHierarchy(smplParent, smplNamespaces, pathToOp);

				final String delegateClass = info.cInfo().getDelegateClassName();
				String simpleName = OpUtils.simpleString(info.cInfo());
				final String codeCall = OpUtils.opCall(info.cInfo());

				// Create a leaf node for this particular Op's signature
				final OpTreeTableNode opSignature = new OpTreeTableNode(simpleName, codeCall, delegateClass);
				opSignature.setCommandInfo(info.cInfo());

				final Trie advTrie = buildTries(delegateClass, '.');
				advTries.put(advTrie, opSignature);
				advOpType.add(opSignature);
				simpleName = simplifyTypes(simpleName);

				if (isSimple(info.cInfo(), simpleName, smplOps)) {
					final OpTreeTableNode simpleOp = new OpTreeTableNode(simpleName, codeCall, delegateClass);
					final Trie smplTrie = buildTries(simpleName);
					smplTries.put(smplTrie, simpleOp);
					smplOpType.add(simpleOp);
				}

				updateWidths(widths, simpleName, codeCall, delegateClass);
			}
		}

		pruneEmptyNodes(smplParent);
	}

	/**
	 * TODO
	 */
	private String simplifyTypes(String simpleName) {
		simpleName = simpleName.replaceAll("ArrayImg|PlanarImg", "Img");
		simpleName = simpleName.replaceAll("\\(int |\\(short |\\(long |\\(double |\\(float |\\(byte ", "(number ");
		simpleName = simpleName.replaceAll(" int | short | long | double | float | byte ", " number ");
		simpleName = simpleName.replaceAll(" [a-zA-Z0-9]*(\\?)?(,|\\))", "$1$2");
		final int splitPoint = simpleName.substring(0, simpleName.indexOf('(')).lastIndexOf(' ');

		return simpleName.substring(splitPoint + 1);
	}

	/**
	 * TODO
	 */
	private Trie buildTries(final String rawDict, final char... delim) {
		final Trie trie = new Trie().removeOverlaps();
		final Set<String> substrings = getSubstringsWithDelim(rawDict.toLowerCase(Locale.getDefault()), delim);
		for (final String substring : substrings)
			trie.addKeyword(substring);

		return trie;
	}

	/**
	 * TODO
	 */
	private boolean isSimple(final CommandInfo info, final String simpleName, final Set<String> smplOps) {
		if (!smplOps.contains(simpleName)) {
			for (final ModuleItem<?> moduleItem : info.inputs()) {
				final Class<?> inputType = moduleItem.getType();
				for (final Class<?> acceptedClass : simpleInputs) {
					if (acceptedClass.isAssignableFrom(inputType)) {
						smplOps.add(simpleName);
						return true;
					}
				}
			}
		}

		return false;
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
	 * TODO
	 */
	private Set<String> getSubstringsWithDelim(final String string, final char... delims) {
		final Set<String> substringsToCheck = new HashSet<>();

		String strOfInterest = string;

		for (final char delim : delims) {
			int dotIndex = 0;
			while (dotIndex >= 0) {
				final int startIndex = dotIndex;
				dotIndex = string.indexOf(delim, dotIndex + 1);

				if (dotIndex < 0) {
					strOfInterest = string.substring(startIndex, string.length());
				} else {
					substringsToCheck.add(string.substring(startIndex, dotIndex + 1));
				}
			}
		}

		// iterate over all substring lengths
		for (int start = 0; start < strOfInterest.length() - 1; start++) {
			// iterate over all substring positions
			for (int end = start + 1; end <= strOfInterest.length(); end++) {
				substringsToCheck.add(strOfInterest.substring(start, end));
			}
		}

		return substringsToCheck;
	}

	private OpTreeTableNode buildOpHierarchy(final OpTreeTableNode parent, final Map<String, OpTreeTableNode> namespaces,
			final String namespace) {

		final StringBuilder sb = new StringBuilder();

		OpTreeTableNode prevParent = parent;
		for (final String ns : namespace.split("\\.")) {
			sb.append(ns);
			final String key = sb.toString().toLowerCase(Locale.getDefault());
			OpTreeTableNode nsNode = namespaces.get(key);
			if (nsNode == null) {
				nsNode = new OpTreeTableNode(ns);
				namespaces.put(key, nsNode);
				prevParent.add(nsNode);
			}
			prevParent = nsNode;
		}

		return prevParent;
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
	 * TODO
	 */
	private void copyPass() {
		setSuccessIcon(opSuccess);
		successLabel.setText("copied ");
		successTimer.restart();
	}

	/**
	 * TODO
	 */
	private void copyFail() {
		setSuccessIcon(opFail);
		successLabel.setText("no selection ");
		successTimer.restart();
	}

	/**
	 * TODO
	 */
	private void setSuccessIcon(final ImageIcon icon) {
		successLabel.setVisible(true);
		successLabel.setIcon(icon);
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

		return name == null ? "" : name.trim();
	}

	/**
	 * TODO
	 */
	private OpTreeTableNode getSelectedNode() {
		final int row = treeTable.getSelectedRow();
		if (row < 0) return null;

		return getNodeAtRow(row);
	}

	/**
	 * TODO
	 */
	private OpTreeTableNode getNodeAtRow(final int row) {
		final TreePath path = treeTable.getPathForRow(row);
		return path == null ? null : (OpTreeTableNode) path.getPath()[path.getPathCount() - 1];
	}

	/**
	 * TODO
	 */
	private class ModeButton extends JButton {
		private final String simpleButtonText = "Developer View";
		private final String simpleToolTip = "<html>Recommended for advanced users<br/>"
				+ " and developers<br/>"
				+ "<ul><li>Browse <b>ALL</b> Ops</li>"
				+ "<li>See Op parameters</li>"
				+ "<li>See Op Javadoc</li></ul></html>";
		private final String advancedButtonText = "User View";
		private final String advancedToolTip = "<html>Recommended for new users<br/>"
				+ " and non-developers</html>";

		private final String simpleFilterLabel = "Filter Ops:  ";
		private final String advancedFilterLabel = "Filter Ops by Class:  ";

		public ModeButton() {
			setLabels(simple);

			addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					if (simple) switchToAdvanced();
					else switchToSimple();

					simple = !simple;

					filterOps(searchField.getDocument());
				}

				private void switchToAdvanced() {
					modeButton.setState(false);
				}

				private void switchToSimple() {
					modeButton.setState(true);
				}
				
			});
		}

		public void setLabels(final boolean simple) {
			setText(simple ? simpleButtonText : advancedButtonText);
			setToolTipText(simple ? simpleToolTip : advancedToolTip);
			searchLabel.setText(simple ?simpleFilterLabel : advancedFilterLabel);
		}

		public void setState(final boolean simple) {
			setLabels(simple);
			if (treeTable != null) {
				cacheExpandedPaths(!simple);
				treeTable.setTreeTableModel(simple ? smplModel : advModel);
				restoreExpandedPaths(simple, false);
			}

			if (autoToggle) toggleDetails();
		}
	}

	/**
	 * Button action listener to open the ImageJ Ops wiki page
	 */
	private class WikiButtonListener implements ActionListener {
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
	 * Button action listener to run the selected row's code snippet via the
	 * {@link OpService}.
	 */
	private class RunButtonListener implements ActionListener {
		@Override
		public void actionPerformed(final ActionEvent e) {
			final OpTreeTableNode selectedNode = getSelectedNode();

			try {
				final String script = makeScript(selectedNode);
				scriptService.run("op_browser.py", script, true);
			} catch (IOException | ScriptException | NoSuchFieldException | SecurityException | InstantiationException
					| IllegalAccessException | ClassNotFoundException exc) {
				logService.error(exc);
			}
		}

		/**
		 * TODO
		 */
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
	 * TODO
	 */
	private class CopyButtonListener implements ActionListener {

		@Override
		public void actionPerformed(final ActionEvent e) {

			final int rowIndex = treeTable.getSelectedRow();
			final int colIndex = treeTable.getSelectedColumn();

			String toCopy;

			if (rowIndex < 0) toCopy = "";
			else if (colIndex < 0) toCopy = getSelectedNode().getCodeCall();
			else toCopy = treeTable.getValueAt(rowIndex, colIndex).toString();

			if (toCopy.isEmpty()) {
				copyFail();
			} else {
				final StringSelection stringSelection = new StringSelection(toCopy);
				final Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
				clpbrd.setContents(stringSelection, null);
				copyPass();
			}
		}

	}

	private class HTMLFetcher extends InterruptableRunner {

		private String url;
		private String requestedClass;
		private StringBuilder sb;

		public HTMLFetcher(final StringBuilder sb, final String url, final String requestedClass) {
			this.url = url;
			this.requestedClass = requestedClass;
			this.sb = sb;
		}

		@Override
		public void run() {
			try {
				final org.jsoup.nodes.Document doc = Jsoup.connect(sb.toString()).get();
				Elements elements = doc.select("div.header");
				elements.addAll(doc.select("div.contentContainer"));
				synchronized (elementsMap) {
					elementsMap.put(url, elements.html());
				}
			} catch (final IOException exc) {
				synchronized (elementsMap) {
					elementsMap.put(url, "Javadoc not available for: " + requestedClass);
				}
			}
			if (poll()) return;
			textPane.setText(elementsMap.get(url));
			scrollToTop();

			stop();
		}
		
	}

	private abstract class InterruptableRunner implements Runnable {
		private boolean stop = false;
	
		public synchronized boolean poll() {
			return stop;
		}

		public synchronized void stop() {
			stop = true;
		}

		public synchronized boolean isDone() {
			return stop;
		}
	}
}
