package com.github.onsdigital.test.api;

import com.github.davidcarboni.cryptolite.Random;
import com.github.davidcarboni.restolino.framework.Api;
import com.github.onsdigital.http.Http;
import com.github.onsdigital.http.Response;
import com.github.onsdigital.junit.DependsOn;
import com.github.onsdigital.zebedee.json.Credentials;
import org.junit.AfterClass;
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
     * Log in as the user so that other tests can use the session.
     */
    @AfterClass
    public static void userLogin() throws IOException {

//        Http http = Sessions.get("user");
//        Response<String> response = http.post(ZebedeeHost.login, credentials, String.class);
//        if (response.statusLine.getStatusCode() != 200) {
//            throw new RuntimeException("Seems we can't log in as a normal user?");
//        }
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
        // The user credentials

        // When
        // We attempt to create a duplicate user
        Response<String> response = http.post(ZebedeeHost.password, credentials, String.class);

        // Then
        // The request should succeed
        assertEquals(response.body, 200, response.statusLine.getStatusCode());
    }

}
