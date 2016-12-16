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

package net.imagej.plot;

import net.imagej.table.Table;

import org.scijava.Context;
import org.scijava.log.LogService;
import org.scijava.ui.UIService;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * A plot of data from a {@link Table}.
 * 
 * @author Curtis Rueden
 */

public class TempMain {

	public static void main(final String... args) {
		new TempMain();
	}

	private UIService ui;
	private LogService log;
	private PlotService plotService;

	public TempMain()
	{
		final Context ctx = new Context();
		ui = ctx.service(UIService.class);
		log = ctx.service(LogService.class);
		plotService = ctx.service(PlotService.class);

		plotLineStyles();
		plotMarkerStyles();
		plotLogarithmic();
	}

	private void plotLineStyles() {
		ScatterPlot plot = plotService.createScatterPlot();
		plot.setTitle("Line Styles");
		Collection<Double> xs = collection(0.0,1.0);
		LineStyle[] lineStyles = LineStyle.values();
		SeriesStyle style = plot.createSeriesStyle();
		style.setColor(Color.BLACK);
		for(int i = 0; i < lineStyles.length; i++) {
			style.setLineStyle(lineStyles[i]);
			double y = i * 1.0;
			plot.addSeries(lineStyles[i].toString(), xs, collection(y,y), style);
		}
		plot.getXAxis().setManualRange(-1.0, 2.0);
		plot.getYAxis().setManualRange(-1.0, (double) lineStyles.length);
		ui.show(plot);
	}

	private void plotMarkerStyles() {
		ScatterPlot plot = plotService.createScatterPlot();
		plot.setTitle("Marker Styles");
		SeriesStyle style = plot.createSeriesStyle();
		Collection<Double> xs = collection(0.0,1.0);
		MarkerStyle[] markerStyles = MarkerStyle.values();
		for(int i = 0; i < markerStyles.length; i++) {
			style.setMarkerStyle(markerStyles[i]);
			double y = i * 1.0;
			plot.addSeries(markerStyles[i].toString(), xs, collection(y,y), style);
		}
		plot.getXAxis().setManualRange(-1.0, 2.0);
		plot.getYAxis().setManualRange(-1.0, (double) markerStyles.length);
		ui.show(plot);
	}

	private void plotLogarithmic() {
		ScatterPlot plot = plotService.createScatterPlot();
		plot.setTitle("Logarithmic");
		SeriesStyle style = plot.createSeriesStyle();
		Collection<Double> xs = new ArrayList<>();
		Collection<Double> ys = new ArrayList<>();
		for(double x = 0; x < 10; x += 0.1) {
			xs.add(x);
			ys.add(Math.exp(Math.sin(x)));
		}
		plot.addSeries("exp(sin(x))", xs, ys, style);
		plot.getXAxis().setAutoRange(true, false);
		plot.getYAxis().setAutoRange(true, true);
		plot.getYAxis().setLogarithmic(true);
		ui.show(plot);
	}

	private static Collection<Double> collection(Double ... values) {
		return Arrays.asList(values);
	}

}
