package com.github.onsdigital.junit;

import java.lang.reflect.Method;

/**
 * Created by david on 31/03/2015.
 */
public class TestMethod {

    Method method;
    Throwable throwable;

    TestMethod(Method method, Throwable throwable) {
        this.method = method;
        this.throwable = throwable;
    }
}
