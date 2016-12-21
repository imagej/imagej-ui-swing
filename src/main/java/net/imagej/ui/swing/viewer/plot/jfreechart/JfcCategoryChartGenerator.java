package net.imagej.ui.swing.viewer.plot.jfreechart;

import net.imagej.plot.*;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.renderer.AbstractRenderer;
import org.jfree.chart.renderer.category.*;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.statistics.DefaultBoxAndWhiskerCategoryDataset;
import org.scijava.ui.awt.AWTColors;
import org.scijava.util.ColorRGB;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * @author Matthias Arzt
 */
class JfcCategoryChartGenerator extends AbstractJfcChartGenerator {

	final private CategoryChart chart;

	private Collection<String> categories;

	private SortedLabelFactory labelFactory;

	private CategoryPlot jfcPlot;

	private LineAndBarDataset lineData;

	private LineAndBarDataset barData;

	private BoxDataset boxData;

	JfcCategoryChartGenerator(CategoryChart chart) {
		this.chart = chart;
	}

	@Override
	Plot getJfcPlot() {
		labelFactory = new SortedLabelFactory();
		categories = chart.getCategoryAxis().getCategories();
		lineData = new LineAndBarDataset(new LineAndShapeRenderer());
		barData = new LineAndBarDataset(createFlatBarRenderer());
		boxData = new BoxDataset();
		jfcPlot = new CategoryPlot();
		jfcPlot.setDomainAxis(new CategoryAxis(chart.getCategoryAxis().getLabel()));
		jfcPlot.getDomainAxis().setCategoryLabelPositions(CategoryLabelPositions.UP_45);
		jfcPlot.setRangeAxis(getJFreeChartAxis(chart.getNumberAxis()));
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

	class BoxDataset {

		private DefaultBoxAndWhiskerCategoryDataset jfcDataset;

		private BoxAndWhiskerRenderer jfcRenderer;

		BoxDataset() {
			jfcDataset = new DefaultBoxAndWhiskerCategoryDataset();
			jfcRenderer = new BoxAndWhiskerRenderer();
			jfcRenderer.setFillBox(false);
		}

		private void addBoxSeries(BoxSeries series) {
			SortedLabel uniqueLabel = labelFactory.newLabel(series.getLabel());
			Iterator<Collection<Double>> vi = series.getValues().iterator();
			Iterator<String> ci = categories.iterator();
			while(vi.hasNext() && ci.hasNext())
				jfcDataset.add(new ArrayList<>(vi.next()), uniqueLabel, ci.next());
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

	class LineAndBarDataset {

		private DefaultCategoryDataset jfcDataset;

		private CategoryItemRenderer jfcRenderer;

		LineAndBarDataset(CategoryItemRenderer renderer) {
			jfcDataset = new DefaultCategoryDataset();
			jfcRenderer = renderer;
		}

		private void addSeries(CategorySeries series) {
			SortedLabel uniqueLabel = labelFactory.newLabel(series.getLabel());
			addSeriesData(uniqueLabel, series.getValues());
			setSeriesStyle(uniqueLabel, series.getStyle());
		}

		private void addSeriesData(SortedLabel uniqueLabel, Collection<Double> values) {
			Iterator<Double> vi = values.iterator();
			Iterator<String> ci = categories.iterator();
			while(vi.hasNext() && ci.hasNext())
				jfcDataset.addValue(vi.next(), uniqueLabel, ci.next());
		}

		private void setSeriesStyle(SortedLabel uniqueLabel, SeriesStyle style) {
			if(style == null)
				return;
			int seriesIndex = jfcDataset.getRowIndex(uniqueLabel);
			if(style.getColor() != null)
				jfcRenderer.setSeriesPaint(seriesIndex, color(style.getColor()));
			JfcLineStyles.modifyRenderer((AbstractRenderer) jfcRenderer, seriesIndex, style.getLineStyle());
			JfcMarkerStyles.modifyRenderer((AbstractRenderer) jfcRenderer, seriesIndex, style.getMarkerStyle());
		}

		void addDatasetToPlot(int datasetIndex) {
			jfcPlot.setDataset(datasetIndex, jfcDataset);
			jfcPlot.setRenderer(datasetIndex, jfcRenderer);
		}

	}

}
