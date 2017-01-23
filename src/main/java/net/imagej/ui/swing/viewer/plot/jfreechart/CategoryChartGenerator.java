/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2016 Board of Regents of the University of
 * Wisconsin-Madison, Broad Institute of MIT and Harvard, and Max Planck
 * Institute of Molecular Cell Biology and Genetics.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

package net.imagej.ui.swing.viewer.plot.jfreechart;

import net.imagej.plot.*;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.*;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.statistics.DefaultBoxAndWhiskerCategoryDataset;
import org.scijava.ui.awt.AWTColors;
import org.scijava.util.ColorRGB;

import java.util.*;
import java.util.List;

import static net.imagej.ui.swing.viewer.plot.jfreechart.Utils.*;

/**
 * @author Matthias Arzt
 */
class CategoryChartGenerator<C> {

	private final CategoryChart<C> chart;

	private final SortedLabelFactory<String> labelFactory = new SortedLabelFactory<>();

	private final CategoryPlot jfcPlot = new CategoryPlot();

	private final LineAndBarDataset lineData;

	private final LineAndBarDataset barData;

	private final BoxDataset boxData;

	private CategoryChartGenerator(CategoryChart<C> chart) {
		this.chart = chart;
		List<SortedLabel<C>> categoryList = setupCategoryList();
		lineData = new LineAndBarDataset(new LineAndShapeRenderer(), categoryList);
		barData = new LineAndBarDataset(createFlatBarRenderer(), categoryList);
		boxData = new BoxDataset(categoryList);
	}

	public static <C> JFreeChart run(CategoryChart<C> chart) {
		return new CategoryChartGenerator<C>(chart).getJFreeChart();
	}

	private List<SortedLabel<C>> setupCategoryList() {
		List<C> categories = chart.categoryAxis().getCategories();
		List<SortedLabel<C>> categoryList = new ArrayList<>(categories.size());
		SortedLabelFactory<C> categoryFactory = new SortedLabelFactory<>();
		for(C category : categories)
			categoryList.add(categoryFactory.newLabel(category));
		return categoryList;
	}

	private JFreeChart getJFreeChart() {
		jfcPlot.setDomainAxis(new CategoryAxis(chart.categoryAxis().getLabel()));
		jfcPlot.getDomainAxis().setCategoryLabelPositions(CategoryLabelPositions.UP_45);
		jfcPlot.setRangeAxis(getJFreeChartAxis(chart.numberAxis()));
		processAllSeries();
		lineData.addDatasetToPlot(0);
		boxData.addDatasetToPlot(1);
		barData.addDatasetToPlot(2);
		return Utils.setupJFreeChart(chart.getTitle(), jfcPlot);
	}

	static private BarRenderer createFlatBarRenderer() {
		BarRenderer jfcBarRenderer = new BarRenderer();
		jfcBarRenderer.setBarPainter(new StandardBarPainter());
		jfcBarRenderer.setShadowVisible(false);
		return jfcBarRenderer;
	}

	private void processAllSeries() {
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

		private final DefaultBoxAndWhiskerCategoryDataset jfcDataset;

		private final BoxAndWhiskerRenderer jfcRenderer;

		private final List<SortedLabel<C>> categoryList;

		BoxDataset(List<SortedLabel<C>> categoryList) {
			jfcDataset = new DefaultBoxAndWhiskerCategoryDataset();
			jfcRenderer = new BoxAndWhiskerRenderer();
			jfcRenderer.setFillBox(false);
			this.categoryList = categoryList;
			setCategories();
		}

		void addBoxSeries(BoxSeries<C> series) {
			SortedLabel uniqueLabel = labelFactory.newLabel(series.getLabel());
			setSeriesData(uniqueLabel, series.getValues());
			setSeriesVisibility(uniqueLabel, true, series.getLegendVisible());
			setSeriesColor(uniqueLabel, series.getColor());
		}


		private void setCategories() {
			SortedLabel uniqueLabel = labelFactory.newLabel("dummy");
			for(SortedLabel<C> category : categoryList)
				jfcDataset.add(Collections.emptyList(), uniqueLabel, category);
			setSeriesVisibility(uniqueLabel, false, false);
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
			jfcPlot.setDataset(datasetIndex, jfcDataset);
			jfcPlot.setRenderer(datasetIndex, jfcRenderer);
		}

	}


	private class LineAndBarDataset {

		private final DefaultCategoryDataset jfcDataset;

		private final AbstractCategoryItemRenderer jfcRenderer;

		private final List<SortedLabel<C>> categoryList;

		LineAndBarDataset(AbstractCategoryItemRenderer renderer, List<SortedLabel<C>> categoryList) {
			jfcDataset = new DefaultCategoryDataset();
			jfcRenderer = renderer;
			this.categoryList = categoryList;
			setCategories();
		}

		private void setCategories() {
			SortedLabel uniqueLabel = labelFactory.newLabel("dummy");
			for(SortedLabel<C> category : categoryList)
				jfcDataset.addValue(0.0, uniqueLabel, category);
			setSeriesVisibility(uniqueLabel, false, false);
		}

		void addSeries(BarSeries<C> series) {
			SortedLabel uniqueLabel = labelFactory.newLabel(series.getLabel());
			addSeriesData(uniqueLabel, series.getValues());
			setSeriesColor(uniqueLabel, series.getColor());
			setSeriesVisibility(uniqueLabel, true, series.getLegendVisible());
		}

		void addSeries(LineSeries<C> series) {
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
			jfcPlot.setDataset(datasetIndex, jfcDataset);
			jfcPlot.setRenderer(datasetIndex, jfcRenderer);
		}

	}

}
