package com.github.onsdigital.test.api;

import com.github.davidcarboni.restolino.framework.Api;
import com.github.onsdigital.http.Response;
import com.github.onsdigital.junit.DependsOn;
import com.github.onsdigital.test.Context;
import com.github.onsdigital.test.base.ZebedeeApiTest;
import com.github.onsdigital.test.json.Credentials;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.Test;

import javax.ws.rs.POST;
import java.io.IOException;

import static com.github.onsdigital.test.AssertResponse.assertBodyNotEmpty;
import static com.github.onsdigital.test.AssertResponse.assertOk;
import static org.junit.Assert.*;

/**
 * Test cases for Login API
 */
@Api
@DependsOn({})
public class Login extends ZebedeeApiTest {

    /**
     * Tests login using the administrator credentials.
     *
     * @throws IOException
     */
    @POST
    @Test
    public void shouldLogInAsAdmin() throws IOException {

        // Given
        // Correct admin credentials
        Credentials credentials = Context.adminCredentials;

        // When
        // We attempt to log in
        Response<String> response = context.getAdministrator().post(ZebedeeHost.login, credentials, String.class);

        // Then
        // The request should succeed
        assertEquals(HttpStatus.OK_200, response.statusLine.getStatusCode());
        assertTrue(StringUtils.isNotBlank(response.body));
    }

    /**
     * Tests login using the publisher credentials.
     *
     * @throws IOException
     */
    @POST
    @Test
    public void shouldLogInAsPublisher() throws IOException {

        // When we attempt to log in as a publisher
        Response<String> response = context.getPublisher().post(ZebedeeHost.login, Context.publisherCredentials, String.class);

        // Then the request should succeed
        assertBodyNotEmpty(response);
        assertOk(response);
    }

    /**
     * Tests login using the publisher credentials.
     *
     * @throws IOException
     */
    @POST
    @Test
    public void shouldLogInAsViewer() throws IOException {

        // Given
        // Correct admin credentials
        Credentials credentials = Context.viewerCredentials;

        // When
        // We attempt to log in
        Response<String> response = context.getViewer().post(ZebedeeHost.login, credentials, String.class);

        // Then
        // The request should succeed
        assertBodyNotEmpty(response);
        assertOk(response);
    }

    /**
     * Tests login using the scallywag credentials - this should fail.
     *
     * @throws IOException
     */
    @POST
    @Test
    public void shouldNotLogInAsScallywag() throws IOException {

        // Given
        // Correct admin credentials
        Credentials credentials = Context.scallywagCredentials;

        // When
        // We attempt to log in
        Response<String> response = context.getScallyWag().post(ZebedeeHost.login, credentials, String.class);

        // Then
        // The request should succeed
        assertNotEquals(HttpStatus.OK_200, response.statusLine.getStatusCode());
    }

    /**
     * Tests login without an email address.
     *
     * @throws IOException
     */
    @POST
    @Test
    public void shouldReturnBadRequestForNoEmailAddress() throws IOException {

        // Given
        // A missing email address in the credentials
        Credentials credentials = credentials(null, Context.adminCredentials.password);

        // When
        // We attempt to log in
        Response<String> response = context.getAdministrator().post(ZebedeeHost.login, credentials, String.class);

        // Then
        // We should get a bad request response
        assertEquals(400, response.statusLine.getStatusCode());
    }

    /**
     * Tests login with an incorrect password.
     *
     * @throws IOException
     */
    @POST
    @Test
    public void shouldReturnUnauthorizedForIncorrectPassword() throws IOException {

        // Given
        // A missing email address in the credentials
        Credentials credentials = credentials(Context.adminCredentials.email, "incorrect");

        // When
        // We attempt to log in
        Response<String> response = context.getAdministrator().post(ZebedeeHost.login, credentials, String.class);

        // Then
        // We should get an unauthorised response
        assertEquals(401, response.statusLine.getStatusCode());
    }

    /**
     * Tests login with an incorrect password.
     *
     * @throws IOException
     */
    @POST
    @Test
    public void shouldReturnExpectationFailedForTemporaryPassword() throws IOException {

        // When
        // We attempt to log in
        Response<String> response = context.getAdministrator().post(ZebedeeHost.login, Context.newUserCredentials, String.class);

        // Then
        // We should get an unauthorised response
        assertEquals(417, response.statusLine.getStatusCode());
    }


    /**
     * Convenience method for generating login credentials.
     *
     * @param email    The email address
     * @param password The password
     * @return A {@link Credentials} instance containing the given details.
     */
    private static Credentials credentials(String email, String password) {
        return credentials(email, password, null);
    }

    /**
     * Convenience method for generating login credentials.
     *
     * @param email    The email address
     * @param password The password
     * @return A {@link Credentials} instance containing the given details.
     */
    private static Credentials credentials(String email, String password, Boolean temporaryPassword) {
        Credentials credentials = new Credentials();
        credentials.email = email;
        credentials.password = password;
        return credentials;
    }

}