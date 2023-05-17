package edu.ucsb.cs156.example.repositories;

import edu.ucsb.cs156.example.entities.Recipes;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface RecipesRepository extends CrudRepository<Recipes, Long> {
  Iterable<Recipes> findAllByName(String name);
}