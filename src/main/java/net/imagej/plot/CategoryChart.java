package net.imagej.plot;

import org.scijava.util.ColorRGB;

import java.util.Collection;
import java.util.List;

/**
 * @author Matthias Arzt
 */
public interface CategoryChart extends AbstractPlot {

	SeriesStyle newSeriesStyle(ColorRGB color, LineStyle lineStyle, MarkerStyle markerStyle);

	LineSeries addLineSeries(String label, Collection<Double> values);

	BarSeries addBarSeries(String label, Collection<Double> values);

	BoxSeries addBoxSeries(String label, Collection<Collection<Double>> values);

	NumberAxis getNumberAxis();

	CategoryAxis getCategoryAxis();

	List<CategoryChartItem> getItems();

	void setTitle(String title);

	String getTitle();

}
