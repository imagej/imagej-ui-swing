package net.imagej.defaultplot;

import net.imagej.plot.*;
import org.scijava.util.ColorRGB;

import java.util.Collection;
import java.util.LinkedList;

/**
 * @author Matthias Arzt
 */
public class DefaultCategoryChart extends DefaultAbstractPlot implements CategoryChart {

	private String title;

	private NumberAxis valueAxis;

	private CategoryAxis categoryAxis;

	private Collection<CategoryChartItem> items;

	DefaultCategoryChart() {
		valueAxis = new DefaultNumberAxis();
		categoryAxis = new DefaultCategoryAxis();
		items = new LinkedList<>();
	}

	@Override
	public SeriesStyle createSeriesStyle(ColorRGB color, LineStyle lineStyle, MarkerStyle markerStyle) {
		return new DefaultSeriesStyle(color, lineStyle, markerStyle);
	}

	@Override
	public LineSeries createLineSeries(String label, Collection<Double> values) {
		return new DefaultLineSeries(label, values);
	}

	@Override
	public BarSeries createBarSeries(String label, Collection<Double> values) {
		return new DefaultBarSeries(label, values);
	}

	@Override
	public BoxSeries createBoxSeries(String label, Collection<Collection<Double>> values) {
		return new DefaultBoxSeries(label, values);
	}

	@Override
	public NumberAxis getNumberAxis() {
		return valueAxis;
	}

	@Override
	public CategoryAxis getCategoryAxis() {
		return categoryAxis;
	}

	@Override
	public Collection<CategoryChartItem> getItems() {
		return items;
	}

}
