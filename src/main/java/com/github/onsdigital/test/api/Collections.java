package com.github.onsdigital.test.api;

/**
 * Created by kanemorgan on 30/03/2015.
 */

import com.github.davidcarboni.restolino.framework.Api;
import com.github.onsdigital.http.Endpoint;
import com.github.onsdigital.http.Http;
import com.github.onsdigital.http.Response;
import com.github.onsdigital.http.Sessions;
import com.github.onsdigital.junit.DependsOn;
import com.github.onsdigital.zebedee.json.CollectionDescription;
import com.github.onsdigital.zebedee.json.CollectionDescriptions;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.Test;

import javax.ws.rs.GET;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

@Api
@DependsOn({LoginAdmin.class, Collection.class})
public class Collections {

    private static Http http = Sessions.get("admin");

    /**
     * Test basic functionality
     *
     * TODO
     */
    @GET
    @Test
    public void collectionShouldAppearInCollectionsList() throws IOException {

        // Given - a new collection
        CollectionDescription collection = Collection.create(http);

        // When - we get the list of collections
        Endpoint endpoint = ZebedeeHost.collections;
        Response<CollectionDescriptions> getResponse = http.get(endpoint, CollectionDescriptions.class);

        // Then - We get the existing collection in the response
        assertEquals(getResponse.statusLine.getStatusCode(), 200);

        for (CollectionDescription collectionDescription : getResponse.body) {
            if (collection.name.equals(collectionDescription.name))
                return;
        }

        fail("The collection was not found.");
    }
}
