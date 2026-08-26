package com.yuceloper.paytrack.income.api;

import com.yuceloper.paytrack.auth.infrastructure.AuthenticatedUser;
import com.yuceloper.paytrack.income.api.dto.IncomeOccurrenceUpdateRequest;
import com.yuceloper.paytrack.income.api.dto.IncomeResponses;
import com.yuceloper.paytrack.income.api.dto.IncomeSourceRequest;
import com.yuceloper.paytrack.income.application.IncomeService;
import com.yuceloper.paytrack.income.domain.IncomeSeriesScope;
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
    public ApiResponse<List<IncomeResponses.Source>> getSources() {
        return ApiResponse.success(service.getSources(AuthenticatedUser.id()));
    }

    @PostMapping("/sources")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<IncomeResponses.Source> createSource(@Valid @RequestBody IncomeSourceRequest request) {
        return ApiResponse.success(service.createSource(AuthenticatedUser.id(), request));
    }

    @GetMapping("/occurrences")
    public ApiResponse<List<IncomeResponses.Occurrence>> getOccurrences(
            @RequestParam LocalDate from,
            @RequestParam LocalDate to
    ) {
        return ApiResponse.success(service.getOccurrences(AuthenticatedUser.id(), from, to));
    }

    @PutMapping("/occurrences/{id}")
    public ApiResponse<IncomeResponses.Occurrence> updateOccurrence(
            @PathVariable Long id,
            @RequestParam(defaultValue = "THIS") IncomeSeriesScope scope,
            @Valid @RequestBody IncomeOccurrenceUpdateRequest request
    ) {
        return ApiResponse.success(service.updateOccurrence(AuthenticatedUser.id(), id, request, scope));
    }

    @DeleteMapping("/occurrences/{id}")
    public ApiResponse<Void> deleteOccurrence(
            @PathVariable Long id,
            @RequestParam(defaultValue = "THIS") IncomeSeriesScope scope
    ) {
        service.deleteOccurrence(AuthenticatedUser.id(), id, scope);
        return ApiResponse.success(null);
    }

    @PatchMapping("/occurrences/{id}/received")
    public ApiResponse<IncomeResponses.Occurrence> markReceived(
            @PathVariable Long id,
            @RequestParam Long accountId
    ) {
        return ApiResponse.success(service.markReceived(AuthenticatedUser.id(), id, accountId));
    }

    @PatchMapping("/occurrences/{id}/pending")
    public ApiResponse<IncomeResponses.Occurrence> markPending(@PathVariable Long id) {
        return ApiResponse.success(service.markPending(AuthenticatedUser.id(), id));
    }
}
