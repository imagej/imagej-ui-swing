package net.imagej.ui.swing.viewer.plot.jfreechart;

import net.imagej.plot.CategoryChart;
import net.imagej.plot.LineSeries;
import net.imagej.plot.SeriesStyle;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
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

	private JFreeChart jFreeChart;

	private CategoryPlot jfcPlot;

	private DefaultCategoryDataset jfcLinesDataset;

	private LineAndShapeRenderer jfcLinesRenderer;

	private SortedLabelFactory sortedLabelFactory;

	public JfcCategoryChartGenerator(CategoryChart chart) {
		this.chart = chart;
	}

	@Override
	public JFreeChart getJFreeChart() {
		sortedLabelFactory = new SortedLabelFactory();
		categories = chart.getCategoryAxis().getCategories();
		jfcPlot = new CategoryPlot();
		jfcPlot.setDomainAxis(new CategoryAxis("Category"));
		jfcPlot.getDomainAxis().setCategoryLabelPositions(CategoryLabelPositions.UP_45);
		jfcPlot.setRangeAxis(new NumberAxis("Value"));
		jfcLinesDataset = new DefaultCategoryDataset();
		jfcLinesRenderer = new LineAndShapeRenderer();
		addAllSeries();
		jfcPlot.setDataset(0, jfcLinesDataset);
		jfcPlot.setRenderer(0, jfcLinesRenderer);
		jFreeChart = new JFreeChart(jfcPlot);
		return jFreeChart;
	}

	private void addAllSeries() {
		for(LineSeries series : chart.getSeriesCollection())
			addSeries(series);
	}

	private void addSeries(LineSeries series) {
		SortedLabel uniqueLabel = sortedLabelFactory.newLabel(series.getLabel());
		addSeriesData(uniqueLabel, series.getValues());
		setSeriesStyle(uniqueLabel, series.getStyle());
	}

	private void addSeriesData(SortedLabel uniqueLabel, Collection<Double> values) {
		Iterator<Double> vi = values.iterator();
		Iterator<String> ci = categories.iterator();
		while(vi.hasNext() && ci.hasNext())
			jfcLinesDataset.addValue(vi.next(), uniqueLabel, ci.next());
	}

	private void setSeriesStyle(SortedLabel uniqueLabel, SeriesStyle style) {
		if(style == null)
			return;
		int index = jfcLinesDataset.getRowIndex(uniqueLabel);
		if(style.getColor() != null)
			jfcLinesRenderer.setSeriesPaint(index, style.getColor());
		JfcLineStyles.modifyRenderer(jfcLinesRenderer, index, style.getLineStyle());
		JfcMarkerStyles.modifyRenderer(jfcLinesRenderer, index, style.getMarkerStyle());
	}

}
