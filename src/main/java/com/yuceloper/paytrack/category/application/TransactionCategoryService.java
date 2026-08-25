package com.yuceloper.paytrack.category.application;

import com.yuceloper.paytrack.category.api.dto.TransactionCategoryDtos;
import com.yuceloper.paytrack.category.domain.TransactionCategory;
import com.yuceloper.paytrack.category.domain.TransactionCategoryType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TransactionCategoryService {

    private final TransactionCategoryRepository repository;

    @Transactional
    public List<TransactionCategoryDtos.Response> getAll(Long userId) {
        ensureDefaults(userId);
        return repository.findAllByUserId(userId).stream()
                .filter(TransactionCategory::isActive)
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public TransactionCategoryDtos.Response create(TransactionCategoryDtos.CreateRequest request) {
        repository.findByUserIdAndName(request.userId(), request.name().trim())
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("Category name already exists");
                });

        TransactionCategory saved = repository.save(TransactionCategory.builder()
                .userId(request.userId())
                .name(request.name().trim())
                .type(request.type())
                .iconKey(normalizeIcon(request.iconKey()))
                .builtIn(false)
                .active(true)
                .build());
        return toResponse(saved);
    }

    private void ensureDefaults(Long userId) {
        createDefault(userId, "Market", TransactionCategoryType.EXPENSE, "shopping_cart");
        createDefault(userId, "Yemek", TransactionCategoryType.EXPENSE, "restaurant");
        createDefault(userId, "Ulaşım", TransactionCategoryType.EXPENSE, "directions_car");
        createDefault(userId, "Yakıt", TransactionCategoryType.EXPENSE, "local_gas_station");
        createDefault(userId, "Fatura", TransactionCategoryType.EXPENSE, "receipt_long");
        createDefault(userId, "Sağlık", TransactionCategoryType.EXPENSE, "health_and_safety");
        createDefault(userId, "Eğlence", TransactionCategoryType.EXPENSE, "celebration");
        createDefault(userId, "Alışveriş", TransactionCategoryType.EXPENSE, "shopping_bag");
        createDefault(userId, "Maaş", TransactionCategoryType.INCOME, "payments");
        createDefault(userId, "Kira", TransactionCategoryType.INCOME, "home");
        createDefault(userId, "Freelance", TransactionCategoryType.INCOME, "work");
        createDefault(userId, "Diğer", TransactionCategoryType.BOTH, "category");
    }

    private void createDefault(Long userId, String name, TransactionCategoryType type, String iconKey) {
        if (repository.findByUserIdAndName(userId, name).isPresent()) return;
        repository.save(TransactionCategory.builder()
                .userId(userId)
                .name(name)
                .type(type)
                .iconKey(iconKey)
                .builtIn(true)
                .active(true)
                .build());
    }

    private TransactionCategoryDtos.Response toResponse(TransactionCategory category) {
        return new TransactionCategoryDtos.Response(
                category.getId(), category.getUserId(), category.getName(), category.getType(),
                category.getIconKey(), category.isBuiltIn(), category.isActive()
        );
    }

    private String normalizeIcon(String iconKey) {
        if (iconKey == null || iconKey.isBlank()) return "category";
        return iconKey.trim();
    }
}
