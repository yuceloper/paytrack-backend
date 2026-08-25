package com.yuceloper.paytrack.payment.api;

import com.yuceloper.paytrack.payment.api.dto.PaymentResponse;
import com.yuceloper.paytrack.payment.application.PaymentService;
import com.yuceloper.paytrack.shared.api.ApiResponse;
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
}
