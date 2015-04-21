package com.github.onsdigital.test.api;

import com.github.davidcarboni.cryptolite.Random;
import com.github.onsdigital.http.Endpoint;
import com.github.onsdigital.http.Http;
import com.github.onsdigital.http.Response;
import com.github.onsdigital.junit.DependsOn;
import com.github.onsdigital.zebedee.json.CollectionDescription;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.Test;

import javax.ws.rs.POST;
import java.io.IOException;

import static org.junit.Assert.*;

@DependsOn(Complete.class)
public class Review {
    Http http = Login.httpPublisher;

    @Test
    public void shouldReviewWithPublisherCredentials() throws IOException {

        // Given - an existing piece of content that is set to complete
        CollectionDescription collection = Collection.create(http);
        String filename = "/" + Random.id() + ".json";
        Content.create(collection.name, "foo", filename, http);
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

    @POST
    @Test
    public void shouldReturn404IfNotComplete() throws IOException {

        // Given - a file is not listed in progress
        CollectionDescription collection = Collection.create(http);
        String filename = "/shouldReturn404IfNotInProgress/" + Random.id() + ".json";
        Content.create(collection.name, "foo", filename, 200, http);

        // When - we call review on the content
        int responseCode = review(collection.name, filename, http);

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
        Complete.complete(collection.name, filename, http);

        // When - we call review on the content
        int responseCode = review(collection.name, directory, http);

        // Then - We get the expected response code
        assertEquals(HttpStatus.BAD_REQUEST_400, responseCode);
    }

    @Test
    public void shouldReturn404IfUriIsAlreadyReviewed() throws IOException {

        // Given - a uri that is already set to review.
        CollectionDescription collection = Collection.create(http);
        String filename = Random.id() + ".json";
        Content.create(collection.name, "foo", filename, 200, http);
        Complete.complete(collection.name, filename, http);
        review(collection.name, filename, http);

        // When - we call review on the content
        int responseCode = review(collection.name, filename, http);

        // Then - We get the expected response code
        assertEquals(HttpStatus.NOT_FOUND_404, responseCode);
    }

    public static int review(String collectionName, String uri, Http http) throws IOException {
        Endpoint contentEndpoint = ZebedeeHost.review.addPathSegment(collectionName).setParameter("uri", uri);
        Response<String> createResponse = http.post(contentEndpoint, "", String.class);
        return createResponse.statusLine.getStatusCode();
    }
}
