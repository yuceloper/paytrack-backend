package com.yuceloper.paytrack.payment.api;

import com.yuceloper.paytrack.payment.api.dto.PaymentResponse;
import com.yuceloper.paytrack.payment.api.dto.PaymentUpsertRequest;
import com.yuceloper.paytrack.payment.application.PaymentService;
import com.yuceloper.paytrack.shared.api.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService service;

    @GetMapping("/upcoming")
    public ApiResponse<List<PaymentResponse>> getUpcoming(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "7") int days
    ) {
        int safeDays = Math.max(1, Math.min(days, 365));
        return ApiResponse.success(service.getUpcoming(userId, safeDays));
    }

    @PostMapping
    public ApiResponse<PaymentResponse> create(@Valid @RequestBody PaymentUpsertRequest request) {
        return ApiResponse.success(service.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<PaymentResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody PaymentUpsertRequest request
    ) {
        return ApiResponse.success(service.update(id, request));
    }

    @PatchMapping("/{id}/paid")
    public ApiResponse<PaymentResponse> markPaid(@PathVariable Long id) {
        return ApiResponse.success(service.markPaid(id));
    }

    @PatchMapping("/{id}/pending")
    public ApiResponse<PaymentResponse> markPending(@PathVariable Long id) {
        return ApiResponse.success(service.markPending(id));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ApiResponse.success(null);
    }
}
