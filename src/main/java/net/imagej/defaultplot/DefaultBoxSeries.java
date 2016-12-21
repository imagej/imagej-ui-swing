package net.imagej.defaultplot;

import net.imagej.plot.BoxSeries;

import java.awt.*;
import java.util.Collection;

/**
 * Created by arzt on 21/12/2016.
 */
public class DefaultBoxSeries implements BoxSeries {

	private String label;

	private Color color;

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
	public Color getColor() {
		return color;
	}

	@Override
	public void setColor(Color color) {
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
