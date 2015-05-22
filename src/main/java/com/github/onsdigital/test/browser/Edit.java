package com.github.onsdigital.test.browser;

import com.github.davidcarboni.cryptolite.Random;
import com.github.onsdigital.junit.DependsOn;
import com.github.onsdigital.test.SetupBeforeTesting;
import com.github.onsdigital.test.api.Collection;
import com.github.onsdigital.test.browser.PageObjects.CollectionsPage;
import com.github.onsdigital.test.browser.PageObjects.LoginPage;
import com.github.onsdigital.zebedee.json.Credentials;
import org.junit.Test;
import org.openqa.selenium.By;

import javax.ws.rs.POST;
import java.io.IOException;

import static junit.framework.TestCase.assertTrue;

@DependsOn({Collection.class})
public class Edit {

    Credentials publisher = SetupBeforeTesting.publisherCredentials;

    @POST
    @Test
    public void shouldEditContent() throws IOException {

        String collectionName = Random.id().substring(0, 5);
        CollectionsPage collectionsPage = new LoginPage().login(publisher.email, publisher.password);

        try {
            // Given the collections page after logging in
            com.github.onsdigital.test.browser.PageObjects.EditPage editPage = collectionsPage.createCollection(collectionName)
                    .clickPreviewLink(By.cssSelector("[ons-nav-item='/economy'] a"))
                    .clickPreviewLink(By.cssSelector("[ons-nav-item='/economy/nationalaccounts'] a"))
                    .clickPreviewLink(By.cssSelector("#statsBulletinHeadlines a"))
                    .clickEdit();

            // When we change the content of the page
            String sectionTitle = Random.id().substring(0,5);
            editPage.openContentAccordion().setSectionTitle(sectionTitle);
            editPage.clickSave();


            System.out.println(editPage.previewSource());

            assertTrue(editPage.previewSource().contains(sectionTitle));

//        editPage.clickCollectionsMenuLink()
//                .clickCollectionForName(collectionName)
//        .clickDelete;

            // Then the collection is created and the browse screen is shown
            //collectionsPage.clickCreateCollection();
        }
        finally {
            collectionsPage.runScript("deleteCollection(" + collectionName + ")");
        }


    }
}
