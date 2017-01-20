package net.imagej.defaultplot;

import net.imagej.plot.NumberAxis;

class DefaultNumberAxis extends AbstractLabeled implements NumberAxis {

	private double min;

	private double max;

	private boolean logarithmic;

	private RangeStrategy rangeStrategy;

	DefaultNumberAxis() {
		min = 0;
		max = 0;
		logarithmic = false;
		rangeStrategy = RangeStrategy.AUTO;
	}

	@Override
	public void setManualRange(double min, double max) {
		rangeStrategy = RangeStrategy.MANUAL;
		this.min = min;
		this.max = max;
	}

	@Override
	public void setAutoRange() {
		this.rangeStrategy = RangeStrategy.AUTO;
	}

	@Override
	public void setAutoIncludeZeroRange() {
		this.rangeStrategy = RangeStrategy.AUTO_INCLUDE_ZERO;
	}

	@Override
	public RangeStrategy getRangeStrategy() {
		return rangeStrategy;
	}

	@Override
	public double getMin() {
		return min;
	}

	@Override
	public double getMax() {
		return max;
	}

	@Override
	public void setLogarithmic(boolean logarithmic) {
		this.logarithmic = logarithmic;
	}

	@Override
	public boolean isLogarithmic() {
		return logarithmic;
	}

}
