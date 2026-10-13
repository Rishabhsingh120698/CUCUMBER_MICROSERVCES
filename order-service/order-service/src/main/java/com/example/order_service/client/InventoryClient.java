package com.example.order_service.client;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class InventoryClient {
    private final HttpClient client = HttpClient.newHttpClient();
    private final String baseUrl;

    public InventoryClient(@Value("${services.inventory.base-url:http://localhost:8082}") String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public void reserveStock(String productName, int quantity) {
        String encoded = URLEncoder.encode(productName, StandardCharsets.UTF_8).replace("+", "%20");
        String path = "/api/inventory/product/" + encoded;

        HttpResponse<String> availability = sendGet(baseUrl + path
                + "/availability?quantity=" + quantity);

        if (availability.statusCode() == 404) {
            throw new InventoryCommunicationException("No inventory found for product: " + productName, 409);
        }
        if (availability.statusCode() != 200) {
            throw new InventoryCommunicationException(
                    "Inventory Service returned HTTP " + availability.statusCode(), 502);
        }
        if (!availability.body().contains("\"available\":true")
                && !availability.body().contains("\"available\": true")) {
            throw new InventoryCommunicationException("Insufficient stock for product: " + productName, 409);
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path + "/reduce?quantity=" + quantity))
                .PUT(HttpRequest.BodyPublishers.noBody())
                .build();
        try {
            HttpResponse<String> reduction = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (reduction.statusCode() == 409) {
                throw new InventoryCommunicationException("Insufficient stock for product: " + productName, 409);
            }
            if (reduction.statusCode() != 200) {
                throw new InventoryCommunicationException(
                        "Could not reserve stock; Inventory Service returned HTTP "
                                + reduction.statusCode(), 502);
            }
        } catch (IOException ex) {
            throw new InventoryCommunicationException("Could not communicate with Inventory Service", 502, ex);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new InventoryCommunicationException("Inventory request was interrupted", 502, ex);
        }
    }

    private HttpResponse<String> sendGet(String url) {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
        try {
            return client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException ex) {
            throw new InventoryCommunicationException("Could not communicate with Inventory Service", 502, ex);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new InventoryCommunicationException("Inventory request was interrupted", 502, ex);
        }
    }
}
