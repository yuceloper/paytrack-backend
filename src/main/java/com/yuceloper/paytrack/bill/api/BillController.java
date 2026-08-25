package com.yuceloper.paytrack.bill.api;

import com.yuceloper.paytrack.bill.api.dto.BillResponse;
import com.yuceloper.paytrack.bill.api.dto.CreateBillRequest;
import com.yuceloper.paytrack.bill.application.BillService;
import com.yuceloper.paytrack.shared.api.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/bills")
@RequiredArgsConstructor
public class BillController {

    private final BillService service;

    @GetMapping
    public ApiResponse<List<BillResponse>> getAll(@RequestParam Long userId) {
        return ApiResponse.success(service.getByUserId(userId));
    }

    @GetMapping("/{id}")
    public ApiResponse<BillResponse> getById(@PathVariable Long id) {
        return ApiResponse.success(service.getById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<BillResponse> create(@Valid @RequestBody CreateBillRequest request) {
        return ApiResponse.success(service.create(request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
