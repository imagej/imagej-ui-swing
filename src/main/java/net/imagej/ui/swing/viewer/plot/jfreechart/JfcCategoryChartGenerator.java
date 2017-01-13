package net.imagej.ui.swing.viewer.plot.jfreechart;

import net.imagej.plot.*;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.renderer.category.*;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.statistics.DefaultBoxAndWhiskerCategoryDataset;
import org.scijava.util.ColorRGB;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * @author Matthias Arzt
 */
class JfcCategoryChartGenerator<C extends Comparable<C>> extends AbstractJfcChartGenerator {

	final private CategoryChart<C> chart;

	private SortedLabelFactory labelFactory;

	private CategoryPlot jfcPlot;

	private LineAndBarDataset<C> lineData;

	private LineAndBarDataset<C> barData;

	private BoxDataset<C> boxData;

	JfcCategoryChartGenerator(CategoryChart<C> chart) {
		this.chart = chart;
	}

	@Override
	Plot getJfcPlot() {
		labelFactory = new SortedLabelFactory();
		lineData = new LineAndBarDataset<>(new LineAndShapeRenderer());
		barData = new LineAndBarDataset<>(createFlatBarRenderer());
		boxData = new BoxDataset<>();
		jfcPlot = new CategoryPlot();
		jfcPlot.setDomainAxis(new CategoryAxis(chart.categoryAxis().getLabel()));
		jfcPlot.getDomainAxis().setCategoryLabelPositions(CategoryLabelPositions.UP_45);
		jfcPlot.setRangeAxis(getJFreeChartAxis(chart.numberAxis()));
		addAllSeries();
		lineData.addDatasetToPlot(0);
		boxData.addDatasetToPlot(1);
		barData.addDatasetToPlot(2);
		return jfcPlot;
	}

	@Override
	String getTitle() {
		return chart.getTitle();
	}

	static private BarRenderer createFlatBarRenderer() {
		BarRenderer jfcBarRenderer = new BarRenderer();
		jfcBarRenderer.setBarPainter(new StandardBarPainter());
		jfcBarRenderer.setShadowVisible(false);
		return jfcBarRenderer;
	}

	private void addAllSeries() {
		for(CategoryChartItem series : chart.getItems()) {
			if(series instanceof BarSeries)
				barData.addSeries((BarSeries) series);
			if(series instanceof LineSeries)
				lineData.addSeries((LineSeries) series);
			if(series instanceof BoxSeries)
				boxData.addBoxSeries((BoxSeries) series);
		}
	}

	class BoxDataset<C extends Comparable<C>> {

		private DefaultBoxAndWhiskerCategoryDataset jfcDataset;

		private BoxAndWhiskerRenderer jfcRenderer;

		BoxDataset() {
			jfcDataset = new DefaultBoxAndWhiskerCategoryDataset();
			jfcRenderer = new BoxAndWhiskerRenderer();
			jfcRenderer.setFillBox(false);
		}

		private void addBoxSeries(BoxSeries<C> series) {
			SortedLabel uniqueLabel = labelFactory.newLabel(series.getLabel());
			for(Map.Entry<C, Collection<Double>> entry : series.getValues().entrySet())
				jfcDataset.add(new ArrayList<>(entry.getValue()), uniqueLabel, entry.getKey());
			int seriesIndex = jfcDataset.getRowIndex(uniqueLabel);
			Paint color = color(series.getColor());
			if(color != null)
				jfcRenderer.setSeriesPaint(seriesIndex, color);
		}

		void addDatasetToPlot(int datasetIndex) {
			jfcPlot.setDataset(datasetIndex, jfcDataset);
			jfcPlot.setRenderer(datasetIndex, jfcRenderer);
		}

	}

	class LineAndBarDataset<C extends Comparable<C>> {

		private DefaultCategoryDataset jfcDataset;

		private AbstractCategoryItemRenderer jfcRenderer;

		LineAndBarDataset(AbstractCategoryItemRenderer renderer) {
			jfcDataset = new DefaultCategoryDataset();
			jfcRenderer = renderer;
		}

		private void addSeries(BarSeries series) {
			SortedLabel uniqueLabel = labelFactory.newLabel(series.getLabel());
			addSeriesData(uniqueLabel, series.getValues());
			setSeriesStyle(uniqueLabel, series.getColor());
		}

		private void addSeries(LineSeries series) {
			SortedLabel uniqueLabel = labelFactory.newLabel(series.getLabel());
			addSeriesData(uniqueLabel, series.getValues());
			setSeriesStyle(uniqueLabel, series.getStyle());
		}

		private void addSeriesData(SortedLabel uniqueLabel, Map<C, Double> values) {
		    for(Map.Entry<C, Double> entry : values.entrySet())
				jfcDataset.addValue(entry.getValue(), uniqueLabel, entry.getKey());
		}

		private void setSeriesStyle(SortedLabel uniqueLabel, SeriesStyle style) {
			if(style == null)
				return;
			int index = jfcDataset.getRowIndex(uniqueLabel);
			RendererModifier.wrap(jfcRenderer).setSeriesStyle(index, style);
		}

		private void setSeriesStyle(SortedLabel uniqueLabel, ColorRGB style) {
			if(style == null)
				return;
			int index = jfcDataset.getRowIndex(uniqueLabel);
			RendererModifier.wrap(jfcRenderer).setSeriesColor(index, style);
		}

		void addDatasetToPlot(int datasetIndex) {
			jfcPlot.setDataset(datasetIndex, jfcDataset);
			jfcPlot.setRenderer(datasetIndex, jfcRenderer);
		}

	}

}
