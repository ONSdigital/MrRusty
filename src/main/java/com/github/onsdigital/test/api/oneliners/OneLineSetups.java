package com.github.onsdigital.test.api.oneliners;

import com.github.davidcarboni.cryptolite.Random;
import com.github.onsdigital.http.Http;
import com.github.onsdigital.http.Response;
import com.github.onsdigital.http.Sessions;
import com.github.onsdigital.test.Context;
import com.github.onsdigital.test.api.*;
import com.github.onsdigital.test.json.*;
import org.eclipse.jetty.http.HttpStatus;

import java.io.IOException;
import java.util.Calendar;

import static org.junit.Assert.assertEquals;

/**
 * Created by thomasridd on 23/04/15.
 */
public class OneLineSetups {

    /**
     * Creates a collection with no content
     *
     * @return The collection's {@link CollectionDescription}
     * @throws IOException
     */
    public static CollectionDescription publishedCollection(Http publisher, Team... teams) throws IOException {
        CollectionDescription collection = Collection.createCollectionDescription(teams);
        return Collection.post(collection, publisher).body;
    }

    /**
     * Creates a collection with randomly generated content in a specific folder
     *
     * @param fileCount the number of files to generate
     *
     * @return The collection's {@link CollectionDescription}
     * @throws IOException
     */
    public static CollectionDescription publishedCollectionWithContent(Context context, int fileCount) throws IOException {
        return publishedCollectionWithContent(context, "", fileCount);
    }

    /**
     * Returns a CollectionDescription with randomly generated content in a specific folder
     *
     *
     * @param context
     * @param directory the parent directory (taxonomy node) for the content
     *                  <p>requires backslashes</p>
     * @param fileCount the number of files to generate
     * @return The collection's {@link CollectionDescription}
     * @throws IOException
     */
    public static CollectionDescription publishedCollectionWithContent(Context context, String directory, int fileCount) throws IOException {
        CollectionDescription collection = Collection.createCollectionDescription();
        Http publisher = context.getPublisher();
        collection = Collection.post(collection, publisher).body;

        for(int i = 0; i < fileCount; i++) {
            String uri = "";

            if(directory.equals("") || directory.equals("/")) {
                uri = "/" + Random.id() + "/data.json";
            } else {
                uri = directory + Random.id() + "/data.json";
            }

            ContentDetail detail = new ContentDetail();
            detail.description = new ContentDetailDescription("title");

            Content.create(collection.id, detail, uri, publisher);
        }

        return Collection.get(collection.id, publisher).body;
    }

    /**
     * Returns a scheduled CollectionDescription with randomly generated content in a specific folder
     *
     * @param directory the base uri
     * @param fileCount the number of files
     * @param delayInSeconds the time, from now, to set the publish
     * @return
     * @throws IOException
     */
    public static CollectionDescription scheduledCollectionWithContent(Context context, String directory, int fileCount, int delayInSeconds) throws IOException {
        CollectionDescription collection = Collection.createCollectionDescription();
        collection.type = CollectionType.scheduled;
        Http publisher = context.getPublisher();

        Calendar calendar = Calendar.getInstance(); // gets a calendar using the default time zone and locale.
        calendar.add(Calendar.SECOND, delayInSeconds);
        collection.publishDate = calendar.getTime();

        collection = Collection.post(collection, publisher).body;
        for(int i = 0; i < fileCount; i++) {
            String uri = "";
            if(directory.equals("") || directory.equals("/")) {
                uri = "/" + Random.id() + "/data.json";
            } else {
                uri = directory + Random.id() + "/data.json";
            }

            ContentDetail detail = new ContentDetail();
            detail.description = new ContentDetailDescription("title");

            Content.create(collection.id, detail, uri, publisher);
        }

        return Collection.get(collection.id, publisher).body;
    }


    public static Http newSessionWithViewerPermissions(Context context, String name, String email) throws IOException {
        // Given
        // A user with no email address
        User user = new User();
        user.email = email;
        user.name = name;

        // Post the user
        Response<User> response = context.getAdministrator().post(ZebedeeHost.users, user, User.class);
        assertEquals(HttpStatus.OK_200, response.statusLine.getStatusCode());

        // Set their password
        Credentials credentials = new Credentials();
        credentials.email = user.email;
        credentials.password = Random.password(8);
        Response<String> responsePassword = context.getAdministrator().post(ZebedeeHost.password, credentials, String.class);
        assertEquals(HttpStatus.OK_200, responsePassword.statusLine.getStatusCode());

        // Change the password as the user to remove the temporary password restrictions.
        Credentials changePasswordCredentials = new Credentials();
        changePasswordCredentials.email = credentials.email;
        changePasswordCredentials.password = credentials.password;
        changePasswordCredentials.oldPassword = credentials.password;
        Response<String> login = new Http().post(ZebedeeHost.password, changePasswordCredentials, String.class);
        assertEquals(HttpStatus.OK_200, login.statusLine.getStatusCode());

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


    public static Http newSessionWithPublisherPermissions(Context context, String name, String email, String password) throws IOException {
        // Given
        // A user with no email address
        User user = new User();
        user.email = email;
        user.name = name;

        // Post the user
        Response<User> response = context.getAdministrator().post(ZebedeeHost.users, user, User.class);
        assertEquals(HttpStatus.OK_200, response.statusLine.getStatusCode());

        // Set their password
        Credentials credentials = new Credentials();
        credentials.email = user.email;
        credentials.password = password;
        Response<String> responsePassword = context.getAdministrator().post(ZebedeeHost.password, credentials, String.class);
        assertEquals(HttpStatus.OK_200, responsePassword.statusLine.getStatusCode());

        // Assign them publisher permissions
        PermissionDefinition definition = new PermissionDefinition();
        definition.email = user.email;
        definition.editor = true;
        Response<String> permissionResponse = context.getAdministrator().post(ZebedeeHost.permission, definition, String.class);
        assertEquals(HttpStatus.OK_200, permissionResponse.statusLine.getStatusCode());

        // Change the password as the user to remove the temporary password restrictions.
        Credentials changePasswordCredentials = new Credentials();
        changePasswordCredentials.email = credentials.email;
        changePasswordCredentials.password = credentials.password;
        changePasswordCredentials.oldPassword = credentials.password;
        Response<String> login = new Http().post(ZebedeeHost.password, changePasswordCredentials, String.class);
        assertEquals(HttpStatus.OK_200, login.statusLine.getStatusCode());

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
    public static Http newSessionWithPublisherPermissions(Context context) throws IOException {
        return newSessionWithPublisherPermissions(context, "Rusty", "Rusty_" + Random.id() + "@example.com", Random.password(8));
    }

    public static User newActiveUserWithViewerPermissions(Context context, String name, String email) throws IOException {
        // Create the user
        User user = new User();
        user.email = email;
        user.name = name;

        // Post the user
        Response<User> response = context.getAdministrator().post(ZebedeeHost.users, user, User.class);
        assertEquals(HttpStatus.OK_200, response.statusLine.getStatusCode());

        // Set their password
        Credentials credentials = new Credentials();
        credentials.email = user.email;
        credentials.password = Random.password(8);
        context.getAdministrator().post(ZebedeeHost.password, credentials, String.class);

        return user;
    }
    public static User newActiveUserWithViewerPermissions(Context context) throws IOException {
        return newActiveUserWithViewerPermissions(context, "Rusty", "Rusty_" + Random.id() + "@example.com");
    }
    public static Http newSessionWithViewerPermissions(Context context) throws IOException {
        return newSessionWithViewerPermissions(context, "Rusty", "Rusty_" + Random.id() + "@example.com");
    }
}
