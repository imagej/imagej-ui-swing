package net.imagej.plot;

import org.scijava.util.ColorRGB;

import java.util.Collection;
import java.util.Map;

/**
 * @author Matthias Arzt
 */
public interface BoxSeries<C> extends CategoryChartItem {

	Map<C, Collection<Double>> getValues();

	void setValues(Map<C, Collection<Double>> values);

	ColorRGB getColor();

	void setColor(ColorRGB color);
}
