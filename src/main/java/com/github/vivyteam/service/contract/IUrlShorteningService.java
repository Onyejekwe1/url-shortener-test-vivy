package com.github.vivyteam.service.contract;

import reactor.core.publisher.Mono;

public interface IUrlShorteningService {
    Mono<String> shortenUrl(String originalUrl);
    Mono<String> getOriginalUrl(String shortUrlId);
}
