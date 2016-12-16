package net.imagej.plot;

/**
 * Created by arzt on 15/12/2016.
 */
public interface ValueAxis {

	void setLabel(String label);

	String getLabel();

	void setManualRange(double min, double max);

	void setAutoRange();

	void setAutoRangeIncludeZero();

	boolean hasManualRange();

	boolean doesAutoRangeIncludesZero();

	Double getMin();

	Double getMax();

	void setLogarithmic(boolean logarithmic);

	boolean isLogarithmic();

}
