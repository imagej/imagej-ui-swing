package net.imagej.plot;

/**
 * @author Matthias Arzt
 */
public interface NumberAxis {

	void setLabel(String label);

	String getLabel();

	void setManualRange(double min, double max);

	void setAutoRange(RangeStrategy rangeStrategy);

	RangeStrategy getRangeStrategy();

	double getMin();

	double getMax();

	void setLogarithmic(boolean logarithmic);

	boolean isLogarithmic();

}
