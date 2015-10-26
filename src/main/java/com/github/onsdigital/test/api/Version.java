package com.github.onsdigital.test.api;

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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@DependsOn(Content.class)
public class Version {

    @POST
    @Test
    public void shouldVersionContent() throws IOException {


        // Given - an existing collection with some content
        CollectionDescription collection = OneLineSetups.publishedCollection();
        String uri = "/businessindustryandtrade/constructionindustry/bulletins/outputintheconstructionindustry/2015-07-10";

        // When - we call version on the content
        Response<String> version = version(collection.id, uri, Login.httpPublisher);

        // Then
        // We get the expected response code
        assertEquals(HttpStatus.OK_200, version.statusLine.getStatusCode());

        // and there are files in the reviewed list that are versioned.
        CollectionDescription updatedCollection = Collection.get(collection.id, Login.httpPublisher).body;
        assertTrue(updatedCollection.reviewedUris.size() > 0);
        assertTrue(updatedCollection.reviewedUris.get(0).contains("/previous/v1"));
    }

    @POST
    @Test
    public void shouldDeleteVersionedContent() throws IOException {
        // Given - an existing collection with some versioned content
        CollectionDescription collection = OneLineSetups.publishedCollection();
        String uri = "/businessindustryandtrade/constructionindustry/bulletins/outputintheconstructionindustry/2015-07-10";
        Response<String> version = version(collection.id, uri, Login.httpPublisher);
        assertEquals(HttpStatus.OK_200, version.statusLine.getStatusCode());

        // When - we call delete version on the content
        Response<String> deleteVersion = version(collection.id, uri, Login.httpPublisher);

        // Then the content is no longer versioned in the collection.
        assertEquals(HttpStatus.OK_200, deleteVersion.statusLine.getStatusCode());

        // and there are files in the reviewed list that are versioned.
        CollectionDescription updatedCollection = Collection.get(collection.id, Login.httpPublisher).body;
        assertFalse(updatedCollection.reviewedUris.size() > 0);
        assertFalse(updatedCollection.reviewedUris.get(0).contains("/previous/v1"));
    }

    /**
     * call the version API endpoint
     * @param collectionName
     * @param uri
     * @param http
     * @return
     * @throws IOException
     */
    public static Response<String> version(String collectionName, String uri, Http http) throws IOException {
        Endpoint contentEndpoint = ZebedeeHost.version.addPathSegment(collectionName).setParameter("uri", uri);
        return http.post(contentEndpoint, "", String.class);
    }

    /**
     * call the delete version API endpoint
     * @param collectionName
     * @param uri
     * @param http
     * @return
     * @throws IOException
     */
    public static Response<String> deleteVersion(String collectionName, String uri, Http http) throws IOException {
        Endpoint contentEndpoint = ZebedeeHost.version.addPathSegment(collectionName).setParameter("uri", uri);
        return http.delete(contentEndpoint, String.class);
    }
}
