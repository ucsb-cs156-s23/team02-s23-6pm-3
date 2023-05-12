package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.entities.Books;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.BooksRepository;
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


@Api(description = "Books")
@RequestMapping("/api/books")
@RestController
@Slf4j
public class BooksController extends ApiController {

    @Autowired
    BooksRepository booksRepository;

    @ApiOperation(value = "List all books")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/all")
    public Iterable<Books> allbooks() {
        Iterable<Books> books = booksRepository.findAll();
        return books;
    }

    @ApiOperation(value = "Get a single book")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("")
    public Books getById(
            @ApiParam("title") @RequestParam String title) {
        Books book = booksRepository.findById(title)
                .orElseThrow(() -> new EntityNotFoundException(Books.class, title));

        return book;
    }

    @ApiOperation(value = "Create a new book")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/post")
    public Books postBooks(
        @ApiParam("title") @RequestParam String title,
        @ApiParam("name") @RequestParam String name,
        @ApiParam("setting") @RequestParam String setting,
        @ApiParam("genre") @RequestParam String genre
        )
        {

        Books book = new Books();
        book.setTitle(title);
        book.setName(name);
        book.setSetting(setting);
        book.setGenre(genre);

        Books savedBook = booksRepository.save(book);

        return savedBook;
    }

    @ApiOperation(value = "Delete a Book")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("")
    public Object deleteBooks(
            @ApiParam("title") @RequestParam String title) {
        Books book = booksRepository.findById(title)
                .orElseThrow(() -> new EntityNotFoundException(Books.class, title));

        booksRepository.delete(book);
        return genericMessage("Books with id %s deleted".formatted(title));
    }

    @ApiOperation(value = "Update a single book")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("")
    public Books updateBooks(
            @ApiParam("title") @RequestParam String title,
            @RequestBody @Valid Books incoming) {

        Books book = booksRepository.findById(title)
                .orElseThrow(() -> new EntityNotFoundException(Books.class, title));


        book.setGenre(incoming.getGenre());  
        book.setName(incoming.getName());
        book.setSetting(incoming.getSetting());


        booksRepository.save(book);

        return book;
    }
}
