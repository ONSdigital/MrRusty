package com.github.onsdigital.test.api;

import com.github.davidcarboni.restolino.framework.Api;
import com.github.onsdigital.http.Endpoint;
import com.github.onsdigital.http.Http;
import com.github.onsdigital.http.Response;
import com.github.onsdigital.junit.DependsOn;
import com.github.onsdigital.test.api.oneliners.OneLineSetups;
import com.github.onsdigital.test.json.CollectionDescription;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.Test;

import javax.ws.rs.POST;
import java.io.IOException;

import static org.junit.Assert.*;

/**
 * Created by kanemorgan on 31/03/2015.
 */
@Api
@DependsOn({Login.class, Collection.class, Content.class})
public class Transfer {

    /**
     * Test basic functionality
     */
    @POST
    @Test
    public void shouldTransferFileBetweenCollections() throws IOException {
        // Given
        // Two collections - one with an item of content
        CollectionDescription collection_1 = OneLineSetups.publishedCollectionWithContent(1);
        CollectionDescription collection_2 = OneLineSetups.publishedCollection();
        String fileUri = collection_1.inProgressUris.get(0);

        // When
        // we transfer that item
        Response<String> response = transfer(collection_1.id, collection_2.id, fileUri, Login.httpPublisher);

        // Then
        // We expect a response of OK
        assertEquals(HttpStatus.OK_200, response.statusLine.getStatusCode());

        // + the file has moved
        collection_1 = Collection.get(collection_1.id, Login.httpPublisher).body;
        collection_2 = Collection.get(collection_2.id, Login.httpPublisher).body;
        assertFalse(collection_1.inProgressUris.contains(fileUri));
        assertTrue(collection_2.inProgressUris.contains(fileUri));
    }

    /**
     * Test should return unauthorized without correct permissions
     */
    @POST
    @Test
    public void shouldReturnUnauthorizedWithoutPublisherPermissions() throws IOException {
        // Given
        // Two collections - one with items of content
        CollectionDescription collection_1 = OneLineSetups.publishedCollectionWithContent(3);
        CollectionDescription collection_2 = OneLineSetups.publishedCollection();

        // When
        // we try to transfer those items using non publisher accounts
        Response<String> response1 = transfer(collection_1.name, collection_2.name, collection_1.inProgressUris.get(0), Login.httpAdministrator);
        Response<String> response2 = transfer(collection_1.name, collection_2.name, collection_1.inProgressUris.get(1), Login.httpViewer);
        Response<String> response3 = transfer(collection_1.name, collection_2.name, collection_1.inProgressUris.get(2), Login.httpScallywag);

        // Then
        // We expect a response of Unauthorized each time
        assertEquals(HttpStatus.UNAUTHORIZED_401, response1.statusLine.getStatusCode());
        assertEquals(HttpStatus.UNAUTHORIZED_401, response2.statusLine.getStatusCode());
        assertEquals(HttpStatus.UNAUTHORIZED_401, response3.statusLine.getStatusCode());
    }

    /**
     * Testing Not Found is returned if origin collection doesn't exist
     */
    @POST
    @Test
    public void shouldReturnNotFoundIfOriginCollectionDoesntExist() throws IOException {
        // Given
        // Two collections - one with items of content
        CollectionDescription collection_1 = Collection.createCollectionDescription();
        CollectionDescription collection_2 = OneLineSetups.publishedCollection();

        // When
        // we try to transfer those items using non publisher accounts
        Response<String> response = transfer(collection_1.name, collection_2.name, "anything.json", Login.httpPublisher);

        // Then
        // We expect a response of Unauthorized each time
        assertEquals(HttpStatus.NOT_FOUND_404, response.statusLine.getStatusCode());
    }

    /**
     * Testing Not Found is returned if destination collection doesn't exist
     */
    @POST
    @Test
    public void shouldReturnNotFoundIfDestinationCollectionDoesntExist() throws IOException {
        // Given
        // Two collections - one with items of content
        CollectionDescription collection_1 = OneLineSetups.publishedCollectionWithContent(1);
        CollectionDescription collection_2 = Collection.createCollectionDescription();

        // When
        // we try to transfer those items using non publisher accounts
        Response<String> response = transfer(collection_1.name, collection_2.name, collection_1.inProgressUris.get(0), Login.httpPublisher);

        // Then
        // We expect a response of Not Found
        assertEquals(HttpStatus.NOT_FOUND_404, response.statusLine.getStatusCode());
    }

    /**
     * Testing what happens if destination collection already has a file with the name
     */
    @POST
    @Test
    public void shouldReturnConflictIfFileWithNameAlreadyExistsInDestination() throws IOException {
        // Given
        // Two collections with an identically named item of content
        // (if content is working you won't be able to do this but let us write the test anyway)
        CollectionDescription collection_1 = OneLineSetups.publishedCollectionWithContent(1);
        CollectionDescription collection_2 = OneLineSetups.publishedCollection();

        Response<String> response1 = Content.create(collection_2.name, "content", collection_1.inProgressUris.get(0), Login.httpPublisher);
        if(response1.statusLine.getStatusCode() != HttpStatus.OK_200) { return; } // Quit if this doesn't work

        // When
        // we try to transfer those items using non publisher accounts
        Response<String> response = transfer(collection_1.name, collection_2.name, collection_1.inProgressUris.get(0), Login.httpPublisher);

        // Then
        // We expect a response of Conflict each time
        assertEquals(HttpStatus.CONFLICT_409, response.statusLine.getStatusCode());
    }

    public static Response<String> transfer(String source, String destination, String uri, Http http) throws IOException {
        Endpoint transferUrl = ZebedeeHost.transfer;

//        Transfer
        TransferRequest transferRequest = new TransferRequest();
        transferRequest.source = source;
        transferRequest.destination = destination;
        transferRequest.uri = uri;

        return http.post(transferUrl, transferRequest, String.class);
    }

}


class TransferRequest {

    // the collections which the source uri needs to go from
    public String source;
    public String destination;
    // the uri of the resource to be moved
    public String uri;
}


