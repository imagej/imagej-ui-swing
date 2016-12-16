package net.imagej.plot;

import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;

import java.awt.*;

public class JFreeChartLineStyles {

	static void modifyRenderer(XYLineAndShapeRenderer renderer, int series, LineStyle style) {
		if(style == null)
			return;
		renderer.setSeriesLinesVisible(series, style != LineStyle.NONE);
		renderer.setSeriesStroke(series, getAwtBasicStroke(style));
	}

	// --- Helper Constants ---

	private static BasicStroke solid = new BasicStroke(1,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND);

	private static BasicStroke dash = new BasicStroke(1,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND,
			.0f,new float[]{6.0f,6.0f},0.0f);

	private static BasicStroke dot = new BasicStroke(1,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND,
			.0f,new float[]{0.6f,4.0f},0.0f);

	private static BasicStroke none = new BasicStroke(1,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND,
			.0f,new float[]{0.0f,100.0f},0.0f);

	// --- Helper Finctions ---

	private static BasicStroke getAwtBasicStroke(LineStyle style) {
		switch (style) {
			case SOLID:
				return solid;
			case NONE:
				return none;
			case DASH:
				return dash;
			case DOT:
				return dot;
		}
		return null;
	}

}
