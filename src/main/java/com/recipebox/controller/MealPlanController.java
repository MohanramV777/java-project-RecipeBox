package com.recipebox.controller;

import com.recipebox.model.MealPlan;
import com.recipebox.service.MealPlanService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/meal-plans")
@CrossOrigin
public class MealPlanController {
    private final MealPlanService service;

    public MealPlanController(MealPlanService service) {
        this.service = service;
    }

    @GetMapping
    public List<MealPlan> week(@RequestParam Long userId,
                               @RequestParam LocalDate date) {
        return service.week(userId, date);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MealPlan create(@Valid @RequestBody MealPlan plan,
                           @RequestParam Long userId) {
        return service.create(plan, userId);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, @RequestParam Long userId) {
        service.delete(id, userId);
    }

    @GetMapping("/shopping-list")
    public List<Map<String, Object>> shoppingList(@RequestParam Long userId,
                                                   @RequestParam LocalDate date) {
        return service.shoppingList(userId, date);
    }
}
