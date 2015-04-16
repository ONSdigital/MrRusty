package com.github.onsdigital.selenium;

/**
 * This exception subclass signals that a {@link PageObject} is not able to work with the current markup.
 * <p/>
 * This should be used to distinguish issues of Selenium interacting with a page from genuine test failures.
 * <p/>
 * Created by david on 08/04/2015.
 */
public class PageObjectException extends RuntimeException {

    public PageObjectException() {
    }

    public PageObjectException(String message) {
        super(message);
    }

    public PageObjectException(String message, Throwable cause) {
        super(message, cause);
    }

    public PageObjectException(Throwable cause) {
        super(cause);
    }

    public PageObjectException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
