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

import java.awt.Frame;
import java.awt.Graphics;
import java.io.IOException;
import java.io.OutputStream;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import net.imagej.updater.util.UpdaterUserInterface;
import net.miginfocom.swing.MigLayout;

import org.scijava.app.StatusService;
import org.scijava.log.LogService;
import org.scijava.util.Prefs;

/**
 * TODO
 * 
 * @author Johannes Schindelin
 */
public class SwingUserInterface extends UpdaterUserInterface {

	protected final LogService log;
	protected final StatusService statusService;

	public SwingUserInterface(final LogService log, final StatusService statusService) {
		this.log = log;
		this.statusService = statusService;
	}

	@Override
	public void error(final String message) {

		log.error(message);
		JOptionPane.showMessageDialog(null, message, "ImageJ Updater",
			JOptionPane.ERROR_MESSAGE);

	}

	@Override
	public void info(final String message, final String title) {

		log.info(message);
		JOptionPane.showMessageDialog(null, message, "ImageJ Updater",
			JOptionPane.INFORMATION_MESSAGE);

	}

	@Override
	public void log(final String message) {

		log.info(message);

	}

	@Override
	public void debug(final String message) {

		log.debug(message);

	}

	@Override
	public OutputStream getOutputStream() {

		// TODO: create a JFrame with a JTextPane
		return System.err;

	}

	@Override
	public void showStatus(final String message) {

		if (statusService != null) statusService.showStatus(message);

	}

	@Override
	public void handleException(final Throwable exception) {

		log.error(exception);

	}

	@Override
	public boolean isBatchMode() {

		return false;

	}

	@Override
	public int optionDialog(final String message, final String title,
		final Object[] options, final int def)
	{

		return JOptionPane.showOptionDialog(null, message, title,
			JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null,
			options, options[def]);

	}

	@Override
	public String getPref(final String key) {

		return Prefs.get(this.getClass(), key);

	}

	@Override
	public void setPref(final String key, final String value) {

		Prefs.put(this.getClass(), key, value);

	}

	@Override
	public void savePreferences() {

		/* is done automatically */

	}

	@Override
	public void openURL(final String url) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public String getString(final String title) {

		final JPanel panel = new JPanel();
		panel.setLayout(new MigLayout());

		panel.add(new JLabel("User"));
		final JTextField user = new JTextField() {
			private static final long serialVersionUID = 1L;
			int counter = 5;

			@Override
			public void paint(Graphics g) {
				super.paint(g);
				if (counter > 0) {
					requestFocusInWindow();
					counter--;
				}
			}
		};
		user.setColumns(20);
		panel.add(user);

		if (JOptionPane.showConfirmDialog(null, panel, title,
				JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
			return user.getText();
		}
		return null;

	}

	@Override
	public String getPassword(final String title) {
		final JPanel panel = new JPanel();
		panel.setLayout(new MigLayout());

		panel.add(new JLabel("Password:"));
		final JPasswordField password = new JPasswordField() {
			private static final long serialVersionUID = 1L;
			int counter = 15;

			@Override
			public void paint(Graphics g) {
				super.paint(g);
				if (counter > 0) {
					requestFocusInWindow();
					counter--;
				}
			}
		};
		password.setColumns(20);
		panel.add(password);

		int option = JOptionPane.showConfirmDialog(null, panel, title,
				JOptionPane.OK_CANCEL_OPTION);
		if (option == JOptionPane.CANCEL_OPTION || option < 0) return null;
		return new String(password.getPassword());

	}

	@Override
	public void addWindow(final Frame window) {

		// TODO How to do this?

	}

	@Override
	public void removeWindow(final Frame window) {

		// TODO How to do this?

	}

	@Override
	public boolean promptYesNo(final String message, final String title) {
		return JOptionPane.showConfirmDialog(null, message, title,
			JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
	}

}
