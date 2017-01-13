package net.imagej.plot;

import java.util.Collection;
import java.util.Map;

/**
 * @author Matthias Arzt
 */
public interface LineSeries<C> extends CategoryChartItem {

    Map<C, Double> getValues();

    void setValues(Map<C, Double> Values);

    SeriesStyle getStyle();

    void setStyle(SeriesStyle style);

}
