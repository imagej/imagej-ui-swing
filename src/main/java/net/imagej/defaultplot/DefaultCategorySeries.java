package net.imagej.defaultplot;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * @author Matthias Arzt
 */

abstract class DefaultCategorySeries<C> extends AbstractChartItem {

	private Map<? extends C, Double> values = Collections.emptyMap();

	DefaultCategorySeries() { }

	public void setValues(Map<? extends C, Double> values) {
		this.values = Collections.unmodifiableMap(values);
	}

	public Map<? extends C, Double> getValues() {
		return values;
	}

	public Collection<? extends C> getCategories() {
		return values.keySet();
	}
}
