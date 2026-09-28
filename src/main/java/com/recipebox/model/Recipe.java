package com.recipebox.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "recipes")
public class Recipe {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String title;

    @NotBlank
    @Column(nullable = false)
    private String cuisine;

    @NotBlank
    @Column(nullable = false, length = 4000)
    private String steps;

    @Min(0)
    private int prepMinutes;

    @PositiveOrZero
    private double servings;

    private boolean favorite;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<RecipeIngredient> ingredients = new ArrayList<>();

    public Recipe() {}

    public void addIngredient(RecipeIngredient item) {
        item.setRecipe(this);
        ingredients.add(item);
    }

    public void removeIngredients() {
        ingredients.clear();
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getCuisine() { return cuisine; }
    public String getSteps() { return steps; }
    public int getPrepMinutes() { return prepMinutes; }
    public double getServings() { return servings; }
    public boolean isFavorite() { return favorite; }
    public AppUser getUser() { return user; }
    public List<RecipeIngredient> getIngredients() { return ingredients; }

    public void setId(Long id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setCuisine(String cuisine) { this.cuisine = cuisine; }
    public void setSteps(String steps) { this.steps = steps; }
    public void setPrepMinutes(int prepMinutes) { this.prepMinutes = prepMinutes; }
    public void setServings(double servings) { this.servings = servings; }
    public void setFavorite(boolean favorite) { this.favorite = favorite; }
    public void setUser(AppUser user) { this.user = user; }
    public void setIngredients(List<RecipeIngredient> ingredients) { this.ingredients = ingredients; }
}
