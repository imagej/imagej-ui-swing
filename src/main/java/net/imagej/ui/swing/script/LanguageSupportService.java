
package net.imagej.ui.swing.script;

import java.util.HashMap;
import java.util.Map;

import org.fife.rsta.ac.LanguageSupport;
import org.scijava.plugin.AbstractSingletonService;
import org.scijava.plugin.Plugin;
import org.scijava.plugin.PluginInfo;
import org.scijava.script.ScriptLanguage;
import org.scijava.service.Service;

/**
 * Service which manages {@link LanguageSupportPlugin}s.
 * 
 * @author Jonathan Hale
 */
@Plugin(type = Service.class)
public class LanguageSupportService extends
	AbstractSingletonService<LanguageSupportPlugin>
{

	final Map<String, LanguageSupportPlugin> languageSupportMap =
		new HashMap<String, LanguageSupportPlugin>();

	@Override
	public Class<LanguageSupportPlugin> getPluginType() {
		return LanguageSupportPlugin.class;
	}

	@Override
	public void initialize() {
		super.initialize();

		for (PluginInfo<LanguageSupportPlugin> p : this.getPlugins()) {
			languageSupportMap.put(p.getLabel().toLowerCase(), this.getInstance(p.getPluginClass()));
		}
	}

	/**
	 * Get a {@link LanguageSupport} for the given language.
	 * 
	 * @param language
	 * @return a {@link LanguageSupport} matching the given language or the
	 *         <code>null</code> if there was none.
	 */
	public LanguageSupport getCompletionProvider(ScriptLanguage language) {
		return languageSupportMap.get(language.getLanguageName().toLowerCase());
	}

}