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
    public void shouldAddNewCollection() throws IOException {

        // Given the collections page after logging in
        CollectionsPage collectionsPage = new LoginPage().login(publisher.email, publisher.password);

        // When the create collection form is filled in and submitted.
        collectionsPage.populateFormWithDefaults();

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
