package com.github.onsdigital.test.api;

import com.github.davidcarboni.cryptolite.Random;
import com.github.davidcarboni.restolino.framework.Api;
import com.github.onsdigital.http.Endpoint;
import com.github.onsdigital.http.Http;
import com.github.onsdigital.http.Response;
import com.github.onsdigital.http.Sessions;
import com.github.onsdigital.junit.DependsOn;
import com.github.onsdigital.zebedee.json.CollectionDescription;
import org.junit.Assert;
import org.junit.Test;

import javax.ws.rs.POST;
import java.io.IOException;

/**
 * Created by kanemorgan on 02/04/2015.
 */

@Api
@DependsOn(Content.class)
public class Approve {

    Http http = Sessions.get("admin");

    @Test
    public void approveACollection() throws IOException {
        CollectionDescription collection = Collection.create(http);
        approve(collection.name, 200);

    }

    //TODO Test for approve with in progress uris
    @POST
    @Test
    public void rejectsCollectionsThatHaveInProgressUris() throws IOException {
        // Given
        // ...a collection
        CollectionDescription collection = Collection.create(http);
        // When
        // ...we do nothing except create a resource
        String filename = Random.id() + ".json";
        Content.create(collection.name, "foo", "/approve/" + filename, 200);

        // We expect
        // ...the resource is in progress so the collection will not be approved
        approve(collection.name, 409);
    }

    public static boolean approve(String collectionID) throws IOException {
        return approve(collectionID, 200);
    }


    private static boolean approve(String collectionID, int expectedResponse) throws IOException {
        Http http = Sessions.get("admin");
        Endpoint endpoint = ZebedeeHost.approve.addPathSegment(collectionID);
        Response<String> postResponse = http.post(endpoint, null, String.class);

        Assert.assertEquals(expectedResponse, postResponse.statusLine.getStatusCode());
        return postResponse.body == "true";

    }
}
