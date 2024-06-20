/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2024 ImageJ developers.
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

package net.imagej.ui.swing.overlay;

import java.awt.Point;
import java.awt.event.InputEvent;

import org.jhotdraw.draw.BezierFigure;
import org.jhotdraw.draw.Figure;
import org.jhotdraw.draw.handle.BezierNodeHandle;

/**
 * The BezierFigure uses a BezierNodeHandle which can change the curve
 * connecting vertices from a line to a Bezier curve. We subclass both the
 * figure and the node handle to defeat this.
 *
 * @author Johannes Schindelin
 */
public class PolygonNodeHandle extends BezierNodeHandle {

	public PolygonNodeHandle(final BezierFigure owner, final int index,
		final Figure transformOwner)
	{
		super(owner, index, transformOwner);
	}

	public PolygonNodeHandle(final BezierFigure owner, final int index) {
		super(owner, index);
	}

	@Override
	public void trackEnd(final Point anchor, final Point lead,
		final int modifiersEx)
	{
		// Remove the behavior associated with the shift keys
		super.trackEnd(anchor, lead, modifiersEx & ~(InputEvent.META_DOWN_MASK |
			InputEvent.CTRL_DOWN_MASK | InputEvent.ALT_DOWN_MASK |
			InputEvent.SHIFT_DOWN_MASK));
	}

}
