package com.yuceloper.paytrack.bill.application;

import com.yuceloper.paytrack.bill.domain.Bill;

import java.util.List;
import java.util.Optional;

public interface BillRepository {
    Bill save(Bill bill);
    Optional<Bill> findById(Long id);
    List<Bill> findAllByUserId(Long userId);
    void deleteById(Long id);
}
