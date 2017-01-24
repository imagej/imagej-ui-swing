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

package net.imagej.ui.swing.viewer.plot.jfreechart;

import net.imagej.plot.*;
import net.imagej.plot.XYPlot;
import net.imagej.plot.XYSeries;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.*;

import java.util.Collection;
import java.util.Iterator;

import static net.imagej.ui.swing.viewer.plot.jfreechart.Utils.*;

/**
 * @author Matthias Arzt
 */
class XYPlotGenerator {

	private final XYPlot xyPlot;

	private final SortedLabelFactory sortedLabelFactory = new SortedLabelFactory();

	private final org.jfree.chart.plot.XYPlot jfcPlot = new org.jfree.chart.plot.XYPlot();

	private final XYSeriesCollection jfcDataSet = new XYSeriesCollection();

	private final XYLineAndShapeRenderer jfcRenderer = new XYLineAndShapeRenderer();

	private XYPlotGenerator(XYPlot xyPlot) {
		this.xyPlot = xyPlot;
	}

	public static JFreeChart run(XYPlot xyPlot) { return new XYPlotGenerator(xyPlot).getJFreeChart();
	}

	private JFreeChart getJFreeChart() {
		jfcPlot.setDataset(jfcDataSet);
		jfcPlot.setDomainAxis(getJFreeChartAxis(xyPlot.xAxis()));
		jfcPlot.setRangeAxis(getJFreeChartAxis(xyPlot.yAxis()));
		jfcPlot.setRenderer(jfcRenderer);
		addAllSeries();
		return Utils.setupJFreeChart(xyPlot.getTitle(), jfcPlot);
	}

	private void addAllSeries() {
		for(XYPlotItem series : xyPlot.getItems())
			if(series instanceof XYSeries)
				addSeries((XYSeries) series);
	}

	private void addSeries(XYSeries series) {
		SortedLabel uniqueLabel = sortedLabelFactory.newLabel(series.getLabel());
		addSeriesData(uniqueLabel, series.getXValues(), series.getYValues());
		setSeriesStyle(uniqueLabel, series.getStyle(), series.getLegendVisible());
	}

	private void addSeriesData(SortedLabel uniqueLabel, Collection<Double> xs, Collection<Double> ys) {
		org.jfree.data.xy.XYSeries series = new org.jfree.data.xy.XYSeries(uniqueLabel, false, true);
		Iterator<Double> xi = xs.iterator();
		Iterator<Double> yi = ys.iterator();
		while (xi.hasNext() && yi.hasNext())
			series.add(xi.next(), yi.next());
		jfcDataSet.addSeries(series);
	}

	private void setSeriesStyle(SortedLabel label, SeriesStyle style, boolean legendVisible) {
		if (style == null)
			return;
		int index = jfcDataSet.getSeriesIndex(label);
		RendererModifier.wrap(jfcRenderer).setSeriesStyle(index, style);
		jfcRenderer.setSeriesVisibleInLegend(index, legendVisible);
	}

}
