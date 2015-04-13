package com.github.onsdigital.test.api;

import com.github.davidcarboni.restolino.framework.Api;
import com.github.onsdigital.junit.DependsOn;
import com.github.onsdigital.http.Endpoint;
import com.github.onsdigital.http.Http;
import com.github.onsdigital.http.Response;
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
    @Test
    public void approveACollection() throws IOException {
        CollectionDescription collection = Collection.create();
        approve(collection.name, 200);

    }

    //TODO Test for approve with in progress uris
    @POST
    @Test
    public void rejectsCollectionsThatHaveInProgressUris() throws IOException {
        CollectionDescription collection = Collection.create();
        Content.create(collection.name,"foo","/where/foo/lives",409);

        approve(collection.name);
    }

    public static boolean approve(String collectionID) throws IOException {
        return approve(collectionID,200);
    }


    private static boolean approve(String collectionID, int expectedResponse ) throws IOException {
        Http http = new Http();
        http.addHeader("X-Florence-Token", Login.florenceToken);
        Endpoint endpoint = new Endpoint( Login.zebedeeHost,"approve/"+collectionID);

        Response<String> postResponse = http.post(endpoint, null, String.class);

        Assert.assertEquals( expectedResponse,postResponse.statusLine.getStatusCode());
        return postResponse.body == "true";

    }
}
