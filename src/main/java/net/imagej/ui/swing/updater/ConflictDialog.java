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

import java.awt.Color;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import net.imagej.updater.Conflicts;
import net.imagej.updater.Conflicts.Conflict;
import net.imagej.updater.Conflicts.Resolution;

/**
 * This dialog lists conflicts and let's the user choose how to resolve (or ignore) them.
 * 
 * @author Johannes Schindelin
 */
@SuppressWarnings("serial")
public abstract class ConflictDialog extends JDialog implements ActionListener {

	protected UpdaterFrame updaterFrame;
	protected JPanel rootPanel;
	public JTextPane panel; // this is public for debugging purposes
	protected SimpleAttributeSet bold, indented, italic, normal, red;
	protected JButton ok, cancel;

	protected List<Conflict> conflictList;
	protected boolean wasCanceled;

	public ConflictDialog(final UpdaterFrame owner, final String title) {
		super(owner, title);

		updaterFrame = owner;

		rootPanel = SwingTools.verticalPanel();
		setContentPane(rootPanel);

		panel = new JTextPane();
		panel.setEditable(false);

		bold = new SimpleAttributeSet();
		StyleConstants.setBold(bold, true);
		indented = new SimpleAttributeSet();
		StyleConstants.setLeftIndent(indented, 40);
		italic = new SimpleAttributeSet();
		StyleConstants.setItalic(italic, true);
		normal = new SimpleAttributeSet();
		red = new SimpleAttributeSet();
		StyleConstants.setForeground(red, Color.RED);

		SwingTools.scrollPane(panel, 650, 450, rootPanel);

		final JPanel buttons = new JPanel();
		ok = SwingTools.button("OK", "Apply resolutions [Enter]", this, buttons);
		cancel = SwingTools.button("Cancel", "Dismiss [Esc]", this, buttons);
		buttons.setMaximumSize(buttons.getPreferredSize()); // do not allow vertical resizing
		rootPanel.add(buttons);

		// do not show, right now
		pack();
		setModal(true);
		setLocationRelativeTo(owner);

		final int ctrl = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
		SwingTools.addAccelerator(cancel, rootPanel, this, KeyEvent.VK_ESCAPE, 0);
		SwingTools.addAccelerator(cancel, rootPanel, this, KeyEvent.VK_W, ctrl);
		SwingTools.addAccelerator(ok, rootPanel, this, KeyEvent.VK_ENTER, 0);

		addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(final WindowEvent e) {
				updateConflictList();
				if (conflictList.size() > 0)
					wasCanceled = true;
			}
		});
	}

	@Override
	public void actionPerformed(final ActionEvent e) {
		if (e.getSource() == cancel) {
			wasCanceled = true;
			dispose();
		}
		else if (e.getSource() == ok) {
			if (!ok.isEnabled()) return;
			dispose();
		}
	}

	@Override
	public void setVisible(final boolean visible) {
		if (SwingUtilities.isEventDispatchThread()) super.setVisible(visible);
		else try {
			SwingUtilities.invokeAndWait(() -> setVisible(visible));
		}
		catch (final InterruptedException e) {
			updaterFrame.log.error(e);
		}
		catch (final InvocationTargetException e) {
			updaterFrame.log.error(e);
		}
	}

	public boolean resolve() {
		listIssues();

		if (panel.getDocument().getLength() > 0) setVisible(true);
		return !wasCanceled;
	}

	protected abstract void updateConflictList();

	protected void listIssues() {
		updateConflictList();
		panel.setText("");

		for (final Conflict conflict : conflictList) {
			maybeAddSeparator();
			newText(conflict.getSeverity().toString() + ": ", conflict.isError() ? red : normal);
			final String filename = conflict.getFilename();
			if (filename != null) addText(filename, bold);
			addText("\n" + conflict.getConflict());
			addText("\n");
			for (final Resolution resolution : conflict.getResolutions()) {
				addText("\n    ");
				addButton(resolution.getDescription(), e -> {
					resolution.resolve();
					listIssues();
				});
			}
		}

		ok.setEnabled(!Conflicts.needsFeedback(conflictList));
		if (ok.isEnabled()) ok.requestFocus();

		if (isShowing()) {
			if (panel.getStyledDocument().getLength() == 0) addText(
				"No more issues to be resolved!", italic);
			panel.setCaretPosition(0);
			panel.repaint();
		}
	}

	protected void addButton(final String label, final ActionListener listener) {
		final JButton button = SwingTools.button(label, null, listener, null);
		selectEnd();
		panel.insertComponent(button);
	}

	protected void selectEnd() {
		final int end = panel.getStyledDocument().getLength();
		panel.select(end, end);
	}

	protected void newText(final String message) {
		newText(message, normal);
	}

	protected void newText(final String message, final SimpleAttributeSet style) {
		if (panel.getStyledDocument().getLength() > 0) addText("\n\n");
		addText(message, style);
	}

	protected void addText(final String message) {
		addText(message, normal);
	}

	protected void addText(final String message, final SimpleAttributeSet style) {
		final int end = panel.getStyledDocument().getLength();
		try {
			panel.getStyledDocument().insertString(end, message, style);
		}
		catch (final BadLocationException e) {
			updaterFrame.log.error(e);
		}
	}

	protected void maybeAddSeparator() {
		if (panel.getText().equals("") && panel.getComponents().length == 0) return;
		addText("\n");
		selectEnd();
		panel.insertComponent(new JSeparator());
	}

}
