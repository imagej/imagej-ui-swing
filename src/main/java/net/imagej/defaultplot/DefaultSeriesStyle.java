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

import net.imagej.plot.LineStyle;
import net.imagej.plot.MarkerStyle;
import net.imagej.plot.SeriesStyle;
import org.scijava.util.ColorRGB;

/**
 * The default implementation of the {@link SeriesStyle} interface.
 *
 * @author Matthias Arzt
 */
class DefaultSeriesStyle implements SeriesStyle {

	private final ColorRGB color;

	private final MarkerStyle markerStyle;

	private final LineStyle lineStyle;

	DefaultSeriesStyle(ColorRGB color, LineStyle lineStyle, MarkerStyle markerStyle) {
		this.color = color;
		this.lineStyle = lineStyle;
		this.markerStyle = markerStyle;
	}

	// -- SeriesStype methods --

	@Override
	public ColorRGB getColor() {
		return color;
	}

	@Override
	public LineStyle getLineStyle() {
		return lineStyle;
	}

	@Override
	public MarkerStyle getMarkerStyle() {
		return markerStyle;
	}


	// -- package-private helpers --

	public static DefaultSeriesStyle emptySeriesStyle() {
		return EMPTY_SERIES_STYLE;
	}

	private static final DefaultSeriesStyle EMPTY_SERIES_STYLE = new DefaultSeriesStyle(null, null, null);

}
