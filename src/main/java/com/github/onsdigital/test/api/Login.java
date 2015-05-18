package com.github.onsdigital.test.api;

import com.github.davidcarboni.restolino.framework.Api;
import com.github.onsdigital.http.Http;
import com.github.onsdigital.http.Response;
import com.github.onsdigital.http.Sessions;
import com.github.onsdigital.junit.DependsOn;
import com.github.onsdigital.test.SetupBeforeTesting;
import com.github.onsdigital.zebedee.json.Credentials;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ws.rs.POST;
import java.io.IOException;

import static org.junit.Assert.*;

/**
 * Test cases for the {@link com.github.onsdigital.zebedee.api.Login} API
 * for initially logging in as the system owner.
 */
@Api
@DependsOn({})
public class Login {

    public static Http httpAdministrator;
    public static Http httpPublisher;
    public static Http httpSecondSetOfEyes;
    public static Http httpThirdSetOfEyes;
    public static Http httpViewer;
    public static Http httpScallywag;
    private static String tokenAdministrator;
    private static String tokenPublisher;
    private static String tokenSecondSetOfEyes;
    private static String tokenThirdSetOfEyes;
    private static String tokenViewer;

    /**
     * Gets a reference to the admin {@link Http} instance for use in these tests.
     */
    @BeforeClass
    public static void getHttp() {
        httpAdministrator = Sessions.get("administrator");
        httpPublisher = Sessions.get("publisher");
        httpSecondSetOfEyes = Sessions.get("secondSetOfEyes");
        httpThirdSetOfEyes = Sessions.get("thirdSetOfEyes");
        httpViewer = Sessions.get("viewer");
        httpScallywag = Sessions.get("scallywag");
    }

    /**
     * Saves the token from the admin login to the {@link Http} instance so that it can be used in other tests.
     */
    @AfterClass
    public static void setToken() {
        httpAdministrator.addHeader("x-florence-token", tokenAdministrator);
        httpPublisher.addHeader("x-florence-token", tokenPublisher);
        httpSecondSetOfEyes.addHeader("x-florence-token", tokenSecondSetOfEyes);
        httpThirdSetOfEyes.addHeader("x-florence-token", tokenThirdSetOfEyes);
        httpViewer.addHeader("x-florence-token", tokenViewer);
    }

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
        Credentials credentials = SetupBeforeTesting.adminCredentials;

        // When
        // We attempt to log in
        Response<String> response = httpAdministrator.post(ZebedeeHost.login, credentials, String.class);
        tokenAdministrator = response.body;

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

        // Given
        // Correct admin credentials
        Credentials credentials = SetupBeforeTesting.publisherCredentials;

        // When
        // We attempt to log in
        Response<String> response = httpAdministrator.post(ZebedeeHost.login, credentials, String.class);
        tokenPublisher = response.body;

        // Then
        // The request should succeed
        assertEquals(HttpStatus.OK_200, response.statusLine.getStatusCode());
        assertTrue(StringUtils.isNotBlank(response.body));

        // ----------------------- Setup second set of eyes
        credentials = SetupBeforeTesting.secondSetOfEyesCredentials;
        tokenSecondSetOfEyes = httpAdministrator.post(ZebedeeHost.login, credentials, String.class).body;
        // ----------------------- Setup third set of eyes

        credentials = SetupBeforeTesting.thirdSetOfEyesCredentials;
        tokenThirdSetOfEyes = httpAdministrator.post(ZebedeeHost.login, credentials, String.class).body;
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
        Credentials credentials = SetupBeforeTesting.contentOwnerCredentials;

        // When
        // We attempt to log in
        Response<String> response = httpAdministrator.post(ZebedeeHost.login, credentials, String.class);
        tokenViewer = response.body;

        // Then
        // The request should succeed
        assertEquals(HttpStatus.OK_200, response.statusLine.getStatusCode());
        assertTrue(StringUtils.isNotBlank(response.body));
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
        Credentials credentials = SetupBeforeTesting.scallywagCredentials;

        // When
        // We attempt to log in
        Response<String> response = httpScallywag.post(ZebedeeHost.login, credentials, String.class);

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
        Credentials credentials = credentials(null, SetupBeforeTesting.adminCredentials.password);

        // When
        // We attempt to log in
        Response<String> response = httpAdministrator.post(ZebedeeHost.login, credentials, String.class);

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
        Credentials credentials = credentials(SetupBeforeTesting.adminCredentials.email, "incorrect");

        // When
        // We attempt to log in
        Response<String> response = httpAdministrator.post(ZebedeeHost.login, credentials, String.class);

        // Then
        // We should get an unauthorised response
        assertEquals(401, response.statusLine.getStatusCode());
    }


    /**
     * Convenience method for generating login credentials.
     *
     * @param email    The email address
     * @param password The password
     * @return A {@link Credentials} instance containing the given details.
     */
    private static Credentials credentials(String email, String password) {
        Credentials credentials = new Credentials();
        credentials.email = email;
        credentials.password = password;
        return credentials;
    }

}