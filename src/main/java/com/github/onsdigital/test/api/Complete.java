package com.github.onsdigital.test.api;

import com.github.davidcarboni.cryptolite.Random;
import com.github.onsdigital.http.Endpoint;
import com.github.onsdigital.http.Http;
import com.github.onsdigital.http.Response;
import com.github.onsdigital.junit.DependsOn;
import com.github.onsdigital.test.api.oneliners.OneLineSetups;
import com.github.onsdigital.zebedee.json.CollectionDescription;
import com.google.gson.JsonObject;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.Test;

import javax.ws.rs.POST;
import java.io.IOException;

import static org.junit.Assert.*;

@DependsOn(Content.class)
public class Complete {


    /**
     * Test basic functionality
     */
    @POST
    @Test
    public void shouldCompleteWithPublisherCredentials() throws IOException {

        // Given - an existing collection with some content
        CollectionDescription collection = OneLineSetups.publishedCollectionWithContent(1);
        String filename = collection.inProgressUris.get(0);

        // When - we call complete on the content
        Response<String> complete = complete(collection.name, filename, Login.httpPublisher);

        // Then
        // We get the expected response code
        // and the content is listed under complete when we get the collection.
        assertEquals(HttpStatus.OK_200, complete.statusLine.getStatusCode());

        CollectionDescription updatedCollection = Collection.get(collection.name, Login.httpPublisher).body;
        assertTrue(updatedCollection.completeUris.contains(filename));
        assertFalse(updatedCollection.inProgressUris.contains(filename));
    }

    /**
     * Test should return Unauthorised without publisher credentials
     */
    @POST
    @Test
    public void shouldReturnUnauthorizedWithoutPublisherCredentials() throws IOException {

        // Given
        // An existing collection with some content
        CollectionDescription collection = OneLineSetups.publishedCollectionWithContent(3);

        // When
        // We call complete on the content
        Response<String> complete1 = complete(collection.name, collection.inProgressUris.get(0), Login.httpAdministrator);
        Response<String> complete2 = complete(collection.name, collection.inProgressUris.get(1), Login.httpViewer);
        Response<String> complete3 = complete(collection.name, collection.inProgressUris.get(2), Login.httpScallywag);

        // Then
        // We expect unauthorised responses
        assertEquals(HttpStatus.UNAUTHORIZED_401, complete1.statusLine.getStatusCode());
        assertEquals(HttpStatus.UNAUTHORIZED_401, complete2.statusLine.getStatusCode());
        assertEquals(HttpStatus.UNAUTHORIZED_401, complete3.statusLine.getStatusCode());

        CollectionDescription updatedCollection = Collection.get(collection.name, Login.httpPublisher).body;
        assertFalse(updatedCollection.completeUris.contains(collection.inProgressUris.get(0)));
        assertFalse(updatedCollection.completeUris.contains(collection.inProgressUris.get(1)));
        assertFalse(updatedCollection.completeUris.contains(collection.inProgressUris.get(2)));

        assertTrue(updatedCollection.inProgressUris.contains(collection.inProgressUris.get(0)));
        assertTrue(updatedCollection.inProgressUris.contains(collection.inProgressUris.get(1)));
        assertTrue(updatedCollection.inProgressUris.contains(collection.inProgressUris.get(2)));
    }

    /**
     * Test returns Unauthorised without publisher credentials
     */
    @POST
    @Test
    public void shouldReturnNotFoundIfUriIsNotInProgress() throws IOException {

        // Given - a file is not listed in progress
        CollectionDescription collection = OneLineSetups.publishedCollection();
        String filename = "/shouldReturn404/" + Random.id() + ".json";

        // When - we call complete on the content
        Response<String> complete = complete(collection.name, filename, Login.httpPublisher);

        // Then - we should be rejected with a Not Found response code
        assertEquals(HttpStatus.NOT_FOUND_404, complete.statusLine.getStatusCode());
    }

    /**
     * Test should return Unauthorised without publisher credentials
     */
    @Test
    public void shouldReturnBadRequestIfGivenUriIsADirectory() throws IOException {

        // Given
        // a collection with content in a directory
        String directory = "/directory/";
        CollectionDescription collection = OneLineSetups.publishedCollectionWithContent(directory, 1);

        // When
        // we call complete on the directory not the file
        Response<String> complete = complete(collection.name, directory, Login.httpPublisher);

        // Then
        // we expect a Bad Request response code
        assertEquals(HttpStatus.BAD_REQUEST_400, complete.statusLine.getStatusCode());
    }

    /**
     * Test should return Not Found if already complete
     */
    @Test
    public void shouldReturnNotFoundIfUriIsAlreadyComplete() throws IOException {

        // Given
        // a collection with content that is set to complete.
        CollectionDescription collection = OneLineSetups.publishedCollectionWithContent(1);
        String fileUri = collection.inProgressUris.get(0);
        complete(collection.name, fileUri, Login.httpPublisher);

        // When
        // we call complete on the content
        Response<String> complete = complete(collection.name, fileUri, Login.httpPublisher);

        // Then
        // We get the expected response code
        assertEquals(HttpStatus.NOT_FOUND_404, complete.statusLine.getStatusCode());
    }

    /**
     * Convenience method to complete content in one line
     * @param collectionName
     * @param uri
     * @param http
     * @return
     * @throws IOException
     */
    public static Response<String> complete(String collectionName, String uri, Http http) throws IOException {
        Endpoint contentEndpoint = ZebedeeHost.complete.addPathSegment(collectionName).setParameter("uri", uri);
        return http.post(contentEndpoint, "", String.class);
    }
}
