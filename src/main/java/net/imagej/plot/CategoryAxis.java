package net.imagej.plot;

import java.util.Comparator;
import java.util.List;

/**
 * @author Matthias Arzt
 */
public interface CategoryAxis<C> extends Labeled {

	void setManualCategories(List<C> categories);

	void clearManualCategories();

	boolean hasManualCategories();

	void setOrder(Comparator<C> comparator);

	void clearOrder();

	List<C> getCategories();

}
