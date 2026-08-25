package com.yuceloper.paytrack.loan.infrastructure;

import com.yuceloper.paytrack.loan.application.LoanRepository;
import com.yuceloper.paytrack.loan.domain.Loan;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

interface LoanJpaRepository extends JpaRepository<Loan, Long> {
    List<Loan> findAllByUserIdOrderByIdDesc(Long userId);
}

@Component
@RequiredArgsConstructor
public class SpringDataLoanRepository implements LoanRepository {
    private final LoanJpaRepository repository;

    @Override public Loan save(Loan loan) { return repository.save(loan); }
    @Override public Optional<Loan> findById(Long id) { return repository.findById(id); }
    @Override public List<Loan> findAllByUserId(Long userId) { return repository.findAllByUserIdOrderByIdDesc(userId); }
    @Override public void deleteById(Long id) { repository.deleteById(id); }
}
