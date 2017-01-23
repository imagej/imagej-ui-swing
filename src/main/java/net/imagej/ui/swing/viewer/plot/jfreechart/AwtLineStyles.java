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

import net.imagej.plot.LineStyle;

import java.awt.*;

/**
 * @author Matthias Arzt
 */

class AwtLineStyles {

	private final boolean visible;

	private final BasicStroke stroke;

	private AwtLineStyles(boolean visible, BasicStroke stroke) {
		this.visible = visible;
		this.stroke = stroke;
	}

	public boolean isVisible() {
		return visible;
	}

	public BasicStroke getStroke() {
		return stroke;
	}

	public static AwtLineStyles getInstance(LineStyle style) {
		if(style != null)
			switch (style) {
				case SOLID:
					return solid;
				case DASH:
					return dash;
				case DOT:
					return dot;
				case NONE:
					return none;
			}
		return solid;
	}

	// --- Helper Constants ---

	private static AwtLineStyles solid = new AwtLineStyles(true, Strokes.solid);

	private static AwtLineStyles dash = new AwtLineStyles(true, Strokes.dash);

	private static AwtLineStyles dot = new AwtLineStyles(true, Strokes.dot);

	private static AwtLineStyles none = new AwtLineStyles(false, Strokes.none);

	static class Strokes {

		private static BasicStroke solid = new BasicStroke(1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);

		private static BasicStroke dash = new BasicStroke(1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
				.0f, new float[]{6.0f, 6.0f}, 0.0f);

		private static BasicStroke dot = new BasicStroke(1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
				.0f, new float[]{0.6f, 4.0f}, 0.0f);

		private static BasicStroke none = new BasicStroke(1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
				.0f, new float[]{0.0f, 100.0f}, 0.0f);
	}

}
