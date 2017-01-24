/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2016 Board of Regents of the University of
 * Wisconsin-Madison, Broad Institute of MIT and Harvard, and Max Planck
 * Institute of Molecular Cell Biology and Genetics.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

package net.imagej.defaultplot;

import net.imagej.plot.*;
import org.scijava.util.ColorRGB;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * Default implementation of {@link CategoryChart}.
 *
 * @author Matthias Arzt
 */
class DefaultCategoryChart<C> extends DefaultAbstractPlot implements CategoryChart<C> {

	private final Class<C> categoryType;

	private final NumberAxis valueAxis;

	private final CategoryAxis<C> categoryAxis;

	private final List<CategoryChartItem<C>> items;

	DefaultCategoryChart(final Class<C> categoryType) {
		this.categoryType = Objects.requireNonNull(categoryType);
		valueAxis = new DefaultNumberAxis();
		categoryAxis = new DefaultCategoryAxis<>(this);
		items = new LinkedList<>();
	}

	// -- CategoryChart methods --

	@Override
	public SeriesStyle newSeriesStyle(ColorRGB color, LineStyle lineStyle, MarkerStyle markerStyle) {
		return new DefaultSeriesStyle(color, lineStyle, markerStyle);
	}

	@Override
	public Class<C> getCategoryType() {
		return categoryType;
	}

	@Override
	public LineSeries<C> addLineSeries() {
		return addItem(new DefaultLineSeries<>());
	}

	@Override
	public BarSeries<C> addBarSeries() {
		return addItem(new DefaultBarSeries<>());
	}

	@Override
	public BoxSeries<C> addBoxSeries() {
		return addItem(new DefaultBoxSeries<C>());
	}

	@Override
	public NumberAxis numberAxis() {
		return valueAxis;
	}

	@Override
	public CategoryAxis<C> categoryAxis() {
		return categoryAxis;
	}

	@Override
	public List<CategoryChartItem<C>> getItems() {
		return Collections.unmodifiableList(items);
	}

	// -- private helper methods --

	private <T extends CategoryChartItem> T addItem(T value) {
		items.add(value);
		return value;
	}
}
