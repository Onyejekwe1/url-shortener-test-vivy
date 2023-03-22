package com.github.vivyteam;

import com.github.vivyteam.controller.UrlShorteningController;
import com.github.vivyteam.service.UrlShorteningService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest
public class UrlShortnerApplicationTests {
	private WebTestClient webTestClient;

	@Mock
	private UrlShorteningService urlShorteningService;

	@InjectMocks
	private UrlShorteningController urlShorteningController;

	@BeforeEach
	public void setUp() {
		MockitoAnnotations.openMocks(this);
		webTestClient = WebTestClient.bindToController(urlShorteningController).build();
	}

	@Test
	public void testShortenUrlEndpoint() {
		String originalUrl = "https://example.com";
		String shortUrlId = "a1B2c3";
		String expectedShortUrl = "http://localhost:9000/" + shortUrlId;

		when(urlShorteningService.shortenUrl(anyString())).thenReturn(Mono.just(expectedShortUrl));

		webTestClient.post().uri("/shorten")
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue("{\"url\":\"" + originalUrl + "\"}")
				.exchange()
				.expectStatus().isEqualTo(HttpStatus.OK)
				.expectBody()
				.jsonPath("$.shortUrl").isEqualTo(expectedShortUrl);

		Mockito.verify(urlShorteningService, Mockito.times(1)).shortenUrl(originalUrl);
	}

	@Test
	public void testRedirectToOriginalUrl() {
		String originalUrl = "https://example.com";
		String shortUrlId = "a1B2c3";

		when(urlShorteningService.getOriginalUrl(shortUrlId)).thenReturn(Mono.just(originalUrl));

		webTestClient.get().uri("/" + shortUrlId)
				.exchange()
				.expectStatus().isEqualTo(HttpStatus.PERMANENT_REDIRECT)
				.expectHeader().valueEquals("Location", originalUrl);
	}

}
