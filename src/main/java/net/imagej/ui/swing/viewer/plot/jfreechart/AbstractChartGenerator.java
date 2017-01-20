package net.imagej.ui.swing.viewer.plot.jfreechart;

import net.imagej.plot.NumberAxis;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.LogAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.plot.Plot;

import java.awt.*;
import java.util.HashMap;
import java.util.Objects;

/**
 * @author Matthias Arzt
 */
abstract class AbstractChartGenerator {

	abstract Plot getJfcPlot();

	abstract String getTitle();

	public JFreeChart getJFreeChart() {
		JFreeChart chart = new JFreeChart(getJfcPlot());
		chart.setTitle(getTitle());
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
