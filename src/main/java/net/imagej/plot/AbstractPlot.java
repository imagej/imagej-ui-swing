package net.imagej.plot;

/**
 * @author Matthias Arzt
 */
public interface AbstractPlot {

	void setTitle(String title);

	String getTitle();

	void setPreferredSize(int width, int height);

	int getPreferredWidth();

	int getPreferredHeight();

}
