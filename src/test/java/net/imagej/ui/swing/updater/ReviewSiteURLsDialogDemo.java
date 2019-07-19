package net.imagej.ui.swing.updater;

import net.imagej.updater.URLChange;
import net.imagej.updater.UpdateSite;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class ReviewSiteURLsDialogDemo {

	public static void main(String...  args) {
		List<URLChange> changes = new ArrayList<>();
		changes.add(URLChange.create(createUpdateSite("a", "https://downloads.micron.ox.ac.uk"),
				"https://better.a.com").get());
		changes.add(URLChange.create(createUpdateSite("b", "https://b.com"),
				"https://better.a.com").get());
		new ReviewSiteURLsDialog(null, changes).setVisible(true);
	}

	private static UpdateSite createUpdateSite(String b, String url) {
		return new UpdateSite(b, url, "", "", "Great Update Site", "Best Maintainer in the World", 0);
	}
}
