package net.imagej.plot;

import java.awt.*;
import java.util.Collection;

/**
 * @author Matthias Arzt
 */
public interface CategoryChart extends AbstractPlot {

	SeriesStyle createSeriesStyle(Color color, LineStyle lineStyle, MarkerStyle markerStyle);

	LineSeries createLineSeries(String label, Collection<Double> values);

	// FIXME: add BarSeries createBarSeries(String label, Collection<Double> values);

	// FIXME: BoxPlot createBoxPlotSeries(String label, Collection<Double> values);

	NumberAxis getNumberAxis();

	CategoryAxis getCategoryAxis();

	Collection<LineSeries> getSeriesCollection();

	void setTitle(String title);

	String getTitle();

}
