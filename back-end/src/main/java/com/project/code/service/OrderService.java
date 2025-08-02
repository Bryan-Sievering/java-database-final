package com.project.code.service;

import com.project.code.model.*;
import com.project.code.repo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private OrderDetailsRepository orderDetailsRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private PurchaseProductDTO purchaseProductDTO;

    public void saveOrder(PlaceOrderRequestDTO placeOrderRequest) {
        // 1. Retrieve or create the Customer
        Customer existingCustomer = customerRepository.findByEmail(placeOrderRequest.getCustomerEmail());
        Customer customer;
        if (existingCustomer == null) {
            customer = new Customer();
            customer.setName(placeOrderRequest.getCustomerName());
            customer.setEmail(placeOrderRequest.getCustomerEmail());
            customer.setPhone(placeOrderRequest.getCustomerPhone());
            customer = customerRepository.save(customer);
        } else {
            customer = existingCustomer;
        }

        // 2. Retrieve the Store
        Store store = storeRepository.findById(placeOrderRequest.getStoreId())
                .orElseThrow(() -> new RuntimeException("Store not found"));

        // 3. Create OrderDetails
        OrderDetails orderDetails = new OrderDetails();
        orderDetails.setCustomer(customer);
        orderDetails.setStore(store);
        orderDetails.setDate(LocalDateTime.now());
        orderDetails.setTotalPrice(placeOrderRequest.getTotalPrice());
        orderDetails = orderDetailsRepository.save(orderDetails);

        // 4. Create and save OrderItems
        List<PurchaseProductDTO> purchaseProducts = placeOrderRequest.getPurchaseProduct();
        for (PurchaseProductDTO productDTO : purchaseProducts) {
            // Check inventory
            Inventory inventory = inventoryRepository.findByProductIdAndStoreId(purchaseProductDTO.getId(), placeOrderRequest.getStoreId());
            if (inventory == null || inventory.getStockLevel() < productDTO.getQuantity()) {
                throw new RuntimeException("Insufficient stock for product ID: " + productDTO.getId());
            }
            // Update inventory stock level
            inventory.setStockLevel(inventory.getStockLevel() - productDTO.getQuantity());
            inventoryRepository.save(inventory);

            // Create OrderItem
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(orderDetails);
            orderItem.setProduct(productRepository.findById(productDTO.getId())
                    .orElseThrow(() -> new RuntimeException("Product not found")));
            orderItem.setQuantity(productDTO.getQuantity());
            orderItem.setPrice(productDTO.getPrice() * productDTO.getQuantity());
            orderItemRepository.save(orderItem);
        }
    }
}