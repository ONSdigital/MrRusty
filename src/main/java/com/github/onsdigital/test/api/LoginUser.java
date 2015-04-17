package com.github.onsdigital.test.api;

import com.github.davidcarboni.restolino.framework.Api;
import com.github.onsdigital.http.Http;
import com.github.onsdigital.http.Response;
import com.github.onsdigital.http.Sessions;
import com.github.onsdigital.junit.DependsOn;
import com.github.onsdigital.zebedee.json.Credentials;
import org.apache.commons.lang3.StringUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ws.rs.POST;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test cases for the {@link com.github.onsdigital.zebedee.api.Login} API
 * for logging in as users other than the system owner.
 */
@Api
@DependsOn({Password.class})
public class LoginUser {

    private static Http http;
    private static String token;

    /**
     * Gets a reference to the admin {@link Http} instance for use in these tests.
     */
    @BeforeClass
    public static void getHttp() {
        http = Sessions.get("user");
    }

    /**
     * Saves the token from the admin login to the {@link Http} instance so that it can be used in other tests.
     */
    @AfterClass
    public static void setToken() {
        http.addHeader("x-florence-token", token);
    }

    /**
     * Tests login as a normal user using the credentials created in {@link Password}.
     *
     * @throws IOException
     */
    @POST
    @Test
    public void shouldLogInAsUser() throws IOException {

        // Given
        // Correct admin credentials
        Credentials credentials = Password.credentials;

        // When
        // We attempt to log in
        Response<String> response = http.post(ZebedeeHost.login, credentials, String.class);
        token = response.body;

        // Then
        // The request should succeed
        assertEquals("Seems we can't log in as a normal user?", 200, response.statusLine.getStatusCode());
        assertTrue(StringUtils.isNotBlank(token));
    }

}