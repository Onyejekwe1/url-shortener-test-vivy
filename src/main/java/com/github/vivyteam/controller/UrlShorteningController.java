package com.github.vivyteam.controller;

import com.github.vivyteam.service.contract.IUrlShorteningService;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.net.URI;

@RestController
public class UrlShorteningController {
    private IUrlShorteningService urlShorteningService;

    public UrlShorteningController(IUrlShorteningService urlShorteningService) {
        this.urlShorteningService = urlShorteningService;
    }

    @PostMapping("/shorten")
    public Mono<String> shortenUrl(@RequestBody String originalUrl) {
        return urlShorteningService.shortenUrl(originalUrl);
    }

    @GetMapping("/original")
    public Mono<String> getOriginalUrl(@RequestParam String shortUrlId) {
        return urlShorteningService.getOriginalUrl(shortUrlId);
    }

    @GetMapping("/{shortUrlId}")
    public Mono<Void> redirectToOriginalUrl(@PathVariable String shortUrlId, ServerHttpResponse response) {
        return urlShorteningService.getOriginalUrl(shortUrlId)
                .flatMap(originalUrl -> {
                    response.setStatusCode(HttpStatus.FOUND);
                    response.getHeaders().setLocation(URI.create(originalUrl));
                    return response.setComplete();
                })
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Short URL not found")));
    }
}
