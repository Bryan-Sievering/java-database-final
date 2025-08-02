package com.project.code.repo;

import com.project.code.model.Review;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends MongoRepository<Review, String> {

    List<Review> findByStoreIdAndProductId(Long storeId, Long productId);
}
