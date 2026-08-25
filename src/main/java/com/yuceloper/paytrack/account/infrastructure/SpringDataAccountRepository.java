package com.yuceloper.paytrack.account.infrastructure;

import com.yuceloper.paytrack.account.domain.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

interface SpringDataAccountRepository extends JpaRepository<Account, Long> {
    List<Account> findAllByUserIdOrderByNameAsc(Long userId);
}
