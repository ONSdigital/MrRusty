package com.github.onsdigital.test;

import com.github.davidcarboni.restolino.json.Serialiser;
import com.github.onsdigital.framework.DependsOn;
import com.github.onsdigital.http.Endpoint;
import com.github.onsdigital.http.Host;
import com.github.onsdigital.http.Http;
import com.github.onsdigital.http.Response;
import com.github.onsdigital.zebedee.json.Credentials;
import org.junit.Test;
import sun.jvm.hotspot.utilities.Assert;

import java.io.IOException;

/**
 * Created by kanemorgan on 30/03/2015.
 */
@DependsOn({})
public class Login {

    public static String florenceToken;
    public static Host zebedeeHost;

    @Test
    public static void adminShouldBeAbleToLogIn() throws IOException {
        loginAttempt("florence@magicroundabout.ons.gov.uk","Doug4l",200);
    }

    @Test
    public static void return400IfEmailNotSpecified() throws IOException {
        //given
        loginAttempt(null, "password", 400);
    }

    @Test
    public static void return401IfWrongPassword() throws IOException {
       loginAttempt("florence@magicroundabout.ons.gov.uk","denied",401);
    }


    private static void loginAttempt(String email,String password, int expectedCode) throws IOException {
        if (zebedeeHost == null){
            zebedeeHost = new Host("http://localhost:8082");
        }

        Endpoint login = new Endpoint(zebedeeHost, "login");
        Http http = new Http();

        Credentials credentials =new Credentials();
        credentials.email = email;
        credentials.password = password;

        Response<String> response = http.post(login, credentials, String.class);
        System.out.println(response);
        checkResponseCode(response, expectedCode);

        // if we managed to log in then make the other tests use us as the user
        if( expectedCode == 200){
            florenceToken = response.body;
        }
    }

    private static void checkResponseCode(Response response, int code){
        org.junit.Assert.assertEquals(response.statusLine.getStatusCode(),code);
    }

}
