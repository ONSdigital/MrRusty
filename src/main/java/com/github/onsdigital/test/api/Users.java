package com.github.onsdigital.test.api;

import com.github.davidcarboni.cryptolite.Random;
import com.github.davidcarboni.restolino.framework.Api;
import com.github.onsdigital.http.Http;
import com.github.onsdigital.http.Response;
import com.github.onsdigital.http.Sessions;
import com.github.onsdigital.junit.DependsOn;
import com.github.onsdigital.zebedee.json.Credentials;
import com.github.onsdigital.zebedee.json.Session;
import com.github.onsdigital.zebedee.json.User;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ws.rs.POST;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * Test cases for the {@link com.github.onsdigital.zebedee.api.Users} API.
 */
@Api
@DependsOn(Login.class)
public class Users {

    static User user;

    /**
     * Ensures we get a 200 OK when creating a valid user.
     *
     * @throws IOException
     */
    @POST
    @Test
    public void shouldCreateUser() throws IOException {

        // Given
        // A valid user
        user = new User();
        user.email = "Rusty_" + Random.id() + "@example.com";
        user.name = "Hello World";

        // When
        // We attempt to create a user
        Response<User> response = Login.httpAdministrator.post(ZebedeeHost.users, user, User.class);

        // Then
        // We should get no conflict
        assertEquals(HttpStatus.OK_200, response.statusLine.getStatusCode());
    }

    /**
     * Ensures we get a 401 Unauthorized when a non-admin user tries to create.
     *
     */
    @POST
    @Test
    public void shouldReturnUnauthorizedIfUserNotAdmin() throws IOException {

        // Given
        // A valid user
        user = new User();
        user.name = "Rusty";

        // When
        // We attempt to create a user with each alternate
        user.email = "Rusty_" + Random.id() + "@example.com";
        Response<User> response1 = Login.httpPublisher.post(ZebedeeHost.users, user, User.class);

        user.email = "Rusty_" + Random.id() + "@example.com";
        Response<User> response2 = Login.httpScallywag.post(ZebedeeHost.users, user, User.class);

        user.email = "Rusty_" + Random.id() + "@example.com";
        Response<User> response3 = Login.httpViewer.post(ZebedeeHost.users, user, User.class);

        // Then
        // We should get no conflict
        assertEquals(HttpStatus.UNAUTHORIZED_401, response1.statusLine.getStatusCode());
        assertEquals(HttpStatus.UNAUTHORIZED_401, response2.statusLine.getStatusCode());
        assertEquals(HttpStatus.UNAUTHORIZED_401, response3.statusLine.getStatusCode());
    }


    /**
     * A user account should be inactive (unable to log in) before the password is set.
     * Otherwise a 401 Unauthorised error
     *
     */
    @POST
    @Test
    public void shouldBeInactiveBeforePasswordIsSet() throws IOException {

        // Given
        // A new user
        User inactive = new User();
        inactive.email = "Rusty_" + Random.id() + "@example.com";
        inactive.name = "Rusty";
        Login.httpAdministrator.post(ZebedeeHost.users, inactive, User.class);

        // When
        // We attempt to log in with that user
        Credentials credentials = new Credentials();
        credentials.email = inactive.email;
        Response<String> response = Login.httpAdministrator.post(ZebedeeHost.login, credentials, String.class);

        // Then
        // We should get unauthorized:
        assertEquals(HttpStatus.UNAUTHORIZED_401, response.statusLine.getStatusCode());
    }

    /**
     * Ensures we get a 409 Conflict when attempting to create a duplicate user.
     *
     * @throws IOException
     */
    @POST
    @Test
    public void shouldReturnConflictIfEmailExists() throws IOException {

        // Given
        // An existing user
        User user = new User();
        user.email = "Rusty_" + Random.id() + "@example.com";
        user.name = "Rusty";
        Response<User> create = Login.httpAdministrator.post(ZebedeeHost.users, user, User.class);
        assertEquals(HttpStatus.OK_200, create.statusLine.getStatusCode());

        // When
        // We attempt to create a duplicate user
        Response<User> response = Login.httpAdministrator.post(ZebedeeHost.users, user, User.class);

        // Then
        // We should get a conflict
        assertEquals(HttpStatus.CONFLICT_409, response.statusLine.getStatusCode());
    }

    /**
     * Ensures we get a 400 Bad Request when attempting to create a user without an email address.
     *
     * @throws IOException
     */
    @POST
    @Test
    public void shouldFailToCreateIfNoEmail() throws IOException {

        // Given
        // A user with no email address
        User user = new User();
        user.email = "";
        user.name = "Rusty";

        // When
        // We attempt to create the user
        Response<User> response = Login.httpAdministrator.post(ZebedeeHost.users, user, User.class);

        // Then
        // We should get a bad request
        assertEquals(HttpStatus.BAD_REQUEST_400, response.statusLine.getStatusCode());
    }

    /**
     * Ensures we get a 400 Bad Request when attempting to create a user without a name.
     *
     * @throws IOException
     */
    @POST
    @Test
    public void shouldFailToCreateIfNoName() throws IOException {

        // Given
        // A user with no email address
        User user = new User();
        user.email = "Rusty_" + Random.id() + "@example.com";
        user.name = "";

        // When
        // We attempt to create the user
        Response<User> response = Login.httpAdministrator.post(ZebedeeHost.users, user, User.class);

        // Then
        // We should get a bad request
        assertEquals(HttpStatus.BAD_REQUEST_400, response.statusLine.getStatusCode());
    }

    public static Http userHttpSession(String name, String email) throws IOException {
        // Given
        // A user with no email address
        User user = new User();
        user.email = email;
        user.name = name;

        // Post the user
        Response<User> response = Login.httpAdministrator.post(ZebedeeHost.users, user, User.class);
        assertEquals(HttpStatus.OK_200, response.statusLine.getStatusCode());

        // Set their password
        Credentials credentials = new Credentials();
        credentials.email = user.email;
        credentials.password = Random.password(8);
        Response<String> responsePassword = Login.httpAdministrator.post(ZebedeeHost.password, credentials, String.class);
        assertEquals(HttpStatus.OK_200, responsePassword.statusLine.getStatusCode());

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
    public static Http userHttpSession() throws IOException {
        return userHttpSession("Rusty", "Rusty_" + Random.id() + "@example.com");
    }

}
