package com.yuceloper.paytrack.account.api;

import com.yuceloper.paytrack.account.api.dto.AccountResponse;
import com.yuceloper.paytrack.account.api.dto.AccountUpsertRequest;
import com.yuceloper.paytrack.account.application.AccountService;
import com.yuceloper.paytrack.shared.api.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService service;

    @GetMapping
    public ApiResponse<List<AccountResponse>> getAll(@RequestParam Long userId) {
        return ApiResponse.success(service.getAll(userId));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<AccountResponse> create(@Valid @RequestBody AccountUpsertRequest request) {
        return ApiResponse.success(service.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<AccountResponse> update(@PathVariable Long id, @Valid @RequestBody AccountUpsertRequest request) {
        return ApiResponse.success(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
