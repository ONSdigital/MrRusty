package com.github.onsdigital.test;

import com.github.onsdigital.http.Endpoint;
import com.github.onsdigital.http.Host;
import com.github.onsdigital.http.Http;
import com.github.onsdigital.http.Response;

import java.io.IOException;

/**
 * Created by david on 26/03/2015.
 */
public class Main {

    public static void main(String[] args) throws IOException {
        Host host = new Host("http://localhost:8082");
        Endpoint endpoint = new Endpoint(host, "login");
        Http http = new Http();
        Response<String> response = http.post(endpoint, null, String.class);
        System.out.println("response = " + response);
    }
}
