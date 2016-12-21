package net.imagej.plot;

import java.awt.*;
import java.util.Collection;

/**
 * @author Matthias Arzt
 */
public interface BoxSeries extends CategoryChartItem, LabeledObject {

	Collection<Collection<Double>> getValues();

	void setValues(Collection<Collection<Double>> values);

	Color getColor();

	void setColor(Color color);
}
