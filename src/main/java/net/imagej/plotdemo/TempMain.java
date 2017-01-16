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
		ui.showUI();

		plotLineStyles();
		plotMarkerStyles();
		plotLogarithmic();
		plotCircle();
		showCategoryChart();
		showSortedCategoryCharts();
	}

	private void plotLineStyles() {
		XYPlot plot = plotService.newXYPlot();
		plot.setTitle("Line Styles");
		List<Double> xs = list(0.0,1.0);
		LineStyle[] lineStyles = LineStyle.values();
		for(int i = 0; i < lineStyles.length; i++) {
			double y = i * 1.0;
			XYSeries series = plot.addXYSeries();
			series.setLabel(lineStyles[i].toString());
			series.setValues(xs, list(y,y));
			series.setStyle(plot.newSeriesStyle(Colors.BLACK, lineStyles[i], MarkerStyle.CIRCLE));
		}
		plot.xAxis().setManualRange(-1.0, 2.0);
		plot.yAxis().setManualRange(-1.0, (double) lineStyles.length);
		ui.show(plot);
	}

	private void plotMarkerStyles() {
		XYPlot plot = plotService.newXYPlot();
		plot.setTitle("Marker Styles");
		List<Double> xs = list(0.0,1.0);
		MarkerStyle[] markerStyles = MarkerStyle.values();
		for(int i = 0; i < markerStyles.length; i++) {
			double y = i * 1.0;
			XYSeries series = plot.addXYSeries();
			series.setLabel(markerStyles[i].toString());
			series.setValues(xs, list(y,y));
			series.setStyle(plot.newSeriesStyle(null, null, markerStyles[i]));
		}
		plot.xAxis().setManualRange(-1.0, 2.0);
		plot.yAxis().setManualRange(-1.0, (double) markerStyles.length);
		ui.show(plot);
	}

	private void plotLogarithmic() {
		XYPlot plot = plotService.newXYPlot();
		plot.setTitle("Logarithmic");
		List<Double> xs = new ArrayList<>();
		List<Double> ys = new ArrayList<>();
		for(double x = 0; x < 10; x += 0.1) {
			xs.add(x);
			ys.add(Math.exp(Math.sin(x)));
		}
		XYSeries series = plot.addXYSeries();
		series.setLabel("exp(sin(x))");
		series.setValues(xs, ys);
		plot.xAxis().setAutoRange();
		plot.yAxis().setAutoRange();
		plot.yAxis().setLogarithmic(true);
		ui.show(plot);
	}

	private void plotCircle() {

		XYPlot plot = plotService.newXYPlot();
		plot.setTitle("Logarithmic");

		List<Double> xs = new ArrayList<>();
		List<Double> ys = new ArrayList<>();
		for(double t = 0; t < 2 * Math.PI; t += 0.1) {
			xs.add(Math.sin(t));
			ys.add(Math.cos(t));
		}

		XYSeries series = plot.addXYSeries();
		series.setLabel("exp(sin(x))");
		series.setValues(xs, ys);

		plot.xAxis().setAutoRange();
		plot.yAxis().setAutoRange();
		ui.show(plot);

	}

	private void showCategoryChart() {

		CategoryChart<String> chart = plotService.newCategoryChart();
		Comparator<String> reverse = (s, t1) -> t1.compareTo(s);
		chart.categoryAxis().setOrder(reverse);

		Map<String, Double> wheelsData = new TreeMap<>();
		wheelsData.put("one wheel", 1.0);
		wheelsData.put("bicycle", 2.0);
		wheelsData.put("car", 4.0);

		LineSeries<String> wheels = chart.addLineSeries();
		wheels.setLabel("wheels");
		wheels.setValues(wheelsData);

		Map<String, Double> speedData = new TreeMap<>();
		speedData.put("one wheel", 10.0);
		speedData.put("bicycle", 30.0);
		speedData.put("car", 200.0);

		BarSeries<String> speed = chart.addBarSeries();
		speed.setLabel("speed");
		speed.setValues(speedData);

		Map<String, Collection<Double>> randomData1 = new TreeMap<>();
		randomData1.put("one wheel", randomCollection(10));
		randomData1.put("bicycle", randomCollection(20));
		randomData1.put("car", randomCollection(30));

		BoxSeries<String> random1 = chart.addBoxSeries();
		random1.setLabel("boxes1");
		random1.setValues(randomData1);
		random1.setColor(Colors.CYAN);

		Map<String, Collection<Double>> randomData2 = new TreeMap<>();
		randomData2.put("one wheel", randomCollection(10));
		randomData2.put("bicycle", randomCollection(20));
		randomData2.put("car", randomCollection(30));

		BoxSeries<String> random2 = chart.addBoxSeries();
		random2.setLabel("boxes2");
		random2.setValues(randomData2);
		random2.setColor(Colors.BLACK);

		ui.show(chart);

	}

	private void showSortedCategoryCharts() {
		showSortedCategoryChart(new AxisManipulator() {
			@Override
			void manipulate(CategoryAxis<String> axis) {
				axis.setManualCategories(list("a","c","b"));
				axis.setLabel("acb");
			}
		});
		showSortedCategoryChart(new AxisManipulator() {
			@Override
			void manipulate(CategoryAxis<String> axis) {
				axis.setManualCategories(list("a","c","b","g"));
				axis.setLabel("acbg");
			}
		});
		showSortedCategoryChart(new AxisManipulator() {
			@Override
			void manipulate(CategoryAxis<String> axis) {
				axis.setManualCategories(list("d","c","a","b"));
				axis.setOrder(String::compareTo);
				axis.setLabel("abcd");
			}
		});
		showSortedCategoryChart(new AxisManipulator() {
			@Override
			void manipulate(CategoryAxis<String> axis) {
				axis.setManualCategories(list());
				axis.setOrder(String::compareTo);
				axis.setLabel("empty");
			}
		});
	}

	static abstract class AxisManipulator {
		abstract void manipulate(CategoryAxis<String> axis);
	}

	private void showSortedCategoryChart(AxisManipulator categoryAxisManipulator) {
		CategoryChart<String> chart = plotService.newCategoryChart();
		categoryAxisManipulator.manipulate(chart.categoryAxis());

		Map<String, Double> data = new TreeMap<>();
		data.put("a", 1.0);
		data.put("b", 2.0);
		data.put("c", 3.0);
		data.put("d", 4.0);

		BarSeries<String> bars = chart.addBarSeries();
		bars.setValues(data);

		ui.show(chart);
	}

	private static <T> List<T> list(T ... values) {
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
