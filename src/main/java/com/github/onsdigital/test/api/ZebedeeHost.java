package com.github.onsdigital.test.api;

import com.github.onsdigital.http.Endpoint;
import com.github.onsdigital.http.Host;

public class ZebedeeHost {

    public static final Host host = new Host("http://localhost:8082");

    public static final Endpoint login = new Endpoint(host, "login");
    public static final Endpoint users = new Endpoint(host, "users");
    public static final Endpoint password = new Endpoint(host, "password");
    public static final Endpoint permission = new Endpoint(host, "permission");
    public static final Endpoint approve = new Endpoint(host, "approve");
    public static final Endpoint collections = new Endpoint(host, "collections");
    public static final Endpoint collection = new Endpoint(host, "collection");
    public static final Endpoint content = new Endpoint(host, "content");
    public static final Endpoint transfer = new Endpoint(host, "transfer");
    public static final Endpoint browse = new Endpoint(host, "browse");
    public static final Endpoint complete = new Endpoint(host, "complete");
    public static final Endpoint review = new Endpoint(host, "review");
    public static final Endpoint teams = new Endpoint(host, "teams");
}
