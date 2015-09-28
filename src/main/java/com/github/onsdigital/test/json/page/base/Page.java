package com.github.onsdigital.test.json.page.base;

import java.net.URI;

public abstract class Page {

    protected PageType type;

    private URI uri;

    private PageDescription description;

    public Page() {
        this.type = getType();
    }

    public abstract PageType getType();

    public PageDescription getDescription() {
        return description;
    }

    public void setDescription(PageDescription description) {
        this.description = description;
    }

    public URI getUri() {
        return uri;
    }

    public void setUri(URI uri) {
        this.uri = uri;
    }
}
