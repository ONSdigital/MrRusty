package com.github.onsdigital.test.api;

/**
 * Created by kanemorgan on 30/03/2015.
 */

import com.github.onsdigital.http.Endpoint;
import com.github.onsdigital.http.Http;
import com.github.onsdigital.http.Response;
import com.github.onsdigital.http.Sessions;
import com.github.onsdigital.junit.DependsOn;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

@DependsOn({LoginAdmin.class, Collection.class})
public class Collections {
    @Test
    public void collectionsSpec() throws IOException {
        Http http = Sessions.get("admin");
        Endpoint endpoint = ZebedeeHost.collections;
        Response<List> getResponse = http.get(endpoint, List.class);

        Assert.assertEquals(getResponse.statusLine.getStatusCode(), 200);
    }
}
