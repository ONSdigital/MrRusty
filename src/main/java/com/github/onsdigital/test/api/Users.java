package com.github.onsdigital.test.api;

import com.github.davidcarboni.cryptolite.Random;
import com.github.davidcarboni.restolino.framework.Api;
import com.github.onsdigital.http.Http;
import com.github.onsdigital.http.Response;
import com.github.onsdigital.http.Sessions;
import com.github.onsdigital.junit.DependsOn;
import com.github.onsdigital.zebedee.json.User;
import org.junit.Test;

import javax.ws.rs.POST;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * Created by kanemorgan on 02/04/2015.
 */

@Api
@DependsOn(Login.class)
public class Users {

    Http http = Sessions.get("admin");

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
        User user = new User();
        user.email = "user." + Random.id() + "@example.com";
        user.name = "Hello World";

        // When
        // We attempt to create a duplicate user
        Response<User> response = http.post(ZebedeeHost.users, user, User.class);

        // Then
        // We should get a conflict
        assertEquals(200, response.statusLine.getStatusCode());
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
        assertEquals(200, create.statusLine.getStatusCode());

        // When
        // We attempt to create a duplicate user
        Response<User> response = http.post(ZebedeeHost.users, user, User.class);

        // Then
        // We should get a conflict
        assertEquals(409, response.statusLine.getStatusCode());
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
        assertEquals(400, response.statusLine.getStatusCode());
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
        assertEquals(400, response.statusLine.getStatusCode());
    }

}
