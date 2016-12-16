package net.imagej.ui.swing.viewer.plot.jfreechart;

import net.imagej.plot.*;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.LogAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.awt.*;
import java.util.Collection;
import java.util.Iterator;

/**
 * Created by arzt on 13/12/2016.
 */
// FIXME make JfcScatterPlot an interface and implement the JFreeChart in JFreeChartScatterPlot
public class JfcScatterPlot implements ScatterPlot, JfcPlot {

	private String chart_title;
	private XYSeriesCollection seriesCollection;
	private JFreeChart chart;
	private ValueAxis xAxis;
	private ValueAxis yAxis;

	JfcScatterPlot() {
		seriesCollection = new XYSeriesCollection();
		chart_title = null;
		xAxis = new DefaultValueAxis();
		yAxis = new DefaultValueAxis();
		chart = ChartFactory.createXYLineChart(chart_title, "", "", seriesCollection);
	}

	public SeriesStyle createSeriesStyle() {
		return new JfcSeriesStyle();
	}

	public void addSeries(String label, Collection<Double> xs, Collection<Double> ys, SeriesStyle style) {
		SortedLabel unique_label = new SortedLabel(label);
		addSeriesData(unique_label, xs, ys);
		setSeriesStyle(unique_label, style);
	}

	private void addSeriesData(SortedLabel unique_label, Collection<Double> xs, Collection<Double> ys) {
		XYSeries series = new XYSeries(unique_label);
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

	public ValueAxis getXAxis() { return xAxis; };

	public ValueAxis getYAxis() { return yAxis; };

	@Override
	public void setTitle(String title) {
		this.chart_title = title;
	}

	@Override
	public String getTitle() {
		return chart_title;
	}

	@Override
	public JFreeChart getJFreeChart() {
		chart.getXYPlot().setDomainAxis(getJFreeChartAxis(xAxis));
		chart.getXYPlot().setRangeAxis(getJFreeChartAxis(yAxis));
		return chart;
	}

	private org.jfree.chart.axis.ValueAxis getJFreeChartAxis(ValueAxis v) {
		if(v.isLogarithmic()) {
			LogAxis axis = new LogAxis(v.getLabel());
			if(v.hasManualRange())
				axis.setRange(v.getMin(), v.getMax());
			return axis;
		} else {
			NumberAxis axis = new NumberAxis(v.getLabel());
			if(v.hasManualRange())
				axis.setRange(v.getMin(), v.getMax());
			else {
				axis.setAutoRange(true);
				axis.setAutoRangeIncludesZero(v.doesAutoRangeIncludesZero());
			}
			return axis;
		}
	}

	private int number_of_labels = 0;

	private class SortedLabel implements Comparable<SortedLabel> {
		SortedLabel(String label) { this.label = label; id = number_of_labels++; }
		@Override public String toString() { return label; }
		@Override public int compareTo(SortedLabel o) { return Integer.compare(id, o.id); }
		private String label;
		private int id;
	}
}
