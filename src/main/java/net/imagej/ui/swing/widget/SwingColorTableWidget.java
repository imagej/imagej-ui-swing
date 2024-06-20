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

package net.imagej.ui.swing.widget;

import java.awt.Insets;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;

import net.imagej.ui.swing.updater.SwingTools;
import net.imagej.widget.ColorTableWidget;
import net.imglib2.display.ColorTable;

import org.scijava.plugin.Plugin;
import org.scijava.ui.swing.widget.SwingInputWidget;
import org.scijava.widget.InputWidget;
import org.scijava.widget.WidgetModel;

/**
 * Render a {@link ColorTable} in Swing.
 *
 * @author Barry DeZonia
 */
@Plugin(type = InputWidget.class)
public class SwingColorTableWidget extends SwingInputWidget<ColorTable>
	implements ColorTableWidget<JPanel>
{

	// -- fields --

	private BufferedImage image;
	private JLabel picLabel;
	private final int height;

	// -- constructors --

	public SwingColorTableWidget() {
		height = colorBarHeight();
		image = new BufferedImage(256, height, BufferedImage.TYPE_INT_RGB);
	}

	// -- InputWidget methods --

	@Override
	public ColorTable getValue() {
		return (ColorTable) get().getValue();
	}

	@Override
	public void set(final WidgetModel model) {
		super.set(model);
		picLabel = new JLabel(); // new ImageIcon(image));
		getComponent().add(picLabel);
		refreshWidget();
	}

	@Override
	public boolean supports(final WidgetModel model) {
		return model.isType(ColorTable.class);
	}

	// -- AbstractUIInputWidget methods ---

	@Override
	public void doRefresh() {
		ColorTable colorTable = getValue();
		fillImage(colorTable);
		picLabel.setIcon(new ImageIcon(image));
		picLabel.repaint();
	}

	// -- helpers --

	private void fillImage(ColorTable cTable) {
		for (int x = 0; x < 256; x++) {
			int r = cTable.get(0, x) & 0xff;
			int g = cTable.get(1, x) & 0xff;
			int b = cTable.get(2, x) & 0xff;
			int rgb = (r << 16) | (g << 8) | b;
			for (int y = 0; y < height; y++) {
				image.setRGB(x, y, rgb);
			}
		}
	}

	private static int colorBarHeight() {
		try {
			final Insets insets = UIManager.getInsets("TextPane.margin");
			return UIManager.getFont("TextField.font").getSize() + insets.top +
				insets.bottom;
		}
		catch (final Exception ignored) {
			// do nothing
		}
		return 24;
	}

}
