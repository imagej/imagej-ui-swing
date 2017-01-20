package net.imagej.defaultplot;

import net.imagej.plot.AbstractPlot;

import java.util.Objects;

/**
 * An abstract class with default behavior for the {@link AbstractPlot} interface.
 *
 * @author Matthias Arzt
 */

abstract class DefaultAbstractPlot implements AbstractPlot {

	private String title;

	private int preferredWidth;

	private int preferredHeight;

	DefaultAbstractPlot() {
		title = "";
		preferredWidth = 600;
		preferredHeight = 400;
	}

	// -- AbstractPlot methods --

	@Override
	public void setTitle(String title) {
		this.title = Objects.requireNonNull(title);
	}

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public void setPreferredSize(int width, int height) {
		this.preferredWidth = width;
		this.preferredHeight = height;
	}

	@Override
	public int getPreferredWidth() {
		return preferredWidth;
	}

	@Override
	public int getPreferredHeight() {
		return preferredHeight;
	}
}
