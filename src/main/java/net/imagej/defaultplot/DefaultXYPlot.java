package net.imagej.defaultplot;

import net.imagej.plot.*;
import org.scijava.util.ColorRGB;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Matthias Arzt
 */
class DefaultXYPlot extends DefaultAbstractPlot implements XYPlot {

	private DefaultNumberAxis xAxis;

	private DefaultNumberAxis yAxis;

	private List<XYPlotItem> items;

	DefaultXYPlot() {
		xAxis = new DefaultNumberAxis();
		yAxis = new DefaultNumberAxis();
		items = new LinkedList<>();
	}

	@Override
	public SeriesStyle newSeriesStyle(ColorRGB color, LineStyle lineStyle, MarkerStyle markerStyle) {
		return new DefaultSeriesStyle(color, lineStyle, markerStyle);
	}

	@Override
	public XYSeries addXYSeries(String label, Collection<Double> xs, Collection<Double> ys) {
		XYSeries result = new DefaultXYSeries(label, xs, ys, null);
		items.add(result);
		return result;
	}

	@Override
	public NumberAxis xAxis() {
		return xAxis;
	}

	@Override
	public NumberAxis yAxis() {
		return yAxis;
	}

	public List<XYPlotItem> getItems() {
		return Collections.unmodifiableList(items);
	}

}
