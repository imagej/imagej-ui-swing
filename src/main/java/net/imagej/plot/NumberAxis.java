package net.imagej.plot;

/**
 * Created by arzt on 15/12/2016.
 */
public interface NumberAxis {

	void setLabel(String label);

	String getLabel();

	void setManualRange(double min, double max);

	void setAutoRange(boolean includeZero, boolean addSpace);

	boolean hasManualRange();

	boolean doesAutoRangeIncludesZero();

	boolean doesAutoRangeAddSpace();

	Double getMin();

	Double getMax();

	void setLogarithmic(boolean logarithmic);

	boolean isLogarithmic();

}
