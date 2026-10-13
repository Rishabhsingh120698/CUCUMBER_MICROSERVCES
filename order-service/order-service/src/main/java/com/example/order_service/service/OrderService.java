package com.example.order_service.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.order_service.client.InventoryClient;
import com.example.order_service.entity.Order;
import com.example.order_service.exception.OrderNotFoundException;
import com.example.order_service.repository.OrderRepository;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final InventoryClient inventoryClient;

    public OrderService(OrderRepository orderRepository, InventoryClient inventoryClient) {
        this.orderRepository = orderRepository;
        this.inventoryClient = inventoryClient;
    }

    @Transactional
    public Order createOrder(Order order) {
        inventoryClient.reserveStock(order.getProductName(), order.getQuantity());
        order.setStatus("CREATED");
        return orderRepository.save(order);
    }

    @Transactional(readOnly = true)
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
    }

    @Transactional
    public Order updateOrder(Long id, Order requestedOrder) {
        Order existing = getOrderById(id);
        existing.setProductName(requestedOrder.getProductName());
        existing.setQuantity(requestedOrder.getQuantity());
        return orderRepository.save(existing);
    }

    @Transactional
    public void deleteOrder(Long id) {
        orderRepository.delete(getOrderById(id));
    }
}
