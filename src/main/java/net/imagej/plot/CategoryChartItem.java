package net.imagej.plot;

import java.util.Collection;

/**
 * @author Matthias Arzt
 */
public interface CategoryChartItem<C> extends Labeled {

    boolean getLegendVisible();

    void setLegendVisible(boolean visible);

    Collection<? extends C> getCategories();
}
