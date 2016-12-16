package net.imagej.plot;

import java.util.Collection;

/**
 * @author Matthias Arzt
 */
class DefaultXYSeries implements XYSeries {

	DefaultXYSeries(String label, Collection<Double> xs, Collection<Double> ys, SeriesStyle style) {
		this.label = label;
		this.xValues = xs;
		this.yValues = ys;
		this.style = style;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public Collection<Double> getXValues() {
		return xValues;
	}

	public void setXValues(Collection<Double> xValues) {
		this.xValues = xValues;
	}

	public Collection<Double> getYValues() {
		return yValues;
	}

	public void setYValues(Collection<Double> yValues) {
		this.yValues = yValues;
	}

	public SeriesStyle getStyle() {
		return style;
	}

	public void setStyle(SeriesStyle style) {
		this.style = style;
	}

	private String label;
	private Collection<Double> xValues;
	private Collection<Double> yValues;
	private SeriesStyle style;

}