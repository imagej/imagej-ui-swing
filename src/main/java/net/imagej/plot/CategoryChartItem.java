package net.imagej.plot;

import java.util.Collection;

/**
 * Interface that is extended by all data series of {@link CategoryChart}.
 *
 * @author Matthias Arzt
 */
public interface CategoryChartItem<C> extends Labeled {

    boolean getLegendVisible();

    void setLegendVisible(boolean visible);

    Collection<? extends C> getCategories();
}
