package net.imagej.defaultplot;

import net.imagej.plot.*;
import org.scijava.util.ColorRGB;

import java.util.Collection;
import java.util.LinkedList;

/**
 * @author Matthias Arzt
 */
class DefaultXYPlot extends DefaultAbstractPlot implements XYPlot {

	private DefaultNumberAxis xAxis;

	private DefaultNumberAxis yAxis;

	private Collection<XYPlotItem> items;

	DefaultXYPlot() {
		xAxis = new DefaultNumberAxis();
		yAxis = new DefaultNumberAxis();
		items = new LinkedList<>();
	}

	@Override
	public SeriesStyle createSeriesStyle(ColorRGB color, LineStyle lineStyle, MarkerStyle markerStyle) {
		return new DefaultSeriesStyle(color, lineStyle, markerStyle);
	}

	@Override
	public XYSeries createXYSeries(String label, Collection<Double> xs, Collection<Double> ys) {
		return new DefaultXYSeries(label, xs, ys, null);
	}

	@Override
	public NumberAxis getXAxis() {
		return xAxis;
	}

	@Override
	public NumberAxis getYAxis() {
		return yAxis;
	}

	public Collection<XYPlotItem> getItems() {
		return items;
	}

}
