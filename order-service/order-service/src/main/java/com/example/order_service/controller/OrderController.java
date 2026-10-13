
package com.example.order_service.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.order_service.entity.Order;
import com.example.order_service.service.OrderService;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // CREATE
    @PostMapping
    public ResponseEntity<Order> createOrder(
            @Valid @RequestBody Order order) {

        Order createdOrder = orderService.createOrder(order);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createdOrder);
    }

    // READ ALL
    @GetMapping
    public List<Order> getAllOrders() {
        return orderService.getAllOrders();
    }

    // READ ONE
    @GetMapping("/{id}")
    public Order getOrderById(
            @PathVariable("id") Long id) {

        return orderService.getOrderById(id);
    }

    // UPDATE
    @PutMapping("/{id}")
    public Order updateOrder(
            @PathVariable("id") Long id,
            @Valid @RequestBody Order order) {

        return orderService.updateOrder(id, order);
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(
            @PathVariable("id") Long id) {

        orderService.deleteOrder(id);

        return ResponseEntity.noContent().build();
    }
}
