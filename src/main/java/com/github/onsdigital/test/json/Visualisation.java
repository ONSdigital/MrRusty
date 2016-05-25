package com.github.onsdigital.test.json;

import com.github.onsdigital.test.json.page.base.Page;
import com.github.onsdigital.test.json.page.base.PageType;

import java.util.Set;

/**
 * Created by dave on 5/25/16.
 */
public class Visualisation extends Page {

    private String uid;
    public String zipTitle;
    private Set<String> filenames;
    private String indexPage = null;

    public String getUid() {
        return uid;
    }

    public Visualisation setUid(String uid) {
        this.uid = uid;
        return this;
    }

    public String getZipTitle() {
        return zipTitle;
    }

    public Visualisation setZipTitle(String zipTitle) {
        this.zipTitle = zipTitle;
        return this;
    }

    public Set<String> getFilenames() {
        return filenames;
    }

    public Visualisation setFilenames(Set<String> filenames) {
        this.filenames = filenames;
        return this;
    }

    public String getIndexPage() {
        return indexPage;
    }

    public Visualisation setIndexPage(String indexPage) {
        this.indexPage = indexPage;
        return this;
    }

    @Override
    public PageType getType() {
        return PageType.visualisation;
    }
}
