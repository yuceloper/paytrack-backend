package com.yuceloper.paytrack.subscription.api;

import com.yuceloper.paytrack.shared.api.ApiResponse;
import com.yuceloper.paytrack.subscription.api.dto.CreateSubscriptionRequest;
import com.yuceloper.paytrack.subscription.api.dto.SubscriptionResponse;
import com.yuceloper.paytrack.subscription.application.SubscriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService service;

    @GetMapping
    public ApiResponse<List<SubscriptionResponse>> getAll(@RequestParam Long userId) {
        return ApiResponse.success(service.getByUserId(userId));
    }

    @GetMapping("/{id}")
    public ApiResponse<SubscriptionResponse> getById(@PathVariable Long id) {
        return ApiResponse.success(service.getById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<SubscriptionResponse> create(@Valid @RequestBody CreateSubscriptionRequest request) {
        return ApiResponse.success(service.create(request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
