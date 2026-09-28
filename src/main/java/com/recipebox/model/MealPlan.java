package com.recipebox.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

@Entity
@Table(name = "meal_plans",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "plan_date"}))
public class MealPlan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "plan_date", nullable = false)
    private LocalDate planDate;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MealType mealType;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "recipe_id", nullable = false)
    private Recipe recipe;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    public MealPlan() {}

    public Long getId() { return id; }
    public LocalDate getPlanDate() { return planDate; }
    public MealType getMealType() { return mealType; }
    public Recipe getRecipe() { return recipe; }
    public AppUser getUser() { return user; }

    public void setId(Long id) { this.id = id; }
    public void setPlanDate(LocalDate planDate) { this.planDate = planDate; }
    public void setMealType(MealType mealType) { this.mealType = mealType; }
    public void setRecipe(Recipe recipe) { this.recipe = recipe; }
    public void setUser(AppUser user) { this.user = user; }
}
