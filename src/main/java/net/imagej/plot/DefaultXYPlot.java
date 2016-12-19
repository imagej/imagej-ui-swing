package net.imagej.plot;

import java.awt.*;
import java.util.Collection;
import java.util.LinkedList;

/**
 * @author Matthias Arzt
 */
class DefaultXYPlot implements XYPlot {

	private DefaultNumberAxis xAxis;

	private DefaultNumberAxis yAxis;

	private String title;

	private Collection<XYItem> seriesCollection;

	DefaultXYPlot() {
		xAxis = new DefaultNumberAxis();
		yAxis = new DefaultNumberAxis();
		seriesCollection = new LinkedList<>();
	}

	@Override
	public SeriesStyle createSeriesStyle(Color color, LineStyle lineStyle, MarkerStyle markerStyle) {
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

	@Override
	public Collection<XYItem> getSeriesCollection() {
		return seriesCollection;
	}

	@Override
	public void setTitle(String title) {
		this.title = title;
	}

	@Override
	public String getTitle() {
		return title;
	}

}
