package com.github.onsdigital.test;

import com.github.davidcarboni.restolino.json.Serialiser;
import com.github.onsdigital.http.Endpoint;
import com.github.onsdigital.http.Host;
import com.github.onsdigital.http.Http;
import com.github.onsdigital.http.Response;
import com.github.onsdigital.zebedee.json.CollectionDescription;
import com.github.onsdigital.zebedee.json.Credentials;
import sun.jvm.hotspot.utilities.Assert;

import java.io.IOException;
import java.lang.reflect.Array;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

/**
 * Created by david on 26/03/2015.
 */
public class Main {

    public static void main(String[] args) throws IOException, ParseException {

        Host host = new Host("http://localhost:8082");
        Serialiser.getBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX");


        // Login

        Endpoint login = new Endpoint(host, "login");
        Http http = new Http();

        Credentials credentials =new Credentials();
        credentials.email="florence@magicroundabout.ons.gov.uk";
        credentials.password = "Doug4l";

        Response<String> response = http.post(login, credentials, String.class);
        System.out.println("response = " + response);

        http.addHeader("X-Florence-Token",response.body);



        // given we create a collection

        CollectionDescription roundabout = new CollectionDescription();
        roundabout.name = "roundabout";
        SimpleDateFormat foo = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX");

//        roundabout.publishDate = foo.parse("1990-01-01");
        roundabout.publishDate = new Date();
        Endpoint collectionEndpoint = new Endpoint(host,"collection");

        Response<String> createResponse = http.post(collectionEndpoint, roundabout, String.class);
        System.out.println(createResponse);
        http.addHeader("X-Florence-Token", response.body);
        Response<CollectionDescription[]> collectionResponse = http.get(new Endpoint(host,"collections"), CollectionDescription[].class);
        System.out.println("collectionResponse = " + Arrays.toString(collectionResponse.body));



        // expect the collection to be in the list of collections returned from get collection
        if(!collectionExists(collectionResponse.body)){
            throw new RuntimeException("Expected the collections Api to return the created Collection");
        }

    }

    private static boolean collectionExists(CollectionDescription[] collections){
        boolean exists = false;
        for(CollectionDescription c:collections){
            if(c.name == "roundabout"){
                exists = true;
            }
        }
        return exists;
    }
}
