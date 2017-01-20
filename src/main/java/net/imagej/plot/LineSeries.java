package net.imagej.plot;

import java.util.Collection;
import java.util.Map;

/**
 * A data series of a {@link CategoryChart} to be disiplayed as line or points.
 *
 * @author Matthias Arzt
 */
public interface LineSeries<C> extends CategoryChartItem {

    Map<? extends C, Double> getValues();

    void setValues(Map<? extends C, Double> Values);

    SeriesStyle getStyle();

    void setStyle(SeriesStyle style);

}
