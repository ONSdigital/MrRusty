package com.github.onsdigital.test;

import com.github.onsdigital.framework.DependsOn;
import com.github.onsdigital.http.Endpoint;
import com.github.onsdigital.http.Host;
import com.github.onsdigital.http.Http;
import com.github.onsdigital.http.Response;
import com.github.onsdigital.zebedee.json.Credentials;
import org.junit.Test;

import java.io.IOException;

/**
 * Created by kanemorgan on 30/03/2015.
 */
@DependsOn({})
public class Login {

    public static String florenceToken;
    public static Host zebedeeHost;

    @Test
    public void main() throws IOException {
        zebedeeHost = new Host("http://localhost:8082");

        Endpoint login = new Endpoint(zebedeeHost, "login");
        Http http = new Http();

        Credentials credentials =new Credentials();
        credentials.email="florence@magicroundabout.ons.gov.uk";
        credentials.password = "Doug4l";

        Response<String> response = http.post(login, credentials, String.class);
        System.out.println("response = " + response);

        florenceToken = response.body;


    }



}
