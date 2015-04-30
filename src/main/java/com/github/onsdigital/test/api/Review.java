package com.github.onsdigital.test.api;

import com.github.davidcarboni.cryptolite.Random;
import com.github.onsdigital.http.Endpoint;
import com.github.onsdigital.http.Http;
import com.github.onsdigital.http.Response;
import com.github.onsdigital.junit.DependsOn;
import com.github.onsdigital.test.api.oneliners.OneLineSetups;
import com.github.onsdigital.zebedee.json.CollectionDescription;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.Test;

import javax.ws.rs.POST;
import java.io.IOException;

import static org.junit.Assert.*;

@DependsOn(Complete.class)
public class Review {

    /**
     *
     * Test basic functionality
     */
    @POST
    @Test
    public void shouldReturnOkayOnReview() throws IOException {

        // Given
        // an existing piece of content that is set to complete by publisher Alice
        CollectionDescription collection = OneLineSetups.publishedCollectionWithContent(1);
        String filename = collection.inProgressUris.get(0);


        Http httpPublisherAlice = OneLineSetups.newSessionWithPublisherPermissions();
        Response<String> complete = Complete.complete(collection.name, filename, httpPublisherAlice);

        // When
        // publisher Bob calls review on the content
        Http httpPublisherBob = OneLineSetups.newSessionWithPublisherPermissions();
        Response<String> response = review(collection.name, filename, httpPublisherBob);

        // Then
        // We get the okay response code
        assertEquals(HttpStatus.OK_200, response.statusLine.getStatusCode());

        // and the content is listed under review when we get the collection.
        CollectionDescription updatedCollection = Collection.get(collection.id, Login.httpPublisher).body;
        assertTrue(updatedCollection.reviewedUris.contains(filename));
        assertFalse(updatedCollection.completeUris.contains(filename));
        assertFalse(updatedCollection.inProgressUris.contains(filename));
    }

    /**
     * Second set of eyes functionality
     *
     * Users that send work from 'in progress' to 'complete' are not allowed to send from 'complete' to 'reviewed'
     */
    @POST
    @Test
    public void shouldReturnUnauthorizedWhenCompleterAttemptsReview() throws IOException {

        // Given
        // an existing piece of content that is set to complete
        CollectionDescription collection = OneLineSetups.publishedCollectionWithContent(1);
        String filename = collection.inProgressUris.get(0);

        Complete.complete(collection.name, filename, Login.httpPublisher);

        // When
        // we call review on the content
        Response<String> response = review(collection.name, filename, Login.httpPublisher);

        // Then
        // We get the okay response code
        assertEquals(HttpStatus.UNAUTHORIZED_401,  response.statusLine.getStatusCode());
    }

    /**
     * Test permissions return Unauthorized for non publishers
     */
    @POST
    @Test
    public void shouldNotReviewWithoutPublisherCredentials() throws IOException {

        // Given
        // content that is set to complete
        CollectionDescription collection = OneLineSetups.publishedCollectionWithContent(3);
        Complete.complete(collection.id, collection.inProgressUris.get(0), Login.httpPublisher);
        Complete.complete(collection.id, collection.inProgressUris.get(1), Login.httpPublisher);
        Complete.complete(collection.id, collection.inProgressUris.get(2), Login.httpPublisher);

        // When
        // we call review on the content with different users

        Response<String> response1 = review(collection.name, collection.inProgressUris.get(0), Login.httpAdministrator);
        Response<String> response2 = review(collection.name, collection.inProgressUris.get(1), Login.httpViewer);
        Response<String> response3 = review(collection.name, collection.inProgressUris.get(2), Login.httpScallywag);

        // Then - We get the expected response code
        assertEquals(HttpStatus.UNAUTHORIZED_401, response1.statusLine.getStatusCode());
        assertEquals(HttpStatus.UNAUTHORIZED_401, response2.statusLine.getStatusCode());
        assertEquals(HttpStatus.UNAUTHORIZED_401, response3.statusLine.getStatusCode());
    }

    /**
     * Test responds Not Found if file does not exist
     */
    @POST
    @Test
    public void shouldReturnNotFoundIfNoSuchFile() throws IOException {

        // Given - a collection + a random file name
        CollectionDescription collection = OneLineSetups.publishedCollection();

        String filename = "/shouldReturnNotFoundIfNoSuchFile/" + Random.id() + ".json";

        // When - we call review on the content
        Response<String> response = review(collection.name, filename, Login.httpPublisher);

        // Then - We get the expected response code
        assertEquals(HttpStatus.NOT_FOUND_404, response.statusLine.getStatusCode());
    }

    /**
     * Test responds Bad Request if file is not in complete state
     */
    @POST
    @Test
    public void shouldReturnBadRequestIfNotComplete() throws IOException {

        // Given
        // a collection with a file not listed as complete
        CollectionDescription collection = OneLineSetups.publishedCollectionWithContent(1);

        // When
        // we call review on the content
        Response<String> response = review(collection.name, collection.inProgressUris.get(0), Login.httpPublisher);

        // Then - We get the expected response code
        assertEquals(HttpStatus.BAD_REQUEST_400, response.statusLine.getStatusCode());
    }

    /**
     * Test responds Bad Request if uri given is to a directory not a file
     */
    @POST
    @Test
    public void shouldReturnBadRequestIfGivenUriIsADirectory() throws IOException {

        // Given
        // a collection that has complete content in a directory
        CollectionDescription collection = OneLineSetups.publishedCollectionWithContent("/directory/", 1);
        Complete.complete(collection.id, collection.inProgressUris.get(0), Login.httpPublisher);

        // When
        // we call review on the directory alone
        Response<String> response = review(collection.name, "/directory/", Login.httpPublisher);


        // Then
        // We get a bad request error code
        assertEquals(HttpStatus.BAD_REQUEST_400, response.statusLine.getStatusCode());
    }

    /**
     * Test responds Bad Request if file is already reviewed
     */
    @Test
    public void shouldReturnBadRequestIfUriIsAlreadyReviewed() throws IOException {

        // Given
        // a uri that is already set to review.
        CollectionDescription collection = OneLineSetups.publishedCollectionWithContent("/directory/", 1);
        String uri = collection.inProgressUris.get(0);

        Complete.complete(collection.id, uri, Login.httpPublisher);
        review(collection.id, uri, Login.httpPublisher); // Review

        // When
        // we call review on the content

        Response<String> response = review(collection.name, uri, Login.httpPublisher);

        // Then
        // We get a bad request error code
        assertEquals(HttpStatus.BAD_REQUEST_400, response.statusLine.getStatusCode());
    }

    public static Response<String> review(String collectionName, String uri, Http http) throws IOException {
        Endpoint contentEndpoint = ZebedeeHost.review.addPathSegment(collectionName).setParameter("uri", uri);
        return http.post(contentEndpoint, "", String.class);
    }
}
