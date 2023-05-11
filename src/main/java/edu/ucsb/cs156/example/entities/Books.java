package edu.ucsb.cs156.example.entities;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.AccessLevel;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;

@Data
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Entity(name = "users")
public class Books {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;
  private String author;
  private String setting;
  //private String pictureUrl;
  private String mainChar;
  private String villain;
  private String genre;
  //private boolean children;
  //private String popularity;
 // private String hostedDomain;
  private boolean fiction;
}
