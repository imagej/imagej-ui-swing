package net.imagej.ui.swing.viewer.plot.jfreechart;

import net.imagej.plot.*;
import net.imagej.plot.XYSeries;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.LogAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.*;

import java.awt.*;
import java.util.Collection;
import java.util.Iterator;

/**
 * @author Matthias Arzt
 */
// FIXME make JfcXYPlotGenerator an interface and implement the JFreeChart in JFreeChartXYPlot
public class JfcXYPlotGenerator extends AbstractJfcChartGenerator {

	private SortedLabelFactory sortedLabelFactory;
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
		sortedLabelFactory = new SortedLabelFactory();
		addAllSeries();
		return jFreeChart;
	}

	private void addAllSeries() {
		for(XYItem series : xyPlot.getSeriesCollection())
			if(series instanceof XYSeries)
				addSeries((XYSeries) series);
	}

	private void addSeries(XYSeries series) {
		SortedLabel uniqueLabel = sortedLabelFactory.newLabel(series.getLabel());
		addSeriesData(uniqueLabel, series.getXValues(), series.getYValues());
		setSeriesStyle(uniqueLabel, series.getStyle(), series.getLegendVisible());
	}

	private void addSeriesData(SortedLabel uniqueLabel, Collection<Double> xs, Collection<Double> ys) {
		org.jfree.data.xy.XYSeries series = new org.jfree.data.xy.XYSeries(uniqueLabel);
		Iterator<Double> xi = xs.iterator();
		Iterator<Double> yi = ys.iterator();
		while (xi.hasNext() && yi.hasNext())
			series.add(xi.next(), yi.next());
		seriesCollection.addSeries(series);
	}

	private void setSeriesStyle(SortedLabel label, SeriesStyle style, boolean legendVisible) {
		if (style == null)
			return;
		XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer)
				jFreeChart.getXYPlot().getRendererForDataset(seriesCollection);
		int index = seriesCollection.getSeriesIndex(label);
		Color color = style.getColor();
		if (color != null)
			renderer.setSeriesPaint(index, color);
		JfcLineStyles.modifyRenderer(renderer, index, style.getLineStyle());
		JfcMarkerStyles.modifyRenderer(renderer, index, style.getMarkerStyle());
		renderer.setSeriesVisibleInLegend(index,legendVisible);
	}

	private org.jfree.chart.axis.ValueAxis getJFreeChartAxis(NumberAxis v) {
		if(v.isLogarithmic())
			return getJFreeChartLogarithmicAxis(v);
		else
			return getJFreeCharLinearAxis(v);
	}

	private ValueAxis getJFreeChartLogarithmicAxis(NumberAxis v) {
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

	private ValueAxis getJFreeCharLinearAxis(NumberAxis v) {
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

}
