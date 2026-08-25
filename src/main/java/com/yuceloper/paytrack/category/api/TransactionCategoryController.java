package com.yuceloper.paytrack.category.api;

import com.yuceloper.paytrack.auth.infrastructure.AuthenticatedUser;
import com.yuceloper.paytrack.category.api.dto.TransactionCategoryDtos;
import com.yuceloper.paytrack.category.application.TransactionCategoryService;
import com.yuceloper.paytrack.shared.api.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class TransactionCategoryController {

    private final TransactionCategoryService service;

    @GetMapping
    public ApiResponse<List<TransactionCategoryDtos.Response>> getAll() {
        return ApiResponse.success(service.getAll(AuthenticatedUser.id()));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<TransactionCategoryDtos.Response> create(
            @Valid @RequestBody TransactionCategoryDtos.CreateRequest request
    ) {
        return ApiResponse.success(service.create(AuthenticatedUser.id(), request));
    }
}
