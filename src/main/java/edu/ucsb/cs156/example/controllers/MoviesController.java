package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.entities.Movies;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.MoviesRepository;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;

import com.fasterxml.jackson.core.JsonProcessingException;

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

@Api(description = "Movies")
@RequestMapping("/api/movies")
@RestController
public class MoviesController extends ApiController {

    @Autowired
    MoviesRepository moviesRepository;

    @ApiOperation(value = "List all moviess")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/all")
    public Iterable<Movies> allMovies() {
        Iterable<Movies> dates = moviesRepository.findAll();
        return dates;
    }

    @ApiOperation(value = "Get a single movies")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("")
    public Movies getById(
            @ApiParam("id") @RequestParam Long id) {
        Movies movies = moviesRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Movies.class, id));

        return movies;
    }

    @ApiOperation(value = "Create a new movies")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/post")
    public Movies postMovies(
            @ApiParam("title") @RequestParam String title,
            @ApiParam("starring") @RequestParam String starring,
            @ApiParam("director") @RequestParam String director
            )
            throws JsonProcessingException {

        Movies movies = new Movies();
        movies.setTitle(title);
        movies.setStarring(starring);
        movies.setDirector(director);

        Movies savedMovies = moviesRepository.save(movies);

        return savedMovies;
    }

    @ApiOperation(value = "Delete a Movies")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("")
    public Object deleteMovies(
            @ApiParam("id") @RequestParam Long id) {
        Movies movies = moviesRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Movies.class, id));

        moviesRepository.delete(movies);
        return genericMessage("Movies with id %s deleted".formatted(id));
    }

    @ApiOperation(value = "Update a single movies")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("")
    public Movies updateMovies(
            @ApiParam("id") @RequestParam Long id,
            @RequestBody @Valid Movies incoming) {

        Movies movies = moviesRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Movies.class, id));

        movies.setTitle(incoming.getTitle());
        movies.setStarring(incoming.getStarring());
        movies.setDirector(incoming.getDirector());

        moviesRepository.save(movies);

        return movies;
    }
}
