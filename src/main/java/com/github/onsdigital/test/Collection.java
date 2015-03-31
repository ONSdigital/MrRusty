package com.github.onsdigital.test;

import com.github.davidcarboni.cryptolite.Random;
import com.github.davidcarboni.restolino.json.Serialiser;
import com.github.onsdigital.framework.DependsOn;
import com.github.onsdigital.http.Endpoint;
import com.github.onsdigital.http.Http;
import com.github.onsdigital.http.Response;
import com.github.onsdigital.zebedee.json.CollectionDescription;
import junit.framework.Assert;
import org.junit.Test;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.junit.Assert.assertTrue;

/**
 * Created by kanemorgan on 30/03/2015.
 */
@DependsOn(Login.class)
public class Collection {
    //TODO: depends on login


    static Endpoint collectionEndpoint = new Endpoint(Login.zebedeeHost, "collection");

    @Test
    public static void post() throws IOException {
        Serialiser.getBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX");
        Http http = new Http();

        http.addHeader("X-Florence-Token",Login.florenceToken);

        CollectionDescription roundabout = new CollectionDescription();
        roundabout.name = Random.id();
        SimpleDateFormat foo = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX");


        roundabout.publishDate = new Date();
        Response<String> createResponse = http.post(collectionEndpoint, roundabout, String.class);
        System.out.println(createResponse);
        assertTrue(createResponse.statusLine.getStatusCode() == 20);

    }
}
