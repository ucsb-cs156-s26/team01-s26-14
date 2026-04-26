package edu.ucsb.cs156.example.repositories;

import edu.ucsb.cs156.example.entities.Article;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;

@Repository
@RepositoryRestResource(exported = false)
public interface ArticleRepository extends CrudRepository<Article, Long> {}
