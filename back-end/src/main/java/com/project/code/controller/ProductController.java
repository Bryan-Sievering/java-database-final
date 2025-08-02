package com.project.code.controller;

import com.project.code.model.Product;
import com.project.code.repo.ProductRepository;
import com.project.code.repo.InventoryRepository;
import com.project.code.service.ServiceClass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/product")
public class ProductController {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ServiceClass serviceClass;

    @Autowired
    private InventoryRepository inventoryRepository;

    @PostMapping
    public Map<String, String> addProduct(@RequestBody Product product) {
        Map<String, String> response = new HashMap<>();
        if (!serviceClass.validateProduct(product)) {
            response.put("message", "Product already present in database");
            return response;
        }
        try {
            productRepository.save(product);
            response.put("message", "Product added successfully");
        } catch (DataIntegrityViolationException e) {
            response.put("message", "SKU should be unique");
        } catch (Exception e) {
            response.put("message", "Error: " + e.getMessage());
        }
        return response;
    }

    @GetMapping("/product/{id}")
    public Map<String, Object> getProductbyId(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        Optional<Product> product = productRepository.findById(id);
        response.put("product", product.orElse(null));
        return response;
    }

    @PutMapping
    public Map<String, String> updateProduct(@RequestBody Product product) {
        Map<String, String> response = new HashMap<>();
        try {
            productRepository.save(product);
            response.put("message", "Product updated successfully");
        } catch (Exception e) {
            response.put("message", "Error: " + e.getMessage());
        }
        return response;
    }

    @GetMapping("/category/{name}/{category}")
    public Map<String, Object> filterbyCategoryProduct(
            @PathVariable("name") String name,
            @PathVariable("category") String category) {

        Map<String, Object> response = new HashMap<>();

        if ("null".equals(name) && !"null".equals(category)) {
            response.put("products", productRepository.findByCategory(category));
        } else if (!"null".equals(name) && "null".equals(category)) {
            response.put("products", productRepository.findProductBySubName(name));
        } else if (!"null".equals(name) && !"null".equals(category)) {
            response.put("products", productRepository.findProductBySubNameAndCategory(name, category));
        } else {
            response.put("products", List.of());
        }
        return response;
    }


    @GetMapping
    public Map<String, Object> listProduct() {
        Map<String, Object> response = new HashMap<>();
        List<Product> products = productRepository.findAll();
        response.put("products", products);
        return response;
    }

    @GetMapping("filter/{category}/{storeid}")
    public Map<String, Object> getProductbyCategoryAndStoreId(
            @PathVariable("category") String category,
            @PathVariable("storeid") Long storeId) {

        Map<String, Object> response = new HashMap<>();
        List<Product> products = productRepository.findProductByCategory(category, storeId);
        response.put("product", products);
        return response;
    }

    @DeleteMapping("/{id}")
    public Map<String, String> deleteProduct(@PathVariable Long id) {
        Map<String, String> response = new HashMap<>();

        // Validate if product exists using ServiceClass method
        if (!serviceClass.validateProductId(id)) {
            response.put("message", "Product with id " + id + " not present in database");
            return response;
        }

        try {
            // First delete inventory entries related to the product
            inventoryRepository.deleteByProductId(id);

            // Then delete the product itself
            productRepository.deleteById(id);

            response.put("message", "Product deleted successfully with id: " + id);
        } catch (Exception e) {
            response.put("message", "Error: " + e.getMessage());
        }

        return response;
    }

    @GetMapping("/searchProduct/{name}")
    public Map<String, Object> searchProduct(@PathVariable String name) {
        Map<String, Object> response = new HashMap<>();
        List<Product> products = productRepository.findProductBySubName(name);
        response.put("products", products);
        return response;
    }
}

