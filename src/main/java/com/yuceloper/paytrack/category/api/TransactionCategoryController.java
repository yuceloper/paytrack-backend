package com.yuceloper.paytrack.category.api;

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
    public ApiResponse<List<TransactionCategoryDtos.Response>> getAll(@RequestParam Long userId) {
        return ApiResponse.success(service.getAll(userId));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<TransactionCategoryDtos.Response> create(
            @Valid @RequestBody TransactionCategoryDtos.CreateRequest request
    ) {
        return ApiResponse.success(service.create(request));
    }
}
