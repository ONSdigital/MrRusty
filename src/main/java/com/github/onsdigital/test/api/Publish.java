package com.github.onsdigital.test.api;

import com.github.davidcarboni.restolino.framework.Api;
import com.github.onsdigital.http.Endpoint;
import com.github.onsdigital.http.Http;
import com.github.onsdigital.http.Response;
import com.github.onsdigital.junit.DependsOn;
import com.github.onsdigital.test.api.oneliners.OneLineSetups;
import com.github.onsdigital.zebedee.json.CollectionDescription;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.Test;

import javax.ws.rs.POST;
import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * Created by thomasridd on 05/06/2015.
 */

@Api
@DependsOn(Approve.class)
public class Publish {

    /**
     * Tests approval using simple collection setup and publisher credentials
     *
     */
    @POST
    @Test
    public void shouldPublishToLaunchpad() throws IOException {

        // Given
        // a collection
        CollectionDescription collection = OneLineSetups.publishedCollection();

        File json = new File("src/main/resources/dummy_csdb/data.json");
        File csdb = new File("src/main/resources/dummy_csdb/dummy_csdb.csdb");

        Content.upload(collection.name, "/shouldPublishToLaunchpad/data.json", json, Login.httpPublisher);
        Content.upload(collection.name, "/shouldPublishToLaunchpad/file.csdb", csdb, Login.httpPublisher);

        Complete.completeAll(collection, Login.httpPublisher);
        Review.reviewAll(collection, Login.httpSecondSetOfEyes);
        Approve.approve(collection.id, Login.httpPublisher);

        // When
        // we approve it using publish credentials
        Response<String> response = publish(collection.id, Login.httpPublisher);

        // Expect
        // a response of okay
        assertEquals(HttpStatus.OK_200, response.statusLine.getStatusCode());
    }

    private static Response<String> publish(String collectionID, Http http) throws IOException {
        Endpoint endpoint = ZebedeeHost.publish.addPathSegment(collectionID);
        return http.post(endpoint, null, String.class);
    }
}
