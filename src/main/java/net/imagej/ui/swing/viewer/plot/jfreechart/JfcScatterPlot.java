package net.imagej.ui.swing.viewer.plot.jfreechart;

import net.imagej.plot.*;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.LogAxis;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.awt.*;
import java.util.Collection;
import java.util.Iterator;

/**
 * @author Matthias Arzt
 */
// FIXME make JfcScatterPlot an interface and implement the JFreeChart in JFreeChartScatterPlot
public class JfcScatterPlot implements ScatterPlot, JfcPlot {

	private String chartTitle;
	private XYSeriesCollection seriesCollection;
	private JFreeChart chart;
	private NumberAxis xAxis;
	private NumberAxis yAxis;

	JfcScatterPlot() {
		seriesCollection = new XYSeriesCollection();
		chartTitle = null;
		xAxis = new DefaultValueAxis();
		yAxis = new DefaultValueAxis();
		chart = ChartFactory.createXYLineChart(chartTitle, "", "", seriesCollection);
	}

	public SeriesStyle createSeriesStyle() {
		return new JfcSeriesStyle();
	}

	public void addSeries(String label, Collection<Double> xs, Collection<Double> ys, SeriesStyle style) {
		SortedLabel uniqueLabel = new SortedLabel(label);
		addSeriesData(uniqueLabel, xs, ys);
		setSeriesStyle(uniqueLabel, style);
	}

	private void addSeriesData(SortedLabel uniqueLabel, Collection<Double> xs, Collection<Double> ys) {
		XYSeries series = new XYSeries(uniqueLabel);
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
			chart.getXYPlot().getRendererForDataset(seriesCollection);
		int index = seriesCollection.getSeriesIndex(label);
		if (color != null) renderer.setSeriesPaint(index, color);
		JfcLineStyles.modifyRenderer(renderer, index, style.getLineStyle());
		JfcMarkerStyles.modifyRenderer(renderer, index, style.getMarkerStyle());
	}

	public NumberAxis getXAxis() { return xAxis; };

	public NumberAxis getYAxis() { return yAxis; };

	@Override
	public void setTitle(String title) {
		this.chartTitle = title;
	}

	@Override
	public String getTitle() {
		return chartTitle;
	}

	@Override
	public JFreeChart getJFreeChart() {
		chart.getXYPlot().setDomainAxis(getJFreeChartAxis(xAxis));
		chart.getXYPlot().setRangeAxis(getJFreeChartAxis(yAxis));
		chart.setTitle(getTitle());
		return chart;
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

	private int numberOfLabels = 0;

	private class SortedLabel implements Comparable<SortedLabel> {
		SortedLabel(String label) { this.label = label; id = numberOfLabels++; }
		@Override public String toString() { return label; }
		@Override public int compareTo(SortedLabel o) { return Integer.compare(id, o.id); }
		private String label;
		private int id;
	}
}
