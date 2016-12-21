package net.imagej.ui.swing.viewer.plot.jfreechart;

import net.imagej.plot.*;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.AbstractRenderer;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.data.category.DefaultCategoryDataset;

import java.util.Collection;
import java.util.Iterator;

/**
 * @author Matthias Arzt
 */
public class JfcCategoryChartGenerator extends AbstractJfcChartGenerator {

	private CategoryChart chart;

	private Collection<String> categories;

	private SortedLabelFactory labelFactory;

	private CategoryPlot jfcPlot;

	private Dataset lineDataset;

	private Dataset barDataset;

	public JfcCategoryChartGenerator(CategoryChart chart) {
		this.chart = chart;
	}

	@Override
	public JFreeChart getJFreeChart() {
		labelFactory = new SortedLabelFactory();
		categories = chart.getCategoryAxis().getCategories();
		lineDataset = new Dataset(new LineAndShapeRenderer());
		barDataset = new Dataset(new BarRenderer());
		addAllSeries();
		jfcPlot = new CategoryPlot();
		jfcPlot.setDomainAxis(new CategoryAxis(chart.getCategoryAxis().getLabel()));
		jfcPlot.getDomainAxis().setCategoryLabelPositions(CategoryLabelPositions.UP_45);
		jfcPlot.setRangeAxis(getJFreeChartAxis(chart.getNumberAxis()));
		lineDataset.addDataset(0);
		barDataset.addDataset(1);
		return new JFreeChart(jfcPlot);
	}

	private void addAllSeries() {
		for(CategorySeries series : chart.getItems()) {
			if(series instanceof BarSeries)
				barDataset.addSeries(series);
			if(series instanceof LineSeries)
				lineDataset.addSeries(series);
		}
	}

	class Dataset {

		private DefaultCategoryDataset jfcDataset;

		private AbstractRenderer jfcRenderer;

		Dataset(AbstractRenderer renderer) {
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
				jfcRenderer.setSeriesPaint(seriesIndex, style.getColor());
			JfcLineStyles.modifyRenderer(jfcRenderer, seriesIndex, style.getLineStyle());
			JfcMarkerStyles.modifyRenderer(jfcRenderer, seriesIndex, style.getMarkerStyle());
		}

		void addDataset(int datasetIndex) {
			jfcPlot.setDataset(datasetIndex, jfcDataset);
			jfcPlot.setRenderer(datasetIndex, (CategoryItemRenderer) jfcRenderer);
		}

	}

}
