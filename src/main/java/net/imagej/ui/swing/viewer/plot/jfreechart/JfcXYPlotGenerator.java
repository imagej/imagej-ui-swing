package net.imagej.ui.swing.viewer.plot.jfreechart;

import net.imagej.plot.*;
import net.imagej.plot.XYPlot;
import net.imagej.plot.XYSeries;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.renderer.xy.DefaultXYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.*;

import java.awt.*;
import java.util.Collection;
import java.util.Iterator;

/**
 * @author Matthias Arzt
 */
// FIXME make JfcXYPlotGenerator an interface and implement the JFreeChart in JFreeChartXYPlot
class JfcXYPlotGenerator extends AbstractJfcChartGenerator {

	private SortedLabelFactory sortedLabelFactory;
	private XYPlot xyPlot;
	private org.jfree.chart.plot.XYPlot jfcPlot;
	private XYSeriesCollection jfcDataSet;
	private XYLineAndShapeRenderer jfcRenderer;

	JfcXYPlotGenerator(XYPlot xyPlot) { this.xyPlot = xyPlot; }

	@Override
	Plot getJfcPlot() {
		jfcDataSet = new XYSeriesCollection();
		jfcRenderer = new XYLineAndShapeRenderer();
		jfcPlot = new org.jfree.chart.plot.XYPlot();
		jfcPlot.setDataset(jfcDataSet);
		jfcPlot.setDomainAxis(getJFreeChartAxis(xyPlot.getXAxis()));
		jfcPlot.setRangeAxis(getJFreeChartAxis(xyPlot.getYAxis()));
		jfcPlot.setRenderer(jfcRenderer);
		sortedLabelFactory = new SortedLabelFactory();
		addAllSeries();
		return jfcPlot;
	}

	@Override
	String getTitle() {
		return xyPlot.getTitle();
	}

	private void addAllSeries() {
		for(XYPlotItem series : xyPlot.getItems())
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
		jfcDataSet.addSeries(series);
	}

	private void setSeriesStyle(SortedLabel label, SeriesStyle style, boolean legendVisible) {
		if (style == null)
			return;
		int index = jfcDataSet.getSeriesIndex(label);
		Color color = color(style.getColor());
		if (color != null)
			jfcRenderer.setSeriesPaint(index, color);
		JfcLineStyles.modifyRenderer(jfcRenderer, index, style.getLineStyle());
		JfcMarkerStyles.modifyRenderer(jfcRenderer, index, style.getMarkerStyle());
		jfcRenderer.setSeriesVisibleInLegend(index,legendVisible);
	}

}
