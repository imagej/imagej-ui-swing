package net.imagej.plot;

import org.scijava.util.ColorRGB;

import java.util.List;

/**
 * Container for data and settings discribing a chart, whose data is organised in categories.
 *
 * @author Matthias Arzt
 */
public interface CategoryChart<C> extends AbstractPlot {

	SeriesStyle newSeriesStyle(ColorRGB color, LineStyle lineStyle, MarkerStyle markerStyle);

	Class<C> getCategoryType();

	LineSeries<C> addLineSeries();

	BarSeries<C> addBarSeries();

	BoxSeries<C> addBoxSeries();

	NumberAxis numberAxis();

	CategoryAxis<C> categoryAxis();

	List<CategoryChartItem<C>> getItems();

	void setTitle(String title);

	String getTitle();

}
