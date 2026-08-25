package com.yuceloper.paytrack.bill.application;

import com.yuceloper.paytrack.bill.api.dto.BillResponse;
import com.yuceloper.paytrack.bill.api.dto.CreateBillRequest;
import com.yuceloper.paytrack.bill.domain.Bill;
import com.yuceloper.paytrack.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BillService {

    private final BillRepository repository;

    public List<BillResponse> getByUserId(Long userId) {
        return repository.findAllByUserId(userId).stream().map(this::toResponse).toList();
    }

    public BillResponse getById(Long id) {
        return toResponse(getEntity(id));
    }

    @Transactional
    public BillResponse create(CreateBillRequest request) {
        Bill bill = Bill.builder()
                .userId(request.userId())
                .name(request.name())
                .provider(request.provider())
                .category(request.category().toUpperCase())
                .expectedAmount(request.expectedAmount())
                .dueDay(request.dueDay())
                .nextDueDate(request.nextDueDate())
                .active(true)
                .build();
        return toResponse(repository.save(bill));
    }

    @Transactional
    public void delete(Long id) {
        getEntity(id);
        repository.deleteById(id);
    }

    private Bill getEntity(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bill", id));
    }

    private BillResponse toResponse(Bill bill) {
        return new BillResponse(
                bill.getId(), bill.getUserId(), bill.getName(), bill.getProvider(), bill.getCategory(),
                bill.getExpectedAmount(), bill.getDueDay(), bill.getNextDueDate(), bill.isActive()
        );
    }
}
