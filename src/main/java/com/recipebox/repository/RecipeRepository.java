package com.recipebox.repository;

import com.recipebox.model.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecipeRepository extends JpaRepository<Recipe, Long> {
    List<Recipe> findByUserIdOrderByIdDesc(Long userId);
    List<Recipe> findByUserIdAndFavoriteTrueOrderByIdDesc(Long userId);
    List<Recipe> findByUserIdAndCuisineIgnoreCaseOrderByIdDesc(Long userId, String cuisine);
    List<Recipe> findByUserIdAndTitleContainingIgnoreCaseOrUserIdAndCuisineContainingIgnoreCase(
            Long userId1, String title, Long userId2, String cuisine);
}
