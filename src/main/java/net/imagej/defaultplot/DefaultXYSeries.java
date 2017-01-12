package net.imagej.defaultplot;

import net.imagej.plot.SeriesStyle;
import net.imagej.plot.XYSeries;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Matthias Arzt
 */
class DefaultXYSeries implements XYSeries {

	private String label = "";

	private boolean legendVisible = true;

	private List<Double> xValues = null;

	private List<Double> yValues = null;

	private SeriesStyle style = null;

	DefaultXYSeries() { }

	@Override
	public String getLabel() {
		return label;
	}

	@Override
	public void setLabel(String label) {
		this.label = label;
	}

	@Override
	public boolean getLegendVisible() {
		return legendVisible;
	}

	@Override
	public void setLegendVisible(boolean legendVisible) {
		this.legendVisible = legendVisible;
	}

	@Override
	public void setValues(List<Double> xValues, List<Double> yValues) {
		this.xValues = Collections.unmodifiableList(new ArrayList<>(xValues));
		this.yValues = Collections.unmodifiableList(new ArrayList<>(yValues));
	}

	@Override
	public List<Double> getXValues() {
		return xValues == null ? Collections.emptyList() : xValues;
	}

	@Override
	public List<Double> getYValues() {
		return yValues == null ? Collections.emptyList() : yValues;
	}

	@Override
	public SeriesStyle getStyle() {
		return style;
	}

	@Override
	public void setStyle(SeriesStyle style) {
		this.style = style;
	}

}
