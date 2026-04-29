package edu.ucsb.cs156.example.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * This is a JPA entity that represents a MenuItemReview, i.e. a review of a certain menu item by a
 * certain student.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity(name = "MenuItemReview")
public class MenuItemReview {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  private long itemId;
  private String reviewerEmail;
  private int stars;
  private LocalDateTime dateReviewed;
  private String comments;
}
