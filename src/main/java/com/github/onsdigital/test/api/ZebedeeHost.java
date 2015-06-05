package com.github.onsdigital.test.api;

import com.github.onsdigital.http.Endpoint;
import com.github.onsdigital.http.Host;
import org.apache.commons.lang3.StringUtils;

public class ZebedeeHost {

    public static final Host zebedeeHost = new Host(StringUtils.defaultIfBlank(getValue("ZEBEDEE_HOST"), "http://localhost:8082"));

    public static final Endpoint login = new Endpoint(zebedeeHost, "login");
    public static final Endpoint users = new Endpoint(zebedeeHost, "users");
    public static final Endpoint password = new Endpoint(zebedeeHost, "password");
    public static final Endpoint permission = new Endpoint(zebedeeHost, "permission");
    public static final Endpoint approve = new Endpoint(zebedeeHost, "approve");
    public static final Endpoint collections = new Endpoint(zebedeeHost, "collections");
    public static final Endpoint collection = new Endpoint(zebedeeHost, "collection");
    public static final Endpoint content = new Endpoint(zebedeeHost, "content");
    public static final Endpoint transfer = new Endpoint(zebedeeHost, "transfer");
    public static final Endpoint browse = new Endpoint(zebedeeHost, "browse");
    public static final Endpoint complete = new Endpoint(zebedeeHost, "complete");
    public static final Endpoint review = new Endpoint(zebedeeHost, "review");
    public static final Endpoint teams = new Endpoint(zebedeeHost, "teams");
    public static final Endpoint dataservices = new Endpoint(zebedeeHost, "dataservices");
    public static final Endpoint cleanup = new Endpoint(zebedeeHost, "cleanup");
    public static final Endpoint publish = new Endpoint(zebedeeHost, "publish");

    private static String getValue(String key) {
        return StringUtils.defaultIfBlank(System.getProperty(key), System.getenv(key));
    }
}
