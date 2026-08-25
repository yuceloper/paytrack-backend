package com.yuceloper.paytrack.subscription.application;

import com.yuceloper.paytrack.subscription.domain.Subscription;

import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository {
    Subscription save(Subscription subscription);
    Optional<Subscription> findById(Long id);
    List<Subscription> findAllByUserId(Long userId);
    void deleteById(Long id);
}
