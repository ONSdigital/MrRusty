package com.github.onsdigital.test.browser;

import com.github.onsdigital.junit.DependsOn;
import com.github.onsdigital.test.SetupBeforeTesting;
import com.github.onsdigital.test.browser.PageObjects.CollectionsPage;
import com.github.onsdigital.test.browser.PageObjects.LoginPage;
import com.github.onsdigital.zebedee.json.Credentials;
import org.junit.Test;

import javax.ws.rs.POST;
import java.io.IOException;

@DependsOn({Login.class})
public class Collections {

    Credentials publisher = SetupBeforeTesting.scallywagCredentials;


    @POST
    @Test
    public void shouldAddNewCollection() throws IOException {

        CollectionsPage collectionsPage = new LoginPage().login(publisher.email, publisher.password);


    }
}
