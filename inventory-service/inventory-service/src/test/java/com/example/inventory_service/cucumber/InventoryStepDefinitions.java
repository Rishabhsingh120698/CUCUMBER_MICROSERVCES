package com.example.inventory_service.cucumber;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;

public class InventoryStepDefinitions {
    private static final String BASE_URL = "http://localhost:8082";
    private final HttpClient client = HttpClient.newHttpClient();
    private HttpResponse<String> response;

    @Given("the inventory service is running")
    public void serviceIsRunning() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/actuator/health")).GET().build();
        HttpResponse<String> health = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, health.statusCode(), "Inventory Service must be running");
        assertTrue(health.body().contains("UP"), "Health endpoint should report UP");
    }

    @When("I create inventory for {string} with quantity {int}")
    public void createInventory(String name, int quantity) throws IOException, InterruptedException {
        String body = "{\"productName\":\"%s\",\"availableQuantity\":%d}".formatted(escape(name), quantity);
        sendJson("POST", "/api/inventory", body);
    }

    @When("I create inventory with blank product name and quantity {int}")
    public void blankName(int quantity) throws IOException, InterruptedException {
        sendJson("POST", "/api/inventory", "{\"productName\":\"\",\"availableQuantity\":" + quantity + "}");
    }

    @When("I create inventory for {string} with negative quantity {int}")
    public void negativeQuantity(String name, int quantity) throws IOException, InterruptedException {
        String body = "{\"productName\":\"%s\",\"availableQuantity\":%d}".formatted(escape(name), quantity);
        sendJson("POST", "/api/inventory", body);
    }

    @Given("inventory exists for {string} with quantity {int}")
    public void inventoryExists(String name, int quantity) throws IOException, InterruptedException {
        createInventory(name, quantity);
        assertEquals(201, response.statusCode(), "Inventory setup failed: " + response.body());
    }

    @When("I retrieve all inventory") 
    public void getAll() throws IOException, InterruptedException {
        send("GET", "/api/inventory");
    }

    @When("I retrieve inventory for {string}")
    public void getByProduct(String name) throws IOException, InterruptedException {
        send("GET", "/api/inventory/product/" + encode(name));
    }

    @When("I check whether {string} has quantity {int} available")
    public void checkAvailability(String name, int quantity) throws IOException, InterruptedException {
        send("GET", "/api/inventory/product/" + encode(name) + "/availability?quantity=" + quantity);
    }

    @When("I reduce stock for {string} by quantity {int}")
    public void reduceStock(String name, int quantity) throws IOException, InterruptedException {
        send("PUT", "/api/inventory/product/" + encode(name) + "/reduce?quantity=" + quantity);
    }

    @When("I add stock for {string} by quantity {int}")
    public void addStock(String name, int quantity) throws IOException, InterruptedException {
        send("PUT", "/api/inventory/product/" + encode(name) + "/stock?quantity=" + quantity);
    }

    @Then("the inventory response status should be {int}")
    public void checkStatus(int status) {
        assertTrue(response != null, "No API response received");
        assertEquals(status, response.statusCode(), "Unexpected response: " + response.body());
    }

    @Then("the inventory response should contain product {string}")
    public void checkProduct(String name) {
        assertTrue(response.body().contains("\"productName\":\"" + escape(name) + "\"")
                || response.body().contains("\"productName\": \"" + escape(name) + "\""),
                "Product not found in response: " + response.body());
    }

    @Then("the availability response should be {string}")
    public void checkAvailabilityResult(String expected) {
        assertTrue(response.body().contains("\"available\":" + expected)
                || response.body().contains("\"available\": " + expected),
                "Availability did not match: " + response.body());
    }

    @Then("the inventory response should contain available quantity {int}")
    public void checkQuantity(int quantity) {
        assertTrue(response.body().contains("\"availableQuantity\":" + quantity)
                || response.body().contains("\"availableQuantity\": " + quantity),
                "Quantity not found in response: " + response.body());
    }

    private void sendJson(String method, String path, String body) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(BASE_URL + path))
                .header("Content-Type", "application/json")
                .method(method, HttpRequest.BodyPublishers.ofString(body)).build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private void send(String method, String path) throws IOException, InterruptedException {
        HttpRequest.Builder builder = HttpRequest.newBuilder().uri(URI.create(BASE_URL + path));
        if ("GET".equals(method)) builder.GET();
        else builder.method(method, HttpRequest.BodyPublishers.noBody());
        response = client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
    }

    private String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
