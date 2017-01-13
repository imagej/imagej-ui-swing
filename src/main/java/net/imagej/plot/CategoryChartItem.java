package net.imagej.plot;

/**
 * @author Matthias Arzt
 */
public interface CategoryChartItem<C> extends Labeled {

    boolean getLegendVisible();

    void setLegendVisible(boolean visible);

}
