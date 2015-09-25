package com.github.onsdigital.test.browser;

import com.github.davidcarboni.cryptolite.Random;
import com.github.onsdigital.junit.DependsOn;
import com.github.onsdigital.test.SetupBeforeTesting;
import com.github.onsdigital.test.api.Collection;
import com.github.onsdigital.test.browser.PageObjects.*;
import com.github.onsdigital.test.json.Credentials;
import org.junit.Test;

@DependsOn({Collections.class})
public class Publish {

    Credentials publisher = SetupBeforeTesting.publisherCredentials;

    @Test
    public void shouldPublishContent() throws InterruptedException {

        // login and create a collection
        String collectionName = Collection.createCollectionNameForTest();
        CollectionsPage collectionsPage = new LoginPage().login(publisher.email, publisher.password);
        BrowsePage browsePage = collectionsPage.createCollection(collectionName);

        // create a new page
        String pageName = Random.id().substring(0, 5);
        CreatePage createPage = browsePage.NavigateToT3Page().clickCreateForCurrentPage();
        EditPage editPage = createPage.createPage(ContentType.bulletin, pageName);

        // add a new section to the page and add markdown content.
        MarkdownEditorPage editorPage = editPage.addContentSection(Random.id().substring(0, 5)).clickEditPage();
        editPage = editorPage.typeContent("omg this is content!").clickSaveAndExit();

        // submit the page for review.
        collectionsPage = editPage.clickSubmitForReview();

        // login as the second set of eyes.
        collectionsPage.clickLogoutMenuLink().typeUsername("p2@t.com").clickLogin();

        // select the page to be reviewed.
        collectionsPage.clickCollectionByName(collectionName)
                .clickCollectionPageByName(pageName)
                .clickEditFile()
                .clickSubmitForApproval();

        // approve collection
        collectionsPage = collectionsPage.clickCollectionByName(collectionName)
                .clickApprove();


        // go to publish screen and select publish for the collection
        PublishingQueuePage publishingQueuePage = collectionsPage.clickPublishingQueueMenuLink();

        // select the page and unlock
        PublishingQueueDetailsPage publishingQueueDetailsPage = publishingQueuePage.selectManualPublish();
        publishingQueueDetailsPage.clickCollectionSection(collectionName);
        collectionsPage = publishingQueueDetailsPage.unlockCollectionWithName(collectionName);


        // select the page and publish
        collectionsPage = collectionsPage.clickCollectionByName(collectionName).clickApprove();
        publishingQueuePage = collectionsPage.clickPublishingQueueMenuLink();
        publishingQueueDetailsPage = publishingQueuePage.selectManualPublish();
        publishingQueueDetailsPage.clickCollectionSection(collectionName);
        publishingQueueDetailsPage.publishCollectionWithName(collectionName);
    }

    @Test
    public void shouldPublishScheduledCollection() {
        // login and create a collection
        String collectionName = Collection.createCollectionNameForTest();
        CollectionsPage collectionsPage = new LoginPage().login(publisher.email, publisher.password);
        BrowsePage browsePage = collectionsPage.createScheduledCollection(collectionName);

        // create a new page
        String pageName = Random.id().substring(0, 5);
        CreatePage createPage = browsePage.NavigateToT3Page().clickCreateForCurrentPage();
        EditPage editPage = createPage.createPage(ContentType.bulletin, pageName);

        // add a new section to the page and add markdown content.
        MarkdownEditorPage editorPage = editPage.addContentSection(Random.id().substring(0, 5)).clickEditPage();
        editPage = editorPage.typeContent("omg this is content!").clickSaveAndExit();

        // submit the page for review.
        collectionsPage = editPage.clickSubmitForReview();

        // login as the second set of eyes.
        collectionsPage.clickLogoutMenuLink().typeUsername("p2@t.com").clickLogin();

        // select the page to be reviewed.
        collectionsPage.clickCollectionByName(collectionName)
                .clickCollectionPageByName(pageName)
                .clickEditFile()
                .clickSubmitForApproval();

        // approve collection
        collectionsPage = collectionsPage.clickCollectionByName(collectionName)
                .clickApprove();


        // go to publish screen and select publish for the collection
        PublishingQueuePage publishingQueuePage = collectionsPage.clickPublishingQueueMenuLink();
        PublishingQueueDetailsPage publishingQueueDetailsPage = publishingQueuePage.selectPublishDateIn2078();
        publishingQueueDetailsPage.clickCollectionSection(collectionName);
        collectionsPage = publishingQueueDetailsPage.unlockCollectionWithName(collectionName);

        // select the page and publish
        collectionsPage = collectionsPage.clickCollectionByName(collectionName).clickApprove();
        publishingQueuePage = collectionsPage.clickPublishingQueueMenuLink();
        publishingQueueDetailsPage = publishingQueuePage.selectPublishDateIn2078();
        publishingQueueDetailsPage.clickCollectionSection(collectionName);
    }

}
