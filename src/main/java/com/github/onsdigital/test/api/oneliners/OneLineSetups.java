package com.github.onsdigital.test.api.oneliners;

import com.github.davidcarboni.cryptolite.Random;
import com.github.onsdigital.http.Http;
import com.github.onsdigital.http.Response;
import com.github.onsdigital.http.Sessions;
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
    public static CollectionDescription publishedCollection() throws IOException {
        CollectionDescription collection = Collection.createCollectionDescription();
        return Collection.post(collection, Login.httpPublisher).body;
    }

    /**
     * Creates a collection with randomly generated content in a specific folder
     *
     * @param fileCount the number of files to generate
     *
     * @return The collection's {@link CollectionDescription}
     * @throws IOException
     */
    public static CollectionDescription publishedCollectionWithContent(int fileCount) throws IOException {
        return publishedCollectionWithContent( "", fileCount);
    }

    /**
     * Returns a CollectionDescription with randomly generated content in a specific folder
     *
     * @param fileCount the number of files to generate
     * @param directory the parent directory (taxonomy node) for the content
     *                  <p>requires backslashes</p>
     * @return The collection's {@link CollectionDescription}
     * @throws IOException
     */
    public static CollectionDescription publishedCollectionWithContent(String directory, int fileCount) throws IOException {
        CollectionDescription collection = Collection.createCollectionDescription();
        collection = Collection.post(collection, Login.httpPublisher).body;

        for(int i = 0; i < fileCount; i++) {
            String uri = "";

            if(directory.equals("") || directory.equals("/")) {
                uri = "/" + Random.id() + "/data.json";
            } else {
                uri = directory + Random.id() + "/data.json";
            }

            ContentDetail detail = new ContentDetail();
            detail.description = new ContentDetailDescription("title");

            Content.create(collection.id, detail, uri, Login.httpPublisher);
        }

        return Collection.get(collection.id, Login.httpPublisher).body;
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
    public static CollectionDescription scheduledCollectionWithContent(String directory, int fileCount, int delayInSeconds) throws IOException {
        CollectionDescription collection = Collection.createCollectionDescription();
        collection.type = CollectionType.scheduled;

        Calendar calendar = Calendar.getInstance(); // gets a calendar using the default time zone and locale.
        calendar.add(Calendar.SECOND, delayInSeconds);
        collection.publishDate = calendar.getTime();

        collection = Collection.post(collection, Login.httpPublisher).body;
        for(int i = 0; i < fileCount; i++) {
            String uri = "";
            if(directory.equals("") || directory.equals("/")) {
                uri = "/" + Random.id() + "/data.json";
            } else {
                uri = directory + Random.id() + "/data.json";
            }

            ContentDetail detail = new ContentDetail();
            detail.description = new ContentDetailDescription("title");

            Content.create(collection.id, detail, uri, Login.httpPublisher);
        }

        return Collection.get(collection.id, Login.httpPublisher).body;
    }


    public static Http newSessionWithViewerPermissions(String name, String email) throws IOException {
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


    public static Http newSessionWithPublisherPermissions(String name, String email) throws IOException {
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

        // Assign them publisher permissions
        PermissionDefinition definition = new PermissionDefinition();
        definition.email = user.email;
        definition.editor = true;
        Response<String> permissionResponse = Login.httpAdministrator.post(ZebedeeHost.permission, definition, String.class);
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
    public static Http newSessionWithPublisherPermissions() throws IOException {
        return newSessionWithPublisherPermissions("Rusty", "Rusty_" + Random.id() + "@example.com");
    }

    public static User newActiveUserWithViewerPermissions(String name, String email) throws IOException {
        // Create the user
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
        Login.httpAdministrator.post(ZebedeeHost.password, credentials, String.class);

        return user;
    }
    public static User newActiveUserWithViewerPermissions() throws IOException {
        return newActiveUserWithViewerPermissions("Rusty", "Rusty_" + Random.id() + "@example.com");
    }
    public static Http newSessionWithViewerPermissions() throws IOException {
        return newSessionWithViewerPermissions("Rusty", "Rusty_" + Random.id() + "@example.com");
    }

    /**
     * Creates and posts an empty team
     *
     * @return The {@link Team}
     *
     * @throws IOException
     */
    public static Team newTeam() throws IOException {
        // Post a new team with name
        String teamName = "Rusty_" + Random.id();
        Teams.postTeam(teamName, Login.httpAdministrator);
        // Retrieve the created Team object
        Team team = Teams.getTeam(teamName, Login.httpAdministrator).body;
        return team;
    }
    /**
     * Creates and posts a team with members
     *
     * @param numberOfUsers the number of users to add to the team
     *
     * @return The {@link Team}
     *
     * @throws IOException
     */
    public static Team newTeam(int numberOfUsers) throws IOException {
        // Post a new team with name
        Team team = newTeam();
        for(int i = 0; i < numberOfUsers; i++) {
            User user = newActiveUserWithViewerPermissions();
            Teams.postMember(team.name, user.email, Login.httpAdministrator);
        }
        return team;
    }
}
