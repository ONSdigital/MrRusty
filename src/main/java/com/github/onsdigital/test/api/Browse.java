package com.github.onsdigital.test.api;

import com.github.onsdigital.http.Endpoint;
import com.github.onsdigital.http.Http;
import com.github.onsdigital.http.Response;
import com.github.onsdigital.http.Sessions;
import com.github.onsdigital.junit.DependsOn;
import com.github.onsdigital.zebedee.json.CollectionDescription;
import com.github.onsdigital.zebedee.json.DirectoryListing;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

@DependsOn({LoginAdmin.class, Collection.class})
public class Browse {

    private static Http http = Sessions.get("admin");
    private static Endpoint browseEndpoint = ZebedeeHost.browse;

    @Test
    public void shouldReturn404IfNoPathSpecified() throws IOException {
        Response<DirectoryListing> getResponse = http.get(browseEndpoint, DirectoryListing.class);
        assertEquals(getResponse.statusLine.getStatusCode(), 404);
    }

    @Test
    public void shouldReturn200WithValidCollectionName() throws IOException {
        CollectionDescription collection = Collection.create(http);
        Response<DirectoryListing> getResponse = http.get(browseEndpoint.addPathSegment(collection.name), DirectoryListing.class);
        Assert.assertEquals(getResponse.statusLine.getStatusCode(), 200);
    }
}
