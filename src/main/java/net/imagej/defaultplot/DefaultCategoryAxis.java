package net.imagej.defaultplot;

import net.imagej.plot.CategoryAxis;
import net.imagej.plot.CategoryChart;
import net.imagej.plot.CategoryChartItem;

import java.util.*;

/**
 * @author Matthias Arzt
 */
class DefaultCategoryAxis<C> implements CategoryAxis<C> {

	private final CategoryChart<C> chart;

	private String label = null;

	private List<? extends C> categories = null;

	private Comparator<? super C> comparator = null;

	DefaultCategoryAxis(CategoryChart<C> chart) {
		this.chart = chart;
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
	public void setManualCategories(List<? extends C> categories) {
		this.categories = categories;
	}

	@Override
	public void clearManualCategories() {
		this.categories = null;
	}

	@Override
	public boolean hasManualCategories() {
		return categories != null;
	}

	@Override
	public void setOrder(Comparator<? super C> comparator) {
		this.comparator = comparator;
	}

	@Override
	public void clearOrder() {
		this.comparator = null;
	}

	@Override
	public List<C> getCategories() {
		List<C> result = getCategoriesDefaultOrder();
		if(comparator != null)
			result.sort(comparator);
		return result;
	}

	private List<C> getCategoriesDefaultOrder() {
		if(categories == null) {
			Set<C> allCategories = newEmptySetOfCategories();
			for (CategoryChartItem item : chart.getItems())
				allCategories.addAll(item.getCategories());
			return new ArrayList<>(allCategories);
		} else
			return new ArrayList<>(categories); // Make copy to avoid the list passed to setManualCategories to be sorted.
	}

	private Set<C> newEmptySetOfCategories() {
		if(Comparable.class.isAssignableFrom(chart.getCategoryType()))
			return new TreeSet<>();
		else
			return new HashSet<>();
	}
}
