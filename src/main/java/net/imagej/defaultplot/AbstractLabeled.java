package net.imagej.defaultplot;

import java.util.Objects;

/**
 * @author Matthias Arzt
 */
class AbstractLabeled {
	private String label = "";

	public void setLabel(String label) {
		this.label = Objects.requireNonNull(label);
	}

	public String getLabel() {
		return label;
	}

}
