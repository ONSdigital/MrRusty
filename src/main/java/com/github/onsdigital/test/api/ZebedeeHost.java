package com.github.onsdigital.test.api;

import com.github.onsdigital.http.Endpoint;
import com.github.onsdigital.http.Host;

/**
 * Created by david on 13/04/2015.
 */
public class ZebedeeHost {

    public static final Host host = new Host("http://localhost:8082");

    public static final Endpoint login = new Endpoint(host, "login");
    public static final Endpoint users = new Endpoint(host, "users");
    public static final Endpoint password = new Endpoint(host, "password");
    public static final Endpoint approve = new Endpoint(host, "approve");
    public static final Endpoint collections = new Endpoint(host, "collections");
    public static final Endpoint collection = new Endpoint(host, "collection");
    public static final Endpoint content = new Endpoint(host, "content");
    public static final Endpoint transfer = new Endpoint(host, "transfer");

}
