package com.yuceloper.paytrack.subscription.infrastructure;

import com.yuceloper.paytrack.subscription.application.SubscriptionRepository;
import com.yuceloper.paytrack.subscription.domain.Subscription;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

interface SubscriptionJpaRepository extends JpaRepository<Subscription, Long> {
    List<Subscription> findAllByUserIdOrderByIdDesc(Long userId);
}

@Component
@RequiredArgsConstructor
public class SpringDataSubscriptionRepository implements SubscriptionRepository {
    private final SubscriptionJpaRepository repository;

    @Override public Subscription save(Subscription subscription) { return repository.save(subscription); }
    @Override public Optional<Subscription> findById(Long id) { return repository.findById(id); }
    @Override public List<Subscription> findAllByUserId(Long userId) { return repository.findAllByUserIdOrderByIdDesc(userId); }
    @Override public void deleteById(Long id) { repository.deleteById(id); }
}
