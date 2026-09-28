package com.recipebox.service;

import com.recipebox.exception.NotFoundException;
import com.recipebox.model.*;
import com.recipebox.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RecipeService {
    private final RecipeRepository recipeRepository;
    private final IngredientRepository ingredientRepository;
    private final AppUserRepository userRepository;

    public RecipeService(RecipeRepository recipeRepository,
                         IngredientRepository ingredientRepository,
                         AppUserRepository userRepository) {
        this.recipeRepository = recipeRepository;
        this.ingredientRepository = ingredientRepository;
        this.userRepository = userRepository;
    }

    public List<Recipe> list(Long userId, String search, String cuisine, boolean favorites) {
        if (!userRepository.existsById(userId)) throw new NotFoundException("User not found");
        if (favorites) return recipeRepository.findByUserIdAndFavoriteTrueOrderByIdDesc(userId);
        if (cuisine != null && !cuisine.isBlank())
            return recipeRepository.findByUserIdAndCuisineIgnoreCaseOrderByIdDesc(userId, cuisine);
        if (search != null && !search.isBlank())
            return recipeRepository.findByUserIdAndTitleContainingIgnoreCaseOrUserIdAndCuisineContainingIgnoreCase(
                    userId, search, userId, search);
        return recipeRepository.findByUserIdOrderByIdDesc(userId);
    }

    public Recipe get(Long id) {
        return recipeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Recipe not found"));
    }

    @Transactional
    public Recipe create(Recipe recipe, Long userId) {
        AppUser user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        recipe.setId(null);
        recipe.setUser(user);
        for (RecipeIngredient item : recipe.getIngredients()) {
            Ingredient ingredient = findOrCreateIngredient(item.getIngredient().getName());
            item.setIngredient(ingredient);
            item.setRecipe(recipe);
        }
        return recipeRepository.save(recipe);
    }

    @Transactional
    public Recipe update(Long id, Recipe incoming, Long userId) {
        Recipe existing = get(id);
        if (!existing.getUser().getId().equals(userId))
            throw new NotFoundException("Recipe not found");

        existing.setTitle(incoming.getTitle());
        existing.setCuisine(incoming.getCuisine());
        existing.setSteps(incoming.getSteps());
        existing.setPrepMinutes(incoming.getPrepMinutes());
        existing.setServings(incoming.getServings());
        existing.setFavorite(incoming.isFavorite());
        existing.removeIngredients();

        for (RecipeIngredient item : incoming.getIngredients()) {
            Ingredient ingredient = findOrCreateIngredient(item.getIngredient().getName());
            item.setIngredient(ingredient);
            item.setRecipe(existing);
            existing.getIngredients().add(item);
        }
        return recipeRepository.save(existing);
    }

    public void delete(Long id, Long userId) {
        Recipe recipe = get(id);
        if (!recipe.getUser().getId().equals(userId))
            throw new NotFoundException("Recipe not found");
        recipeRepository.delete(recipe);
    }

    public Recipe toggleFavorite(Long id, Long userId) {
        Recipe recipe = get(id);
        if (!recipe.getUser().getId().equals(userId))
            throw new NotFoundException("Recipe not found");
        recipe.setFavorite(!recipe.isFavorite());
        return recipeRepository.save(recipe);
    }

    private Ingredient findOrCreateIngredient(String name) {
        return ingredientRepository.findByNameIgnoreCase(name.trim())
                .orElseGet(() -> ingredientRepository.save(new Ingredient(name.trim())));
    }
}
