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

import java.awt.Font;

import javax.swing.JPanel;

import net.imagej.plot.Plot;
import net.imagej.plot.PlotDisplay;
import net.imagej.plot.PlotDisplayPanel;
import net.imagej.table.Table;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;
import org.scijava.ui.viewer.DisplayWindow;

/**
 * A JFreeChart-driven display panel for {@link Plot}s.
 * 
 * @author Curtis Rueden
 */
public class SwingPlotDisplayPanel extends JPanel implements PlotDisplayPanel {

	// -- instance variables --

	private final DisplayWindow window;
	private final PlotDisplay display;
	private final JFreeChart chart;

	// -- constructor --

	public SwingPlotDisplayPanel(final PlotDisplay display,
		final DisplayWindow window)
	{
		this.display = display;
		this.window = window;
		chart = makeChart();
		ChartPanel panel = new ChartPanel(chart);
		add(panel);
		window.setContent(this);
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
	public void redoLayout() {
		// FIXME
	}

	@Override
	public void setLabel(final String s) {
		// FIXME
	}

	@Override
	public void redraw() {
		// FIXME
	}

	// -- Helper methods --

	private JFreeChart makeChart() {
		final Table<?, ?> data = display.get(0).getData();

		// FIXME: make the dataset based on the above Table object instead.
		// if data cell is of type Number, then cast; else convert.
		// convertService.convert(dataCell, Number.class)
		// convertService.convert(dataCell, Double.class)
		// Or, just Double.parseDouble(dataCell.toString()); // basic conversion
		// Or: throw an exception
		// We could type Plot on numerical columns only
		// Or: throw IllegalStateException or something if Table columns are non-numeric.
		// Or: skip table columns that are non-numeric.
		DefaultPieDataset dataset = new DefaultPieDataset();
		dataset.setValue("One", new Double(43.2));
		dataset.setValue("Two", new Double(10.0));
		dataset.setValue("Three", new Double(27.5));
		dataset.setValue("Four", new Double(17.5));
		dataset.setValue("Five", new Double(11.0));
		dataset.setValue("Six", new Double(19.4));

		JFreeChart chart = ChartFactory.createPieChart(
			"Pie Chart Demo 1",  // chart title
			dataset,             // data
			true,               // include legend
			true,
			false
				);

		PiePlot plot = (PiePlot) chart.getPlot();
		plot.setLabelFont(new Font("SansSerif", Font.PLAIN, 12));
		plot.setNoDataMessage("No data available");
		plot.setCircular(false);
		plot.setLabelGap(0.02);
		return chart;
	}
}
