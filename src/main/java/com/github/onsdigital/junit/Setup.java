package com.github.onsdigital.junit;

/**
 * A class that should be run before the first round of testing.
 */
public interface Setup {
    void setup() throws Exception;
}
