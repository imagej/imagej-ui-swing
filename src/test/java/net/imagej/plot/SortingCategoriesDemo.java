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

import java.util.*;

/**
 * @author Matthias Arzt
 */

class SortingCategoriesDemo extends ChartDemo{

	public void run() {
		showSortedCategoryChart(new AxisManipulator() {
			@Override
			void manipulate(CategoryAxis<String> axis) {
				axis.setManualCategories(Arrays.asList("a","c","b"));
				axis.setLabel("acb");
			}
		});
		showSortedCategoryChart(new AxisManipulator() {
			@Override
			void manipulate(CategoryAxis<String> axis) {
				axis.setManualCategories(Arrays.asList("a","c","b","g"));
				axis.setLabel("acbg");
			}
		});
		showSortedCategoryChart(new AxisManipulator() {
			@Override
			void manipulate(CategoryAxis<String> axis) {
				axis.setManualCategories(Arrays.asList("d","c","a","b"));
				axis.setOrder(String::compareTo);
				axis.setLabel("abcd");
			}
		});
		showSortedCategoryChart(new AxisManipulator() {
			@Override
			void manipulate(CategoryAxis<String> axis) {
				axis.setManualCategories(Arrays.asList());
				axis.setOrder(String::compareTo);
				axis.setLabel("empty");
			}
		});
	}

	private static abstract class AxisManipulator {
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

	public static void main(final String... args) {
		new SortingCategoriesDemo().run();
	}

}
