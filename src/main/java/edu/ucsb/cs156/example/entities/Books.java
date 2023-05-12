package edu.ucsb.cs156.example.entities;

import javax.persistence.Entity;
import javax.persistence.Id;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity(name = "books")
public class Books {
  @Id
  //@GeneratedValue(strategy = GenerationType.IDENTITY)
  private String title;
  private String author;
  private String setting;
  private String genre;
}
