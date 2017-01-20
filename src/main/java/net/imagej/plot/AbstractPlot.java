package net.imagej.plot;

/**
 * Super interface to all charts that are provided by {@link PlotService}.
 *
 * @author Matthias Arzt
 */
public interface AbstractPlot {

	void setTitle(String title);

	String getTitle();

	void setPreferredSize(int width, int height);

	int getPreferredWidth();

	int getPreferredHeight();

}
