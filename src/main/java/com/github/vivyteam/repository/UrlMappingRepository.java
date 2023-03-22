package com.github.vivyteam.repository;

import com.github.vivyteam.domain.UrlMapping;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface UrlMappingRepository extends ReactiveMongoRepository<UrlMapping, String> {
    Mono<UrlMapping> findByOriginalUrl(String originalUrl);
}
