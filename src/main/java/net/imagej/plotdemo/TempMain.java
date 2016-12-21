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

package net.imagej.plotdemo;

import net.imagej.plot.*;
import net.imagej.table.Table;

import org.scijava.Context;
import org.scijava.log.LogService;
import org.scijava.ui.UIService;
import org.scijava.util.Colors;

import java.util.*;

/**
 * A plot of data from a {@link Table}.
 * 
 * @author Curtis Rueden
 */

public class TempMain {

	private UIService ui;

	private LogService log;

	private PlotService plotService;

	public static void main(final String... args) {
		new TempMain();
	}

	public TempMain() {
		final Context ctx = new Context();
		ui = ctx.service(UIService.class);
		log = ctx.service(LogService.class);
		plotService = ctx.service(PlotService.class);

		plotLineStyles();
		plotMarkerStyles();
		plotLogarithmic();

		showCategoryChart();
	}

	private void plotLineStyles() {
		XYPlot plot = plotService.createXYPlot();
		plot.setTitle("Line Styles");
		Collection<Double> xs = collection(0.0,1.0);
		LineStyle[] lineStyles = LineStyle.values();
		for(int i = 0; i < lineStyles.length; i++) {
			double y = i * 1.0;
			XYSeries series = plot.createXYSeries(lineStyles[i].toString(), xs, collection(y,y));
			SeriesStyle style = plot.createSeriesStyle(Colors.BLACK, lineStyles[i], MarkerStyle.CIRCLE);
			series.setStyle(style);
			plot.getItems().add(series);
		}
		plot.getXAxis().setManualRange(-1.0, 2.0);
		plot.getYAxis().setManualRange(-1.0, (double) lineStyles.length);
		ui.show(plot);
	}

	private void plotMarkerStyles() {
		XYPlot plot = plotService.createXYPlot();
		plot.setTitle("Marker Styles");
		Collection<Double> xs = collection(0.0,1.0);
		MarkerStyle[] markerStyles = MarkerStyle.values();
		for(int i = 0; i < markerStyles.length; i++) {
			double y = i * 1.0;
			XYSeries series = plot.createXYSeries(markerStyles[i].toString(), xs, collection(y,y));
			SeriesStyle style = plot.createSeriesStyle(null, null, markerStyles[i]);
			series.setStyle(style);
			plot.getItems().add(series);
		}
		plot.getXAxis().setManualRange(-1.0, 2.0);
		plot.getYAxis().setManualRange(-1.0, (double) markerStyles.length);
		ui.show(plot);
	}

	private void plotLogarithmic() {
		XYPlot plot = plotService.createXYPlot();
		plot.setTitle("Logarithmic");
		Collection<Double> xs = new ArrayList<>();
		Collection<Double> ys = new ArrayList<>();
		for(double x = 0; x < 10; x += 0.1) {
			xs.add(x);
			ys.add(Math.exp(Math.sin(x)));
		}
		XYSeries series = plot.createXYSeries("exp(sin(x))", xs, ys);
		plot.getItems().add(series);
		plot.getXAxis().setAutoRange(RangeStrategy.AUTO);
		plot.getYAxis().setAutoRange(RangeStrategy.AUTO);
		plot.getYAxis().setLogarithmic(true);
		ui.show(plot);
	}

	private void showCategoryChart() {
		CategoryChart chart = plotService.createCategoryChart();
		chart.getCategoryAxis().setCategories(collection("one wheel", "bicycle", "car"));
		LineSeries wheels = chart.createLineSeries("speed", collection(1.0, 2.0, 4.0));
		BarSeries speed = chart.createBarSeries("speed", collection(6.0, 55.0, 200.0));
		BoxSeries boxes = chart.createBoxSeries("boxes", collection(
				randomCollection(10),
				randomCollection(20),
				randomCollection(30)));
		boxes.setColor(Colors.BLACK);
		BoxSeries boxes2 = chart.createBoxSeries("boxes", collection(
				randomCollection(10),
				randomCollection(20),
				randomCollection(30)));
		boxes2.setColor(Colors.CYAN);
		chart.getItems().add(wheels);
		chart.getItems().add(speed);
		chart.getItems().add(boxes);
		chart.getItems().add(boxes2);
		ui.show(chart);
	}

	private static <T> Collection<T> collection(T ... values) {
		return Arrays.asList(values);
	}

	private static Collection<Double> randomCollection(int size) {
		Random rand = new Random();
		Vector<Double> result = new Vector<>(size);
		for(int i = 0; i < size; i++)
			result.add(rand.nextGaussian()*20);
		return result;
	}
}
