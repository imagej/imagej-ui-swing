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

import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.event.UndoableEditListener;

import net.imagej.display.ImageDisplay;

import org.jhotdraw.draw.tool.Tool;

/**
 * A JHotDraw {@link Tool} (<em>not</em> a {@link org.scijava.tool.Tool}) with
 * some extra needed API methods.
 * 
 * @author Johannes Schindelin
 */
public interface JHotDrawTool extends Tool {

	void addUndoableEditListener(UndoableEditListener l);

	void removeUndoableEditListener(UndoableEditListener listener);

	void setInputMap(InputMap map);

	void setActionMap(ActionMap map);

	/** Gets the {@link ImageDisplay} associated with this JHotDraw tool. */
	ImageDisplay getDisplay();

	/** Gets the {@link JHotDrawAdapter} associated with this JHotDraw tool. */
	JHotDrawAdapter<?> getAdapter();

	boolean isConstructing();

}
