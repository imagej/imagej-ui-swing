package net.imagej.defaultplot;

import net.imagej.plot.BarSeries;

import java.util.Collection;

/**
 * @author Matthias Arzt
 */
public class DefaultBarSeries extends DefaultCategorySeries implements BarSeries {
	DefaultBarSeries(String label, Collection<Double> values) {
		super(label, values);
	}
}
