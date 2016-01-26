package com.github.onsdigital.test.browser.model;

import com.github.davidcarboni.cryptolite.Random;

public class User {

    private String random = Random.id().substring(0, 5);
    public String name = createUserNameForTest();
    public String email = createUserEmailForTest();
    public String password = createUserPassword();

    private String createUserPassword() {
        return "whatever";
    }

    public String createUserNameForTest() {
        return "Rusty_" + random + "_user";
    }
    public String createUserEmailForTest() {
        return "Rusty_" + random + "@magicroundabout.ons.gov.uk";
    }

}
