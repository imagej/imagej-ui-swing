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

import net.imagej.plot.NumberAxis;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.LogAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.plot.Plot;

import java.awt.*;
import java.util.Objects;

/**
 * @author Matthias Arzt
 */
class Utils {

	static JFreeChart setupJFreeChart(String title, Plot plot) {
		JFreeChart chart = new JFreeChart(plot);
		chart.setTitle(title);
		chart.setBackgroundPaint(Color.WHITE);
		chart.getLegend().setFrame(BlockBorder.NONE);
		return chart;
	}

	static org.jfree.chart.axis.ValueAxis getJFreeChartAxis(NumberAxis v) {
		if(v.isLogarithmic())
			return getJFreeChartLogarithmicAxis(v);
		else
			return getJFreeCharLinearAxis(v);
	}

	static ValueAxis getJFreeChartLogarithmicAxis(NumberAxis v) {
		LogAxis axis = new LogAxis(v.getLabel());
		switch (v.getRangeStrategy()) {
			case MANUAL:
				axis.setRange(v.getMin(), v.getMax());
				break;
			default:
				axis.setAutoRange(true);
		}
		return axis;
	}

	static ValueAxis getJFreeCharLinearAxis(NumberAxis v) {
		org.jfree.chart.axis.NumberAxis axis = new org.jfree.chart.axis.NumberAxis(v.getLabel());
		switch(v.getRangeStrategy()) {
			case MANUAL:
				axis.setRange(v.getMin(), v.getMax());
				break;
			case AUTO:
				axis.setAutoRange(true);
				axis.setAutoRangeIncludesZero(false);
				break;
			case AUTO_INCLUDE_ZERO:
				axis.setAutoRange(true);
				axis.setAutoRangeIncludesZero(true);
				break;
			default:
				axis.setAutoRange(true);
		}
		return axis;
	}

	static class SortedLabelFactory<T> {
		private int n;
		SortedLabelFactory() { n = 0; }
		SortedLabel<T> newLabel(T label) { return new SortedLabel<>(n++, label); }
	}

	static class SortedLabel<T> implements Comparable<SortedLabel> {
		SortedLabel(final int id, final T label) { this.label = Objects.requireNonNull(label); this.id = id; }
		@Override public String toString() { return label.toString(); }
		@Override public int compareTo(SortedLabel o) { return Integer.compare(id, o.id); }
		public T getLabel() { return label; }
		private final T label;
		private final int id;
	}

}
