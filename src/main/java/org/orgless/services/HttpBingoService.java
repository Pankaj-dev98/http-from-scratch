package org.orgless.services;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class HttpBingoService {

    private static final HttpClient CLIENT = HttpClient.newHttpClient();

    public static HttpResponse<InputStream> getResponse(String endpoint) {
        System.out.println("sending get request__" + "https://httpbin.org" + endpoint);
        try {

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://httpbingo.org" + endpoint))
                .header("Accept", "application/json")
                .GET()
                .build();

            return CLIENT.send(request, HttpResponse.BodyHandlers.ofInputStream());
        } catch (InterruptedException | IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}