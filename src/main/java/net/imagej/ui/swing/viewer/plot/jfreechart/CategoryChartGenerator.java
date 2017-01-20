package net.imagej.ui.swing.viewer.plot.jfreechart;

import net.imagej.plot.*;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.renderer.category.*;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.statistics.DefaultBoxAndWhiskerCategoryDataset;
import org.scijava.ui.awt.AWTColors;
import org.scijava.util.ColorRGB;

import java.util.*;
import java.util.List;

/**
 * @author Matthias Arzt
 */
class CategoryChartGenerator<C> extends AbstractChartGenerator {

	private final CategoryChart<C> chart;

	private SortedLabelFactory<String> labelFactory;

	private CategoryPlot jfcPlot;

	private LineAndBarDataset lineData;

	private LineAndBarDataset barData;

	private BoxDataset boxData;

	private List<SortedLabel<C>> categoryList;

	private SortedLabelFactory<C> categoryFactory;

	CategoryChartGenerator(CategoryChart<C> chart) {
		this.chart = chart;
	}

	@Override
	Plot getJfcPlot() {
		labelFactory = new SortedLabelFactory<>();
		categoryFactory = new SortedLabelFactory<>();
		categoryList = new ArrayList<>();
		for(C category : chart.categoryAxis().getCategories())
			categoryList.add(categoryFactory.newLabel(category));
		lineData = new LineAndBarDataset(new LineAndShapeRenderer());
		barData = new LineAndBarDataset(createFlatBarRenderer());
		boxData = new BoxDataset();
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

	private class BoxDataset {

		private DefaultBoxAndWhiskerCategoryDataset jfcDataset;

		private BoxAndWhiskerRenderer jfcRenderer;

		BoxDataset() {
			jfcDataset = new DefaultBoxAndWhiskerCategoryDataset();
			jfcRenderer = new BoxAndWhiskerRenderer();
			jfcRenderer.setFillBox(false);
		}

		private void setCategories() {
			SortedLabel uniqueLabel = labelFactory.newLabel("dummy");
			for(SortedLabel<C> category : categoryList)
				jfcDataset.add(Collections.emptyList(), uniqueLabel, category);
			setSeriesVisibility(uniqueLabel, false, false);
		}

		private void addBoxSeries(BoxSeries<C> series) {
			SortedLabel uniqueLabel = labelFactory.newLabel(series.getLabel());
			setSeriesData(uniqueLabel, series.getValues());
			setSeriesVisibility(uniqueLabel, true, series.getLegendVisible());
			setSeriesColor(uniqueLabel, series.getColor());
		}

		private void setSeriesData(SortedLabel uniqueLabel, Map<? extends C, ? extends Collection<Double>> data) {
			for(SortedLabel<C> category : categoryList) {
				Collection<Double> value = data.get(category.getLabel());
				if(value != null)
					jfcDataset.add(new ArrayList<>(value), uniqueLabel, category);
			}
		}

		private void setSeriesColor(SortedLabel uniqueLabel, ColorRGB color) {
			if(color == null)
				return;
			int index = jfcDataset.getRowIndex(uniqueLabel);
			if(index < 0)
				return;
			jfcRenderer.setSeriesPaint(index, AWTColors.getColor(color));
		}

		private void setSeriesVisibility(SortedLabel uniqueLabel, boolean seriesVsisible, boolean legendVisible) {
			int index = jfcDataset.getRowIndex(uniqueLabel);
			if(index < 0)
				return;
			jfcRenderer.setSeriesVisible(index, seriesVsisible, false);
			jfcRenderer.setSeriesVisibleInLegend(index, legendVisible, false);
		}


		void addDatasetToPlot(int datasetIndex) {
			setCategories();
			jfcPlot.setDataset(datasetIndex, jfcDataset);
			jfcPlot.setRenderer(datasetIndex, jfcRenderer);
		}

	}


	private class LineAndBarDataset {

		private DefaultCategoryDataset jfcDataset;

		private AbstractCategoryItemRenderer jfcRenderer;

		LineAndBarDataset(AbstractCategoryItemRenderer renderer) {
			jfcDataset = new DefaultCategoryDataset();
			jfcRenderer = renderer;
		}

		private void setCategories() {
			SortedLabel uniqueLabel = labelFactory.newLabel("dummy");
			for(SortedLabel<C> category : categoryList)
				jfcDataset.addValue(0.0, uniqueLabel, category);
			setSeriesVisibility(uniqueLabel, false, false);
		}

		private void addSeries(BarSeries<C> series) {
			SortedLabel uniqueLabel = labelFactory.newLabel(series.getLabel());
			addSeriesData(uniqueLabel, series.getValues());
			setSeriesColor(uniqueLabel, series.getColor());
			setSeriesVisibility(uniqueLabel, true, series.getLegendVisible());
		}

		private void addSeries(LineSeries<C> series) {
			SortedLabel uniqueLabel = labelFactory.newLabel(series.getLabel());
			addSeriesData(uniqueLabel, series.getValues());
			setSeriesStyle(uniqueLabel, series.getStyle());
			setSeriesVisibility(uniqueLabel, true, series.getLegendVisible());
		}

		private void setSeriesVisibility(SortedLabel uniqueLabel, boolean seriesVsisible, boolean legendVisible) {
			int index = jfcDataset.getRowIndex(uniqueLabel);
			if(index < 0)
				return;
			jfcRenderer.setSeriesVisible(index, seriesVsisible, false);
			jfcRenderer.setSeriesVisibleInLegend(index, legendVisible, false);
		}

		private void addSeriesData(SortedLabel uniqueLabel, Map<? extends C, Double> values) {
		    for(SortedLabel<C> category : categoryList) {
		    	Double value = values.get(category.getLabel());
		    	if(value != null)
					jfcDataset.addValue(value, uniqueLabel, category);
			}
		}

		private void setSeriesStyle(SortedLabel uniqueLabel, SeriesStyle style) {
			if(style == null)
				return;
			int index = jfcDataset.getRowIndex(uniqueLabel);
			if(index < 0)
				return;
			RendererModifier.wrap(jfcRenderer).setSeriesStyle(index, style);
		}

		private void setSeriesColor(SortedLabel uniqueLabel, ColorRGB style) {
			if(style == null)
				return;
			int index = jfcDataset.getRowIndex(uniqueLabel);
			if(index < 0)
				return;
			RendererModifier.wrap(jfcRenderer).setSeriesColor(index, style);
		}

		void addDatasetToPlot(int datasetIndex) {
			setCategories();
			jfcPlot.setDataset(datasetIndex, jfcDataset);
			jfcPlot.setRenderer(datasetIndex, jfcRenderer);
		}

	}

}
