package com.recipebox.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

@Entity
@Table(name = "recipe_ingredients")
public class RecipeIngredient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "recipe_id", nullable = false)
    private Recipe recipe;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ingredient_id", nullable = false)
    private Ingredient ingredient;

    @Positive
    @Column(nullable = false)
    private double quantity;

    @NotBlank
    @Column(nullable = false)
    private String unit;

    public RecipeIngredient() {}

    public RecipeIngredient(Ingredient ingredient, double quantity, String unit) {
        this.ingredient = ingredient;
        this.quantity = quantity;
        this.unit = unit;
    }

    public Long getId() { return id; }
    public Recipe getRecipe() { return recipe; }
    public Ingredient getIngredient() { return ingredient; }
    public double getQuantity() { return quantity; }
    public String getUnit() { return unit; }

    public void setId(Long id) { this.id = id; }
    public void setRecipe(Recipe recipe) { this.recipe = recipe; }
    public void setIngredient(Ingredient ingredient) { this.ingredient = ingredient; }
    public void setQuantity(double quantity) { this.quantity = quantity; }
    public void setUnit(String unit) { this.unit = unit; }
}
