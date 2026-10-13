
Feature: Order management API

  Scenario: Create a valid order
    Given the order service is running
    When I create an order for "Cucumber Phone" with quantity 2
    Then the response status should be 201
    And the response should contain product "Cucumber Phone"
    And the order status should be "CREATED"

  Scenario: Reject a blank product name
    Given the order service is running
    When I create an order for "" with quantity 2
    Then the response status should be 400

  Scenario: Reject a missing product name
    Given the order service is running
    When I create an order with missing product name and quantity 2
    Then the response status should be 400

  Scenario: Reject a negative quantity
    Given the order service is running
    When I create an order for "Invalid Phone" with quantity -5
    Then the response status should be 400

  Scenario: Reject a zero quantity
    Given the order service is running
    When I create an order for "Zero Phone" with quantity 0
    Then the response status should be 400

  Scenario: Reject a missing quantity
    Given the order service is running
    When I create an order for "Missing Quantity Phone" without quantity
    Then the response status should be 400

  Scenario: Retrieve all orders
    Given the order service is running
    And an order exists for "Read All Test" with quantity 3
    When I retrieve all orders
    Then the response status should be 200
    And the response should contain product "Read All Test"

  Scenario: Retrieve an existing order
    Given the order service is running
    And an order exists for "Read One Test" with quantity 2
    When I retrieve the order I just created
    Then the response status should be 200
    And the response should contain product "Read One Test"

  Scenario: Return not found for a missing order
    Given the order service is running
    When I retrieve order with id 9223372036854775807
    Then the response status should be 404

  Scenario: Update an existing order
    Given the order service is running
    And an order exists for "Before Update" with quantity 2
    When I update the order I just created to product "After Update" with quantity 5
    Then the response status should be 200
    And the response should contain product "After Update"

  Scenario: Reject an update with an invalid quantity
    Given the order service is running
    And an order exists for "Update Validation Test" with quantity 2
    When I update the order I just created to product "Invalid Update" with quantity -1
    Then the response status should be 400

  Scenario: Return not found when updating a missing order
    Given the order service is running
    When I update order 9223372036854775807 to product "Missing Order" with quantity 2
    Then the response status should be 404

  Scenario: Delete an existing order
    Given the order service is running
    And an order exists for "Delete Test" with quantity 1
    When I delete the order I just created
    Then the response status should be 204

  Scenario: Return not found when deleting a missing order
    Given the order service is running
    When I delete order 9223372036854775807
    Then the response status should be 404
