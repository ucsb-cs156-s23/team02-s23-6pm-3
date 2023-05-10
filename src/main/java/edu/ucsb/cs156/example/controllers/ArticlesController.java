package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.entities.Article;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.ArticleRepository;
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
@Slf4j
public class ArticlesController extends ApiController {

    @Autowired
    ArticleRepository articleRepository;

    @ApiOperation(value = "List all articles")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/all")
    public Iterable<Article> allArticles() {
        Iterable<Article>articles = articleRepository.findAll();
        return articles;
    }

    @ApiOperation(value = "Get a single article")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("")
    public Article getById(
            @ApiParam("id") @RequestParam Long id) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Article.class, id));

        return article;
    }

    @ApiOperation(value = "Create a new article")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/post")
    public Article postArticle(
            @ApiParam("title") @RequestParam String title,
            @ApiParam("image") @RequestParam String image,
            @ApiParam("content") @RequestParam String content)
            throws JsonProcessingException {

        log.info("content={}", content);

        Article article = new Article();
        article.setTitle(title);
        article.setImage(image);
        article.setContent(content);

        Article savedArticle = articleRepository.save(article);

        return savedArticle;
    }

    @ApiOperation(value = "Delete a Article")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("")
    public Object deleteArticle(
            @ApiParam("id") @RequestParam Long id) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Article.class, id));

        articleRepository.delete(article);
        return genericMessage("Article with id %s deleted".formatted(id));
    }

    @ApiOperation(value = "Update a single article")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("")
    public Article updateArticle(
            @ApiParam("id") @RequestParam Long id,
            @RequestBody @Valid Article incoming) {

        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Article.class, id));

        article.setTitle(incoming.getTitle());
        article.setImage(incoming.getImage());
        article.setContent(incoming.getContent());

        articleRepository.save(article);

        return article;
    }
}
