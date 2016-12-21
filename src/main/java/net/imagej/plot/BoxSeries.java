package net.imagej.plot;

import org.scijava.util.ColorRGB;

import java.util.Collection;

/**
 * @author Matthias Arzt
 */
public interface BoxSeries extends CategoryChartItem, LabeledObject {

	Collection<Collection<Double>> getValues();

	void setValues(Collection<Collection<Double>> values);

	ColorRGB getColor();

	void setColor(ColorRGB color);
}
