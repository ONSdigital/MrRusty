package com.github.onsdigital.test.api;

/**
 * Created by kanemorgan on 30/03/2015.
 */

import com.github.davidcarboni.restolino.framework.Api;
import com.github.onsdigital.http.Endpoint;
import com.github.onsdigital.http.Http;
import com.github.onsdigital.http.Response;
import com.github.onsdigital.junit.DependsOn;
import com.github.onsdigital.test.api.oneliners.OneLineSetups;
import com.github.onsdigital.zebedee.json.CollectionDescription;
import com.github.onsdigital.zebedee.json.CollectionDescriptions;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.Test;

import javax.ws.rs.GET;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

@Api
@DependsOn({Login.class, Collection.class})
public class Collections {

    /**
     * Test basic functionality
     *
     */
    @GET
    @Test
    public void collectionShouldAppearInCollectionsList() throws IOException {

        // Given
        // a new collection
        CollectionDescription collection = OneLineSetups.publishedCollection();

        // When
        // we get the list of collections
        Endpoint endpoint = ZebedeeHost.collections;
        Response<CollectionDescriptions> getResponse = Login.httpPublisher.get(endpoint, CollectionDescriptions.class);

        // Then
        // we get the existing collection in the response
        assertEquals(getResponse.statusLine.getStatusCode(), 200);

        for (CollectionDescription collectionDescription : getResponse.body) {
            if (collection.name.equals(collectionDescription.name))
                return;
        }
        fail("The collection was not found.");
    }

    /**
     * Test functionality limiting view permissions by team
     *
     * TODO
     */
    @GET
    //@Test
    public void collectionListShouldBeLimitedToThoseWithViewPermissions() throws IOException {

        // Given
        // two collections A & B + one user Alice
        // add Alice to team Alpha and give access to collection A
        CollectionDescription collectionA = OneLineSetups.publishedCollection();
        CollectionDescription collectionB = OneLineSetups.publishedCollection();

        Http httpAlice = OneLineSetups.newSessionWithViewerPermissions();

        // TODO create Alpha + permissions for Alpha to access collection A + add alice to Alpha

        // When
        // we get the list of collections for Alice
        Response<CollectionDescriptions> getResponse = httpAlice.get(ZebedeeHost.collections, CollectionDescriptions.class);

        // Then
        // we expect alice to have access to one and only one collection and for that to be collection A
        assertEquals(HttpStatus.OK_200, getResponse.statusLine.getStatusCode());

        CollectionDescriptions collectionDescriptions = getResponse.body;
        assertEquals(1, collectionDescriptions.size());

        CollectionDescription collectionDescription = collectionDescriptions.get(0);
        assertEquals(collectionA.name, collectionDescription.name);
    }
}
