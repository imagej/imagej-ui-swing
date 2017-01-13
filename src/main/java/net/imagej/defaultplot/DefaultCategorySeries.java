package net.imagej.defaultplot;

import java.util.Map;

/**
 * @author Matthias Arzt
 */

abstract public class DefaultCategorySeries<C> {

	private Map<C, Double> values = null;

	private String label = null;

	private boolean legendVisible = true;

	DefaultCategorySeries() { }

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public boolean getLegendVisible() {
		return legendVisible;
	}

	public void setLegendVisible(boolean visible) {
		legendVisible = visible;
	}

	public void setValues(Map<C, Double> values) {
		this.values = values;
	}

	public Map<C, Double> getValues() {
		return values;
	}

}
