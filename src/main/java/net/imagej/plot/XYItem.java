package net.imagej.plot;

/**
 * @author Matthias Arzt
 */
public interface XYItem {

	String getLabel();

	void setLabel(String label);

	boolean getLegendVisible();

	void setLegendVisible(boolean visible);

}
