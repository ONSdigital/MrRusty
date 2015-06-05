package com.github.onsdigital.test.browser;

import com.github.davidcarboni.cryptolite.Random;
import com.github.onsdigital.junit.DependsOn;
import com.github.onsdigital.test.SetupBeforeTesting;
import com.github.onsdigital.test.browser.PageObjects.BrowsePage;
import com.github.onsdigital.test.browser.PageObjects.CollectionsPage;
import com.github.onsdigital.test.browser.PageObjects.LoginPage;
import com.github.onsdigital.zebedee.json.Credentials;
import org.junit.Test;

import javax.ws.rs.POST;
import java.io.IOException;

@DependsOn({Login.class})
public class Collections {

    Credentials publisher = SetupBeforeTesting.publisherCredentials;

    @POST
    @Test
    public void shouldAddNewScheduledCollection() throws IOException {

        // Given the collections page after logging in
        CollectionsPage collectionsPage = new LoginPage().login(publisher.email, publisher.password);

        // When the create collection form is filled in and submitted.
        collectionsPage.typeCollectionName(Random.id())
        .selectTeamByIndex(1)
        .selectScheduledPublish()
        .selectDayByIndex(1)
        .selectMonthByIndex(1)
        .selectYear(2015);

        // Then the collection is created and the browse screen is shown
        collectionsPage.clickCreateCollection();
    }

    @POST
    @Test
    public void shouldAddNewManualCollection() throws IOException {

        // Given the collections page after logging in
        CollectionsPage collectionsPage = new LoginPage().login(publisher.email, publisher.password);

        // When the create collection form is filled in and submitted.
        collectionsPage.typeCollectionName(Random.id())
                .selectTeamByIndex(1)
                .selectManualPublish();

        // Then the collection is created and the browse screen is shown
        collectionsPage.clickCreateCollection();
    }

    @POST
    @Test
    public void shouldDisplayErrorWhenCollectionNameExists() throws IOException {

        // Given the collections page after creating a new collection.
        CollectionsPage collectionsPage = new LoginPage().login(publisher.email, publisher.password);
        String collectionId = Random.id();
        collectionsPage.populateFormWithDefaults(collectionId);
        BrowsePage browsePage = collectionsPage.clickCreateCollection();
        collectionsPage = browsePage.clickCollectionsMenuLink();

        // When the create collection form is filled in and submitted
        // using an existing collection name.
        collectionsPage.populateFormWithDefaults(collectionId);

        // Then the collection is not created and the user stays on the collections page.
        collectionsPage.clickCreateCollectionExpectingError();
    }
}
