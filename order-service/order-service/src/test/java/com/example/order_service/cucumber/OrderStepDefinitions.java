
package com.example.order_service.cucumber;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;

public class OrderStepDefinitions {

    private static final String BASE_URL =
            "http://localhost:8081";

    private final HttpClient httpClient =
            HttpClient.newHttpClient();

    private HttpResponse<String> response;

    private Long createdOrderId;

    // Check whether the Order Service is running
    @Given("the order service is running")
    public void theOrderServiceIsRunning()
            throws IOException, InterruptedException {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/actuator/health"))
                .GET()
                .build();

        HttpResponse<String> healthResponse = httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );

        assertEquals(
                200,
                healthResponse.statusCode(),
                "Order Service must be running"
        );
    }

    // Create an order with product name and quantity
    @When("I create an order for {string} with quantity {int}")
    public void iCreateAnOrder(String productName, int quantity)
            throws IOException, InterruptedException {

        String body = """
                {
                  "productName": "%s",
                  "quantity": %d
                }
                """.formatted(escapeJson(productName), quantity);

        sendPost(body);
    }

    // Create an order without the productName field
    @When("I create an order with missing product name and quantity {int}")
    public void iCreateOrderWithoutProductName(int quantity)
            throws IOException, InterruptedException {

        String body = """
                {
                  "quantity": %d
                }
                """.formatted(quantity);

        sendPost(body);
    }

    // Create an order without the quantity field
    @When("I create an order for {string} without quantity")
    public void iCreateOrderWithoutQuantity(String productName)
            throws IOException, InterruptedException {

        String body = """
                {
                  "productName": "%s"
                }
                """.formatted(escapeJson(productName));

        sendPost(body);
    }

    // Create an order and remember its generated ID
    @Given("an order exists for {string} with quantity {int}")
    public void anOrderExists(String productName, int quantity)
            throws IOException, InterruptedException {

        String body = """
                {
                  "productName": "%s",
                  "quantity": %d
                }
                """.formatted(escapeJson(productName), quantity);

        sendPost(body);

        assertEquals(
                201,
                response.statusCode(),
                "Test setup must successfully create an order. Response: "
                        + response.body()
        );

        createdOrderId = extractOrderId(response.body());
    }

    // Retrieve every order
    @When("I retrieve all orders")
    public void iRetrieveAllOrders()
            throws IOException, InterruptedException {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/orders"))
                .GET()
                .build();

        send(request);
    }

    // Retrieve an order using the ID captured earlier
    @When("I retrieve the order I just created")
    public void iRetrieveTheOrderIJustCreated()
            throws IOException, InterruptedException {

        ensureCreatedOrderId();

        getOrder(createdOrderId);
    }

    // Retrieve an order using a supplied ID
    @When("I retrieve order with id {long}")
    public void iRetrieveOrderWithId(long id)
            throws IOException, InterruptedException {

        getOrder(id);
    }

    // Update the order created by the Given step
    @When("I update the order I just created to product {string} with quantity {int}")
    public void iUpdateTheOrderIJustCreated(
            String productName,
            int quantity)
            throws IOException, InterruptedException {

        ensureCreatedOrderId();

        updateOrder(createdOrderId, productName, quantity);
    }

    // Update an order using a supplied ID
    @When("I update order {long} to product {string} with quantity {int}")
    public void iUpdateOrder(
            long id,
            String productName,
            int quantity)
            throws IOException, InterruptedException {

        updateOrder(id, productName, quantity);
    }

    // Delete the order created by the Given step
    @When("I delete the order I just created")
    public void iDeleteTheOrderIJustCreated()
            throws IOException, InterruptedException {

        ensureCreatedOrderId();

        deleteOrder(createdOrderId);
    }

    // Delete an order using a supplied ID
    @When("I delete order {long}")
    public void iDeleteOrder(long id)
            throws IOException, InterruptedException {

        deleteOrder(id);
    }

    // Verify the HTTP status code
    @Then("the response status should be {int}")
    public void theResponseStatusShouldBe(int expectedStatus) {

        assertTrue(response != null, "No API response was received");

        assertEquals(
                expectedStatus,
                response.statusCode(),
                "Unexpected response body: "
                        + (response == null ? "" : response.body())
        );
    }

    // Verify that a product appears in the JSON response
    @Then("the response should contain product {string}")
    public void theResponseShouldContainProduct(String productName) {

        assertTrue(response != null, "No API response was received");

        String expectedField =
                "\"productName\":\"" + escapeJson(productName) + "\"";

        String expectedFieldWithSpace =
                "\"productName\": \"" + escapeJson(productName) + "\"";

        assertTrue(
                response.body().contains(expectedField)
                        || response.body().contains(expectedFieldWithSpace),
                "Expected product was not found: " + productName
                        + ". Actual response: " + response.body()
        );
    }

    // Verify the order status
    @Then("the order status should be {string}")
    public void theOrderStatusShouldBe(String expectedStatus) {

        assertTrue(response != null, "No API response was received");

        String expectedField = "\"status\":\"" + expectedStatus + "\"";
        String expectedFieldWithSpace =
                "\"status\": \"" + expectedStatus + "\"";

        assertTrue(
                response.body().contains(expectedField)
                        || response.body().contains(expectedFieldWithSpace),
                "Expected status was not found: " + expectedStatus
                        + ". Actual response: " + response.body()
        );
    }

    // -------------------------
    // Reusable HTTP operations
    // -------------------------

    private void sendPost(String body)
            throws IOException, InterruptedException {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/orders"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        send(request);

        if (response.statusCode() == 201) {
            createdOrderId = extractOrderId(response.body());
        }
    }

    private void getOrder(long id)
            throws IOException, InterruptedException {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/orders/" + id))
                .GET()
                .build();

        send(request);
    }

    private void updateOrder(
            long id,
            String productName,
            int quantity)
            throws IOException, InterruptedException {

        String body = """
                {
                  "productName": "%s",
                  "quantity": %d
                }
                """.formatted(escapeJson(productName), quantity);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/orders/" + id))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(body))
                .build();

        send(request);
    }

    private void deleteOrder(long id)
            throws IOException, InterruptedException {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/orders/" + id))
                .DELETE()
                .build();

        send(request);
    }

    private void send(HttpRequest request)
            throws IOException, InterruptedException {

        response = httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );
    }

    // Extract the generated database ID from the JSON response
    private Long extractOrderId(String body) {

        Matcher matcher = Pattern
                .compile("\"id\"\\s*:\\s*(\\d+)")
                .matcher(body);

        if (!matcher.find()) {
            throw new AssertionError(
                    "Could not find the order ID in response: " + body
            );
        }

        return Long.parseLong(matcher.group(1));
    }

    private void ensureCreatedOrderId() {

        if (createdOrderId == null) {
            throw new AssertionError(
                    "No order ID is available. Create an order first."
            );
        }
    }

    private String escapeJson(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }
}
