package net.imagej.plot;

import java.awt.*;
import java.util.Collection;
import java.util.LinkedList;

/**
 * @author Matthias Arzt
 */
public class DefaultXYPlot implements XYPlot {

	private DefaultNumberAxis xAxis;

	private DefaultNumberAxis yAxis;

	private String title;

	private Collection<XYSeries> seriesCollection;

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
	public XYSeries createXYSeries(String label, Collection<Double> xs, Collection<Double> ys, SeriesStyle style) {
		return new DefaultXYSeries(label, xs, ys, style);
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
	public Collection<XYSeries> getSeriesCollection() {
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
