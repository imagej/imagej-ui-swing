package net.imagej.plot;

import org.scijava.util.ColorRGB;

import java.util.Collection;
import java.util.List;

/**
 * @author Matthias Arzt
 */
public interface XYPlot extends AbstractPlot {

	SeriesStyle newSeriesStyle(ColorRGB color, LineStyle lineStyle, MarkerStyle markerStyle);

	XYSeries addXYSeries();

	NumberAxis xAxis();

	NumberAxis yAxis();

	List<XYPlotItem> getItems();

}
