package com.github.onsdigital.test.api;

import com.github.davidcarboni.restolino.framework.Api;
import com.github.onsdigital.http.Response;
import com.github.onsdigital.junit.DependsOn;
import com.github.onsdigital.test.base.ZebedeeApiTest;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Tests verify behaviour of the CollectionOwnerEndpoint.
 */
@Api
@DependsOn(com.github.onsdigital.test.api.Login.class)
public class UserPublisherType extends ZebedeeApiTest {

    static final CollectionOwnerModel dataVisualisation = new CollectionOwnerModel("DATA_VISUALISATION");
    static final CollectionOwnerModel publishingSupport = new CollectionOwnerModel("PUBLISHING_SUPPORT");

    /**
     * Verifies the endpoint return the correct collection owner type for a data vis publisher user.
     */
    @Test
    public void shouldReturnDataVisualisationOwner() throws Exception {
        Response<CollectionOwnerModel> response = context.getDataVis().get(ZebedeeHost.userPublisherType, CollectionOwnerModel.class);
        assertEquals(HttpStatus.OK_200, response.statusLine.getStatusCode());
        assertEquals(dataVisualisation.userPublisherType, response.body.userPublisherType);
    }

    /**
     * Verifies the endpoint return the correct collection owner type for a publishing support user.
     */
    @Test
    public void shouldReturnPublishingSupport() throws Exception {
        Response<CollectionOwnerModel> response = context.getPublisher().get(ZebedeeHost.userPublisherType, CollectionOwnerModel.class);
        assertEquals(HttpStatus.OK_200, response.statusLine.getStatusCode());
        assertEquals(publishingSupport.userPublisherType, response.body.userPublisherType);
    }

    private static class CollectionOwnerModel {
        String userPublisherType;

        public CollectionOwnerModel(String collectionOwner) {
            this.userPublisherType = collectionOwner;
        }
    }
}
