package com.github.onsdigital.test.browser;

import com.github.davidcarboni.cryptolite.Random;
import com.github.onsdigital.junit.DependsOn;
import com.github.onsdigital.test.SetupBeforeTesting;
import com.github.onsdigital.test.api.Collection;
import com.github.onsdigital.test.browser.PageObjects.*;
import com.github.onsdigital.zebedee.json.Credentials;
import org.junit.Test;

import javax.ws.rs.POST;
import java.io.IOException;

@DependsOn({Collection.class})
public class Create {

    Credentials publisher = SetupBeforeTesting.publisherCredentials;

    @POST
    @Test
    public void shouldCreatePage() throws IOException {

        String collectionName = Random.id().substring(0, 5);
        CollectionsPage collectionsPage = new LoginPage().login(publisher.email, publisher.password);

        CreatePage createPage = collectionsPage.createCollection(collectionName)
                .clickBrowseTreeItem("/")
                .clickBrowseTreeItem("/economy")
                .clickBrowseTreeItem("/economy/nationalaccounts")
                .clickCreateMenuItem();

        EditPage editPage = createPage.selectPageType(ContentType.article)
                .typePageName(Random.id().substring(0, 5))
                .clickCreatePage();

        System.out.println(createPage.previewSource());

        //assertTrue(createPage.previewSource().contains(sectionTitle));

//        editPage.clickCollectionsMenuLink()
//                .clickCollectionForName(collectionName)
//        .clickDelete;

        // Then the collection is created and the browse screen is shown
        //collectionsPage.clickCreateCollection();

    }
}
