package com.github.vivyteam.service;

import com.github.vivyteam.domain.UrlMapping;
import com.github.vivyteam.repository.UrlMappingRepository;
import com.github.vivyteam.service.contract.IUrlShorteningService;
import com.github.vivyteam.service.contract.IdGeneratorInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.data.redis.core.ReactiveValueOperations;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
public class UrlShorteningService implements IUrlShorteningService {

    private UrlMappingRepository urlMappingRepository;
    private IdGeneratorInterface idGeneratorInterface;
    private static final Duration CACHE_EXPIRATION = Duration.ofHours(6);
    @Autowired
    private ReactiveRedisOperations<String, String> reactiveRedisOperations;

    @Value("${BASE_URL}")
    private String baseUrl;

    public UrlShorteningService(UrlMappingRepository urlMappingRepository, IdGeneratorInterface idGeneratorInterface) {
        this.urlMappingRepository = urlMappingRepository;
        this.idGeneratorInterface = idGeneratorInterface;
    }

    @Override
    public Mono<String> shortenUrl(String originalUrl) {
        return urlMappingRepository.findByOriginalUrl(originalUrl)
                .switchIfEmpty(Mono.defer(() -> {
                    String shortUrlId = idGeneratorInterface.generateId();
                    UrlMapping urlMapping = new UrlMapping(shortUrlId, originalUrl);
                    return urlMappingRepository.save(urlMapping)
                            .doOnSuccess(savedMapping -> cacheValueWithExpiration(savedMapping.getId(), savedMapping.getOriginalUrl()));
                }))
                .map(urlMapping -> baseUrl + urlMapping.getId());
    }

    @Override
    public Mono<String> getOriginalUrl(String shortUrlId) {
        ReactiveValueOperations<String, String> valueOps = reactiveRedisOperations.opsForValue();

        return valueOps.get(shortUrlId)
                .switchIfEmpty(Mono.defer(() -> urlMappingRepository.findById(shortUrlId)
                        .map(UrlMapping::getOriginalUrl)
                        .doOnNext(originalUrl -> valueOps.set(shortUrlId, originalUrl))));
    }

/*    private String generateShortUrlId() {
        return RandomStringUtils.randomAlphanumeric(8);
    }*/

    // Helper method to set value and expiration
    private Mono<Boolean> cacheValueWithExpiration(String key, String value) {
        ReactiveValueOperations<String, String> valueOps = reactiveRedisOperations.opsForValue();
        return valueOps.set(key, value)
                .and(reactiveRedisOperations.expire(key, CACHE_EXPIRATION)).hasElement();
    }
}
