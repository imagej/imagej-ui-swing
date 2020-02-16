/*-
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

import net.imagej.updater.FilesCollection;
import net.imagej.updater.URLChange;
import net.imagej.updater.UpdateSite;
import net.imagej.updater.util.AvailableSites;
import net.imagej.updater.util.HTTPSUtil;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.awt.event.ActionEvent;
import java.util.*;

import static org.junit.Assert.*;
import static org.junit.Assume.assumeFalse;
import static org.junit.Assume.assumeTrue;

public class ReviewSiteURLsDialogTest {

	@Rule
	public TemporaryFolder root = new TemporaryFolder();

	@Test
	public void deactivatedSitesTest() {

		// initialize files collection
		FilesCollection files = new FilesCollection(root.getRoot());

		// add an official update site
		UpdateSite siteA = createOfficialSite("a", "http://sites.imagej.net/a/");
		files.addUpdateSite(siteA);

		// this will propose a change to the URL
		Optional< URLChange > urlChange =
				URLChange.create(siteA, "http://sites.imagej.net/b/");
		assertTrue(urlChange.isPresent());
		assertEquals("http://sites.imagej.net/b/", urlChange.get().getNewURL());

		// the change should be accepted without showing a user interface since the site is deactivated
		List< URLChange > changes = Collections.singletonList(urlChange.get());
		assertFalse(ReviewSiteURLsDialog.shouldBeDisplayed(changes));

		// apply the change to the URL
		AvailableSites.applySitesURLUpdates(files, changes);

		// test if the change to the URL was applied
		assertEquals("http://sites.imagej.net/b/", siteA.getURL());
	}

	@Test
	public void cancelBtnTest() {

		// initialize files collection
		FilesCollection files = new FilesCollection(root.getRoot());

		// add an official update site and activate it
		UpdateSite siteA = createOfficialSite("a", "http://sites.imagej.net/a/");
		siteA.setActive(true);
		files.addUpdateSite(siteA);

		// propose a change to the site URL
		Optional< URLChange > change =
				URLChange.create(siteA, "http://sites.imagej.net/b/");

		// this change should be approved by the user since the update site is active
		List< URLChange > changes = Collections.singletonList(change.get());
		assertTrue(ReviewSiteURLsDialog.shouldBeDisplayed(changes));

		// create review dialog
		ReviewSiteURLsDialog
				dialog = new ReviewSiteURLsDialog(null, changes);

		// trigger cancel button
		dialog.actionPerformed(createActionEvent(dialog.cancel));

		// apply changes to update site URLs
		AvailableSites.applySitesURLUpdates(files, changes);

		// test if change to URL got reverted
		assertEquals("http://sites.imagej.net/a/", siteA.getURL());
	}

	@Test
	public void submitBtnTest() {

		// initialize files collection
		FilesCollection files = new FilesCollection(root.getRoot());

		// add an official update site and activate it
		UpdateSite siteA = createOfficialSite("a", "http://sites.imagej.net/a/");
		siteA.setActive(true);
		files.addUpdateSite(siteA);

		// propose a change to the site URL
		Optional< URLChange > urlChange =
				URLChange.create(siteA, "http://sites.imagej.net/b/");
		List< URLChange > urlChanges = Collections.singletonList(urlChange.get());

		// create review dialog
		ReviewSiteURLsDialog
				dialog = new ReviewSiteURLsDialog(null, urlChanges);

		// trigger submit button
		dialog.actionPerformed(createActionEvent(dialog.submit));

		// apply changes to update site URLs
		AvailableSites.applySitesURLUpdates(files, urlChanges);

		// test if change to URL got applied
		assertEquals("http://sites.imagej.net/b/", siteA.getURL());
	}

	@Test
	public void keepAllTest() {

		// initialize files collection
		FilesCollection files = new FilesCollection(root.getRoot());

		// add an official update site and activate it
		UpdateSite siteA = createOfficialSite("a", "http://sites.imagej.net/a/");
		siteA.setActive(true);
		files.addUpdateSite(siteA);

		// add automated change to the URL
		URLChange change = URLChange.create(siteA, "http://newdomain.net/a/").get();
		List< URLChange > changes = Collections.singletonList(change);

		// since the update site is active, the change needs to be approved, therefore the dialog should be displayed
		assertTrue(change.isRecommended());
		assertTrue(ReviewSiteURLsDialog.shouldBeDisplayed(changes));

		// create review dialog
		ReviewSiteURLsDialog dialog = new ReviewSiteURLsDialog(null, changes);

		// trigger action to mark all changes to URLs as disapproved
		dialog.actionPerformed(createActionEvent(dialog.keepAll));

		// test if site lost URL update approval
		assertFalse(change.isApproved());

		// test if option to remember choice is activated
		assertFalse(dialog.stopAsking.isSelected());

		// trigger submit action
		dialog.actionPerformed(createActionEvent(dialog.submit));

		// apply changes
		AvailableSites.applySitesURLUpdates(files, changes);

		// test whether the old update site URL is still in use
		assertEquals("http://sites.imagej.net/a/", siteA.getURL());
		assertFalse(siteA.shouldKeepURL());
	}

	@Test
	public void rememberKeptChoicesTest() {

		// initialize files collection
		FilesCollection files = new FilesCollection(root.getRoot());

		// add an official update site and activate it
		UpdateSite siteA = createOfficialSite("a", "http://sites.imagej.net/a/");
		siteA.setActive(true);
		files.addUpdateSite(siteA);

		// add automated change to the URL
		URLChange change = URLChange.create(siteA, "http://newdomain.net/a/").get();
		List< URLChange > changes = Collections.singletonList(change);

		// create review dialog
		ReviewSiteURLsDialog dialog = new ReviewSiteURLsDialog(null, changes);

		// trigger action to mark all changes to URLs as disapproved
		dialog.actionPerformed(createActionEvent(dialog.keepAll));

		// trigger toggle to remember choice
		dialog.stopAsking.setSelected(true);

		// trigger submit action
		dialog.actionPerformed(createActionEvent(dialog.submit));

		// apply changes
		AvailableSites.applySitesURLUpdates(files,changes);

		// test whether the old update site URL is still in use
		assertEquals("http://sites.imagej.net/a/", siteA.getURL());
		assertTrue(siteA.shouldKeepURL());

		// again add automated change to URL
		URLChange change2 = URLChange.create(siteA, "http://newdomain.net/a/").get();
		// test whether the choice of keeping the old URL is remembered
		assertFalse(change2.isRecommended());
		assertFalse(change2.isApproved());
		assertFalse(ReviewSiteURLsDialog.shouldBeDisplayed(Collections.singletonList(change2)));
	}

	@Test
	public void httpsUpgradeTest() {

		// check if HTTPS is supported, if not, ignore test
		HTTPSUtil.checkHTTPSSupport(null);
		assumeTrue(HTTPSUtil.supportsHTTPS());

		// initialize files collection
		FilesCollection files = new FilesCollection(root.getRoot());

		// add an official update site
		UpdateSite siteA = createOfficialSite("a", "http://sites.imagej.net/a/");
		URLChange changeInactiveSite =
				URLChange.create(siteA, "https://sites.imagej.net/a/").get();

		// the change should be accepted without showing a user interface since the site is deactivated
		assertFalse(ReviewSiteURLsDialog.shouldBeDisplayed(Collections.singletonList(changeInactiveSite)));

		// add another official update site, activate it
		UpdateSite siteB = createOfficialSite("b", "http://sites.imagej.net/b/");
		siteB.setActive(true);
		URLChange changeActiveSite =
				URLChange.create(siteB, "https://sites.imagej.net/b/").get();

		// the change should be accepted after showing a user interface since one site is activated
		List<URLChange> changes = Arrays.asList(changeActiveSite, changeInactiveSite);
		assertTrue(ReviewSiteURLsDialog.shouldBeDisplayed(Collections.singletonList(changeActiveSite)));

		// create review dialog
		ReviewSiteURLsDialog dialog = new ReviewSiteURLsDialog(null, changes);
		assertEquals(2, dialog.urlChanges.size());

		// apply the change to the URL
		AvailableSites.applySitesURLUpdates(files, changes);

		// test if the change to the URL was applied
		assertEquals("https://sites.imagej.net/a/", siteA.getURL());
		assertEquals("https://sites.imagej.net/b/", siteB.getURL());
		assertFalse(siteA.shouldKeepURL());
		assertFalse(siteB.shouldKeepURL());
	}

	@Test
	public void httpDowngradeTest() {

		// check if HTTPS is not supported, otherwise ignore test
		HTTPSUtil.checkHTTPSSupport(null);
		assumeFalse(HTTPSUtil.supportsHTTPS());

		// initialize files collection
		FilesCollection files = new FilesCollection(root.getRoot());

		// add an official update site
		UpdateSite siteA = createOfficialSite("a", "https://sites.imagej.net/a/");
		files.addUpdateSite(siteA);

		// create list to hold changes to update site URLs
		List<URLChange> urlChanges = new ArrayList<>();

		// check if update site URL has to be changed
		Optional< URLChange > changeA = URLChange.create(siteA,
				HTTPSUtil.fixImageJUserSiteProtocol(siteA.getURL()));
		changeA.ifPresent( urlChanges::add );

		// the change should be accepted without showing a user interface since the site is deactivated
		assertFalse(ReviewSiteURLsDialog.shouldBeDisplayed(urlChanges));

		// add another official update site, activate it
		UpdateSite siteB = createOfficialSite("b", "https://sites.imagej.net/b/");
		siteB.setActive(true);
		files.addUpdateSite(siteB);

		// check if update site URL has to be changed
		Optional< URLChange > changeB = URLChange.create(siteB,
				HTTPSUtil.fixImageJUserSiteProtocol(siteB.getURL()));
		changeB.ifPresent( urlChanges::add );

		// the change should be accepted after showing a user interface since one site is activated
		assertTrue(ReviewSiteURLsDialog.shouldBeDisplayed(urlChanges));

		// create review dialog
		ReviewSiteURLsDialog dialog = new ReviewSiteURLsDialog(null, urlChanges);
		assertEquals(2, dialog.urlChanges.size());

		// apply the change to the URL
		AvailableSites.applySitesURLUpdates(files, urlChanges);

		// test if the change to the URL was applied
		assertEquals("http://sites.imagej.net/a/", siteA.getURL());
		assertEquals("http://sites.imagej.net/b/", siteB.getURL());
		assertFalse(siteA.shouldKeepURL());
		assertFalse(siteB.shouldKeepURL());
	}

	private ActionEvent createActionEvent(Object source) {
		return new ActionEvent(source, 0, "");
	}

	private UpdateSite createOfficialSite(String name, String url) {
		UpdateSite site = new UpdateSite(name, url, "", "", "", "", 0);
		site.setOfficial(true);
		return site;
	}

}
