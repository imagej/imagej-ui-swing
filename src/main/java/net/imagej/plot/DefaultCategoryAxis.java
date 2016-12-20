package net.imagej.plot;

import java.util.Collection;

/**
 * @author Matthias Arzt
 */
public class DefaultCategoryAxis implements CategoryAxis{

	private String label;

	private Collection<String> categories;

	DefaultCategoryAxis() {
		label = null;
		categories = null;
	}

	@Override
	public void setLabel(String label) {
		this.label = label;
	}

	@Override
	public String getLabel() {
		return label;
	}

	@Override
	public void setCategories(Collection<String> categories) {
		this.categories = categories;
	}

	@Override
	public Collection<String> getCategories() {
		return categories;
	}
}
