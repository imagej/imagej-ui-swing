package net.imagej.plot;

import java.awt.*;
import java.util.Collection;

/**
 * @author Matthias Arzt
 */
public interface XYPlot {

	SeriesStyle createSeriesStyle(Color color, LineStyle lineStyle, MarkerStyle markerStyle);

	XYSeries createXYSeries(String label, Collection<Double> xs, Collection<Double> ys, SeriesStyle style);

	NumberAxis getXAxis();

	NumberAxis getYAxis();

	Collection<XYSeries> getSeriesCollection();

	void setTitle(String title);

	String getTitle();
}
