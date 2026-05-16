package com.interviewmentor.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utility for making HTTP requests to the Spring Boot Backend API.
 */
public class ApiClient {

    private static final String BASE_URL = "http://localhost:8080/api";
    private static final HttpClient client = HttpClient.newBuilder().version(HttpClient.Version.HTTP_2).build();
    private static final Gson gson;

    static {
        // Register adapter for LocalDateTime
        gson = new GsonBuilder().registerTypeAdapter(LocalDateTime.class, 
            (JsonDeserializer<LocalDateTime>) (json, type, jsonDeserializationContext) -> 
            LocalDateTime.parse(json.getAsString(), DateTimeFormatter.ISO_LOCAL_DATE_TIME))
            .create();
    }

    public static <T> T get(String endpoint, Class<T> responseType) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + endpoint))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                return gson.fromJson(response.body(), responseType);
            }
        } catch (Exception e) {
            System.err.println("[ApiClient] GET error: " + e.getMessage());
        }
        return null;
    }

    public static <T> T post(String endpoint, Object requestBody, Class<T> responseType) {
        try {
            String jsonBody = gson.toJson(requestBody);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + endpoint))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                if (responseType == Void.class) return null;
                return gson.fromJson(response.body(), responseType);
            }
        } catch (Exception e) {
            System.err.println("[ApiClient] POST error: " + e.getMessage());
        }
        return null;
    }
}
