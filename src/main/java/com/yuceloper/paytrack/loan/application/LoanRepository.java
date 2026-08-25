package com.yuceloper.paytrack.loan.application;

import com.yuceloper.paytrack.loan.domain.Loan;

import java.util.List;
import java.util.Optional;

public interface LoanRepository {
    Loan save(Loan loan);
    Optional<Loan> findById(Long id);
    List<Loan> findAllByUserId(Long userId);
    void deleteById(Long id);
}
