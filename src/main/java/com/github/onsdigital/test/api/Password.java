package com.github.onsdigital.test.api;

import com.github.davidcarboni.cryptolite.Random;
import com.github.davidcarboni.restolino.framework.Api;
import com.github.onsdigital.http.Http;
import com.github.onsdigital.http.Response;
import com.github.onsdigital.http.Sessions;
import com.github.onsdigital.junit.DependsOn;
import com.github.onsdigital.test.Context;
import com.github.onsdigital.test.base.ZebedeeApiTest;
import com.github.onsdigital.test.json.Credentials;
import com.github.onsdigital.test.json.PermissionDefinition;
import com.github.onsdigital.test.json.User;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.POST;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * Created by kanemorgan on 02/04/2015.
 */

@Api
@DependsOn(Users.class)
public class Password extends ZebedeeApiTest {

    private User user;
    private Credentials credentials;

    @Before
    public void setUp() throws Exception {
        user = Users.createRandomTestUser();
        credentials = new Credentials();
        credentials.email = user.email;
        credentials.password = Random.password(8);
    }

    /**
     * Ensures we get a 200 OK when changing passwords for a user.
     *
     * @throws IOException
     */
    @POST
    @Test
    public void setPassword_givenAdminSession_shouldSetTemporaryPassword() throws IOException {

        // Given
        // A user with credentials
        newSessionWithPublisherPermissions(context, "Rusty", credentials.email, Random.password(8));

        // When
        // We attempt to create a new password
        Response<String> response = context.getAdministrator().post(ZebedeeHost.password, credentials, String.class);

        // Then
        // The request should succeed
        // the new password should be temporary and lastAdmin should be recorded
        assertEquals(HttpStatus.OK_200, response.statusLine.getStatusCode());

        User updated = Users.get(credentials.email, context.getPublisher()).body;
        assertEquals(Boolean.TRUE, updated.temporaryPassword);

        String email = Context.adminCredentials.email;
        assertEquals(email, updated.lastAdmin);
    }

    /**
     * Ensures we get a 200 OK when creating a valid user.
     *
     * @throws IOException
     */
    @POST
    @Test
    public void setPassword_givenSelfSession_shouldSetPermanentPassword() throws IOException {

        // Given
        // A user with credentials
        User self = Context.user("Rusty", "Rusty_" + Random.id() + "@example.com");
        String password = Random.password(8);
        Http httpSelf = newSessionWithPublisherPermissions(context, self.name, self.email, password);

        Credentials updateCredentials = Context.credentials(self.email, Random.password(8));
        updateCredentials.oldPassword = password;

        // When
        // We attempt to create a duplicate user
        Response<String> response = httpSelf.post(ZebedeeHost.password, updateCredentials, String.class);

        // Then
        // The request should succeed
        // the new password should be temporary and lastAdmin should be recorded
        assertEquals(HttpStatus.OK_200, response.statusLine.getStatusCode());

        User updated = Users.get(self.email, context.getPublisher()).body;
        assertEquals(Boolean.FALSE, updated.temporaryPassword);

        assertEquals(self.email.toLowerCase(), updated.lastAdmin.toLowerCase());
    }


    /**
     * Ensures we get a 200 OK when creating a valid user.
     *
     * @throws IOException
     */
    @POST
    @Test
    public void shouldReturnUnauthorizedIfSessionIsNotAdmin() throws IOException {

        // Given
        // A set of user credentials

        // When
        // We attempt to create a duplicate user
        Credentials changePasswordCredentials = new Credentials();
        changePasswordCredentials.email = Context.adminUser.email;
        changePasswordCredentials.password = credentials.password;
        changePasswordCredentials.oldPassword = credentials.password;
        Response<String> response1 = context.getViewer().post(ZebedeeHost.password, changePasswordCredentials, String.class);
        Response<String> response3 = context.getScallyWag().post(ZebedeeHost.password, changePasswordCredentials, String.class);

        // Then
        // Each request should fail
        assertEquals(HttpStatus.UNAUTHORIZED_401, response1.statusLine.getStatusCode());
        assertEquals(HttpStatus.UNAUTHORIZED_401, response3.statusLine.getStatusCode());
    }

    @POST
    @Test
    public void shouldReturnUnauthorizedIfExistingPasswordIsIncorrect() throws IOException {

        // Given
        // A set of user credentials

        // When
        // We attempt to create a duplicate user
        Credentials changePasswordCredentials = new Credentials();
        changePasswordCredentials.email = Context.publisherCredentials.email;
        changePasswordCredentials.password = "wrong password";
        changePasswordCredentials.oldPassword = "new password";
        Response<String> response1 = context.getPublisher().post(ZebedeeHost.password, changePasswordCredentials, String.class);

        // Then
        // Each request should fail
        assertEquals(HttpStatus.UNAUTHORIZED_401, response1.statusLine.getStatusCode());
    }

    private static Http newSessionWithPublisherPermissions(Context context, String name, String email, String password) throws IOException {
        // Given
        // A user with no email address
        User user = new User();
        user.email = email;
        user.name = name;

        // Post the user
        Response<User> response = context.getAdministrator().post(ZebedeeHost.users, user, User.class);
        assertEquals(HttpStatus.OK_200, response.statusLine.getStatusCode());

        // Set their password
        Credentials credentials = new Credentials();
        credentials.email = user.email;
        credentials.password = password;
        Response<String> responsePassword = context.getAdministrator().post(ZebedeeHost.password, credentials, String.class);
        assertEquals(HttpStatus.OK_200, responsePassword.statusLine.getStatusCode());

        // Assign them publisher permissions
        PermissionDefinition definition = new PermissionDefinition();
        definition.email = user.email;
        definition.editor = true;
        Response<String> permissionResponse = context.getAdministrator().post(ZebedeeHost.permission, definition, String.class);
        assertEquals(HttpStatus.OK_200, permissionResponse.statusLine.getStatusCode());

        // Change the password as the user to remove the temporary password restrictions.
        Credentials changePasswordCredentials = new Credentials();
        changePasswordCredentials.email = credentials.email;
        changePasswordCredentials.password = credentials.password;
        changePasswordCredentials.oldPassword = credentials.password;
        Response<String> login = new Http().post(ZebedeeHost.password, changePasswordCredentials, String.class);
        assertEquals(HttpStatus.OK_200, login.statusLine.getStatusCode());

        // Create a session
        Http http = Sessions.get(email);

        // Log the user in
        Response<String> responseLogin = http.post(ZebedeeHost.login, credentials, String.class);
        assertEquals(HttpStatus.OK_200, responseLogin.statusLine.getStatusCode());
        String token = responseLogin.body;

        // Add their session token
        http.addHeader("x-florence-token", token);

        return http;
    }
}
