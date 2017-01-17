package net.imagej.defaultplot;

import net.imagej.plot.*;
import org.scijava.util.ColorRGB;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Matthias Arzt
 */
public class DefaultCategoryChart<C> extends DefaultAbstractPlot implements CategoryChart<C> {

	private String title;

	final private Class<C> categoryType;

	private NumberAxis valueAxis;

	private CategoryAxis<C> categoryAxis;

	private List<CategoryChartItem<C>> items;

	DefaultCategoryChart(final Class<C> categoryType) {
		this.categoryType = categoryType;
		valueAxis = new DefaultNumberAxis();
		categoryAxis = new DefaultCategoryAxis<>(this);
		items = new LinkedList<>();
	}

	@Override
	public SeriesStyle newSeriesStyle(ColorRGB color, LineStyle lineStyle, MarkerStyle markerStyle) {
		return new DefaultSeriesStyle(color, lineStyle, markerStyle);
	}

	@Override
	public Class<C> getCategoryType() {
		return categoryType;
	}

	@Override
	public LineSeries<C> addLineSeries() {
		return addItem(new DefaultLineSeries<>());
	}

	@Override
	public BarSeries<C> addBarSeries() {
		return addItem(new DefaultBarSeries<>());
	}

	@Override
	public BoxSeries<C> addBoxSeries() {
		return addItem(new DefaultBoxSeries<C>());
	}

	@Override
	public NumberAxis numberAxis() {
		return valueAxis;
	}

	@Override
	public CategoryAxis<C> categoryAxis() {
		return categoryAxis;
	}

	@Override
	public List<CategoryChartItem<C>> getItems() {
		return Collections.unmodifiableList(items);
	}

	private <T extends CategoryChartItem> T addItem(T value) { items.add(value); return value; }
}
