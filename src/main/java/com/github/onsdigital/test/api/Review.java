package com.github.onsdigital.test.api;

import com.github.davidcarboni.cryptolite.Random;
import com.github.onsdigital.http.Endpoint;
import com.github.onsdigital.http.Http;
import com.github.onsdigital.http.Response;
import com.github.onsdigital.zebedee.json.CollectionDescription;
import com.google.gson.JsonObject;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

public class Review {
    Http http = Login.httpAdministrator;

    @Test
    public void shouldReview() throws IOException {

        // Given - an existing piece of content that is set to complete
        CollectionDescription collection = Collection.create(http);
        String filename = "/" + Random.id() + ".json";
        Content.create(collection.name, "foo", filename, 200, http);
        Complete.complete(collection.name, filename, http);

        // When - we call review on the content
        int responseCode = review(collection.name, filename, http);

        // Then - We get the expected response code
        assertEquals(HttpStatus.OK_200, responseCode);

        // and the content is listed under review when we get the collection.
        CollectionDescription updatedCollection = Collection.get(collection.name, http);
        assertTrue(updatedCollection.reviewedUris.contains(filename));
        assertFalse(updatedCollection.completeUris.contains(filename));
        assertFalse(updatedCollection.inProgressUris.contains(filename));
    }

    public static int review(String collectionName, String uri, Http http) throws IOException {
        Endpoint contentEndpoint = ZebedeeHost.review.addPathSegment(collectionName).setParameter("uri", uri);
        Response<JsonObject> createResponse = http.post(contentEndpoint, "", JsonObject.class);
        return createResponse.statusLine.getStatusCode();
    }
}
