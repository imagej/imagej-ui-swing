package net.imagej.defaultplot;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

/**
 * @author Matthias Arzt
 */

abstract class DefaultCategorySeries<C> {

	private Map<? extends C, Double> values = null;

	private String label = "unnamed";

	private boolean legendVisible = true;

	DefaultCategorySeries() { }

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = Objects.requireNonNull(label);
	}

	public boolean getLegendVisible() {
		return legendVisible;
	}

	public void setLegendVisible(boolean visible) {
		legendVisible = visible;
	}

	public void setValues(Map<? extends C, Double> values) {
		this.values = values;
	}

	public Map<? extends C, Double> getValues() {
		return values;
	}

	public Collection<? extends C> getCategories() {
		return values.keySet();
	}
}
