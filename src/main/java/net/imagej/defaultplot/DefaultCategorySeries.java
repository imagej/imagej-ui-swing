package net.imagej.defaultplot;

import java.util.Collection;

/**
 * @author Matthias Arzt
 */

abstract public class DefaultCategorySeries {

	private Collection<Double> values;

	private String label;

	DefaultCategorySeries(String label, Collection<Double> values) {
		this.label = label;
		this.values = values;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public void setValues(Collection<Double> Values) {
		this.values = values;
	}

	public Collection<Double> getValues() {
		return values;
	}

}
