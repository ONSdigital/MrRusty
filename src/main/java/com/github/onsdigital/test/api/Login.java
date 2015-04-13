package com.github.onsdigital.test.api;

import com.github.onsdigital.http.Http;
import com.github.onsdigital.http.Response;
import com.github.onsdigital.http.Sessions;
import com.github.onsdigital.junit.DependsOn;
import com.github.onsdigital.zebedee.json.Credentials;
import org.apache.commons.lang3.StringUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * API tests for /login
 */
@DependsOn({})
public class Login {

    private static Http http;
    private static String token;

    /**
     * Gets a reference to the admin {@link Http} instance for use in these tests.
     */
    @BeforeClass
    public static void getHttp() {
        http = Sessions.get("admin");
    }

    /**
     * Saves the token from the admin login to the {@link Http} instance so that it can be used in other tests.
     */
    @AfterClass
    public static void setToken() {
        http.addHeader("x-florence-token", token);
    }

    /**
     * Tests login using the default administrator credentials.
     *
     * @throws IOException
     */
    @Test
    public void shouldLogInAsAdmin() throws IOException {

        // Given
        // Correct admin credentials
        Credentials credentials = credentials("florence@magicroundabout.ons.gov.uk", "Doug4l");

        // When
        // We attempt to log in
        Response<String> response = http.post(ZebedeeHost.login, credentials, String.class);

        // Then
        // The request should succeed
        assertEquals(response.statusLine.getStatusCode(), 200);
        assertTrue(StringUtils.isNotBlank(response.body));
        token = response.body;
    }

    /**
     * Tests login without an email address.
     *
     * @throws IOException
     */
    @Test
    public void shouldReturnBadRequestForNoEmailAddress() throws IOException {

        // Given
        // A missing email address in the credentials
        Credentials credentials = credentials(null, "Doug4l");

        // When
        // We attempt to log in
        Response<String> response = http.post(ZebedeeHost.login, credentials, String.class);

        // Then
        // We should get a bad request response
        assertEquals(response.statusLine.getStatusCode(), 400);
        assertTrue(StringUtils.isBlank(response.body));
    }

    /**
     * Tests login without an email address.
     *
     * @throws IOException
     */
    @Test
    public void shouldReturnUnauthorizedForIncorrectPassword() throws IOException {

        // Given
        // A missing email address in the credentials
        Credentials credentials = credentials(null, "incorrect");

        // When
        // We attempt to log in
        Response<String> response = http.post(ZebedeeHost.login, credentials, String.class);

        // Then
        // We should get an unauthorised response
        assertEquals(response.statusLine.getStatusCode(), 401);
        assertTrue(StringUtils.isBlank(response.body));
    }

    /**
     * Convenience method for generating login credentials.
     *
     * @param email
     * @param password
     * @return
     */
    private Credentials credentials(String email, String password) {
        Credentials credentials = new Credentials();
        credentials.email = email;
        credentials.password = password;
        return credentials;
    }

}