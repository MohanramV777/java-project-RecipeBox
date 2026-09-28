package com.recipebox.service;

import com.recipebox.exception.BusinessRuleException;
import com.recipebox.exception.NotFoundException;
import com.recipebox.model.*;
import com.recipebox.repository.AppUserRepository;
import com.recipebox.repository.MealPlanRepository;
import com.recipebox.repository.RecipeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.DayOfWeek;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MealPlanService {
    private final MealPlanRepository mealPlanRepository;
    private final RecipeRepository recipeRepository;
    private final AppUserRepository userRepository;

    public MealPlanService(MealPlanRepository mealPlanRepository,
                           RecipeRepository recipeRepository,
                           AppUserRepository userRepository) {
        this.mealPlanRepository = mealPlanRepository;
        this.recipeRepository = recipeRepository;
        this.userRepository = userRepository;
    }

    public List<MealPlan> week(Long userId, LocalDate anyDate) {
        LocalDate monday = anyDate.with(DayOfWeek.MONDAY);
        return mealPlanRepository.findByUserIdAndPlanDateBetweenOrderByPlanDateAsc(
                userId, monday, monday.plusDays(6));
    }

    @Transactional
    public MealPlan create(MealPlan plan, Long userId) {
        AppUser user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Recipe recipe = recipeRepository.findById(plan.getRecipe().getId())
                .orElseThrow(() -> new NotFoundException("Recipe not found"));

        if (!recipe.getUser().getId().equals(userId))
            throw new BusinessRuleException("You can only plan one of your own recipes.");

        if (mealPlanRepository.findByUserIdAndPlanDate(userId, plan.getPlanDate()).isPresent())
            throw new BusinessRuleException("A meal is already planned for " + plan.getPlanDate());

        plan.setId(null);
        plan.setUser(user);
        plan.setRecipe(recipe);
        return mealPlanRepository.save(plan);
    }

    public void delete(Long id, Long userId) {
        MealPlan plan = mealPlanRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Meal plan entry not found"));
        if (!plan.getUser().getId().equals(userId))
            throw new NotFoundException("Meal plan entry not found");
        mealPlanRepository.delete(plan);
    }

    public List<Map<String, Object>> shoppingList(Long userId, LocalDate date) {
        List<MealPlan> plans = week(userId, date);
        Map<String, ShoppingItem> totals = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

        for (MealPlan plan : plans) {
            for (RecipeIngredient ri : plan.getRecipe().getIngredients()) {
                String key = ri.getIngredient().getName().trim().toLowerCase();
                ShoppingItem item = totals.get(key);
                if (item == null) {
                    item = new ShoppingItem(ri.getIngredient().getName(), ri.getUnit(), 0);
                    totals.put(key, item);
                }
                if (!item.unit().equalsIgnoreCase(ri.getUnit())) {
                    throw new BusinessRuleException(
                            "Cannot combine " + ri.getIngredient().getName() +
                            " because its units differ across planned recipes.");
                }
                item.add(ri.getQuantity());
            }
        }

        return totals.values().stream().map(item -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("ingredient", item.name());
            row.put("quantity", item.quantity());
            row.put("unit", item.unit());
            return row;
        }).collect(Collectors.toList());
    }

    private static class ShoppingItem {
        private final String name;
        private final String unit;
        private double quantity;

        ShoppingItem(String name, String unit, double quantity) {
            this.name = name;
            this.unit = unit;
            this.quantity = quantity;
        }
        void add(double value) { quantity += value; }
        String name() { return name; }
        String unit() { return unit; }
        double quantity() { return quantity; }
    }
}
