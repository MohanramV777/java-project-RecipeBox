package com.recipebox.controller;

import com.recipebox.model.Recipe;
import com.recipebox.service.RecipeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recipes")
@CrossOrigin
public class RecipeController {
    private final RecipeService service;

    public RecipeController(RecipeService service) {
        this.service = service;
    }

    @GetMapping
    public List<Recipe> list(@RequestParam Long userId,
                             @RequestParam(required = false) String search,
                             @RequestParam(required = false) String cuisine,
                             @RequestParam(defaultValue = "false") boolean favorites) {
        return service.list(userId, search, cuisine, favorites);
    }

    @GetMapping("/{id}")
    public Recipe get(@PathVariable Long id) {
        return service.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Recipe create(@Valid @RequestBody Recipe recipe, @RequestParam Long userId) {
        return service.create(recipe, userId);
    }

    @PutMapping("/{id}")
    public Recipe update(@PathVariable Long id, @Valid @RequestBody Recipe recipe,
                         @RequestParam Long userId) {
        return service.update(id, recipe, userId);
    }

    @PatchMapping("/{id}/favorite")
    public Recipe favorite(@PathVariable Long id, @RequestParam Long userId) {
        return service.toggleFavorite(id, userId);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, @RequestParam Long userId) {
        service.delete(id, userId);
    }
}
