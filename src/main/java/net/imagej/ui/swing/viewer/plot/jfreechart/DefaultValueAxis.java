package net.imagej.ui.swing.viewer.plot.jfreechart;

import net.imagej.plot.NumberAxis;

public class DefaultValueAxis implements NumberAxis {

	@Override
	public void setLabel(String label) {
		this.label = label;
	}

	@Override
	public String getLabel() {
		return label;
	}

	@Override
	public void setManualRange(double min, double max) {
		rangeIsSet = true;
		rangeContainsZero = false;
		this.min = min;
		this.max = max;
	}

	@Override
	public void setAutoRange(boolean includeZero, boolean add_space) {
		rangeIsSet = false;
		rangeContainsZero = includeZero;
		rangeAddSpace = add_space;
	}

	@Override
	public boolean hasManualRange() {
		return rangeIsSet;
	}

	@Override
	public boolean doesAutoRangeIncludesZero() { return rangeContainsZero; }

	@Override
	public boolean doesAutoRangeAddSpace() { return rangeAddSpace; }

	@Override
	public Double getMin() {
		return rangeIsSet ? min : null;
	}

	@Override
	public Double getMax() {
		return rangeIsSet ? max : null;
	}

	@Override
	public void setLogarithmic(boolean logarithmic) {
		this.logarithmic = logarithmic;
	}

	@Override
	public boolean isLogarithmic() {
		return logarithmic;
	}

	DefaultValueAxis() {
		min = 0;
		max = 0;
		rangeIsSet = false;
		rangeContainsZero = false;
		logarithmic = false;
	}

	private String label;
	private double min;
	private double max;
	private boolean rangeIsSet;
	private boolean logarithmic;
	private boolean rangeContainsZero;
	private boolean rangeAddSpace;
}
