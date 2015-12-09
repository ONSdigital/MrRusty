package com.github.onsdigital.test.publisher;


import com.github.onsdigital.Scripts;
import com.github.onsdigital.http.Endpoint;
import com.github.onsdigital.http.Host;
import com.github.onsdigital.http.Response;
import com.github.onsdigital.test.api.Approve;
import com.github.onsdigital.test.api.Publish;
import com.github.onsdigital.test.api.ZebedeeHost;
import com.github.onsdigital.test.api.oneliners.OneShot;
import com.github.onsdigital.test.configuration.Configuration;
import com.github.onsdigital.test.json.CollectionDescription;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * Created by thomasridd on 14/09/15.
 */
public class TimedPublisher {
    public static final Host florenceHost = new Host(Configuration.getFlorenceUrl());
    public static final Endpoint florenceContent = new Endpoint(florenceHost, "");
    private static String getValue(String key) {
        return StringUtils.defaultIfBlank(System.getProperty(key), System.getenv(key));
    }

    @BeforeClass
    public static void setup() throws Exception {
        OneShot.setup();
        System.out.println("TimedPublisher setup complete");

    }

    @Test
    public void manualPublishPipeline_givenBulletinCollection_shouldPublishToFlorenceInUnder60Seconds() throws Exception {
        // Given
        // a sample collection
        System.out.println("Manual Publish: Building a bulletin collection");
        final CollectionDescription sample = Scripts.buildReviewedCustomCollection(5, 0, 0, OneShot.httpPublisher, OneShot.httpSecondSetOfEyes);
        Approve.approve(sample.id, OneShot.httpPublisher);

        List<String> uris = randomUrisFromCollection(sample, 10);

        long start = System.currentTimeMillis();

        // When
        // we start a publish asynchronously
        System.out.println("Manual Publish: Sending publish");
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Publish.publish(sample.id, OneShot.httpPublisher);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();


        // Then
        // We poll every five seconds to see if the files reach remote system
        boolean urisArePublished = false;
        while(System.currentTimeMillis() - start < 60000) {
            System.out.println(System.currentTimeMillis() - start + "ms: checking");

            urisArePublished = false;
            if (urisArePublishedToFlorence(uris)) {
                urisArePublished = true;
                System.out.println(System.currentTimeMillis() - start + "ms: published bulletins");
                break;
            }
            Thread.sleep(1000);
            System.out.println("Waiting");
        }

        assertTrue(urisArePublished);
    }

    @Test
    public void manualPublishPipeline_givenDatasetCollection_shouldPublishToFlorenceInUnder60Seconds() throws Exception {
        // Given
        // a sample collection
        System.out.println("Manual Publish: Building dataset collection");
        final CollectionDescription sample = Scripts.buildReviewedCustomCollection(0, 10, 0, OneShot.httpPublisher, OneShot.httpSecondSetOfEyes);
        Approve.approve(sample.id, OneShot.httpPublisher);

        List<String> uris = randomUrisFromCollection(sample, 10);

        long start = System.currentTimeMillis();

        // When
        // we start a publish asynchronously
        System.out.println("Manual Publish: Sending publish");
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    OneShot.httpPublisher.justPost(ZebedeeHost.publish.addPathSegment(sample.id).setParameter("skipVerification", "true"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();


        // Then
        // We poll every five seconds to see if the files reach remote system
        boolean urisArePublished = false;
        while(System.currentTimeMillis() - start < 60000) {
            System.out.println(System.currentTimeMillis() - start + "ms: checking");

            urisArePublished = false;
            if (urisArePublishedToFlorence(uris)) {
                urisArePublished = true;
                System.out.println(System.currentTimeMillis() - start + "ms: published datasets");
                break;
            }
            Thread.sleep(1000);
        }

        assertTrue(urisArePublished);
    }

    @Test
    public void manualPublishPipeline_givenCsdbCollection_shouldPublishToFlorenceInUnder60Seconds() throws Exception {
        // Given
        // a sample collection
        System.out.println("Manual Publish: Building csdb collection");
        final CollectionDescription sample = Scripts.buildReviewedCustomCollection(0, 0, 2, OneShot.httpPublisher, OneShot.httpSecondSetOfEyes);
        Approve.approve(sample.id, OneShot.httpPublisher);

        List<String> uris = randomUrisFromCollection(sample, 10);
        System.out.println("Manual Publish: Csdb collection built - starting publish");

        long start = System.currentTimeMillis();

        // When
        // we start a publish asynchronously
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    OneShot.httpPublisher.justPost(ZebedeeHost.publish.addPathSegment(sample.id).setParameter("skipVerification", "true"));
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

    @Test
    public void manualPublishPipeline_givenMegaCollection_shouldPublishToFlorenceInUnder60Seconds() throws Exception {
        // Given
        // a sample collection
        System.out.println("Manual Publish: Building mother of all collections");
        final CollectionDescription sample = Scripts.buildReviewedCustomCollection(5, 5, 2, OneShot.httpPublisher, OneShot.httpSecondSetOfEyes);
        System.out.println("Manual Publish: Approving m.o.a.c");
        Approve.approve(sample.id, OneShot.httpPublisher);

        List<String> uris = randomUrisFromCollection(sample, 10);
        System.out.println("Manual Publish: Mother of all collections built - starting publish");

        long start = System.currentTimeMillis();

        // When
        // we start a publish asynchronously
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    OneShot.httpPublisher.justPost(ZebedeeHost.publish.addPathSegment(sample.id).setParameter("skipVerification", "true"));
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

        Collections.shuffle(uris);

        return uris.subList(0, count);
    }
}
