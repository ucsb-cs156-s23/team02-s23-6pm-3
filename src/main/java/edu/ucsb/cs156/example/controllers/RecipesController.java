package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.entities.Recipes;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.RecipesRepository;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;


@Api(description = "Recipes")
@RequestMapping("/api/recipes")
@RestController
@Slf4j
public class RecipesController extends ApiController {

    @Autowired
    RecipesRepository recipesRepository;

    @ApiOperation(value = "List all recipes")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/all")
    public Iterable<Recipes> allRecipes() {
        Iterable<Recipes> recipe = recipesRepository.findAll();
        return recipe;
    }

    @ApiOperation(value = "Get a single recipe")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("")
    public Recipes getById(
            @ApiParam("id") @RequestParam Long id) {
        Recipe recipe = recipesRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Recipes.class, id));

        return recipes;
    }

    @ApiOperation(value = "Create a new recipe")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/post")
    public Recipes postRecipes(
            @ApiParam("name") @RequestParam String name,
            @ApiParam("mealtype") @RequestParam String mealtype,
            @ApiParam("preptime") @RequestParam String preptime,
            @ApiParam("cooktime") @RequestParam String cooktime,
            @ApiParam("totalcalories") @RequestParam String totalcalories
            )
            throws JsonProcessingException {

        Recipes recipe = new Recipes();
        rest.setName(name);
        rest.setMealType(mealtype);
        rest.setPrepTime(preptime);
        rest.setCookTime(cooktime);
        rest.setTotalCalories(totalcalories);

        Recipes savedRecipe = recipesRepository.save(recipe);

        return savedRecipe;
    }

    @ApiOperation(value = "Delete a Recipe")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("")
    public Object deleteRecipe(
            @ApiParam("code") @RequestParam Long id) {
        Recipes recipe = recipesRepository.findById(recipe)
                .orElseThrow(() -> new EntityNotFoundException(Recipes.class, id));

        recipesRepository.delete(rest);
        return genericMessage("Recipes with id %s deleted".formatted(code));
    }

    @ApiOperation(value = "Update a single recipe")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("")
    public Recipes updateRecipe(
            @ApiParam("id") @RequestParam Long id,
            @RequestBody @Valid Recipes incoming) {

        Recipes recipe = recipesRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Recipes.class, code));


        recipe.setName(incoming.getName());  
        recipe.setCuisine(incoming.getCuisine());
        recipe.setLocation(incoming.getLocation());


        recipesRepository.save(rest);

        return rest;
    }
}
