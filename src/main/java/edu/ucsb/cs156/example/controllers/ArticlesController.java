package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.entities.Articles;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.ArticlesRepository;
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

@Api(description = "Articles")
@RequestMapping("/api/articles")
@RestController
public class ArticlesController extends ApiController {

    @Autowired
    ArticlesRepository articlesRepository;

    @ApiOperation(value = "List all articless")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/all")
    public Iterable<Articles> allArticles() {
        Iterable<Articles> dates = articlesRepository.findAll();
        return dates;
    }

    @ApiOperation(value = "Get a single articles")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("")
    public Articles getById(
            @ApiParam("id") @RequestParam Long id) {
        Articles articles = articlesRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Articles.class, id));

        return articles;
    }

    @ApiOperation(value = "Create a new articles")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/post")
    public Articles postArticles(
            @ApiParam("title") @RequestParam String title,
            @ApiParam("image") @RequestParam String image,
            @ApiParam("content") @RequestParam String content
            )
            throws JsonProcessingException {

        Articles articles = new Articles();
        articles.setTitle(title);
        articles.setImage(image);
        articles.setContent(content);

        Articles savedArticles = articlesRepository.save(articles);

        return savedArticles;
    }

    @ApiOperation(value = "Delete a Articles")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("")
    public Object deleteArticles(
            @ApiParam("id") @RequestParam Long id) {
        Articles articles = articlesRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Articles.class, id));

        articlesRepository.delete(articles);
        return genericMessage("Articles with id %s deleted".formatted(id));
    }

    @ApiOperation(value = "Update a single articles")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("")
    public Articles updateArticles(
            @ApiParam("id") @RequestParam Long id,
            @RequestBody @Valid Articles incoming) {

        Articles articles = articlesRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Articles.class, id));

        articles.setTitle(incoming.getTitle());
        articles.setImage(incoming.getImage());
        articles.setContent(incoming.getContent());

        articlesRepository.save(articles);

        return articles;
    }
}
