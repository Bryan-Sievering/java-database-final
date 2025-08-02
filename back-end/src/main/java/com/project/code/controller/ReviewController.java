package com.project.code.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.project.code.repo.ReviewRepository;
import com.project.code.repo.CustomerRepository;
import com.project.code.model.Review;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;


@RestController
@RequestMapping("/reviews")
public class ReviewController {
    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @GetMapping("/{storeId}/{productId}")
    public Map<String, Object> getReviews(@PathVariable long storeId, @PathVariable long productId) {
        Map<String, Object> response = new HashMap<>();

        List<Review> reviews = reviewRepository.findByStoreIdAndProductId(storeId, productId);
        List<Map<String, Object>> reviewsWithCustomerNames = new ArrayList<>();

        for (Review review : reviews) {
            Map<String, Object> reviewMap = new HashMap<>();
            reviewMap.put("comment", review.getComment());
            reviewMap.put("rating", review.getRating());

            customerRepository.findById(review.getCustomerId()).ifPresentOrElse(
                customer -> reviewMap.put("customerName", customer.getName()),
                () -> reviewMap.put("customerName", "Unknown")
            );

            reviewsWithCustomerNames.add(reviewMap);
        }

        response.put("reviews", reviewsWithCustomerNames);
        return response;
    }
}