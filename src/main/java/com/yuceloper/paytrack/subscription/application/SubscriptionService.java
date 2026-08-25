package com.yuceloper.paytrack.subscription.application;

import com.yuceloper.paytrack.shared.exception.ResourceNotFoundException;
import com.yuceloper.paytrack.subscription.api.dto.CreateSubscriptionRequest;
import com.yuceloper.paytrack.subscription.api.dto.SubscriptionResponse;
import com.yuceloper.paytrack.subscription.domain.Subscription;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubscriptionService {

    private final SubscriptionRepository repository;

    public List<SubscriptionResponse> getByUserId(Long userId) {
        return repository.findAllByUserId(userId).stream().map(this::toResponse).toList();
    }

    public SubscriptionResponse getById(Long id) {
        return toResponse(getEntity(id));
    }

    @Transactional
    public SubscriptionResponse create(CreateSubscriptionRequest request) {
        Subscription subscription = Subscription.builder()
                .userId(request.userId())
                .name(request.name())
                .provider(request.provider())
                .amount(request.amount())
                .currency(request.currency().toUpperCase())
                .billingPeriod(request.billingPeriod().toUpperCase())
                .billingDay(request.billingDay())
                .nextBillingDate(request.nextBillingDate())
                .active(true)
                .build();
        return toResponse(repository.save(subscription));
    }

    @Transactional
    public void delete(Long id) {
        getEntity(id);
        repository.deleteById(id);
    }

    private Subscription getEntity(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription", id));
    }

    private SubscriptionResponse toResponse(Subscription subscription) {
        return new SubscriptionResponse(
                subscription.getId(), subscription.getUserId(), subscription.getName(), subscription.getProvider(),
                subscription.getAmount(), subscription.getCurrency(), subscription.getBillingPeriod(),
                subscription.getBillingDay(), subscription.getNextBillingDate(), subscription.isActive()
        );
    }
}
