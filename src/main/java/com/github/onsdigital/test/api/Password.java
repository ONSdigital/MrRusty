package com.github.onsdigital.test.api;

import com.github.davidcarboni.cryptolite.Random;
import com.github.davidcarboni.restolino.framework.Api;
import com.github.onsdigital.http.Http;
import com.github.onsdigital.http.Response;
import com.github.onsdigital.junit.DependsOn;
import com.github.onsdigital.zebedee.json.Credentials;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.BeforeClass;
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

    static Credentials credentials;

    Http http = Login.httpAdministrator;

    /**
     * Set up a user's credentials for the test suite.
     */
    @BeforeClass
    public static void setupUserCredentials() {
        credentials = new Credentials();
        credentials.email = Users.user.email;
        credentials.password = Random.password(8);
    }

    /**
     * Ensures we get a 200 OK when creating a valid user.
     *
     * @throws IOException
     */
    @POST
    @Test
    public void shouldSetPasswordIfSessionIsAdmin() throws IOException {

        // Given
        // A user with credentials

        // When
        // We attempt to create a duplicate user
        Response<String> response = Login.httpAdministrator.post(ZebedeeHost.password, credentials, String.class);

        // Then
        // The request should succeed
        assertEquals(HttpStatus.OK_200, response.statusLine.getStatusCode());

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
        Response<String> response1 = Login.httpViewer.post(ZebedeeHost.password, credentials, String.class);
        Response<String> response2 = Login.httpPublisher.post(ZebedeeHost.password, credentials, String.class);
        Response<String> response3 = Login.httpScallywag.post(ZebedeeHost.password, credentials, String.class);

        // Then
        // Each request should fail
        assertEquals(HttpStatus.UNAUTHORIZED_401, response1.statusLine.getStatusCode());
        assertEquals(HttpStatus.UNAUTHORIZED_401, response2.statusLine.getStatusCode());
        assertEquals(HttpStatus.UNAUTHORIZED_401, response3.statusLine.getStatusCode());
    }

}
