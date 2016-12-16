package net.imagej.plot;

/**
 * @author Matthias Arzt
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
