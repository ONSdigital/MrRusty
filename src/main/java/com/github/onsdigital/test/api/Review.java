package com.github.onsdigital.test.api;

import com.github.davidcarboni.cryptolite.Random;
import com.github.onsdigital.http.Endpoint;
import com.github.onsdigital.http.Http;
import com.github.onsdigital.http.Response;
import com.github.onsdigital.junit.DependsOn;
import com.github.onsdigital.test.api.oneliners.OneLineSetups;
import com.github.onsdigital.test.base.ZebedeeApiTest;
import com.github.onsdigital.test.json.CollectionDescription;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.Test;

import javax.ws.rs.POST;
import java.io.IOException;

import static org.junit.Assert.*;

@DependsOn(Complete.class)
public class Review extends ZebedeeApiTest {

    /**
     *
     * Test basic functionality
     */
    @POST
    @Test
    public void shouldReturnOkayOnReview() throws IOException {

        // Given
        // an existing piece of content that is set to complete by publisher Alice
        CollectionDescription collection = OneLineSetups.publishedCollectionWithContent(context, 1);
        String filename = collection.inProgressUris.get(0);

        Response<String> complete = Complete.complete(collection.id, filename, context.getPublisher());

        // When
        // publisher Bob calls review on the content
        Response<String> response = review(collection.id, filename, context.getSecondSetOfEyes());

        // Then
        // We get the okay response code
        assertEquals(HttpStatus.OK_200, response.statusLine.getStatusCode());

        // and the content is listed under review when we get the collection.
        CollectionDescription updatedCollection = Collection.get(collection.id, context.getPublisher()).body;
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
        CollectionDescription collection = OneLineSetups.publishedCollectionWithContent(context, 1);
        String filename = collection.inProgressUris.get(0);

        Complete.complete(collection.id, filename, context.getPublisher());

        // When
        // we call review on the content
        Response<String> response = review(collection.id, filename, context.getPublisher());

        // Then
        // We get the okay response code
        assertEquals(HttpStatus.UNAUTHORIZED_401, response.statusLine.getStatusCode());
    }

    /**
     * Test permissions return Unauthorized for non publishers
     */
    @POST
    @Test
    public void shouldNotReviewWithoutPublisherCredentials() throws IOException {

        // Given
        // content that is set to complete
        CollectionDescription collection = OneLineSetups.publishedCollectionWithContent(context, 2);
        Complete.complete(collection.id, collection.inProgressUris.get(0), context.getPublisher());
        Complete.complete(collection.id, collection.inProgressUris.get(1), context.getPublisher());

        // When
        // we call review on the content with different users

        Response<String> response2 = review(collection.id, collection.inProgressUris.get(0), context.getViewer());
        Response<String> response3 = review(collection.id, collection.inProgressUris.get(1), context.getScallyWag());

        // Then - We get the expected response code
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
        CollectionDescription collection = OneLineSetups.publishedCollection(context.getPublisher());

        String filename = Random.id() + ".json";

        // When - we call review on the content
        Response<String> response = review(collection.name, filename, context.getSecondSetOfEyes());

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
        CollectionDescription collection = OneLineSetups.publishedCollectionWithContent(context, 1);

        // When
        // we call review on the content
        Response<String> response = review(collection.id, collection.inProgressUris.get(0), context.getSecondSetOfEyes());

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
        CollectionDescription collection = OneLineSetups.publishedCollectionWithContent(context, "/directory/", 1);
        Complete.complete(collection.id, collection.inProgressUris.get(0), context.getPublisher());

        // When
        // we call review on the directory alone
        Response<String> response = review(collection.id, "/directory/", context.getSecondSetOfEyes());


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
        CollectionDescription collection = OneLineSetups.publishedCollectionWithContent(context, "/directory/", 1);
        String uri = collection.inProgressUris.get(0);

        Complete.complete(collection.id, uri, context.getPublisher());
        review(collection.id, uri, context.getSecondSetOfEyes()); // Review

        // When
        // we call review on the content again
        Response<String> response = review(collection.id, uri, context.getThirdSetOfEyes());

        // Then
        // We get a bad request error code
        assertEquals(HttpStatus.BAD_REQUEST_400, response.statusLine.getStatusCode());
    }

    public static Response<String> review(String collectionID, String uri, Http http) throws IOException {
        Endpoint contentEndpoint = ZebedeeHost.review.addPathSegment(collectionID).setParameter("uri", uri);
        return http.post(contentEndpoint, "", String.class);
    }

    public static int reviewAll(CollectionDescription collectionDescription, Http http) throws IOException {
        if (collectionDescription.completeUris != null) {
            for (String uri : collectionDescription.completeUris) {
                Response<String> stringResponse = review(collectionDescription.id, uri, http);
                if (stringResponse.statusLine.getStatusCode() != HttpStatus.OK_200) {
                    System.out.println("Error " + stringResponse.statusLine.getStatusCode() + " in reviewAll");
                    return stringResponse.statusLine.getStatusCode();
                }
            }
        }
        return HttpStatus.OK_200;
    }
}
