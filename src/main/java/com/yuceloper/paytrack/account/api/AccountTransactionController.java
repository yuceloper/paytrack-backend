package com.yuceloper.paytrack.account.api;

import com.yuceloper.paytrack.account.api.dto.AccountTransactionDtos;
import com.yuceloper.paytrack.account.application.AccountTransactionService;
import com.yuceloper.paytrack.auth.infrastructure.AuthenticatedUser;
import com.yuceloper.paytrack.shared.api.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/account-transactions")
@RequiredArgsConstructor
public class AccountTransactionController {

    private final AccountTransactionService service;

    @GetMapping
    public ApiResponse<List<AccountTransactionDtos.Response>> getRange(
            @RequestParam LocalDate from,
            @RequestParam LocalDate to
    ) {
        return ApiResponse.success(service.getRange(AuthenticatedUser.id(), from, to));
    }

    @PostMapping("/transfer")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<AccountTransactionDtos.Response> transfer(
            @Valid @RequestBody AccountTransactionDtos.TransferRequest request
    ) {
        return ApiResponse.success(service.transfer(AuthenticatedUser.id(), request));
    }

    @PostMapping("/manual")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<AccountTransactionDtos.Response> manual(
            @Valid @RequestBody AccountTransactionDtos.ManualRequest request
    ) {
        return ApiResponse.success(service.manual(AuthenticatedUser.id(), request));
    }
}
