package net.imagej.plot;

import java.util.Comparator;
import java.util.List;

/**
 * Category axis of a {@link CategoryChart}.
 *
 * @author Matthias Arzt
 */
public interface CategoryAxis<C> extends Labeled {

	/**
	 * Manually set the list of categories to be displayed in a {@link CategoryChart}.
	 * The categories are displayed as ordered in the list, unless otherwise specified by calling the setOrder method.
	 */
	void setManualCategories(List<? extends C> categories);

	/** Clear the manually set list of categories. */
	void clearManualCategories();

	boolean hasManualCategories();

	/** Specify the order used to display the categories in a {@link CategoryChart}. */
	void setOrder(Comparator<? super C> comparator);

	void clearOrder();

	/** Returns the list of categories to be displayed in the {@link CategoryChart}. */
	List<C> getCategories();

}
