package com.github.onsdigital.test.browser;

import com.github.onsdigital.test.api.Collection;
import com.github.onsdigital.test.browser.PageObjects.BrowsePage;
import com.github.onsdigital.test.browser.PageObjects.CollectionsPage;
import com.github.onsdigital.test.browser.PageObjects.LoginPage;
import com.github.onsdigital.test.json.Credentials;

/**
 * Created by thomasridd on 25/09/15.
 */
public class Utils {

    public static void downloadXLSX(Credentials publisher) {
        // login and create a collection
        String collectionName = Collection.createCollectionNameForTest();
        CollectionsPage collectionsPage = new LoginPage().login(publisher.email, publisher.password);
        BrowsePage browsePage = collectionsPage.createCollection(collectionName);
        browsePage.NavigateToT3Page();
    }
}
