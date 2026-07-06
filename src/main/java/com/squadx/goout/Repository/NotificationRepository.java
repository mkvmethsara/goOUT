package com.squadx.goout.Repository;

import com.squadx.goout.Entity.Notification;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends MongoRepository<Notification, String> {

    // Custom query to get a user's notifications, newest ones at the top!
    List<Notification> findByUserIdOrderByCreatedAtDesc(String userId);
}