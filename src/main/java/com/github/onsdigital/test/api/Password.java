package com.github.onsdigital.test.api;

import com.github.davidcarboni.cryptolite.Random;
import com.github.davidcarboni.restolino.framework.Api;
import com.github.onsdigital.http.Http;
import com.github.onsdigital.http.Response;
import com.github.onsdigital.junit.DependsOn;
import com.github.onsdigital.test.SetupBeforeTesting;
import com.github.onsdigital.test.api.oneliners.OneLineSetups;
import com.github.onsdigital.test.json.Credentials;
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
public class Password {

    private User user;
    private Credentials credentials;
    Http http = Login.httpAdministrator;

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
        OneLineSetups.newSessionWithPublisherPermissions("Rusty", credentials.email, Random.password(8));

        // When
        // We attempt to create a new password
        Response<String> response = Login.httpAdministrator.post(ZebedeeHost.password, credentials, String.class);

        // Then
        // The request should succeed
        // the new password should be temporary and lastAdmin should be recorded
        assertEquals(HttpStatus.OK_200, response.statusLine.getStatusCode());

        User updated = Users.get(credentials.email, Login.httpPublisher).body;
        assertEquals(Boolean.TRUE, updated.temporaryPassword);

        String email = SetupBeforeTesting.adminCredentials.email;
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
        User self = SetupBeforeTesting.user("Rusty", "Rusty_" + Random.id() + "@example.com");
        String password = Random.password(8);
        Http httpSelf = OneLineSetups.newSessionWithPublisherPermissions(self.name, self.email, password);

        Credentials updateCredentials = SetupBeforeTesting.credentials(self.email, Random.password(8));
        updateCredentials.oldPassword = password;

        // When
        // We attempt to create a duplicate user
        Response<String> response = httpSelf.post(ZebedeeHost.password, updateCredentials, String.class);

        // Then
        // The request should succeed
        // the new password should be temporary and lastAdmin should be recorded
        assertEquals(HttpStatus.OK_200, response.statusLine.getStatusCode());

        User updated = Users.get(self.email, Login.httpPublisher).body;
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
        changePasswordCredentials.email = SetupBeforeTesting.adminUser.email;
        changePasswordCredentials.password = credentials.password;
        changePasswordCredentials.oldPassword = credentials.password;
        Response<String> response1 = Login.httpViewer.post(ZebedeeHost.password, changePasswordCredentials, String.class);
        Response<String> response3 = Login.httpScallywag.post(ZebedeeHost.password, changePasswordCredentials, String.class);

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
        changePasswordCredentials.email = SetupBeforeTesting.publisherCredentials.email;
        changePasswordCredentials.password = "wrong password";
        changePasswordCredentials.oldPassword = "new password";
        Response<String> response1 = Login.httpPublisher.post(ZebedeeHost.password, changePasswordCredentials, String.class);

        // Then
        // Each request should fail
        assertEquals(HttpStatus.UNAUTHORIZED_401, response1.statusLine.getStatusCode());
    }

}
