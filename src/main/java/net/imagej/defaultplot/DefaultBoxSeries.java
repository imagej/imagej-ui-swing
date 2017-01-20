package net.imagej.defaultplot;

import net.imagej.plot.BoxSeries;
import org.scijava.util.ColorRGB;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * @author Matthias Arzt
 */
class DefaultBoxSeries<C> extends AbstractChartItem implements BoxSeries<C> {

	private ColorRGB color = null;

	private Map<C, Collection<Double>> values = Collections.emptyMap();

	public DefaultBoxSeries() { }

	@Override
	public Map<C, Collection<Double>> getValues() {
		return values;
	}

	@Override
	public void setValues(Map<? extends C, ? extends Collection<Double>> values) {
		this.values = Collections.unmodifiableMap(values);
	}

	@Override
	public ColorRGB getColor() {
		return color;
	}

	@Override
	public void setColor(ColorRGB color) {
		this.color = color;
	}

	@Override
	public Collection getCategories() {
		return values.keySet();
	}
}
