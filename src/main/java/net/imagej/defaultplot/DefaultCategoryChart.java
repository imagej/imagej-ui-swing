package net.imagej.defaultplot;

import net.imagej.plot.*;
import org.scijava.util.ColorRGB;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Matthias Arzt
 */
public class DefaultCategoryChart extends DefaultAbstractPlot implements CategoryChart {

	private String title;

	private NumberAxis valueAxis;

	private CategoryAxis categoryAxis;

	private List<CategoryChartItem> items;

	DefaultCategoryChart() {
		valueAxis = new DefaultNumberAxis();
		categoryAxis = new DefaultCategoryAxis();
		items = new LinkedList<>();
	}

	@Override
	public SeriesStyle newSeriesStyle(ColorRGB color, LineStyle lineStyle, MarkerStyle markerStyle) {
		return new DefaultSeriesStyle(color, lineStyle, markerStyle);
	}

	@Override
	public LineSeries addLineSeries(String label, Collection<Double> values) {
		return addItem(new DefaultLineSeries(label, values));
	}

	@Override
	public BarSeries addBarSeries(String label, Collection<Double> values) {
		return addItem(new DefaultBarSeries(label, values));
	}

	@Override
	public BoxSeries addBoxSeries(String label, Collection<Collection<Double>> values) {
		return addItem(new DefaultBoxSeries(label, values));
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
	public List<CategoryChartItem> getItems() {
		return Collections.unmodifiableList(items);
	}

	private <T extends CategoryChartItem> T addItem(T value) { items.add(value); return value; }
}
