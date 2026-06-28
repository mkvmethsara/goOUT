package com.squadx.goout.Repository;

import com.squadx.goout.Entity.Post;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends MongoRepository<Post, String> {

    // This magic method name tells Spring Boot to sort the feed newest-first!
    List<Post> findAllByOrderByCreatedAtDesc();
}