package com.recipebox.controller;

import com.recipebox.exception.BusinessRuleException;
import com.recipebox.model.AppUser;
import com.recipebox.repository.AppUserRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@CrossOrigin
public class UserController {
    private final AppUserRepository repository;

    public UserController(AppUserRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<AppUser> all() {
        return repository.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AppUser create(@Valid @RequestBody AppUser user) {
        if (repository.findByEmailIgnoreCase(user.getEmail()).isPresent())
            throw new BusinessRuleException("Email already exists.");
        return repository.save(user);
    }
}
