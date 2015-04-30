package com.github.onsdigital.test.api.oneliners;

import com.github.davidcarboni.cryptolite.Random;
import com.github.onsdigital.http.Http;
import com.github.onsdigital.http.Response;
import com.github.onsdigital.http.Sessions;
import com.github.onsdigital.test.api.*;
import com.github.onsdigital.zebedee.json.CollectionDescription;
import com.github.onsdigital.zebedee.json.Credentials;
import com.github.onsdigital.zebedee.json.Team;
import com.github.onsdigital.zebedee.json.User;
import org.eclipse.jetty.http.HttpStatus;

import java.io.IOException;

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
                uri = "/" + Random.id() + ".json";
            } else {
                uri = directory + Random.id() + ".json";
            }
            Content.create(collection.id, uri, uri, Login.httpPublisher);
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
