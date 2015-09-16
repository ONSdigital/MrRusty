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

import static org.junit.Assert.assertTrue;

/**
 * Created by thomasridd on 05/06/2015.
 */

@Api
@DependsOn(com.github.onsdigital.test.api.Publish.class)
public class Train {
    public static final Host florenceHost = new Host(Configuration.getFlorenceUrl());
    public static final Endpoint florenceContent = new Endpoint(florenceHost, "");

    //@Test
    public void manualPublishPipeline_givenMegaCollection_shouldPublishToFlorenceInUnder60Seconds() throws Exception {
        // Given
        // a sample collection
        System.out.println("");
        System.out.println("Manual Publish: Building big collection");
        System.out.println("");
        final CollectionDescription sample = Scripts.buildReviewedCustomCollection(5, 5, 2, Login.httpPublisher, Login.httpSecondSetOfEyes);
        System.out.println("");
        System.out.println("Manual Publish: Approving big collection");
        Approve.approve(sample.id, Login.httpPublisher);

        List<String> uris = randomUrisFromCollection(sample, 10);
        System.out.println("Manual Publish: Big collection approved");

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
            if (urisArePublishedToFlorence(uris, Login.httpPublisher)) {
                urisArePublished = true;
                System.out.println(System.currentTimeMillis() - start + "ms: published csdb");
                break;
            }
            Thread.sleep(1000);
        }

        assertTrue(urisArePublished);
    }
    private boolean urisArePublishedToFlorence(List<String> uris, Http http) throws IOException {

        for (String uri: uris) {
            Response<Path> pathResponse = http.get(florenceContent.addPathSegment(uri));
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
