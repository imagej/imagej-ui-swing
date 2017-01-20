package net.imagej.defaultplot;

import net.imagej.plot.*;
import org.scijava.util.ColorRGB;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * The default implementation of the {@link XYPlot} interface.
 *
 * @author Matthias Arzt
 */
class DefaultXYPlot extends DefaultAbstractPlot implements XYPlot {

	private final DefaultNumberAxis xAxis;

	private final DefaultNumberAxis yAxis;

	private final List<XYPlotItem> items;

	DefaultXYPlot() {
		xAxis = new DefaultNumberAxis();
		yAxis = new DefaultNumberAxis();
		items = new LinkedList<>();
	}

	// -- XYPlot methods --

	@Override
	public SeriesStyle newSeriesStyle(ColorRGB color, LineStyle lineStyle, MarkerStyle markerStyle) {
		return new DefaultSeriesStyle(color, lineStyle, markerStyle);
	}

	@Override
	public XYSeries addXYSeries() {
		XYSeries result = new DefaultXYSeries();
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

	@Override
	public List<XYPlotItem> getItems() {
		return Collections.unmodifiableList(items);
	}

}
