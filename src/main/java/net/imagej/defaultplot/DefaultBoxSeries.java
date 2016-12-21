package net.imagej.defaultplot;

import net.imagej.plot.BoxSeries;
import org.scijava.util.ColorRGB;

import java.util.Collection;

/**
 * Created by arzt on 21/12/2016.
 */
public class DefaultBoxSeries implements BoxSeries {

	private String label;

	private ColorRGB color;

	private Collection<Collection<Double>> values;

	public DefaultBoxSeries(String label, Collection<Collection<Double>> values) {
		this.label = label;
		this.values = values;
	}

	@Override
	public Collection<Collection<Double>> getValues() {
		return values;
	}

	@Override
	public void setValues(Collection<Collection<Double>> values) {
		this.values = values;
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
	public void setLabel(String label) {
		this.label = label;
	}

	@Override
	public String getLabel() {
		return label;
	}
}
