package com.github.onsdigital.junit;

/**
 * A class that should be run after all tests that can run have run.
 */
public interface Teardown {
    void teardown();
}