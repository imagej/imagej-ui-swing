package net.imagej.plot;

import org.scijava.Context;
import org.scijava.ui.UIService;

/**
 * @author Matthias Arzt
 */
class ChartDemo {

	final UIService ui;

	final PlotService plotService;

	ChartDemo() {
		final Context ctx = new Context();
		ui = ctx.service(UIService.class);
		plotService = ctx.service(PlotService.class);
	}

}
