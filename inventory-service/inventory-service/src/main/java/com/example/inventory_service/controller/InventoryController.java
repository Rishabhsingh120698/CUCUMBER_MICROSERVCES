package com.example.inventory_service.controller;

import java.util.List;
import java.util.Map;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.inventory_service.entity.Inventory;
import com.example.inventory_service.service.InventoryService;

@RestController
@RequestMapping("/api/inventory")
@Validated
public class InventoryController {
    private final InventoryService service;

    public InventoryController(InventoryService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Inventory> create(@Valid @RequestBody Inventory inventory) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createInventory(inventory));
    }

    @GetMapping
    public List<Inventory> getAll() {
        return service.getAllInventory();
    }

    @GetMapping("/{id}")
    public Inventory getById(@PathVariable("id") Long id) {
        return service.getById(id);
    }

    @GetMapping("/product/{productName}")
    public Inventory getByProductName(@PathVariable("productName") String productName) {
        return service.getByProductName(productName);
    }

    @GetMapping("/product/{productName}/availability")
    public Map<String, Object> availability(
            @PathVariable("productName") String productName,
            @RequestParam("quantity") @Positive(message = "Quantity must be greater than zero") int quantity) {
        return Map.of("productName", productName,
                "requestedQuantity", quantity,
                "available", service.isAvailable(productName, quantity));
    }

    @PutMapping("/{id}")
    public Inventory update(@PathVariable("id") Long id, @Valid @RequestBody Inventory inventory) {
        return service.update(id, inventory);
    }

    @PutMapping("/product/{productName}/stock")
    public Inventory addStock(
            @PathVariable("productName") String productName,
            @RequestParam("quantity") @Positive(message = "Quantity must be greater than zero") int quantity) {
        return service.addStock(productName, quantity);
    }

    @PutMapping("/product/{productName}/reduce")
    public Inventory reduceStock(
            @PathVariable("productName") String productName,
            @RequestParam("quantity") @Positive(message = "Quantity must be greater than zero") int quantity) {
        return service.reduceStock(productName, quantity);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
