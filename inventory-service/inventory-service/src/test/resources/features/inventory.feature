Feature: Inventory management API

  Scenario: Create valid inventory
    Given the inventory service is running
    When I create inventory for "Cucumber Phone" with quantity 10
    Then the inventory response status should be 201
    And the inventory response should contain product "Cucumber Phone"
    And the inventory response should contain available quantity 10

  Scenario: Reject blank product name
    Given the inventory service is running
    When I create inventory with blank product name and quantity 5
    Then the inventory response status should be 400

  Scenario: Reject negative stock
    Given the inventory service is running
    When I create inventory for "Negative Stock Phone" with negative quantity -2
    Then the inventory response status should be 400

  Scenario: Retrieve all inventory
    Given the inventory service is running
    And inventory exists for "Read All Inventory Phone" with quantity 12
    When I retrieve all inventory
    Then the inventory response status should be 200
    And the inventory response should contain product "Read All Inventory Phone"

  Scenario: Retrieve inventory by product name
    Given the inventory service is running
    And inventory exists for "Read One Inventory Phone" with quantity 8
    When I retrieve inventory for "Read One Inventory Phone"
    Then the inventory response status should be 200
    And the inventory response should contain product "Read One Inventory Phone"

  Scenario: Check available stock
    Given the inventory service is running
    And inventory exists for "Availability Phone" with quantity 10
    When I check whether "Availability Phone" has quantity 4 available
    Then the inventory response status should be 200
    And the availability response should be "true"

  Scenario: Report insufficient stock
    Given the inventory service is running
    And inventory exists for "Low Stock Phone" with quantity 2
    When I check whether "Low Stock Phone" has quantity 5 available
    Then the inventory response status should be 200
    And the availability response should be "false"

  Scenario: Add stock
    Given the inventory service is running
    And inventory exists for "Add Stock Phone" with quantity 3
    When I add stock for "Add Stock Phone" by quantity 4
    Then the inventory response status should be 200
    And the inventory response should contain available quantity 7

  Scenario: Reduce stock
    Given the inventory service is running
    And inventory exists for "Reduce Stock Phone" with quantity 9
    When I reduce stock for "Reduce Stock Phone" by quantity 3
    Then the inventory response status should be 200
    And the inventory response should contain available quantity 6

  Scenario: Reject reducing stock below zero
    Given the inventory service is running
    And inventory exists for "Insufficient Stock Phone" with quantity 1
    When I reduce stock for "Insufficient Stock Phone" by quantity 5
    Then the inventory response status should be 409
