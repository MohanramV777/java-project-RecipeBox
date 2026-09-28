package com.recipebox.repository;

import com.recipebox.model.MealPlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MealPlanRepository extends JpaRepository<MealPlan, Long> {
    List<MealPlan> findByUserIdAndPlanDateBetweenOrderByPlanDateAsc(Long userId, LocalDate start, LocalDate end);
    Optional<MealPlan> findByUserIdAndPlanDate(Long userId, LocalDate date);
}
