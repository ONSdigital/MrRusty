package com.github.onsdigital.test.browser;

import com.github.onsdigital.junit.DependsOn;
import com.github.onsdigital.test.Context;
import com.github.onsdigital.test.base.FlorenceBrowserTest;
import com.github.onsdigital.test.browser.PageObjects.CollectionsPage;
import com.github.onsdigital.test.browser.PageObjects.LoginPage;
import com.github.onsdigital.test.browser.PageObjects.UsersPage;
import com.github.onsdigital.test.browser.model.User;
import com.github.onsdigital.test.json.Credentials;
import org.junit.Test;

import javax.ws.rs.POST;
import java.io.IOException;

@DependsOn({Login.class})
public class Users extends FlorenceBrowserTest {

    Credentials publisher = Context.systemCredentials;

    @POST
    @Test
    public void shouldAddNewUsers() throws IOException {

        // Given the collections page after logging in
        CollectionsPage collectionsPage = new LoginPage().login(publisher.email, publisher.password);

        // Move to users page
        UsersPage usersPage = collectionsPage.clickUsersMenuLink();

        User publisher1 = new User();
        usersPage.CreatePublisherUser(publisher1.name, publisher1.email, publisher1.password);

        User publisher2 = new User();
        usersPage.CreatePublisherUser(publisher2.name, publisher2.email, publisher2.password);

        User viewer1 = new User();
        usersPage.CreateViewerUser(viewer1.name, viewer1.email, viewer1.password);

        User viewer2 = new User();
        usersPage.CreateViewerUser(viewer2.name, viewer2.email, viewer2.password);

    }

    @POST
    @Test
    public void shouldLogoutAndLoginAsNewUser() throws IOException {
        // Given the collections page after logging in
        CollectionsPage collectionPage = new LoginPage().login(publisher.email, publisher.password);

        // Create a user
        User user1 = new User();
        UsersPage usersPage = collectionPage.clickUsersMenuLink();
        usersPage.CreatePublisherUser(user1.name, user1.email, user1.password);

        usersPage.clickLogoutMenuLink();

        // logging in with new user
        LoginPage loginPage = new LoginPage().loginLogin(user1.email.toLowerCase(), user1.password);
        loginPage.changePassword(user1.password, "set up new password");
    }
}
