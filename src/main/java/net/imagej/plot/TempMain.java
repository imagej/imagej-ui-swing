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

import net.imagej.table.DefaultGenericTable;
import net.imagej.table.DoubleColumn;
import net.imagej.table.GenericColumn;
import net.imagej.table.GenericTable;
import net.imagej.table.Table;

import org.scijava.Context;
import org.scijava.log.LogService;
import org.scijava.ui.UIService;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Random;

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

		plotSomething();
		plotLineStyles();
		plotMarkerStyles();
		plotLogarithmic();
	}

	private void plotSomething() {
		ScatterPlot plot = plotService.createScatterPlot();
		plot.setTitle("Population of largest cities!");
		GenericTable table2 = createSampleTable3();
		SeriesStyle style = plot.createSeriesStyle();
		style.setColor(Color.BLACK);
		plot.addSeries("blub", (DoubleColumn) table2.get(0), (DoubleColumn) table2.get(1), style);
		GenericTable table3 = createSampleTable3();
		style.setColor(Color.BLUE);
		style.setLineStyle(LineStyle.NONE);
		plot.addSeries("blub", (DoubleColumn) table3.get(0), (DoubleColumn) table3.get(1), style);
		ui.show(plot);
	}

	private void plotLineStyles() {
		ScatterPlot plot = plotService.createScatterPlot();
		Collection<Double> xs = collection(0.0,1.0);
		LineStyle[] lineStyles = LineStyle.values();
		SeriesStyle style = plot.createSeriesStyle();
		style.setColor(Color.BLACK);
		for(int i = 0; i < lineStyles.length; i++) {
			style.setLineStyle(lineStyles[i]);
			double y = i * 1.0;
			plot.addSeries(lineStyles[i].toString(), xs, collection(y,y), style);
		}
		ui.show(plot);
	}

	private void plotMarkerStyles() {
		ScatterPlot plot = plotService.createScatterPlot();
		SeriesStyle style = plot.createSeriesStyle();
		Collection<Double> xs = collection(0.0,1.0);
		MarkerStyle[] markerStyles = MarkerStyle.values();
		for(int i = 0; i < markerStyles.length; i++) {
			style.setMarkerStyle(markerStyles[i]);
			double y = i * 1.0;
			plot.addSeries(markerStyles[i].toString(), xs, collection(y,y), style);
		}
		ui.show(plot);
	}

	private void plotLogarithmic() {
		ScatterPlot plot = plotService.createScatterPlot();
		SeriesStyle style = plot.createSeriesStyle();
		Collection<Double> xs = new ArrayList<>();
		Collection<Double> ys = new ArrayList<>();
		for(double x = 1; x < 10; x += 0.1) {
			xs.add(x);
			ys.add(Math.sin(x) + 10.0);
		}
		plot.addSeries("sin(x)", xs, ys, style);
		plot.getXAxis().setAutoRange();
		plot.getYAxis().setAutoRangeIncludeZero();
		ui.show(plot);
	}


	/**
	 * This function shows how to create a table with information
	 * about the largest towns in the world.
	 *
	 * @return a table with strings and numbers
	 */
	private GenericTable createTable()
	{
		// we create two columns
		GenericColumn nameColumn = new GenericColumn("Town");
		DoubleColumn populationColumn = new DoubleColumn("Population");

		// we fill the columns with information about the largest towns in the world.
		nameColumn.add("Karachi");
		populationColumn.add(23500000.0);

		nameColumn.add("Bejing");
		populationColumn.add(21516000.0);

		nameColumn.add("Sao Paolo");
		populationColumn.add(21292893.0);

		// but actually, the largest town is Shanghai,
		// so let's add it at the beginning of the table.
		nameColumn.add(0, "Shanghai");
		populationColumn.add(0, 24256800.0);

		// After filling the columns, you can create a table
		GenericTable table = new DefaultGenericTable();

		// and add the columns to that table
		table.add(nameColumn);
		table.add(populationColumn);

		return table;
	}

	private static GenericTable createSampleTable2() {
		Random random = new Random();
		GenericTable table = new DefaultGenericTable();
		GenericColumn nameColumn = new GenericColumn("Town");
		DoubleColumn populationColumn = new DoubleColumn("Population");
		DoubleColumn sizeColumn = new DoubleColumn("Size");
		for(String town : new String[]{"sadf","sdf","C","D"}) {
			for(int i = 0; i < 20; i++) {
				nameColumn.add(town);
				populationColumn.add(random.nextGaussian() * 2 + 3);
				sizeColumn.add(random.nextGaussian() + 1);
			}
		}
		table.add(nameColumn);
		table.add(populationColumn);
		table.add(sizeColumn);
		return table;
	}

	private static GenericTable createSampleTable3() {
		Random random = new Random();
		GenericTable table = new DefaultGenericTable();
		DoubleColumn populationColumn = new DoubleColumn("Population");
		DoubleColumn sizeColumn = new DoubleColumn("Size");
		for(int i = 0; i < 20; i++) {
			populationColumn.add(random.nextGaussian() * 2 + 3);
			sizeColumn.add(random.nextGaussian() + 1);
		}
		table.add(populationColumn);
		table.add(sizeColumn);
		return table;
	}

	private static Collection<Double> collection(Double ... values) {
		return Arrays.asList(values);
	}

}
