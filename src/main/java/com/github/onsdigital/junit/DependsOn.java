package com.github.onsdigital.junit;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by kanemorgan on 30/03/2015.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface DependsOn {
    Class<?>[] value();

}
