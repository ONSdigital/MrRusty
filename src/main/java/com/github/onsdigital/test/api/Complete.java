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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@DependsOn(Content.class)
public class Complete extends ZebedeeApiTest {


    /**
     * Test basic functionality
     */
    @POST
    @Test
    public void shouldCompleteWithPublisherCredentials() throws IOException {

        // Given - an existing collection with some content
        CollectionDescription collection = OneLineSetups.publishedCollectionWithContent(context, 1);
        String filename = collection.inProgressUris.get(0);

        // When - we call complete on the content
        Response<String> complete = complete(collection.id, filename, context.getPublisher());

        // Then
        // We get the expected response code
        // and the content is listed under complete when we get the collection.
        assertEquals(HttpStatus.OK_200, complete.statusLine.getStatusCode());

        CollectionDescription updatedCollection = Collection.get(collection.id, context.getPublisher()).body;
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
        CollectionDescription collection = OneLineSetups.publishedCollectionWithContent(context, 3);

        // When
        // We call complete on the content
        Response<String> complete2 = complete(collection.id, collection.inProgressUris.get(1), context.getViewer());
        Response<String> complete3 = complete(collection.id, collection.inProgressUris.get(2), context.getScallyWag());

        // Then
        // We expect unauthorised responses
        assertEquals(HttpStatus.UNAUTHORIZED_401, complete2.statusLine.getStatusCode());
        assertEquals(HttpStatus.UNAUTHORIZED_401, complete3.statusLine.getStatusCode());

        CollectionDescription updatedCollection = Collection.get(collection.id, context.getPublisher()).body;
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
        CollectionDescription collection = OneLineSetups.publishedCollection(context.getPublisher());
        String filename = "/shouldReturn404/" + Random.id() + ".json";

        // When - we call complete on the content
        Response<String> complete = complete(collection.id, filename, context.getPublisher());

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
        CollectionDescription collection = OneLineSetups.publishedCollectionWithContent(context, directory, 1);

        // When
        // we call complete on the directory not the file
        Response<String> complete = complete(collection.id, directory, context.getPublisher());

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
        CollectionDescription collection = OneLineSetups.publishedCollectionWithContent(context, 1);
        String fileUri = collection.inProgressUris.get(0);
        complete(collection.id, fileUri, context.getPublisher());

        // When
        // we call complete on the content
        Response<String> complete = complete(collection.id, fileUri, context.getPublisher());

        // Then
        // We get the expected response code
        assertEquals(HttpStatus.NOT_FOUND_404, complete.statusLine.getStatusCode());
    }

    /**
     * Convenience method to complete content in one line
     *
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

    /**
     * Convenience method to complete content in one line
     *
     * @param collectionDescription
     * @param http
     * @throws IOException
     */
    public static int completeAll(CollectionDescription collectionDescription, Http http) throws IOException {
        if (collectionDescription.inProgressUris != null) {
            for (String uri : collectionDescription.inProgressUris) {
                Response<String> complete = complete(collectionDescription.id, uri, http);
                if (complete.statusLine.getStatusCode() != 200) {
                    System.out.println("Error " + complete.statusLine.getStatusCode() + " could not complete uri " + uri);
                    return complete.statusLine.getStatusCode();
                }
            }
        }
        return HttpStatus.OK_200;
    }
}
