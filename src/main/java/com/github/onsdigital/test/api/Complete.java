package com.github.onsdigital.test.api;

import com.github.davidcarboni.cryptolite.Random;
import com.github.onsdigital.http.Endpoint;
import com.github.onsdigital.http.Http;
import com.github.onsdigital.http.Response;
import com.github.onsdigital.junit.DependsOn;
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
        CollectionDescription collection = Collection.createCollectionDescription();
        Collection.post(collection, Login.httpPublisher);
        String filename = "/" + Random.id() + ".json";
        Content.create(collection.name, "foo", filename, Login.httpPublisher);

        // When - we call complete on the content
        int responseCode = complete(collection.name, filename, Login.httpPublisher);

        // Then - We get the expected response code
        assertEquals(HttpStatus.OK_200, responseCode);

        // and the content is listed under complete when we get the collection.
        CollectionDescription updatedCollection = Collection.get(collection.name, Login.httpPublisher).body;
        assertTrue(updatedCollection.completeUris.contains(filename));
        assertFalse(updatedCollection.inProgressUris.contains(filename));
    }

    /**
     * Test
     */
    @POST
    @Test
    public void shouldReturnUnauthorizedWithoutPublisherCredentials() throws IOException {

        // Given
        // An existing collection with some content
        CollectionDescription collection = Collection.createCollectionDescription();
        Collection.post(collection, Login.httpPublisher);
        String filename1 = "/Administrator/" + Random.id() + ".json";
        String filename2 = "/Viewer/" + Random.id() + ".json";
        String filename3 = "/Scallywag/" + Random.id() + ".json";
        Content.create(collection.name, "administrator", filename1, Login.httpPublisher);
        Content.create(collection.name, "viewer", filename2, Login.httpPublisher);
        Content.create(collection.name, "scallywag", filename3, Login.httpPublisher);

        // When
        // We call complete on the content
        int responseCode1 = complete(collection.name, filename1, Login.httpAdministrator);
        int responseCode2 = complete(collection.name, filename2, Login.httpViewer);
        int responseCode3 = complete(collection.name, filename3, Login.httpScallywag);

        // Then
        // We expect unauthorised responses
        assertEquals(HttpStatus.UNAUTHORIZED_401, responseCode1);
        assertEquals(HttpStatus.UNAUTHORIZED_401, responseCode2);
        assertEquals(HttpStatus.UNAUTHORIZED_401, responseCode3);

        // and also
        // the content has not been moved
        CollectionDescription updatedCollection = Collection.get(collection.name, Login.httpPublisher).body;
        assertFalse(updatedCollection.completeUris.contains(filename1));
        assertFalse(updatedCollection.completeUris.contains(filename2));
        assertFalse(updatedCollection.completeUris.contains(filename3));
        assertTrue(updatedCollection.inProgressUris.contains(filename1));
        assertTrue(updatedCollection.inProgressUris.contains(filename2));
        assertTrue(updatedCollection.inProgressUris.contains(filename3));
    }

    @POST
    @Test
    public void shouldReturnNotFoundIfUriIsNotInProgress() throws IOException {

        // Given - a file is not listed in progress
        CollectionDescription collection = Collection.createCollectionDescription();
        Collection.post(collection, Login.httpPublisher);
        String filename = "/shouldReturn404/" + Random.id() + ".json";

        // When - we call complete on the content
        int responseCode = complete(collection.name, filename, Login.httpPublisher);

        // Then - We get the expected response code
        assertEquals(HttpStatus.NOT_FOUND_404, responseCode);
    }

    @Test
    public void shouldReturnBadRequestIfGivenUriIsADirectory() throws IOException {

        // Given - a directory that exists in progress
        CollectionDescription collection = Collection.createCollectionDescription();
        Collection.post(collection, Login.httpPublisher);
        String directory = "/shouldReturn400/";
        String filename = directory + Random.id() + ".json";
        Content.create(collection.name, "shouldReturn400", filename, Login.httpPublisher);

        // When - we call complete on the content
        int responseCode = complete(collection.name, directory, Login.httpPublisher);

        // Then - We get the expected response code
        assertEquals(HttpStatus.BAD_REQUEST_400, responseCode);
    }

    @Test
    public void shouldReturnNotFoundIfUriIsAlreadyComplete() throws IOException {

        // Given - a uri that is already set to complete.
        CollectionDescription collection = Collection.createCollectionDescription();
        Collection.post(collection, Login.httpPublisher);
        String filename = Random.id() + ".json";
        Content.create(collection.name, "shouldReturn400", filename, Login.httpPublisher);
        complete(collection.name, filename, Login.httpPublisher);

        // When - we call complete on the content
        int responseCode = complete(collection.name, filename, Login.httpPublisher);

        // Then - We get the expected response code
        assertEquals(HttpStatus.NOT_FOUND_404, responseCode);
    }

    /**
     *
     * @param collectionName the name of the parent collection
     * @param uri the filename within the collection
     * @param http the session we are calling complete from
     * @return the {@link HttpStatus} code for the response
     * @throws IOException
     */
    public static int complete(String collectionName, String uri, Http http) throws IOException {
        Endpoint contentEndpoint = ZebedeeHost.complete.addPathSegment(collectionName).setParameter("uri", uri);
        Response<String> createResponse = http.post(contentEndpoint, "", String.class);
        return createResponse.statusLine.getStatusCode();
    }
}
