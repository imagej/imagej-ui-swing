package net.imagej.ui.swing.ops;


import org.scijava.Context;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;

@Plugin(type = Service.class)
public class DefaultOpFinderService extends AbstractService implements OpFinderService {

	@Parameter
	private Context context;

	private OpFinder opFinder;

	@Override
	public void showOpFinder() {
		boolean initSize = opFinder == null || opFinder.isVisible() == false;

		if (opFinder == null) {
			makeOpFinder();
		}

		if (initSize) {
			opFinder.setVisible(true);
			opFinder.pack();
			opFinder.setLocationRelativeTo(null); // center on screen
		}
		opFinder.requestFocus();
	}

	private synchronized void makeOpFinder() {
		if (opFinder == null)
			opFinder = new OpFinder(context);
	}

}
