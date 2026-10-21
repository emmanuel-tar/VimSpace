package com.vimspace.foh.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vimspace.contract.*;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class ApiClient {
    private static final String BASE_URL = "http://localhost:8080/api";
    private final HttpClient client;
    private final ObjectMapper mapper;
    private String authToken;

    public ApiClient() {
        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .build();
        this.mapper = new ObjectMapper();
    }

    public Optional<LoginResponse> login(String username, String password) {
        try {
            LoginRequest req = new LoginRequest(username, password);
            String json = mapper.writeValueAsString(req);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/auth/login"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                LoginResponse loginResp = mapper.readValue(response.body(), LoginResponse.class);
                this.authToken = loginResp.getToken();
                return Optional.of(loginResp);
            }
        } catch (Exception e) {
            System.err.println("Login error: " + e.getMessage());
        }
        return Optional.empty();
    }

    public Optional<ProductDTO> getProductByBarcode(String barcode) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/products/barcode/" + barcode.trim()))
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                return Optional.of(mapper.readValue(response.body(), ProductDTO.class));
            }
        } catch (Exception e) {
            System.err.println("Get product by barcode error: " + e.getMessage());
        }
        return Optional.empty();
    }

    public List<ProductDTO> searchProducts(String query) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/products/search?q=" + java.net.URLEncoder.encode(query, "UTF-8")))
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                return mapper.readValue(response.body(), new TypeReference<List<ProductDTO>>() {});
            }
        } catch (Exception e) {
            System.err.println("Search products error: " + e.getMessage());
        }
        return Collections.emptyList();
    }

    public Optional<SaleResponse> completeSale(SaleRequest saleRequest) {
        try {
            String json = mapper.writeValueAsString(saleRequest);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/sales"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                return Optional.of(mapper.readValue(response.body(), SaleResponse.class));
            }
        } catch (Exception e) {
            System.err.println("Complete sale error: " + e.getMessage());
        }
        return Optional.empty();
    }

    public String getAuthToken() {
        return authToken;
    }
}
