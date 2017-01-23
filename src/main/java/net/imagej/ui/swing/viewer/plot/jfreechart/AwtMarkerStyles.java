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

package net.imagej.ui.swing.viewer.plot.jfreechart;

import net.imagej.plot.MarkerStyle;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;

/**
 * @author Matthias Arzt
 */

class AwtMarkerStyles {

	private final boolean visible;

	private final boolean filled;

	private final Shape shape;

	private AwtMarkerStyles(boolean visible, boolean filled, Shape shape) {
		this.visible = visible;
		this.filled = filled;
		this.shape = shape;
	}

	public boolean isVisible() {
		return visible;
	}

	public boolean isFilled() {
		return filled;
	}

	public Shape getShape() {
		return shape;
	}

	public static AwtMarkerStyles getInstance(MarkerStyle style) {
		if(style != null)
			switch (style) {
				case NONE:
					return none;
				case PLUS:
					return plus;
				case X:
					return x;
				case STAR:
					return star;
				case SQUARE:
					return square;
				case FILLEDSQUARE:
					return filledSquare;
				case CIRCLE:
					return circle;
				case FILLEDCIRCLE:
					return filledCircle;
			}
		return square;
	}

	// --- Helper Constants ---

	private static AwtMarkerStyles none = new AwtMarkerStyles(false, false, null);

	private static AwtMarkerStyles plus = new AwtMarkerStyles(true, false, Shapes.plus);

	private static AwtMarkerStyles x = new AwtMarkerStyles(true, false, Shapes.x);

	private static AwtMarkerStyles star = new AwtMarkerStyles(true, false, Shapes.star);

	private static AwtMarkerStyles square = new AwtMarkerStyles(true, false, Shapes.square);

	private static AwtMarkerStyles filledSquare = new AwtMarkerStyles(true, true, Shapes.square);

	private static AwtMarkerStyles circle = new AwtMarkerStyles(true, false, Shapes.circle);

	private static AwtMarkerStyles filledCircle = new AwtMarkerStyles(true, true, Shapes.circle);


	static private class Shapes {

		private static Shape x = getAwtXShape();

		private static Shape plus = getAwtPlusShape();

		private static Shape star = getAwtStarShape();

		private static Shape square = new Rectangle2D.Double(-3.0, -3.0, 6.0, 6.0);

		private static Shape circle = new Ellipse2D.Double(-3.0, -3.0, 6.0, 6.0);

		private static Shape getAwtXShape() {
			final Path2D p = new Path2D.Double();
			final double s = 3.0;
			p.moveTo(-s, -s);
			p.lineTo(s, s);
			p.moveTo(s, -s);
			p.lineTo(-s, s);
			return p;
		}

		private static Shape getAwtPlusShape() {
			final Path2D p = new Path2D.Double();
			final double t = 4.0;
			p.moveTo(0, -t);
			p.lineTo(0, t);
			p.moveTo(t, 0);
			p.lineTo(-t, 0);
			return p;
		}

		private static Shape getAwtStarShape() {
			final Path2D p = new Path2D.Double();
			final double s = 3.0;
			p.moveTo(-s, -s);
			p.lineTo(s, s);
			p.moveTo(s, -s);
			p.lineTo(-s, s);
			final double t = 4.0;
			p.moveTo(0, -t);
			p.lineTo(0, t);
			p.moveTo(t, 0);
			p.lineTo(-t, 0);
			return p;
		}

	}

}
