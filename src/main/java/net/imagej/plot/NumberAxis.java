package net.imagej.plot;

/**
 * @author Matthias Arzt
 */
public interface NumberAxis extends LabeledObject {

	void setManualRange(double min, double max);

	void setAutoRange(RangeStrategy rangeStrategy);

	RangeStrategy getRangeStrategy();

	double getMin();

	double getMax();

	void setLogarithmic(boolean logarithmic);

	boolean isLogarithmic();

}
