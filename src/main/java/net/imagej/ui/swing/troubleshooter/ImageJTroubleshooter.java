
package net.imagej.ui.swing.troubleshooter;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import javax.script.ScriptException;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;

import net.imagej.ui.swing.updater.SwingTools;

import org.scijava.path.TroubleshooterUI;
import org.scijava.path.Path;
import org.scijava.path.TroubleshootingService;
import org.scijava.plugin.Menu;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.script.ScriptService;
import org.scijava.ui.swing.wizard.Wizard;
import org.scijava.ui.swing.wizard.WizardPanelDescriptor;

@Plugin(type = TroubleshooterUI.class, menu = { @Menu(label = "Help"),
	@Menu(label = "Troubleshoot...") })
public class ImageJTroubleshooter implements TroubleshooterUI {

	private JFrame frame;
	private Map<String, IJWizardPanelDescriptor> panels;
	private static final String ROOT = "root";
	private static final String TAIL = "tail";
	private static final String ROOT_TITLE = "What went wrong?";
	private static final String TAIL_TITLE = "Did that fix your problem?";

	@Parameter
	private TroubleshootingService ts;

	@Parameter
	private ScriptService ss;

	@Override
	public void run() {
		SwingTools.invokeOnEDT(new Runnable() {

			@Override
			public void run() {
				frame = new JFrame();
				Wizard w = new Wizard(frame);
				initPanels(ts.getInstances(), w);
				w.setTitle(ROOT_TITLE);
				w.setCurrentPanel(ROOT);
				w.showModalDialog();
			}
		});
	}

	private void initPanels(List<Path> instances, Wizard w) {
		if (panels == null) {
			synchronized (this) {
				if (panels == null) {
					panels = new HashMap<String, IJWizardPanelDescriptor>();
					for (Path path : ts.getInstances()) {
						String lastId = ROOT;
						IJWizardPanelDescriptor currentPanel = getPanel(lastId, null);
						Iterator<String> iterator = path.iterator();
						while (iterator.hasNext()) {
							String label = iterator.next();
							String title = label;
							JButton button = new JButton(path.getDescription(label));
							button.setFocusPainted(false);
							button.setContentAreaFilled(false);
							if (!iterator.hasNext()) {
								label = TAIL;
								title = TAIL_TITLE;
								initTail(w);
							}
							button.addActionListener(new WizardListener(w, ss, path
								.getScriptName(), path.getScript(), label, title));
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
					button.addActionListener(new WizardListener(w, ss, "", "", ROOT,
						ROOT_TITLE));
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
					p = new IJWizardPanelDescriptor(id, backId);
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
		final String title;

		private String scriptName;
		private String script;
		private ScriptService ss;

		public WizardListener(Wizard w, ScriptService ss, String scriptName,
			String script, String id)
		{
			this(w, ss, scriptName, script, id, null);
		}

		public WizardListener(Wizard w, ScriptService ss, String scriptName,
			String script, String id, String title)
		{
			wizard = w;
			this.id = id;
			this.title = title;
			this.scriptName = scriptName;
			this.script = script;
			this.ss = ss;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (id.equals(TAIL)) {
				try {
					ss.run(scriptName, script, false, (Object[]) null);
				}
				catch (IOException e1) {
					System.out.println(e1);
				}
				catch (ScriptException e1) {
					System.out.println(e1);
				}
			}

			wizard.setCurrentPanel(id);
			if (title != null) wizard.setTitle(title);
		}
	}

	private static class IJWizardPanelDescriptor extends WizardPanelDescriptor {

		public static final String BUTTON_PANEL_IDENTIFIER = "buttenPanelIdentifier";
		private final String backId;
		private final String title;

		public IJWizardPanelDescriptor(final String backId, final String title) {
			super(BUTTON_PANEL_IDENTIFIER, new Box(BoxLayout.Y_AXIS));
			this.backId = backId;
			this.title = title;
		}

		public void addButton(final JButton button) {
			Box box = (Box) getPanelComponent();
			box.add(button);
			box.add(Box.createVerticalStrut(10));
		}

		@Override
		public Object getBackPanelDescriptor() {
			return backId;
		}

		@Override
		public boolean aboutToDisplayPanel(final Object prevId) {
			getWizard().setTitle(title);
			return super.aboutToDisplayPanel(prevId);
		}
	}
}
