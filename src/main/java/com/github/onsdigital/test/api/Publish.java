package com.github.onsdigital.test.api;

import com.github.davidcarboni.cryptolite.Random;
import com.github.davidcarboni.restolino.framework.Api;
import com.github.onsdigital.http.Endpoint;
import com.github.onsdigital.http.Host;
import com.github.onsdigital.http.Http;
import com.github.onsdigital.http.Response;
import com.github.onsdigital.junit.DependsOn;
import com.github.onsdigital.test.Context;
import com.github.onsdigital.test.api.oneliners.OneLineSetups;
import com.github.onsdigital.test.base.ZebedeeApiTest;
import com.github.onsdigital.test.configuration.Configuration;
import com.github.onsdigital.test.json.CollectionDescription;
import com.github.onsdigital.test.json.CollectionType;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.Test;

import javax.ws.rs.POST;
import java.io.File;
import java.io.IOException;

import static com.github.onsdigital.test.AssertResponse.assertOk;
import static org.junit.Assert.*;

/**
 * Created by thomasridd on 05/06/2015.
 */

@Api
@DependsOn({Approve.class, Permissions.class})
public class Publish extends ZebedeeApiTest {

    public static final Host florenceHost = new Host(Configuration.getFlorenceUrl());

    /**
     * Tests approval using simple collection setup and publisher credentials
     *
     * This is for original style files with type = "dataset"
     */
    @POST
    @Test
    public void dataPublisher_givenDataset_shouldPublishXls() throws IOException {

        // Given
        // a collection that we add files to and then
        CollectionDescription collection = OneLineSetups.publishedCollection(context.getPublisher());

        File jsonParent = new File("src/main/resources/dummy_dataset/data.json");
        File json = new File("src/main/resources/dummy_dataset/2015/data.json");
        File csdb = new File("src/main/resources/dummy_dataset/2015/3a82caed.xls");


        String baseUri = "/archive/" + Random.id() + "/datasets/dataset";
        Content.upload(collection.id, baseUri + "/data.json", jsonParent, context.getPublisher());
        Content.upload(collection.id, baseUri + "/2015/data.json", json, context.getPublisher());
        Content.upload(collection.id, baseUri + "/2015/3a82caed.xls", csdb, context.getPublisher());

        Complete.complete(collection.id, baseUri + "/data.json", context.getPublisher());
        Complete.complete(collection.id, baseUri + "/2015/data.json", context.getPublisher());

        Review.review(collection.id, baseUri + "/data.json", context.getSecondSetOfEyes());
        Review.review(collection.id, baseUri + "/2015/data.json", context.getSecondSetOfEyes());
        Approve.approve(collection.id, context.getPublisher());

        // When
        // we approve it using publish credentials
        Response<String> response = publishWithBreak(collection.id, context.getPublisher());

        // Expect
        // a response of okay
        assertEquals("Publish failed", HttpStatus.OK_200, response.statusLine.getStatusCode());
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
        CollectionDescription collection = OneLineSetups.publishedCollection(context.getPublisher());

        File jsonParent = new File("src/main/resources/dummy_timeseriesdataset/data.json");
        File json = new File("src/main/resources/dummy_timeseriesdataset/2015/data.json");
        File csdb = new File("src/main/resources/dummy_timeseriesdataset/2015/dummy_csdb.csdb");

        String baseUri = "/archive/" + Random.id() + "/datasets/timeseriesdataset";
        Content.upload(collection.id, baseUri + "/data.json", jsonParent, context.getPublisher());
        Content.upload(collection.id, baseUri + "/2015/data.json", json, context.getPublisher());
        Content.upload(collection.id, baseUri + "/2015/CXNV.csdb", csdb, context.getPublisher());

        assertOk(Complete.complete(collection.id, baseUri + "/data.json", context.getPublisher()));
        assertOk(Complete.complete(collection.id, baseUri + "/2015/data.json", context.getPublisher()));

        assertOk(Review.review(collection.id, baseUri + "/data.json", context.getSecondSetOfEyes()));
        assertOk(Review.review(collection.id, baseUri + "/2015/data.json", context.getSecondSetOfEyes()));
        assertOk(Approve.approve(collection.id, context.getPublisher()));

        // When
        // we approve it using publish credentials
        Response<String> response = publishWithBreak(collection.id, context.getPublisher());

        // Expect
        // a response of okay
        assertEquals(HttpStatus.OK_200, response.statusLine.getStatusCode());
    }

    @POST
    @Test
    public void zebedee_whenPublishCalled_shouldPostToTheDestination() throws IOException {
        // Given
        // a collection that we add files to and then
        CollectionDescription collection = OneLineSetups.publishedCollectionWithContent(context, "/rusty/", 2);

        assertNotNull(collection);

        assertEquals("Complete failed", HttpStatus.OK_200, Complete.completeAll(collection, context.getPublisher()));
        collection = Collection.get(collection.id, context.getPublisher()).body;

        assertEquals("Review failed", HttpStatus.OK_200, Review.reviewAll(collection, context.getSecondSetOfEyes()));
        assertEquals("Approve failed", HttpStatus.OK_200, Approve.approve(collection.id, context.getPublisher()).statusLine.getStatusCode());

        // When
        // we approve it using publish credentials
        Response<String> response = publishWithBreak(collection.id, context.getPublisher());

        // Expect
        // a response of okay
        assertEquals("Publish failed", HttpStatus.OK_200, response.statusLine.getStatusCode());
    }

    @POST
    @Test
    public void timedPublish_ifCollectionNotApproved_shouldRevertToManual() throws IOException, InterruptedException {
        // Given
        // a collection that we add files to and then
        CollectionDescription collection = OneLineSetups.scheduledCollectionWithContent(context, "/rusty/", 2, 5);

        assertNotNull(collection);

        // When
        // we approve it using publish credentials
        Thread.sleep(6000);
        collection = Collection.get(collection.id, context.getPublisher()).body;

        // Expect
        // a response of okay
        assertEquals(CollectionType.manual, collection.type);
    }

    public static Response<String> publishWithBreak(String collectionID, Http http) throws IOException {
        Endpoint endpoint = ZebedeeHost.publish
                .addPathSegment(collectionID)
                .setParameter("breakbeforefiletransfer", "true")
                .setParameter("skipVerification", "true");
        return http.post(endpoint, null, String.class);
    }

    public static Response<String> publish(String collectionID, Http http) throws IOException {
        Endpoint endpoint = ZebedeeHost.publish.addPathSegment(collectionID).setParameter("skipVerification", "true");
        return http.post(endpoint, null, String.class);
    }

    /**
     * Publish the collection for the given id and poll the api to check when the publish is complete.
     * @param collectionID
     * @param httpPublisher
     * @throws IOException
     */
    public static void publishAndWait(Context context, String collectionID, Http httpPublisher, int secondsToWait) throws IOException {
        publish(collectionID, httpPublisher);

        int count = 0;
        boolean published = false;

        while (count < secondsToWait) {
            Response<CollectionDescription> response = Collection.get(collectionID, context.getPublisher());

            if (response.statusLine.getStatusCode() == 404 || response.body.publishComplete) {
                published = true;
                break;
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }

            count++;
        }

        if (!published)
            fail("Collection was not published");
    }
}
