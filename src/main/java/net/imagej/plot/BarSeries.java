package net.imagej.plot;

import org.scijava.util.ColorRGB;

import java.util.Map;

/**
 * @author Matthias Arzt
 */
public interface BarSeries<C> extends CategoryChartItem {

    Map<? extends C, Double> getValues();

    void setValues(Map<? extends C, Double> Values);

    ColorRGB getColor();

    void setColor(ColorRGB color);

}
