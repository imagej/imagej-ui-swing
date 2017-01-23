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

import java.util.*;

/**
 * @author Matthias Arzt
 */
class BoxPlotDemo extends ChartDemo{

	public void run() {
		CategoryChart<String> chart = plotService.newCategoryChart(String.class);

		Map<String, Collection<Double>> randomData1 = new TreeMap<>();
		randomData1.put("A", collectionOfRandomNumbers(10));
		randomData1.put("B", collectionOfRandomNumbers(20));
		randomData1.put("C", collectionOfRandomNumbers(30));

		BoxSeries<String> boxSeries1 = chart.addBoxSeries();
		boxSeries1.setLabel("boxes1");
		boxSeries1.setValues(randomData1);
		boxSeries1.setColor(Colors.CYAN);

		Map<String, Collection<Double>> randomData2 = new TreeMap<>();
		randomData2.put("A", collectionOfRandomNumbers(10));
		randomData2.put("B", collectionOfRandomNumbers(20));
		randomData2.put("C", collectionOfRandomNumbers(30));

		BoxSeries<String> boxSeries2 = chart.addBoxSeries();
		boxSeries2.setLabel("boxes2");
		boxSeries2.setValues(randomData2);
		boxSeries2.setColor(Colors.BLACK);

		ui.show(chart);
	}

	private static Collection<Double> collectionOfRandomNumbers(int size) {
		Random rand = new Random();
		Vector<Double> result = new Vector<>(size);
		for(int i = 0; i < size; i++)
			result.add(rand.nextGaussian()*20);
		return result;
	}

	public static void main(final String... args) {
		new BoxPlotDemo().run();
	}
}
