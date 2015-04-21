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
import org.eclipse.jetty.http.HttpStatus;

import javax.ws.rs.GET;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

@DependsOn({LoginAdmin.class, Collection.class})
public class Browse {

    private static Http http = Sessions.get("admin");
    private static Endpoint browseEndpoint = ZebedeeHost.browse;

    /**
     * TODO Update with permissions
     */
    @GET
    @Test
    public void shouldReturn404IfNoPathSpecified() throws IOException {
        // Given
        // admin login XXXXXXX

        // When
        // we call get without a collection specified
        Response<DirectoryListing> getResponse = http.get(browseEndpoint, DirectoryListing.class);

        // Expect
        // Response of {@link HttpStatus#NOT_FOUND_404}
        assertEquals(getResponse.statusLine.getStatusCode(), HttpStatus.NOT_FOUND_404);
    }

    /**
     * TODO Update with permissions
     */
    @Test
    public void shouldReturn200WithValidCollectionName() throws IOException {
        // Given
        // admin login XXXXXXXX & a collection
        CollectionDescription collection = Collection.create(http);

        // When
        // we call get
        Response<DirectoryListing> getResponse = http.get(browseEndpoint.addPathSegment(collection.name), DirectoryListing.class);

        // Expect
        // a correct call
        Assert.assertEquals(getResponse.statusLine.getStatusCode(), 200);
    }
}
