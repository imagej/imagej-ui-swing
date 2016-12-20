package net.imagej.defaultplot;

import net.imagej.plot.LineSeries;

import java.util.Collection;

/**
 * @author Matthias Arzt
 */
public class DefaultLineSeries extends DefaultCategorySeries implements LineSeries {
	DefaultLineSeries(String label, Collection<Double> values) {
		super(label, values);
	}
}
