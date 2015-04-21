package com.github.onsdigital.test.api;

import com.github.davidcarboni.cryptolite.Random;
import com.github.onsdigital.http.Endpoint;
import com.github.onsdigital.http.Http;
import com.github.onsdigital.http.Response;
import com.github.onsdigital.junit.DependsOn;
import com.github.onsdigital.zebedee.json.CollectionDescription;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

/**
 * Created by kanemorgan on 31/03/2015.
 */

@DependsOn({Login.class, Collection.class, Content.class})
public class Transfer {

    Http http = Login.httpPublisher;

    @Test
    public void main() throws IOException {
        CollectionDescription collection_1 = Collection.create(http);
        CollectionDescription collection_2 = Collection.create(http);
        String fileUri = Random.id() + ".json";
        Content.create(collection_1.name, "content", fileUri, 200, http);

        transfer(collection_1.name, collection_2.name, fileUri, 200, http);


    }

    public static void transfer(String source, String destination, String uri, int expectedResponse, Http http) throws IOException {
        Endpoint transferUrl = ZebedeeHost.transfer;

//        Transfer
        TransferRequest transferRequest = new TransferRequest();
        transferRequest.source = source;
        transferRequest.destination = destination;
        transferRequest.uri = uri;

        Response<String> response = http.post(transferUrl, transferRequest, String.class);

        Assert.assertEquals(response.statusLine.getStatusCode(), expectedResponse);

    }

}


class TransferRequest {

    // the collections which the source uri needs to go from
    public String source;
    public String destination;
    // the uri of the resource to be moved
    public String uri;
}


