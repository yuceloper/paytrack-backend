package com.yuceloper.paytrack.bill.infrastructure;

import com.yuceloper.paytrack.bill.application.BillRepository;
import com.yuceloper.paytrack.bill.domain.Bill;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

interface BillJpaRepository extends JpaRepository<Bill, Long> {
    List<Bill> findAllByUserIdOrderByIdDesc(Long userId);
}

@Component
@RequiredArgsConstructor
public class SpringDataBillRepository implements BillRepository {
    private final BillJpaRepository repository;

    @Override public Bill save(Bill bill) { return repository.save(bill); }
    @Override public Optional<Bill> findById(Long id) { return repository.findById(id); }
    @Override public List<Bill> findAllByUserId(Long userId) { return repository.findAllByUserIdOrderByIdDesc(userId); }
    @Override public void deleteById(Long id) { repository.deleteById(id); }
}
