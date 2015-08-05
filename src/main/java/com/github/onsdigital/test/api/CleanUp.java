package com.github.onsdigital.test.api;

import com.github.onsdigital.http.Endpoint;
import com.github.onsdigital.http.Http;
import com.github.onsdigital.http.Response;
import com.github.onsdigital.zebedee.json.CollectionDescription;
import com.github.onsdigital.zebedee.json.CollectionDescriptions;

import java.io.IOException;

/**
 * Created by thomasridd on 03/06/15.
 */
public class CleanUp {
    /**
     * Run the cleanup
     *
     */
    public static void cleanUpAllCollectionsBeginningWithRusty() throws IOException {

        // We get the list of collections
        Endpoint endpoint = ZebedeeHost.collections;
        Response<CollectionDescriptions> getResponse = Login.httpPublisher.get(endpoint, CollectionDescriptions.class);

        if (getResponse == null) {
            return;
        }

        CollectionDescriptions descriptions = getResponse.body;


        // Send a delete request for all those that begin with Rusty
        for(CollectionDescription description: descriptions) {
            if (description.name.startsWith("Rusty")) {
                CollectionDescription fullDescription = Collection.get(description.id, Login.httpPublisher).body;

                if(fullDescription.inProgressUris != null) {
                    for (String uri : fullDescription.inProgressUris) {
                        deleteContent(fullDescription.id, uri, Login.httpPublisher);
                    }
                }

                if(fullDescription.completeUris != null) {
                    for (String uri : fullDescription.completeUris) {
                        deleteContent(fullDescription.id, uri, Login.httpPublisher);
                    }
                }

                if(fullDescription.reviewedUris != null) {
                    for (String uri : fullDescription.reviewedUris) {
                        deleteContent(fullDescription.id, uri, Login.httpPublisher);
                    }
                }
                deleteCollection(fullDescription.id, Login.httpPublisher);
            }
        }
    }

    static Response<String> deleteCollection(String name, Http http) throws IOException {
        Endpoint endpoint = ZebedeeHost.collection.addPathSegment(name);
        return http.delete(endpoint, String.class);
    }
    static Response<String> deleteContent(String collectionName, String uri, Http http) throws IOException {
        Endpoint contentEndpoint = ZebedeeHost.content.addPathSegment(collectionName).setParameter("uri", uri);

        return http.delete(contentEndpoint, String.class);
    }
}
