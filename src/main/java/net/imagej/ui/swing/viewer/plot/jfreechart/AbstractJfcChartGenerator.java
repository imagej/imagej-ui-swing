package net.imagej.ui.swing.viewer.plot.jfreechart;

import net.imagej.plot.NumberAxis;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.LogAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.block.BlockFrame;
import org.jfree.chart.plot.Plot;
import org.scijava.ui.awt.AWTColors;
import org.scijava.util.ColorRGB;

import java.awt.*;

/**
 * @author Matthias Arzt
 */
abstract class AbstractJfcChartGenerator {

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

	static class SortedLabelFactory {
		private int n;
		SortedLabelFactory() { n = 0; }
		SortedLabel newLabel(String label) { return new SortedLabel(n++, label); }
	}

	static class SortedLabel implements Comparable<SortedLabel> {
		SortedLabel(final int id, final String label) { this.label = label; this.id = id; }
		@Override public String toString() { return label; }
		@Override public int compareTo(SortedLabel o) { return Integer.compare(id, o.id); }
		private String label;
		private int id;
	}

	static Color color(ColorRGB color) {
		return AWTColors.getColor(color);
	}

}
