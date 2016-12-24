package net.imagej.defaultplot;

import net.imagej.plot.AbstractPlot;

/**
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

	@Override
	public void setTitle(String title) {
		this.title = title;
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

	public int getPreferredWidth() {
		return preferredWidth;
	}

	@Override
	public int getPreferredHeight() {
		return preferredHeight;
	}
}
