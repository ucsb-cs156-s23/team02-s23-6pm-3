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
@Slf4j
public class MoviesController extends ApiController {

    @Autowired
    MoviesRepository moviesRepository;

    @ApiOperation(value = "List all movies")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/all")
    public Iterable<Movies> allMovies() {
        Iterable<Movies> movie = moviesRepository.findAll();
        return movie;
    }

    @ApiOperation(value = "Get a single movie")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("")
    public Movies getById(
            @ApiParam("id") @RequestParam Long id) {
        Movies movie = moviesRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Movies.class, id));

        return movie;
    }

    @ApiOperation(value = "Create a new movie")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/post")
    public Movies postMovies(
            @ApiParam("title") @RequestParam String title,
            @ApiParam("director") @RequestParam String director,
            @ApiParam("year") @RequestParam int year,
            @ApiParam("starringActors") @RequestParam String starringActors
            )
            throws JsonProcessingException {

        Movies movie = new Movies();
        movie.setTitle(title);
        movie.setDirector(director);
        movie.setYear(year);
        movie.setStarringActors(straringActors);

        Movies savedMovie = moviesRepository.save(movie);

        return savedMovie;
    }

    @ApiOperation(value = "Delete a Movie")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("")
    public Object deleteMovie(
            @ApiParam("id") @RequestParam Long id) {
        Movies movie = moviesRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Movies.class, id));

        moviesRepository.delete(movie);
        return genericMessage("Movies with id %s deleted".formatted(id));
    }

    @ApiOperation(value = "Update a single movie")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("")
    public Movies updateMovie(
            @ApiParam("id") @RequestParam Long id,
            @RequestBody @Valid Movies incoming) {

        Movies movie = moviesRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Movies.class, id));
        movie.setTitle(incoming.getTitle());
        movie.setDirector(incoming.getDirector());
        movie.setYear(incoming.getYear());
        movie.setStarringActors(incoming.getStarringActors());
        moviesRepository.save(movie);
        return movie;
    }
}
