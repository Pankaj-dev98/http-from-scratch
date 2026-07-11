package org.orgless.services;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class FunFactService {
    public static String getFunFact() {
        try (HttpClient client = HttpClient.newHttpClient()) {

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://uselessfacts.jsph.pl/api/v2/facts/random"))
                .header("Accept", "application/json")
                .GET()
                .build();

            try {
                HttpResponse<String> response = client.send(
                    request,
                    HttpResponse.BodyHandlers.ofString());

                String body = response.body();

                String key = "\"text\":\"";
                int start = body.indexOf(key);

                if (start == -1) {
                    return "Could not retrieve a fun fact.";
                }

                start += key.length();
                int end = body.indexOf('"', start);

                return body.substring(start, end)
                    .replace("\\n", "\n")
                    .replace("\\\"", "\"");

            } catch (IOException | InterruptedException e) {
                return "Failed to fetch a fun fact: " + e.getMessage();
            }
        }
    }
}