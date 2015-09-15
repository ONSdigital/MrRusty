package com.github.onsdigital.test.api;

import com.github.davidcarboni.cryptolite.Random;
import com.github.davidcarboni.restolino.framework.Api;
import com.github.onsdigital.Scripts;
import com.github.onsdigital.http.Endpoint;
import com.github.onsdigital.http.Host;
import com.github.onsdigital.http.Http;
import com.github.onsdigital.http.Response;
import com.github.onsdigital.junit.DependsOn;
import com.github.onsdigital.test.api.oneliners.OneLineSetups;
import com.github.onsdigital.test.api.oneliners.OneShot;
import com.github.onsdigital.test.configuration.Configuration;
import com.github.onsdigital.test.json.CollectionDescription;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.Test;

import javax.ws.rs.POST;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by thomasridd on 05/06/2015.
 */

@Api
@DependsOn(com.github.onsdigital.test.api.Permissions.class)
public class Publish {
    public static final Host florenceHost = new Host(Configuration.getFlorenceUrl());
    public static final Endpoint florenceContent = new Endpoint(florenceHost, "");

    /**
     * Tests approval using simple collection setup and publisher credentials
     *
     * This is for original style files with type = "dataset"
     */
    @POST
    @Test
    public void dataPublisher_givenDataset_shouldPublishCSDBFiles() throws IOException {

        // Given
        // a collection that we add files to and then
        CollectionDescription collection = OneLineSetups.publishedCollection();

        File json = new File("src/main/resources/dummy_dataset/data.json");
        File csdb = new File("src/main/resources/dummy_dataset/dummy_csdb.csdb");


        String baseUri = "/archive/" + Random.id() + "/datasets/dataset";
        Content.upload(collection.id, baseUri + "/data.json", json, Login.httpPublisher);
        Content.upload(collection.id, baseUri + "/CXNV.csdb", csdb, Login.httpPublisher);

        Complete.complete(collection.id, baseUri + "/data.json", Login.httpPublisher);
        Review.review(collection.id, baseUri + "/data.json", Login.httpSecondSetOfEyes);
        Approve.approve(collection.id, Login.httpPublisher);

        // When
        // we approve it using publish credentials
        Response<String> response = publishWithBreak(collection.id, Login.httpPublisher);

        // Expect
        // a response of okay
        assertEquals(HttpStatus.OK_200, response.statusLine.getStatusCode());
    }

    /**
     * Tests approval using simple collection setup and publisher credentials
     *
     * This is for advanced style files with type = "timeseries_dataset"
     */
    @POST
    @Test
    public void dataPublisher_givenTimeseriesDataset_shouldPublishCSDBFiles() throws IOException {

        // Given
        // a collection
        CollectionDescription collection = OneLineSetups.publishedCollection();

        File json = new File("src/main/resources/dummy_timeseriesdataset/data.json");
        File csdb = new File("src/main/resources/dummy_timeseriesdataset/dummy_csdb.csdb");

        String baseUri = "/archive/" + Random.id() + "/datasets/timeseriesdataset";
        Content.upload(collection.id, baseUri + "/data.json", json, Login.httpPublisher);
        Content.upload(collection.id, baseUri + "/CXNV.csdb", csdb, Login.httpPublisher);

        Complete.complete(collection.id, baseUri + "/data.json", Login.httpPublisher);
        Review.review(collection.id, baseUri + "/data.json", Login.httpSecondSetOfEyes);
        Approve.approve(collection.id, Login.httpPublisher);

        // When
        // we approve it using publish credentials
        Response<String> response = publishWithBreak(collection.id, Login.httpPublisher);

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
        Response<String> response = publishWithBreak(collection.id, Login.httpPublisher);
    }

    @Test
    public void manualPublishPipeline_givenMegaCollection_shouldPublishToFlorenceInUnder60Seconds() throws Exception {
        // Given
        // a sample collection
        System.out.println("Manual Publish: Building mother of all collections");
        final CollectionDescription sample = Scripts.buildReviewedCustomCollection(5, 5, 2, Login.httpPublisher, Login.httpSecondSetOfEyes);
        System.out.println("Manual Publish: Approving m.o.a.c");
        Approve.approve(sample.id, Login.httpPublisher);

        List<String> uris = randomUrisFromCollection(sample, 10);
        System.out.println("Manual Publish: Mother of all collections built - starting publish");

        long start = System.currentTimeMillis();

        // When
        // we start a publish asynchronously
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Login.httpPublisher.justPost(ZebedeeHost.publish.addPathSegment(sample.id));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        // Then
        // We poll every five seconds to see if the files reach remote system
        boolean urisArePublished = false;
        while(System.currentTimeMillis() - start < 60000) {

            urisArePublished = false;
            if (urisArePublishedToFlorence(uris)) {
                urisArePublished = true;
                System.out.println(System.currentTimeMillis() - start + "ms: published csdb");
                break;
            }
            Thread.sleep(1000);
        }

        assertTrue(urisArePublished);
    }
    private boolean urisArePublishedToFlorence(List<String> uris) throws IOException {

        for (String uri: uris) {
            Response<Path> pathResponse = OneShot.httpPublisher.get(florenceContent.addPathSegment(uri));
            if (pathResponse.statusLine.getStatusCode() != HttpStatus.OK_200) {
                return false;
            }
        }
        return true;
    }
    private List<String> randomUrisFromCollection(CollectionDescription collection, int count) {
        List<String> uris = collection.reviewedUris;

        if (uris.size() <= count) { return uris; }

        java.util.Collections.shuffle(uris);

        return uris.subList(0, count);
    }

    public static Response<String> publishWithBreak(String collectionID, Http http) throws IOException {
        Endpoint endpoint = ZebedeeHost.publish.addPathSegment(collectionID).setParameter("breakbeforefiletransfer", "true");
        return http.post(endpoint, null, String.class);
    }

    public static Response<String> publish(String collectionID, Http http) throws IOException {
        Endpoint endpoint = ZebedeeHost.publish.addPathSegment(collectionID);
        return http.post(endpoint, null, String.class);
    }


}
