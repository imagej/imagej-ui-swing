package net.imagej.plot;

import java.awt.*;
import java.util.Collection;

/**
 * @author Matthias Arzt
 */
public interface CategoryChart extends AbstractPlot {

	SeriesStyle createSeriesStyle(Color color, LineStyle lineStyle, MarkerStyle markerStyle);

	LineSeries createLineSeries(String label, Collection<Double> values);

	BarSeries createBarSeries(String label, Collection<Double> values);

	BoxSeries createBoxSeries(String label, Collection<Collection<Double>> values);

	NumberAxis getNumberAxis();

	CategoryAxis getCategoryAxis();

	Collection<CategoryChartItem> getItems();

	void setTitle(String title);

	String getTitle();

}
