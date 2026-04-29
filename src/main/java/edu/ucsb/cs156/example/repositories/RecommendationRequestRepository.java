package edu.ucsb.cs156.example.repositories;

import edu.ucsb.cs156.example.entities.RecommendationRequest;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;

/** The RecommendationRequestRepository is a repository for RecommendationRequest entities. */
@Repository
@RepositoryRestResource(exported = false)
public interface RecommendationRequestRepository
    extends CrudRepository<RecommendationRequest, Long> {}
