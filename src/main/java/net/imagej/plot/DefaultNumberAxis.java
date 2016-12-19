package net.imagej.plot;

class DefaultNumberAxis implements NumberAxis {

	private String label;

	private double min;

	private double max;

	private boolean logarithmic;

	private RangeStrategy rangeStrategy;

	DefaultNumberAxis() {
		min = 0;
		max = 0;
		logarithmic = false;
		rangeStrategy = RangeStrategy.TIGHT;
	}

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
		rangeStrategy = RangeStrategy.MANUAL;
		this.min = min;
		this.max = max;
	}

	@Override
	public void setAutoRange(RangeStrategy rangeStrategy) {
		this.rangeStrategy = rangeStrategy;
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
