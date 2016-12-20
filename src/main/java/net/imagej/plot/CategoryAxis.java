package net.imagej.plot;

import java.util.Collection;

/**
 * @author Matthias Arzt
 */
public interface CategoryAxis extends LabeledObject {

	void setCategories(Collection<String> categories);

	Collection<String> getCategories();

}
