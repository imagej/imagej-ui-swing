package net.imagej.plot;

/**
 * @author Matthias Arzt
 */
public interface NumberAxis extends Labeled {

	void setManualRange(double min, double max);

	void setAutoRange();

	void setAutoIncludeZeroRange();

	RangeStrategy getRangeStrategy();

	double getMin();

	double getMax();

	void setLogarithmic(boolean logarithmic);

	boolean isLogarithmic();

	enum RangeStrategy {
		MANUAL, AUTO, AUTO_INCLUDE_ZERO
	}
}
