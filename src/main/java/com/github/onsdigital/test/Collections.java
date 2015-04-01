package com.github.onsdigital.test;

/**
 * Created by kanemorgan on 30/03/2015.
 */

import com.github.onsdigital.framework.DependsOn;
import com.github.onsdigital.http.Endpoint;
import com.github.onsdigital.http.Http;
import com.github.onsdigital.http.Response;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

@DependsOn({Login.class, Collection.class})
public class Collections {
    //TODO: depends on collection
    @Test
    public void collectionsSpec() throws IOException {
        Http http = new Http();
        http.addHeader("X-Florence-Token", Login.florenceToken);
        Endpoint endpoint = new Endpoint(Login.zebedeeHost, "collections");
        Response<List> getResponse = http.get(endpoint, List.class);

        Assert.assertEquals(getResponse.statusLine.getStatusCode(), 200);
    }
}
