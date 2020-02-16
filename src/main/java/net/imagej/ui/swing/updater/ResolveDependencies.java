/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2020 ImageJ developers.
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

package net.imagej.ui.swing.updater;

import java.util.ArrayList;

import net.imagej.updater.Conflicts;
import net.imagej.updater.Conflicts.Conflict;
import net.imagej.updater.FilesCollection;

/**
 * This dialog displays the update or upload conflicts for resolution.
 * 
 * @author Johannes Schindelin
 */
@SuppressWarnings("serial")
public class ResolveDependencies extends ConflictDialog {

	protected Conflicts conflicts;
	protected boolean forUpload;

	public ResolveDependencies(final UpdaterFrame owner,
		final FilesCollection files)
	{
		this(owner, files, false);
	}

	public ResolveDependencies(final UpdaterFrame owner,
		final FilesCollection files, final boolean forUpload)
	{
		super(owner, "Resolve dependencies");

		this.forUpload = forUpload;
		conflicts = new Conflicts(files);
		conflictList = new ArrayList<>();
	}

	@Override
	protected void updateConflictList() {
		conflictList.clear();
		for (final Conflict conflict : conflicts.getConflicts(forUpload))
			conflictList.add(conflict);
	}
}
