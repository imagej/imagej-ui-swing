
package net.imagej.ui.swing.troubleshooter;

import javax.swing.JFrame;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.scijava.path.BranchingPathNode;

@SuppressWarnings("serial")
public class TroubleshootingFrame extends JFrame implements
	ListSelectionListener
{

	public TroubleshootingFrame(BranchingPathNode root) {
		// TODO Auto-generated constructor stub
	}

	// TODO bottom frame has:
	// - "back" link
	// - "Submit a bug report" link

	@Override
	public void valueChanged(ListSelectionEvent e) {
		// TODO Auto-generated method stub

	}

}
