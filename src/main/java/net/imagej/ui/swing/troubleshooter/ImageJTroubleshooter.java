
package net.imagej.ui.swing.troubleshooter;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;

import net.imagej.ui.swing.updater.SwingTools;
import net.sf.fmj.ui.wizard.Wizard;
import net.sf.fmj.ui.wizard.WizardPanelDescriptor;

import org.scijava.help.TroubleshooterUI;
import org.scijava.help.TroubleshootingPath;
import org.scijava.help.TroubleshootingService;
import org.scijava.plugin.Menu;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

@Plugin(type = TroubleshooterUI.class, menu = { @Menu(label = "Help"),
	@Menu(label = "Troubleshoot...") })
public class ImageJTroubleshooter implements TroubleshooterUI {

	private JFrame frame;
	private Map<String, IJWizardPanelDescriptor> panels;
	private static final String ROOT = "root";
	private static final String TAIL = "tail";

	@Parameter
	private TroubleshootingService ts;

	@Override
	public void run() {
		SwingTools.invokeOnEDT(new Runnable() {

			@Override
			public void run() {
				frame = new JFrame();
				Wizard w = new Wizard(frame);
				initPanels(ts.getInstances(), w);
				w.setTitle("What went wrong?");
				w.setCurrentPanel(ROOT);
				w.showModalDialog();
			}
		});
	}

	private void initPanels(List<TroubleshootingPath> instances, Wizard w) {
		if (panels == null) {
			synchronized (this) {
				if (panels == null) {
					panels = new HashMap<String, IJWizardPanelDescriptor>();
					for (TroubleshootingPath path : ts.getInstances()) {
						String lastId = ROOT;
						IJWizardPanelDescriptor currentPanel = getPanel(lastId, null);
						Iterator<String> iterator = path.iterator();
						while (iterator.hasNext()) {
							String label = iterator.next();
							JButton button = new JButton(path.getDescription(label));
							button.setFocusPainted(false);
							button.setContentAreaFilled(false);
							if (!iterator.hasNext()) {
								label = TAIL;
								initTail(w);
							}
							button.addActionListener(new WizardListener(w, label));
							currentPanel.addButton(button);
							if (iterator.hasNext()) {
								currentPanel = getPanel(label, lastId);
								lastId = label;
							}
						}
					}
				}
			}
		}

		for (final String id : panels.keySet()) {
			w.registerWizardPanel(id, panels.get(id));
		}
	}

	private void initTail(final Wizard w) {
		IJWizardPanelDescriptor tail = panels.get(TAIL);
		if (tail == null) {
			synchronized (this) {
				tail = panels.get(TAIL);
				if (tail == null) {
					tail = getPanel(TAIL, null);
					JButton button = new JButton("Yes that solved my problem!");
					button.setFocusPainted(false);
					button.setContentAreaFilled(false);
					button.addActionListener(new ActionListener() {

						@Override
						public void actionPerformed(ActionEvent e) {
							w.close(Wizard.FINISH_RETURN_CODE);
						}
					});
					tail.addButton(button);
					button =
						new JButton("That didn't solve my problem - I want to start over");
					button.setFocusPainted(false);
					button.setContentAreaFilled(false);
					button.addActionListener(new WizardListener(w, ROOT));
					tail.addButton(button);
				}
			}
		}
	}

	private IJWizardPanelDescriptor
		getPanel(final String id, final String backId)
	{
		IJWizardPanelDescriptor p = panels.get(id);
		if (p == null) {
			synchronized (this) {
				p = panels.get(id);
				if (p == null) {
					p = new IJWizardPanelDescriptor(backId);
					p.setPanelDescriptorIdentifier(id);
					panels.put(id, p);
				}
			}
		}
		return p;
	}

	private static class WizardListener implements ActionListener {

		final Wizard wizard;
		final String id;

		public WizardListener(Wizard w, String id) {
			wizard = w;
			this.id = id;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			wizard.setCurrentPanel(id);
		}

	}

	private static class IJWizardPanelDescriptor extends WizardPanelDescriptor {

		private final String backId;

		public IJWizardPanelDescriptor(final String backId) {
			super(backId, new Box(BoxLayout.Y_AXIS));
			this.backId = backId;
		}

		public void addButton(final JButton button) {
			Box box = (Box) getPanelComponent();
			box.add(button);
			box.add(Box.createVerticalStrut(10));
		}

		public Object getBackPanelDescriptor() {
			return backId;
		}

	}
}
