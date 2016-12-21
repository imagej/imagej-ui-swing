package net.imagej.plot;

import java.awt.*;
import java.util.Collection;

/**
 * @author Matthias Arzt
 */
public interface XYPlot extends AbstractPlot {

	SeriesStyle createSeriesStyle(Color color, LineStyle lineStyle, MarkerStyle markerStyle);

	XYSeries createXYSeries(String label, Collection<Double> xs, Collection<Double> ys);

	NumberAxis getXAxis();

	NumberAxis getYAxis();

	Collection<XYPlotItem> getItems();

	void setTitle(String title);

	String getTitle();

}
