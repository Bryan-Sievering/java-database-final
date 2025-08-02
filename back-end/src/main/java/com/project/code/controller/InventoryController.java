package com.project.code.controller;

import com.project.code.model.CombinedRequest;
import com.project.code.model.Inventory;
import com.project.code.model.Product;
import com.project.code.repo.InventoryRepository;
import com.project.code.repo.ProductRepository;
import com.project.code.service.ServiceClass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/inventory")
public class InventoryController {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private ServiceClass serviceClass;

    @PutMapping
    public Map<String, String> updateInventory(@RequestBody CombinedRequest request) {
        Map<String, String> response = new HashMap<>();
        Product product = request.getProduct();
        Inventory inventory = request.getInventory();

        if (!serviceClass.validateProductId(product.getId())) {
            response.put("message", "Invalid product ID");
            return response;
        }

        try {
            Long storeId = null;
            if (inventory.getStore() != null) {
                Object id = inventory.getStore().getId();
                if (id instanceof String) {
                    storeId = Long.valueOf((String) id);
                } else if (id instanceof Long) {
                    storeId = (Long) id;
                }
            }

            Inventory existingInventory = inventoryRepository.findByProductIdAndStoreId(product.getId(), storeId);

            if (existingInventory != null) {
                existingInventory.setQuantity(inventory.getQuantity());
                inventoryRepository.save(existingInventory);
                productRepository.save(product);
                response.put("message", "Successfully updated product");
            } else {
                response.put("message", "No data available");
            }
        } catch (DataIntegrityViolationException e) {
            response.put("message", "Data integrity violation: " + e.getMessage());
        } catch (Exception e) {
            response.put("message", "Error: " + e.getMessage());
        }

        return response;
    }


    @PostMapping
    public Map<String, String> saveInventory(@RequestBody Inventory inventory) {
        Map<String, String> response = new HashMap<>();
        try {
            if (!serviceClass.validateInventory(inventory)) {
                response.put("message", "Data already present");
            } else {
                inventoryRepository.save(inventory);
                response.put("message", "Data saved successfully");
            }
        } catch (DataIntegrityViolationException e) {
            response.put("message", "Data integrity violation: " + e.getMessage());
        } catch (Exception e) {
            response.put("message", "Error: " + e.getMessage());
        }
        return response;
    }

    @GetMapping("/{storeid}")
    public Map<String, Object> getAllProducts(@PathVariable("storeid") Long storeId) {
        Map<String, Object> response = new HashMap<>();
        List<Product> products = productRepository.findProductsByStoreId(storeId);
        response.put("products", products);
        return response;
    }

    @GetMapping("filter/{category}/{name}/{storeid}")
    public Map<String, Object> getProductName(
        @PathVariable("category") String category,        
        @PathVariable("name") String name,
        @PathVariable("storeid") Long storeId) {

        Map<String, Object> response = new HashMap<>();

        if ("null".equals(category) && !"null".equals(name)) {
            response.put("product", productRepository.findByNameLike(storeId, name));
        } else if (!"null".equals(category) && "null".equals(name)) {
            response.put("product", productRepository.findByCategoryAndStoreId(storeId, category));
        } else if (!"null".equals(category) && !"null".equals(name)) {
            response.put("product", productRepository.findByNameAndCategory(storeId, name, category));
        } else {
            response.put("product", List.of());
        }

        return response;
    }

    @GetMapping("search/{name}/{storeId}")
    public Map<String, Object> searchProduct(
            @PathVariable("name") String name,
            @PathVariable("storeId") Long storeId) {

        Map<String, Object> response = new HashMap<>();
        response.put("product", productRepository.findByNameLike(storeId, name));
        return response;
    }

    @DeleteMapping("/{id}")
    public Map<String, String> removeProduct(@PathVariable("id") Long id) {
        Map<String, String> response = new HashMap<>();

        if (!serviceClass.validateProductId(id)) {
            response.put("message", "Product not present in database");
            return response;
        }

        try {
            inventoryRepository.deleteByProductId(id);
            productRepository.deleteById(id);
            response.put("message", "Product deleted successfully");
        } catch (Exception e) {
            response.put("message", "Error: " + e.getMessage());
        }

        return response;
    }

    @GetMapping("validate/{quantity}/{storeId}/{productId}")
    public boolean validateQuantity(
            @PathVariable("quantity") int quantity,
            @PathVariable("storeId") Long storeId,
            @PathVariable("productId") Long productId) {

            Inventory inventory = inventoryRepository.findByProductIdAndStoreId(productId, storeId);
            return inventory != null && inventory.getQuantity() >= quantity;           
    }
}