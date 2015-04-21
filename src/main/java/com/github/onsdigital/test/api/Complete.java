package com.github.onsdigital.test.api;

import com.github.davidcarboni.cryptolite.Random;
import com.github.onsdigital.http.Endpoint;
import com.github.onsdigital.http.Http;
import com.github.onsdigital.http.Response;
import com.github.onsdigital.http.Sessions;
import com.github.onsdigital.junit.DependsOn;
import com.github.onsdigital.zebedee.json.CollectionDescription;
import com.google.gson.JsonObject;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

@DependsOn(Content.class)
public class Complete {

    Http http = Sessions.get("admin");

    @Test
    public void shouldComplete() throws IOException {

        // Given - an existing collection with some content
        CollectionDescription collection = Collection.create(http);
        String filename = "/" + Random.id() + ".json";
        Content.create(collection.name, "foo", filename, 200, http);

        // When - we call complete on the content
        int responseCode = complete(collection.name, filename, http);

        // Then - We get the expected response code
        assertEquals(HttpStatus.OK_200, responseCode);

        // and the content is listed under complete when we get the collection.
        CollectionDescription updatedCollection = Collection.get(collection.name, http);
        assertTrue(updatedCollection.completeUris.contains(filename));
        assertFalse(updatedCollection.inProgressUris.contains(filename));
    }

    @Test
    public void shouldReturn404IfNotInProgress() throws IOException {

        // Given - a file is not listed in progress
        CollectionDescription collection = Collection.create(http);
        String filename = "/foobarred/" + Random.id() + ".json";

        // When - we call complete on the content
        int responseCode = complete(collection.name, filename, http);

        // Then - We get the expected response code
        assertEquals(HttpStatus.NOT_FOUND_404, responseCode);
    }

    @Test
    public void shouldReturn400IfGivenUriIsADirectory() throws IOException {

        // Given - a directory that exists in progress
        CollectionDescription collection = Collection.create(http);
        String directory = "/foobarred/";
        String filename = directory + Random.id() + ".json";
        Content.create(collection.name, "foo", filename, 200, http);

        // When - we call complete on the content
        int responseCode = complete(collection.name, directory, http);

        // Then - We get the expected response code
        assertEquals(HttpStatus.BAD_REQUEST_400, responseCode);
    }

    @Test
    public void shouldReturn404IfUriIsAlreadyComplete() throws IOException {

        // Given - a uri that is already set to complete.
        CollectionDescription collection = Collection.create(http);
        String filename = Random.id() + ".json";
        Content.create(collection.name, "foo", filename, 200, http);
        complete(collection.name, filename, http);

        // When - we call complete on the content
        int responseCode = complete(collection.name, filename, http);

        // Then - We get the expected response code
        assertEquals(HttpStatus.NOT_FOUND_404, responseCode);
    }

    public static int complete(String collectionName, String uri, Http http) throws IOException {
        Endpoint contentEndpoint = ZebedeeHost.complete.addPathSegment(collectionName).setParameter("uri", uri);
        Response<JsonObject> createResponse = http.post(contentEndpoint, "", JsonObject.class);
        return createResponse.statusLine.getStatusCode();
    }
}
