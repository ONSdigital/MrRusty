package com.github.onsdigital.test.json.page.partial;

import java.net.URI;

public class Link  {

    private String title;
    private URI uri;
    //Index used for ordering
    private Integer index;

    public Link(URI uri) {
        this(uri, null);
    }

    /**
     * Creates the reference to given page using only uri of the page
     *
     * @param uri
     * @param index Index used for odering of links when set
     */
    public Link(URI uri, Integer index) {
        this.index = index;
        setUri(uri);
    }

    public URI getUri() {
        return uri;
    }

    public void setUri(URI uri) {
        this.uri = uri;
    }


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

}
