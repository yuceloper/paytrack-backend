package com.yuceloper.paytrack.income.api;

import com.yuceloper.paytrack.income.api.dto.IncomeResponses;
import com.yuceloper.paytrack.income.api.dto.IncomeSourceRequest;
import com.yuceloper.paytrack.income.application.IncomeService;
import com.yuceloper.paytrack.shared.api.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/incomes")
@RequiredArgsConstructor
public class IncomeController {

    private final IncomeService service;

    @GetMapping("/sources")
    public ApiResponse<List<IncomeResponses.Source>> getSources(@RequestParam Long userId) {
        return ApiResponse.success(service.getSources(userId));
    }

    @PostMapping("/sources")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<IncomeResponses.Source> createSource(@Valid @RequestBody IncomeSourceRequest request) {
        return ApiResponse.success(service.createSource(request));
    }

    @GetMapping("/occurrences")
    public ApiResponse<List<IncomeResponses.Occurrence>> getOccurrences(
            @RequestParam Long userId,
            @RequestParam LocalDate from,
            @RequestParam LocalDate to
    ) {
        return ApiResponse.success(service.getOccurrences(userId, from, to));
    }

    @PatchMapping("/occurrences/{id}/received")
    public ApiResponse<IncomeResponses.Occurrence> markReceived(
            @PathVariable Long id,
            @RequestParam(required = false) Long accountId
    ) {
        return ApiResponse.success(service.markReceived(id, accountId));
    }

    @PatchMapping("/occurrences/{id}/pending")
    public ApiResponse<IncomeResponses.Occurrence> markPending(@PathVariable Long id) {
        return ApiResponse.success(service.markPending(id));
    }
}
