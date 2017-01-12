package net.imagej.plot;

import org.scijava.util.ColorRGB;

import java.util.Collection;

/**
 * @author Matthias Arzt
 */
public interface BarSeries extends Labeled, CategoryChartItem {

    Collection<Double> getValues();

    void setValues(Collection<Double> Values);

    ColorRGB getColor();

    void setColor(ColorRGB color);

}
