package com.github.onsdigital.test.api;

import com.github.davidcarboni.restolino.framework.Api;
import com.github.davidcarboni.restolino.json.Serialiser;
import com.github.onsdigital.http.Endpoint;
import com.github.onsdigital.http.Http;
import com.github.onsdigital.http.Response;
import com.github.onsdigital.junit.DependsOn;
import com.github.onsdigital.test.api.oneliners.OneLineSetups;
import com.github.onsdigital.test.base.ZebedeeApiTest;
import com.github.onsdigital.test.json.CollectionDescription;
import com.github.onsdigital.test.json.ContentDetail;
import com.github.onsdigital.test.json.serialiser.IsoDateSerializer;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.Test;

import javax.ws.rs.GET;
import java.io.IOException;
import java.util.Date;

import static org.junit.Assert.assertEquals;

@Api
@DependsOn(Permissions.class)
public class CollectionBrowseTree extends ZebedeeApiTest {

    public CollectionBrowseTree() {
        // Set ISO date formatting in Gson to match Javascript Date.toISODate()
        Serialiser.getBuilder().registerTypeAdapter(Date.class, new IsoDateSerializer());
    }

    /**
     * Viewer permissions should return {@link HttpStatus#OK_200} for any permitted collection, {@link HttpStatus#UNAUTHORIZED_401} otherwise
     */
    @GET
    @Test
    public void getShouldReturn200ForViewerWithPermissions() throws IOException {
        // Given
        // a collection
        CollectionDescription collection = OneLineSetups.publishedCollection(context.getPublisher());

        // When
        // we attempt to get the browse tree as a publisher
        Response<ContentDetail> response = get(collection.id, context.getPublisher());
        Response<ContentDetail> responseAdmin = get(collection.id, context.getAdministrator());

        // We expect
        // a response of 200
        assertEquals(HttpStatus.OK_200, response.statusLine.getStatusCode());
        assertEquals(HttpStatus.OK_200, responseAdmin.statusLine.getStatusCode());
    }

    /**
     * Viewer permissions should return {@link HttpStatus#OK_200} for any permitted collection, {@link HttpStatus#UNAUTHORIZED_401} otherwise
     */
    @GET
    @Test
    public void getShouldReturn404ForCollectionThatDoesNotExist() throws IOException {
        // Given

        // When
        // we attempt to get for a collection that does not exist.
        Response<ContentDetail> response = get("SomeCollectionDoesNotExist", context.getPublisher());

        // We expect
        // a response of 404
        assertEquals(HttpStatus.NOT_FOUND_404, response.statusLine.getStatusCode());
    }

    /**
     * Admins should return {@link HttpStatus#UNAUTHORIZED_401}
     */
    @GET
    @Test
    public void getShouldReturn401WithAdminPermissions() throws IOException {
        // Given
        // a collection
        CollectionDescription collection = Collection.createCollectionDescription();
        collection = Collection.post(collection, context.getPublisher()).body;

        // When
        // we attempt to retrieve it as an administrator
        Response<ContentDetail> responseScally = get(collection.id, context.getScallyWag());
        Response<ContentDetail> responseViewer = get(collection.id, context.getViewer());

        // We expect
        // a response of 401 - unauthorized
        assertEquals(HttpStatus.UNAUTHORIZED_401, responseScally.statusLine.getStatusCode());
        assertEquals(HttpStatus.UNAUTHORIZED_401, responseViewer.statusLine.getStatusCode());
    }

    public static Response<ContentDetail> get(String id, Http http) throws IOException {
        Endpoint idUrl = ZebedeeHost.collectionBrowseTree.addPathSegment(id);
        return http.get(idUrl, ContentDetail.class);
    }
}
