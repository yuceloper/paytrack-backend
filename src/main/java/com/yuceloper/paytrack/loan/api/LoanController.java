package com.yuceloper.paytrack.loan.api;

import com.yuceloper.paytrack.auth.infrastructure.AuthenticatedUser;
import com.yuceloper.paytrack.loan.api.dto.CreateLoanRequest;
import com.yuceloper.paytrack.loan.api.dto.LoanResponse;
import com.yuceloper.paytrack.loan.application.LoanService;
import com.yuceloper.paytrack.shared.api.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/loans")
@RequiredArgsConstructor
public class LoanController {

    private final LoanService service;

    @GetMapping
    public ApiResponse<List<LoanResponse>> getAll() {
        return ApiResponse.success(service.getByUserId(AuthenticatedUser.id()));
    }

    @GetMapping("/{id}")
    public ApiResponse<LoanResponse> getById(@PathVariable Long id) {
        return ApiResponse.success(service.getById(AuthenticatedUser.id(), id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<LoanResponse> create(@Valid @RequestBody CreateLoanRequest request) {
        return ApiResponse.success(service.create(AuthenticatedUser.id(), request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(AuthenticatedUser.id(), id);
    }
}
