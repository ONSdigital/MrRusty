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

    /**
     * Test basic functionality
     */
    @POST
    @Test
    public void shouldReviewWithPublisherCredentials() throws IOException {

        // Given - an existing piece of content that is set to complete
        CollectionDescription collection = Collection.createCollectionDescription();
        Collection.post(collection, Login.httpPublisher);
        String filename = "/" + Random.id() + ".json";
        Content.create(collection.name, "shouldReviewWithPublisherCredentials", filename, Login.httpPublisher);
        Complete.complete(collection.name, filename, Login.httpPublisher);

        // When - we call review on the content
        int responseCode = review(collection.name, filename, Login.httpPublisher);

        // Then - We get the expected response code
        assertEquals(HttpStatus.OK_200, responseCode);

        // and the content is listed under review when we get the collection.
        CollectionDescription updatedCollection = Collection.get(collection.name, Login.httpPublisher).body;
        assertTrue(updatedCollection.reviewedUris.contains(filename));
        assertFalse(updatedCollection.completeUris.contains(filename));
        assertFalse(updatedCollection.inProgressUris.contains(filename));
    }

    /**
     * Test permissions return Unauthorized for non publishers
     */
    @POST
    @Test
    public void shouldNotReviewWithoutPublisherCredentials() throws IOException {

        // Given
        // content that is set to complete
        CollectionDescription collection = Collection.createCollectionDescription();
        Collection.post(collection, Login.httpPublisher);
        String filename = "/" + Random.id() + ".json";
        Content.create(collection.name, "shouldReviewWithPublisherCredentials", "/Administrator" + filename, Login.httpPublisher);
        Content.create(collection.name, "shouldReviewWithPublisherCredentials", "/Viewer" + filename, Login.httpPublisher);
        Content.create(collection.name, "shouldReviewWithPublisherCredentials", "/Scallywag" + filename, Login.httpPublisher);
        Complete.complete(collection.name, "/Administrator" +filename, Login.httpPublisher);
        Complete.complete(collection.name, "/Viewer" + filename, Login.httpPublisher);
        Complete.complete(collection.name, "/Scallywag" +filename, Login.httpPublisher);

        // When
        // we call review on the content with different users
        int responseCode1 = review(collection.name, "/Administrator" + filename, Login.httpAdministrator);
        int responseCode2 = review(collection.name, "/Viewer" + filename, Login.httpViewer);
        int responseCode3 = review(collection.name, "/Scallywag" + filename, Login.httpScallywag);

        // Then - We get the expected response code
        assertEquals(HttpStatus.UNAUTHORIZED_401, responseCode1);
        assertEquals(HttpStatus.UNAUTHORIZED_401, responseCode2);
        assertEquals(HttpStatus.UNAUTHORIZED_401, responseCode3);
    }

    /**
     * Test responds Not Found if file does not exist
     */
    @POST
    @Test
    public void shouldReturnNotFoundIfNoSuchFile() throws IOException {

        // Given - a collection + a random file name
        CollectionDescription collection = Collection.createCollectionDescription();
        Collection.post(collection, Login.httpPublisher);

        String filename = "/shouldReturnNotFoundIfNoSuchFile/" + Random.id() + ".json";

        // When - we call review on the content
        int responseCode = review(collection.name, filename, Login.httpPublisher);

        // Then - We get the expected response code
        assertEquals(HttpStatus.NOT_FOUND_404, responseCode);
    }

    /**
     * Test responds Bad Request if file is not in complete state
     */
    @POST
    @Test
    public void shouldReturnBadRequestIfNotComplete() throws IOException {

        // Given - a file is not listed in progress
        CollectionDescription collection = Collection.createCollectionDescription();
        Collection.post(collection, Login.httpPublisher);

        String filename = "/shouldReturn400IfNotInProgress/" + Random.id() + ".json";
        Content.create(collection.name, "shouldReturn400", filename, Login.httpPublisher);

        // When - we call review on the content
        int responseCode = review(collection.name, filename, Login.httpPublisher);

        // Then - We get the expected response code
        assertEquals(HttpStatus.BAD_REQUEST_400, responseCode);
    }

    /**
     * Test responds Bad Request if uri given is to a directory not a file
     */
    @POST
    @Test
    public void shouldReturnBadRequestIfGivenUriIsADirectory() throws IOException {

        // Given - a directory that exists in progress
        CollectionDescription collection = Collection.createCollectionDescription();
        Collection.post(collection, Login.httpPublisher);

        String directory = "/shouldReturnBadRequest/";
        String filename = directory + Random.id() + ".json";
        Content.create(collection.name, "shouldReturnBadRequest", filename, Login.httpPublisher);
        Complete.complete(collection.name, filename, Login.httpPublisher);

        // When - we call review on the content
        int responseCode = review(collection.name, directory, Login.httpPublisher);

        // Then - We get the expected response code
        assertEquals(HttpStatus.BAD_REQUEST_400, responseCode);
    }

    /**
     * Test responds Bad Request if file is already reviewed
     */
    @Test
    public void shouldReturnBadRequestIfUriIsAlreadyReviewed() throws IOException {

        // Given - a uri that is already set to review.
        CollectionDescription collection = Collection.createCollectionDescription(); // Create collection
        Collection.post(collection, Login.httpPublisher);
        String filename = Random.id() + ".json"; // Add content
        Content.create(collection.name, "shouldReturnNotFound", filename, Login.httpPublisher);
        Complete.complete(collection.name, filename, Login.httpPublisher); // Complete
        review(collection.name, filename, Login.httpPublisher); // Review

        // When - we call review on the content
        int responseCode = review(collection.name, filename, Login.httpPublisher);

        // Then - We get the expected response code
        assertEquals(HttpStatus.BAD_REQUEST_400, responseCode);
    }

    public static int review(String collectionName, String uri, Http http) throws IOException {
        Endpoint contentEndpoint = ZebedeeHost.review.addPathSegment(collectionName).setParameter("uri", uri);
        Response<String> createResponse = http.post(contentEndpoint, "", String.class);
        return createResponse.statusLine.getStatusCode();
    }
}
