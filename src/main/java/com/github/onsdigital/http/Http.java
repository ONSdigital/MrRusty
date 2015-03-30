package com.github.onsdigital.http;

import com.github.davidcarboni.restolino.json.Serialiser;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by david on 25/03/2015.
 */
public class Http implements Closeable {

    private CloseableHttpClient httpClient;
    private ArrayList<Header> headers = new ArrayList<>();

    /**
     * Sends a GET request and returns the response.
     *
     * @param endpoint      The endpoint to send the request to.
     * @param responseClass The class to deserialise the Json response to. Can be null if no response message is expected.
     * @param headers       Any additional headers to send with this request. You can use {@link org.apache.http.HttpHeaders} constants for header names.
     * @param <T>           The type to deserialise the response to.
     * @return A {@link Response} containing the deserialised body, if any.
     * @throws IOException If an error occurs.
     */
    public <T> Response<T> get(Endpoint endpoint, Class<T> responseClass, NameValuePair... headers) throws IOException {

        // Create the request
        HttpGet get = new HttpGet(endpoint.url());
        get.setHeaders(combineHeaders(headers));


        // Send the request and process the response
        try (CloseableHttpResponse response = httpClient().execute(get)) {
            System.out.println(response);
            T body = deserialiseResponseMessage(response, responseClass);
            return new Response<>(response.getStatusLine(), body);
        }
    }

    /**
     * Sends a POST request and returns the response.
     *
     * @param endpoint       The endpoint to send the request to.
     * @param requestMessage A message to send in the request body. Can be null.
     * @param responseClass  The class to deserialise the Json response to. Can be null if no response message is expected.
     * @param headers        Any additional headers to send with this request. You can use {@link org.apache.http.HttpHeaders} constants for header names.
     * @param <T>            The type to deserialise the response to.
     * @return A {@link Response} containing the deserialised body, if any.
     * @throws IOException If an error occurs.
     */
    public <T> Response<T> post(Endpoint endpoint, Object requestMessage, Class<T> responseClass, NameValuePair... headers) throws IOException {

        // Create the request
        HttpPost post = new HttpPost(endpoint.url());
        post.setHeaders(combineHeaders(headers));

        // Add the request message if there is one
      post.setEntity(serialiseRequestMessage(requestMessage));

        // Send the request and process the response
        try (CloseableHttpResponse response = httpClient().execute(post)) {
            T body = deserialiseResponseMessage(response, responseClass);
            return new Response<>(response.getStatusLine(), body);
        }
    }

    /**
     * Sends a POST request and returns the response.
     *
     * @param endpoint       The endpoint to send the request to.
     * @param requestMessage A message to send in the request body. Can be null.
     * @param responseClass  The class to deserialise the Json response to. Can be null if no response message is expected.
     * @param headers        Any additional headers to send with this request. You can use {@link org.apache.http.HttpHeaders} constants for header names.
     * @param <T>            The type to deserialise the response to.
     * @return A {@link Response} containing the deserialised body, if any.
     * @throws IOException If an error occurs.
     */
    public <T> Response<T> put(Endpoint endpoint, Object requestMessage, Class<T> responseClass, NameValuePair... headers) throws IOException {

        // Create the request
        HttpPut put = new HttpPut(endpoint.url());
        put.setHeaders(combineHeaders(headers));

        // Add the request message if there is one
        put.setEntity(serialiseRequestMessage(requestMessage));

        // Send the request and process the response
        try (CloseableHttpResponse response = httpClient().execute(put)) {
            T body = deserialiseResponseMessage(response, responseClass);
            return new Response<>(response.getStatusLine(), body);
        }
    }

    /**
     * Sends a POST request and returns the response.
     *
     * @param endpoint       The endpoint to send the request to.
     * @param responseClass  The class to deserialise the Json response to. Can be null if no response message is expected.
     * @param headers        Any additional headers to send with this request. You can use {@link org.apache.http.HttpHeaders} constants for header names.
     * @param <T>            The type to deserialise the response to.
     * @return A {@link Response} containing the deserialised body, if any.
     * @throws IOException If an error occurs.
     */
    public <T> Response<T> delete(Endpoint endpoint, Class<T> responseClass, NameValuePair... headers) throws IOException {

        // Create the request
        HttpDelete delete = new HttpDelete(endpoint.url());
        delete.setHeaders(combineHeaders(headers));

        // Send the request and process the response
        try (CloseableHttpResponse response = httpClient().execute(delete)) {
            T body = deserialiseResponseMessage(response, responseClass);
            return new Response<>(response.getStatusLine(), body);
        }
    }

    /**
     * Adds a header that will be used for all requests made by this instance.
     * @param name The header name. You can use {@link org.apache.http.HttpHeaders} constants for header names.
     * @param value The header value.
     */
    public void addHeader(String name, String value) {
        headers.add(new BasicHeader(name, value));
    }

    /**
     * Sets the combined request headers.
     * @param headers Additional header values to add over and above {@link #headers}.
     */

    private Header[] combineHeaders(NameValuePair[] headers) {

        Header[] fullHeaders = new Header[this.headers.size() + headers.length];

        // Add class-level headers (for all requests)
        for (int i = 0; i < this.headers.size(); i++) {
            fullHeaders[i] = this.headers.get(i);
        }

        // Add headers specific to this request:
        for (int i = 0; i < headers.length; i++) {
            NameValuePair header = headers[i];
            fullHeaders[i+this.headers.size()] = new BasicHeader(header.getName(), header.getValue());
        }

        System.out.println( Arrays.toString(fullHeaders));
        return fullHeaders;
    }

    /**
     * Serialises the given object as a {@link StringEntity}.
     *
     * @param requestMessage The object to be serialised.
     * @throws UnsupportedEncodingException If a serialisation error occurs.
     */
    private StringEntity serialiseRequestMessage(Object requestMessage) throws UnsupportedEncodingException {
        StringEntity result = null;

        // Add the request message if there is one
        if (requestMessage != null) {
            // Send the message
            String message = Serialiser.serialise(requestMessage);
            result = new StringEntity(message);
        }

        return result;
    }

    /**
     * Deserialises the given {@link CloseableHttpResponse} to the specified type.
     *
     * @param response      The response.
     * @param responseClass The type to deserialise to.
     * @param <T>           The type to deserialise to.
     * @return The deserialised response, or null if the response does not contain an entity.
     * @throws IOException If an error occurs.
     */
    private <T> T deserialiseResponseMessage(CloseableHttpResponse response, Class<T> responseClass) throws IOException {
        T body = null;

        HttpEntity entity = response.getEntity();
        if (entity != null) {
            try (InputStream inputStream = entity.getContent()) {
                body = Serialiser.deserialise(inputStream, responseClass);
            }
        } else {
            EntityUtils.consume(entity);
        }

        return body;
    }

    private CloseableHttpClient httpClient() {
        if (httpClient == null) {
            httpClient = HttpClients.createDefault();
        }
        return httpClient;
    }

    @Override
    public void close() throws IOException {
        if (httpClient != null) {
            httpClient.close();
        }
    }

}
