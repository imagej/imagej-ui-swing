package net.imagej.defaultplot;

import net.imagej.plot.Labeled;

import java.util.Objects;

/**
 * An abstract class that gives default behavior for the {@link Labeled} interface.
 *
 * @author Matthias Arzt
 */
abstract class AbstractLabeled implements Labeled {

	private String label = "";

	// -- Labeled methods --

	public void setLabel(String label) {
		this.label = Objects.requireNonNull(label);
	}

	public String getLabel() {
		return label;
	}

}
