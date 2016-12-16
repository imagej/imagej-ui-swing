package net.imagej.ui.swing.viewer.plot.jfreechart;

import net.imagej.plot.*;
import net.imagej.plot.XYSeries;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.LogAxis;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.*;

import java.awt.*;
import java.util.Collection;
import java.util.Iterator;

/**
 * @author Matthias Arzt
 */
// FIXME make JfcXYPlotGenerator an interface and implement the JFreeChart in JFreeChartXYPlot
public class JfcXYPlotGenerator implements JfcPlotGenerator {

	private XYPlot xyPlot;
	private JFreeChart jFreeChart;
	private XYSeriesCollection seriesCollection;

	public JfcXYPlotGenerator(XYPlot xyPlot) { this.xyPlot = xyPlot; }

	@Override
	public JFreeChart getJFreeChart() {
		seriesCollection = new XYSeriesCollection();
		jFreeChart = ChartFactory.createXYLineChart("", "", "", seriesCollection);
		jFreeChart.getXYPlot().setDomainAxis(getJFreeChartAxis(xyPlot.getXAxis()));
		jFreeChart.getXYPlot().setRangeAxis(getJFreeChartAxis(xyPlot.getYAxis()));
		jFreeChart.setTitle(xyPlot.getTitle());
		addAllSeries();
		return jFreeChart;
	}

	private void addAllSeries() {
		int id = 0;
		for(XYSeries series : xyPlot.getSeriesCollection())
			addSeries(id, series);
	}

	private void addSeries(int id, XYSeries series) {
		SortedLabel uniqueLabel = new SortedLabel(id, series.getLabel());
		addSeriesData(uniqueLabel, series.getXValues(), series.getYValues());
		setSeriesStyle(uniqueLabel, series.getStyle());
	}

	private void addSeriesData(SortedLabel uniqueLabel, Collection<Double> xs, Collection<Double> ys) {
		org.jfree.data.xy.XYSeries series = new org.jfree.data.xy.XYSeries(uniqueLabel);
		Iterator<Double> xi = xs.iterator();
		Iterator<Double> yi = ys.iterator();
		while (xi.hasNext() && yi.hasNext())
			series.add(xi.next(), yi.next());
		seriesCollection.addSeries(series);
	}

	private void setSeriesStyle(SortedLabel label, SeriesStyle style) {
		if (style == null)
			return;
		Color color = style.getColor();
		XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer)
				jFreeChart.getXYPlot().getRendererForDataset(seriesCollection);
		int index = seriesCollection.getSeriesIndex(label);
		if (color != null) renderer.setSeriesPaint(index, color);
		JfcLineStyles.modifyRenderer(renderer, index, style.getLineStyle());
		JfcMarkerStyles.modifyRenderer(renderer, index, style.getMarkerStyle());
	}

	private org.jfree.chart.axis.ValueAxis getJFreeChartAxis(NumberAxis v) {
		if(v.isLogarithmic()) {
			LogAxis axis = new LogAxis(v.getLabel());
			if(v.hasManualRange())
				axis.setRange(v.getMin(), v.getMax());
			else
				axis.setAutoRange(true);
			return axis;
		} else {
			org.jfree.chart.axis.NumberAxis axis = new org.jfree.chart.axis.NumberAxis(v.getLabel());
			if(v.hasManualRange())
				axis.setRange(v.getMin(), v.getMax());
			else {
				axis.setAutoRange(true);
				axis.setAutoRangeIncludesZero(v.doesAutoRangeIncludesZero());
			}
			return axis;
		}
	}

	private class SortedLabel implements Comparable<SortedLabel> {
		SortedLabel(final int id, final String label) { this.label = label; this.id = id; }
		@Override public String toString() { return label; }
		@Override public int compareTo(SortedLabel o) { return Integer.compare(id, o.id); }
		private String label;
		private int id;
	}
}
