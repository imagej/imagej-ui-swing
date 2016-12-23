package net.imagej.ui.swing.viewer.plot.utils;

import net.imagej.plot.LineStyle;

import java.awt.*;

/**
 * @author Matthias Arzt
 */

public class AwtLineStyles {

	private boolean visible;

	private BasicStroke stroke;

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
