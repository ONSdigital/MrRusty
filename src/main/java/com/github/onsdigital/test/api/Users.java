package com.github.onsdigital.test.api;

import com.github.davidcarboni.cryptolite.Random;
import com.github.davidcarboni.restolino.framework.Api;
import com.github.onsdigital.http.Http;
import com.github.onsdigital.http.Response;
import com.github.onsdigital.http.Sessions;
import com.github.onsdigital.junit.DependsOn;
import com.github.onsdigital.zebedee.json.Credentials;
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
@DependsOn(LoginAdmin.class)
public class Users {

    static User user;

    private static Http http;

    @BeforeClass
    public static void getAdminSession() {
        http = Sessions.get("admin");
    }

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
        user.email = "user." + Random.id() + "@example.com";
        user.name = "Hello World";

        // When
        // We attempt to create a duplicate user
        Response<User> response = http.post(ZebedeeHost.users, user, User.class);

        // Then
        // We should get a conflict
        assertEquals(HttpStatus.OK_200, response.statusLine.getStatusCode());
    }

    /**
     * Ensures we get a 401 Unauthorized when a non-admin user tries to create.
     *
     * TODO Update with permissions
     *
     */
    @POST
    @Test
    public void shouldNotCreateUserIfNotAdmin() throws IOException {

    }


    /**
     * A user account should be inactive after creation and before the password is set.
     * Otherwise a 401 Unauthorised error
     *
     */
    @POST
    @Test
    public void shouldBeInactiveBeforePasswordIsSet() throws IOException {

        // Given
        // A new user
        User inactive = new User();
        inactive.email = "user." + Random.id() + "@example.com";
        inactive.name = "I'm inactive";
        http.post(ZebedeeHost.users, inactive, User.class);

        // When
        // We attempt to log in with that user
        Credentials credentials = new Credentials();
        credentials.email = inactive.email;
        Response<String> response = http.post(ZebedeeHost.login, credentials, String.class);

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
    public void shouldConflictIfEmailExists() throws IOException {

        // Given
        // An existing user
        User user = new User();
        user.email = "duplicate." + Random.id() + "@example.com";
        user.name = "Coming around again";
        Response<User> create = http.post(ZebedeeHost.users, user, User.class);
        assertEquals(HttpStatus.OK_200, create.statusLine.getStatusCode());

        // When
        // We attempt to create a duplicate user
        Response<User> response = http.post(ZebedeeHost.users, user, User.class);

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
        user.name = "I have no email address";

        // When
        // We attempt to create the user
        Response<User> response = http.post(ZebedeeHost.users, user, User.class);

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
        user.email = "no.name." + Random.id() + "@example.com";
        user.name = "";

        // When
        // We attempt to create the user
        Response<User> response = http.post(ZebedeeHost.users, user, User.class);

        // Then
        // We should get a bad request
        assertEquals(HttpStatus.BAD_REQUEST_400, response.statusLine.getStatusCode());
    }

}
