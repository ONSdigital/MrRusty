package com.github.onsdigital.test.api;

import com.github.davidcarboni.cryptolite.Random;
import com.github.davidcarboni.restolino.framework.Api;
import com.github.onsdigital.http.Endpoint;
import com.github.onsdigital.http.Http;
import com.github.onsdigital.http.Response;
import com.github.onsdigital.junit.DependsOn;
import com.github.onsdigital.test.Context;
import com.github.onsdigital.test.base.ZebedeeApiTest;
import com.github.onsdigital.test.json.Credentials;
import com.github.onsdigital.test.json.User;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.Test;

import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import java.io.IOException;

import static com.github.onsdigital.test.api.oneliners.OneLineSetups.newSessionWithViewerPermissions;
import static org.junit.Assert.assertEquals;

/**
 * Test cases for the Zebedee users API.
 */
@Api
@DependsOn(com.github.onsdigital.test.api.Login.class)
public class Users extends ZebedeeApiTest {
    private User user;

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
        user = createRandomTestUser();

        // When
        // We attempt to create a user
        Response<User> response = context.getAdministrator().post(ZebedeeHost.users, user, User.class);

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
        user = createRandomTestUser();

        // When
        // We attempt to create a user with each alternate
        user.email = generateRandomTestUserEmail();
        Response<User> response1 = context.getPublisher().post(ZebedeeHost.users, user, User.class);

        user.email = generateRandomTestUserEmail();
        Response<User> response2 = context.getScallyWag().post(ZebedeeHost.users, user, User.class);

        user.email = generateRandomTestUserEmail();
        Response<User> response3 = context.getViewer().post(ZebedeeHost.users, user, User.class);

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
        User inactive = createRandomTestUser();
        context.getAdministrator().post(ZebedeeHost.users, inactive, User.class);

        // When
        // We attempt to log in with that user
        Credentials credentials = new Credentials();
        credentials.email = inactive.email;
        Response<String> response = context.getAdministrator().post(ZebedeeHost.login, credentials, String.class);

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
        User user = createRandomTestUser();
        Response<User> create = context.getAdministrator().post(ZebedeeHost.users, user, User.class);
        assertEquals(HttpStatus.OK_200, create.statusLine.getStatusCode());

        // When
        // We attempt to create a duplicate user
        Response<User> response = context.getAdministrator().post(ZebedeeHost.users, user, User.class);

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
        User user = createRandomTestUser();
        user.email = "";

        // When
        // We attempt to create the user
        Response<User> response = context.getAdministrator().post(ZebedeeHost.users, user, User.class);

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
        User user = createRandomTestUser();
        user.name = "";

        // When
        // We attempt to create the user
        Response<User> response = context.getAdministrator().post(ZebedeeHost.users, user, User.class);

        // Then
        // We should get a bad request
        assertEquals(HttpStatus.BAD_REQUEST_400, response.statusLine.getStatusCode());
    }

    @DELETE
    @Test
    public void deleteUser_givenAdminLogin_shouldRemoveUser() throws IOException {
        // Given
        // A created user
        User deleteUser = createRandomTestUser();
        Response<User> response = context.getAdministrator().post(ZebedeeHost.users, deleteUser, User.class);

        // When
        // we delete the user
        Response<String> delete = delete(deleteUser.email, context.getAdministrator());

        // Then
        // the delete should go through & the user should not exist
        assertEquals(HttpStatus.OK_200, delete.statusLine.getStatusCode());

        Response<User> userResponse = get(deleteUser.email, context.getPublisher());
        assertEquals(HttpStatus.NOT_FOUND_404, userResponse.statusLine.getStatusCode());
    }

    @DELETE
    @Test
    public void deleteUser_givenRandomEmail_shouldReturnNotFoundException() throws IOException {
        // Given
        // A random and presumably non existent email
        String email = generateRandomTestUserEmail();

        // When
        // we delete the user
        Response<String> delete = delete(email, context.getAdministrator());

        // Then
        // the delete should return Not found
        assertEquals(HttpStatus.NOT_FOUND_404, delete.statusLine.getStatusCode());
    }

    @DELETE
    @Test
    public void deleteUser_givenNonAdminLogin_shouldFail() throws IOException {
        // Given
        // A created user
        User deleteUser = createRandomTestUser();
        Response<User> response = context.getAdministrator().post(ZebedeeHost.users, deleteUser, User.class);

        // When
        // we delete the user using a non admin login
        Response<String> delete = delete(deleteUser.email, context.getPublisher());

        // Then
        // the delete should go through & the user should not exist
        assertEquals(HttpStatus.UNAUTHORIZED_401, delete.statusLine.getStatusCode());

        Response<User> userResponse = get(deleteUser.email, context.getPublisher());
        assertEquals(HttpStatus.OK_200, userResponse.statusLine.getStatusCode());
    }

    public static Response<User> get(String email, Http http) throws IOException {
        Endpoint idUrl = ZebedeeHost.users.setParameter("email", email);
        return http.get(idUrl, User.class);
    }
    public static Response<String> delete(String email, Http http) throws IOException {
        Endpoint idUrl = ZebedeeHost.users.setParameter("email", email);
        return http.delete(idUrl, String.class);
    }

    public static User createRandomTestUser() {
        User user = new User();
        user.email = generateRandomTestUserEmail();
        user.name = "Rusty";
        return user;
    }

    public static User createTestViewerUser(Context context) throws IOException {
        User user = createRandomTestUser();
        newSessionWithViewerPermissions(context, user.name, user.email);
        return user;
    }

    public static String generateRandomTestUserEmail() {
        return "Rusty_" + Random.id() + "@example.com";
    }
}
