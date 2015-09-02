package com.github.onsdigital.test.api;

import com.github.davidcarboni.cryptolite.Random;
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
import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by thomasridd on 05/06/2015.
 */

@Api
@DependsOn(com.github.onsdigital.test.api.Permissions.class)
public class Publish {

    /**
     * Tests approval using simple collection setup and publisher credentials
     *
     */
    @POST
    @Test
    public void shouldPublishCSDBFilesToLaunchpad() throws IOException {

        // Given
        // a collection that we add files to and then
        CollectionDescription collection = OneLineSetups.publishedCollection();

        File json = new File("src/main/resources/dummy_csdb/data.json");
        File csdb = new File("src/main/resources/dummy_csdb/dummy_csdb.csdb");


        String baseUri = "/" + Random.id() + "/datasets/shouldPublishCSDBFilesToLaunchpad";
        Content.upload(collection.id, baseUri + "/data.json", json, Login.httpPublisher);
        Content.upload(collection.id, baseUri + "/CXNV.csdb", csdb, Login.httpPublisher);

        Complete.complete(collection.id, baseUri + "/data.json", Login.httpPublisher);
        Review.review(collection.id, baseUri + "/data.json", Login.httpSecondSetOfEyes);
        Approve.approve(collection.id, Login.httpPublisher);

        // When
        // we approve it using publish credentials
        Response<String> response = publish(collection.id, Login.httpPublisher);

        // Expect
        // a response of okay
        assertEquals(HttpStatus.OK_200, response.statusLine.getStatusCode());
    }

    /**
     * Tests approval using simple collection setup and publisher credentials
     *
     */
    @POST
    //@Test
    public void shouldPublishCSDBFilesWithoutExtensionToLaunchpad() throws IOException {

        // Given
        // a collection
        CollectionDescription collection = OneLineSetups.publishedCollection();

        File json = new File("src/main/resources/dummy_csdb_no_extension/data.json");
        File csdb = new File("src/main/resources/dummy_csdb_no_extension/dummy_csdb");

        String baseUri = Random.id() + "/datasets/shouldPublishCSDBFilesWithoutExtensionToLaunchpad";

        Content.upload(collection.id, baseUri + "/data.json", json, Login.httpPublisher);
        Content.upload(collection.id, baseUri + "/OTT", csdb, Login.httpPublisher);


        Complete.complete(collection.id, baseUri + "/data.json", Login.httpPublisher);

        Review.reviewAll(Collection.get(collection.id, Login.httpPublisher).body, Login.httpThirdSetOfEyes);

        Approve.approve(collection.id, Login.httpPublisher);

        // When
        // we approve it using publish credentials
        Response<String> response = publish(collection.id, Login.httpPublisher);


        // Expect
        // a response of okay
        assertEquals(HttpStatus.OK_200, response.statusLine.getStatusCode());
    }

    @POST
    @Test
    public void zebedee_whenPublishCalled_shouldPostToTheDestination() throws IOException {
        // Given
        // a collection that we add files to and then
        CollectionDescription collection = OneLineSetups.publishedCollectionWithContent("/economy/", 2);

        assertNotNull(collection);

        assertEquals(HttpStatus.OK_200, Complete.completeAll(collection, Login.httpPublisher));
        collection = Collection.get(collection.id, Login.httpPublisher).body;

        assertEquals(HttpStatus.OK_200, Review.reviewAll(collection, Login.httpSecondSetOfEyes));
        assertEquals(HttpStatus.OK_200, Approve.approve(collection.id, Login.httpPublisher).statusLine.getStatusCode());

        // When
        // we approve it using publish credentials
        Response<String> response = publish(collection.id, Login.httpPublisher);
    }

    private static Response<String> publish(String collectionID, Http http) throws IOException {
        Endpoint endpoint = ZebedeeHost.publish.addPathSegment(collectionID).setParameter("breakbeforefiletransfer", "false");
        return http.post(endpoint, null, String.class);
    }
}
