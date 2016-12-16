package net.imagej.plot;

import java.util.Collection;

public interface ScatterPlot {

	SeriesStyle createSeriesStyle();

	void addSeries(String label, Collection<Double> xs, Collection<Double> ys, SeriesStyle style);

	NumberAxis getXAxis();

	NumberAxis getYAxis();

	void setTitle(String title);

	String getTitle();
}
