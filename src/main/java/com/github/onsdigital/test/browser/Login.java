package com.github.onsdigital.test.browser;

import com.github.onsdigital.junit.DependsOn;
import com.github.onsdigital.test.SetupBeforeTesting;
import com.github.onsdigital.test.browser.PageObjects.CollectionsPage;
import com.github.onsdigital.test.browser.PageObjects.LoginPage;
import com.github.onsdigital.test.json.Credentials;
import org.junit.Test;

import javax.ws.rs.POST;
import java.io.IOException;

// depend on the last API tests
@DependsOn(com.github.onsdigital.test.api.Publish.class)
public class Login {

    /**
     * Tests login using the administrator credentials.
     *
     * @throws IOException
     */
    @POST
    @Test
    public void shouldLogInAsAdmin() throws IOException {

        // Given correct admin credentials
        Credentials credentials = SetupBeforeTesting.adminCredentials;
        LoginPage loginPage = new LoginPage();

        // When We submit the credentials on the login page.
        // Then we are shown the collections page.
        loginPage.login(credentials.email, credentials.password);
    }

    /**
     * Tests login using the publisher credentials.
     *
     * @throws IOException
     */
    @POST
    @Test
    public void shouldLogInAsPublisher() throws IOException {

        // Given correct admin credentials
        Credentials credentials = SetupBeforeTesting.publisherCredentials;
        LoginPage loginPage = new LoginPage();

        // When We submit the credentials on the login page.
        // Then we are shown the collections page.
        loginPage.login(credentials.email, credentials.password);
    }

    /**
     * Tests logout after logging in.
     *
     * @throws IOException
     */
    @POST
    @Test
    public void shouldLogout() throws IOException {

        // Given a user is logged in with correct credentials
        Credentials credentials = SetupBeforeTesting.publisherCredentials;
        LoginPage loginPage = new LoginPage();

        CollectionsPage page = loginPage.login(credentials.email, credentials.password);

        // When We logout
        // Then we are shown the login page.
        page.clickLogoutMenuLink();
    }
}
