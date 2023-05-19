package edu.ucsb.cs156.example.repositories;

import edu.ucsb.cs156.example.entities.Movies;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface MoviesRepository extends CrudRepository<Movies, Long> {
  Iterable<Movies> findAllByTitle(String title);
}