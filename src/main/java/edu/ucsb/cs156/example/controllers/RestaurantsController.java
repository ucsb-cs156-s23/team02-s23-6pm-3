package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.entities.Restaurants;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.RestaurantsRepository;
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


@Api(description = "Restaurants")
@RequestMapping("/api/restaurants")
@RestController
@Slf4j
public class RestaurantsController extends ApiController {

    @Autowired
    RestaurantsRepository restaurantsRepository;

    @ApiOperation(value = "List all restaurants")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/all")
    public Iterable<Restaurants> allrests() {
        Iterable<Restaurants> rests = restaurantsRepository.findAll();
        return rests;
    }

    @ApiOperation(value = "Get a single restaurant")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("")
    public Restaurants getById(
            @ApiParam("code") @RequestParam String code) {
        Restaurants rests = restaurantsRepository.findById(code)
                .orElseThrow(() -> new EntityNotFoundException(Restaurants.class, code));

        return rests;
    }

    @ApiOperation(value = "Create a new restaurant")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/post")
    public Restaurants postRests(
        @ApiParam("code") @RequestParam String code,
        @ApiParam("name") @RequestParam String name,
        @ApiParam("cuisine") @RequestParam String cuisine,
        @ApiParam("location") @RequestParam String location
        )
        {

        Restaurants rest = new Restaurants();
        rest.setCode(code);
        rest.setName(name);
        rest.setCuisine(cuisine);
        rest.setLocation(location);

        Restaurants savedRest = restaurantsRepository.save(rest);

        return savedRest;
    }

    @ApiOperation(value = "Delete a Restaurants")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("")
    public Object deleteRest(
            @ApiParam("code") @RequestParam String code) {
        Restaurants rest = restaurantsRepository.findById(code)
                .orElseThrow(() -> new EntityNotFoundException(Restaurants.class, code));

        restaurantsRepository.delete(rest);
        return genericMessage("Restaurants with id %s deleted".formatted(code));
    }

    @ApiOperation(value = "Update a single restaurant")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("")
    public Restaurants updateRest(
            @ApiParam("code") @RequestParam String code,
            @RequestBody @Valid Restaurants incoming) {

        Restaurants rest = restaurantsRepository.findById(code)
                .orElseThrow(() -> new EntityNotFoundException(Restaurants.class, code));


        rest.setName(incoming.getName());  
        rest.setCuisine(incoming.getCuisine());
        rest.setLocation(incoming.getLocation());


        restaurantsRepository.save(rest);

        return rest;
    }
}
