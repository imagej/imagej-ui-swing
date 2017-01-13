package net.imagej.defaultplot;

import net.imagej.plot.CategoryAxis;

import java.util.Collection;

/**
 * @author Matthias Arzt
 */
public class DefaultCategoryAxis<C> implements CategoryAxis<C> {

	private String label = null;

	DefaultCategoryAxis() { }

	@Override
	public void setLabel(String label) {
		this.label = label;
	}

	@Override
	public String getLabel() {
		return label;
	}

}
