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

import org.scijava.util.Colors;

import java.util.Arrays;
import java.util.List;

/**
 * @author Matthias Arzt
 */
class LineStyleDemo extends ChartDemo {


	public void run() {
		LineStyle[] lineStyles = LineStyle.values();

		XYPlot plot = plotService.newXYPlot();
		plot.setTitle("Line Styles");
		plot.xAxis().setManualRange(-1.0, 2.0);
		plot.yAxis().setManualRange(-1.0, (double) lineStyles.length);

		for(int i = 0; i < lineStyles.length; i++)
			addSeries(plot, i, lineStyles[i]);

		ui.show(plot);
	}

	private void addSeries(XYPlot plot, double y, LineStyle lineStyle) {
		XYSeries series = plot.addXYSeries();
		series.setLabel(lineStyle.toString());
		series.setValues(Arrays.asList(0.0,1.0), Arrays.asList(y,y));
		series.setStyle(plot.newSeriesStyle(Colors.BLACK, lineStyle, MarkerStyle.CIRCLE));
	}

	public static void main(final String... args) {
		new LineStyleDemo().run();
	}

}
