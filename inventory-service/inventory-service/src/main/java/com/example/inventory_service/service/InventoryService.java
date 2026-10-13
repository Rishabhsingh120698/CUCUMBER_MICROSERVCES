package com.example.inventory_service.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.inventory_service.entity.Inventory;
import com.example.inventory_service.exception.InsufficientStockException;
import com.example.inventory_service.exception.InventoryNotFoundException;
import com.example.inventory_service.repository.InventoryRepository;

@Service
public class InventoryService {
    private final InventoryRepository repository;

    public InventoryService(InventoryRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public Inventory createInventory(Inventory inventory) {
        if (repository.findByProductNameIgnoreCase(inventory.getProductName()).isPresent()) {
            throw new IllegalArgumentException("Inventory already exists for product: " + inventory.getProductName());
        }
        return repository.save(inventory);
    }

    @Transactional(readOnly = true)
    public List<Inventory> getAllInventory() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public Inventory getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new InventoryNotFoundException("Inventory not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public Inventory getByProductName(String name) {
        return repository.findByProductNameIgnoreCase(name)
                .orElseThrow(() -> new InventoryNotFoundException("Inventory not found for product: " + name));
    }

    @Transactional(readOnly = true)
    public boolean isAvailable(String name, int quantity) {
        requirePositive(quantity);
        return getByProductName(name).getAvailableQuantity() >= quantity;
    }

    @Transactional
    public Inventory update(Long id, Inventory requested) {
        Inventory existing = getById(id);
        existing.setProductName(requested.getProductName());
        existing.setAvailableQuantity(requested.getAvailableQuantity());
        return repository.save(existing);
    }

    @Transactional
    public Inventory addStock(String name, int quantity) {
        requirePositive(quantity);
        Inventory item = getByProductName(name);
        item.setAvailableQuantity(item.getAvailableQuantity() + quantity);
        return repository.save(item);
    }

    @Transactional
    public Inventory reduceStock(String name, int quantity) {
        requirePositive(quantity);
        Inventory item = getByProductName(name);
        if (item.getAvailableQuantity() < quantity) {
            throw new InsufficientStockException("Insufficient stock for product: " + name);
        }
        item.setAvailableQuantity(item.getAvailableQuantity() - quantity);
        return repository.save(item);
    }

    @Transactional
    public void delete(Long id) {
        repository.delete(getById(id));
    }

    private void requirePositive(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }
    }
}
