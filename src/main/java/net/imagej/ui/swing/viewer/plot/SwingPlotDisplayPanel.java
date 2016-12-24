/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2016 Board of Regents of the University of
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

package net.imagej.ui.swing.viewer.plot;

import java.awt.*;
import javax.swing.*;

import net.imagej.plot.AbstractPlot;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.scijava.convert.ConvertService;
import org.scijava.ui.viewer.DisplayWindow;

/**
 * A JFreeChart-driven display panel for {@link JfcPlotGenerator}s.
 * 
 * @author Curtis Rueden
 */
public class SwingPlotDisplayPanel extends JPanel implements PlotDisplayPanel {

	// -- instance variables --

	private final DisplayWindow window;
	private final PlotDisplay display;
	private final ConvertService convertService;
	private Dimension prefferedSize;

	// -- constructor --

	public SwingPlotDisplayPanel(final PlotDisplay display,
		final DisplayWindow window, final ConvertService convertService)
	{
		this.display = display;
		this.window = window;
		this.convertService = convertService;
		setLayout(new BorderLayout());
		AbstractPlot plot = display.get(0);
		prefferedSize = new Dimension(plot.getPreferredWidth(), plot.getPreferredHeight());
		try {
			add(makeJFreeChartPanel(plot));
		} catch (ConverterFailedException e) {
			add(new JLabel(e.getMessage()));
		}
		window.setContent(this);
	}

	JPanel makeJFreeChartPanel(AbstractPlot plot) {
		final JFreeChart chart = convertService.convert(plot, JFreeChart.class);
		if(chart == null)
			throw new ConverterFailedException("No sci-java  plugin of type Converter<" +
				plot.getClass().getName() + ", JFreeChart> found.");
		return new ChartPanel(chart);
	}

	private class ConverterFailedException extends RuntimeException {
		ConverterFailedException(String message) {
			super(message);
		}
	}

	// -- PlotDisplayPanel methods --

	@Override
	public PlotDisplay getDisplay() {
		return display;
	}

	// -- DisplayPanel methods --

	@Override
	public DisplayWindow getWindow() {
		return window;
	}

	@Override
	public void redoLayout() { }

	@Override
	public void setLabel(final String s) { }

	@Override
	public void redraw() { }

	@Override
	public Dimension getPreferredSize() {
		return prefferedSize;
	}

}
