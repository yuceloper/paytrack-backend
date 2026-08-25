package com.yuceloper.paytrack.creditcard.api;

import com.yuceloper.paytrack.creditcard.api.dto.CreateCreditCardRequest;
import com.yuceloper.paytrack.creditcard.api.dto.CreditCardResponse;
import com.yuceloper.paytrack.creditcard.application.CreditCardService;
import com.yuceloper.paytrack.shared.api.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/credit-cards")
@RequiredArgsConstructor
public class CreditCardController {

    private final CreditCardService service;

    @GetMapping
    public ApiResponse<List<CreditCardResponse>> getAll(@RequestParam Long userId) {
        return ApiResponse.success(service.getByUserId(userId));
    }

    @GetMapping("/{id}")
    public ApiResponse<CreditCardResponse> getById(@PathVariable Long id) {
        return ApiResponse.success(service.getById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CreditCardResponse> create(@Valid @RequestBody CreateCreditCardRequest request) {
        return ApiResponse.success(service.create(request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
