package com.yuceloper.paytrack.payment.api;

import com.yuceloper.paytrack.auth.infrastructure.AuthenticatedUser;
import com.yuceloper.paytrack.payment.api.dto.PaymentResponse;
import com.yuceloper.paytrack.payment.api.dto.PaymentUpsertRequest;
import com.yuceloper.paytrack.payment.application.PaymentService;
import com.yuceloper.paytrack.payment.domain.PaymentSeriesScope;
import com.yuceloper.paytrack.shared.api.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService service;

    @GetMapping
    public ApiResponse<List<PaymentResponse>> getRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return ApiResponse.success(service.getRange(AuthenticatedUser.id(), from, to));
    }

    @GetMapping("/upcoming")
    public ApiResponse<List<PaymentResponse>> getUpcoming(
            @RequestParam(defaultValue = "7") int days
    ) {
        int safeDays = Math.max(1, Math.min(days, 365));
        return ApiResponse.success(service.getUpcoming(AuthenticatedUser.id(), safeDays));
    }

    @PostMapping
    public ApiResponse<PaymentResponse> create(@Valid @RequestBody PaymentUpsertRequest request) {
        return ApiResponse.success(service.create(AuthenticatedUser.id(), request));
    }

    @PutMapping("/{id}")
    public ApiResponse<PaymentResponse> update(
            @PathVariable Long id,
            @RequestParam(defaultValue = "THIS") PaymentSeriesScope scope,
            @Valid @RequestBody PaymentUpsertRequest request
    ) {
        return ApiResponse.success(service.update(AuthenticatedUser.id(), id, request, scope));
    }

    @PatchMapping("/{id}/paid")
    public ApiResponse<PaymentResponse> markPaid(
            @PathVariable Long id,
            @RequestParam(required = false) Long accountId
    ) {
        return ApiResponse.success(service.markPaid(AuthenticatedUser.id(), id, accountId));
    }

    @PatchMapping("/{id}/pending")
    public ApiResponse<PaymentResponse> markPending(@PathVariable Long id) {
        return ApiResponse.success(service.markPending(AuthenticatedUser.id(), id));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(
            @PathVariable Long id,
            @RequestParam(defaultValue = "THIS") PaymentSeriesScope scope
    ) {
        service.delete(AuthenticatedUser.id(), id, scope);
        return ApiResponse.success(null);
    }
}
