package uk.ac.man.cs.eventlite.controllers;

import static org.hamcrest.MatcherAssert.assertThat;

import static org.hamcrest.core.StringContains.containsString;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.http.HttpHeaders;

import uk.ac.man.cs.eventlite.EventLite;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = EventLite.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("test")
public class VenuesControllerIntegrationTest extends AbstractTransactionalJUnit4SpringContextTests {

	@LocalServerPort
	private int port;

	private WebTestClient client;

	@BeforeEach
	public void setup() {
	    client = WebTestClient.bindToServer()
	            .baseUrl("http://localhost:" + port)
	            .defaultHeaders(httpHeaders -> httpHeaders.setBasicAuth("Tom", "Carroll"))
	            .build();
	}

	@Test
	public void testGetAllVenues() {
		client.get().uri("/venues").accept(MediaType.TEXT_HTML).exchange().expectStatus().isOk();
	}

//	@Test
//	public void getVenueNotFound() {
//		client.get().uri("/venues/99").accept(MediaType.TEXT_HTML).exchange().expectStatus().isNotFound().expectHeader()
//				.contentTypeCompatibleWith(MediaType.TEXT_HTML).expectBody(String.class).consumeWith(result -> {
//					assertThat(result.getResponseBody(), containsString("99"));
//				});
//	}
	
	@Test
	public void checkVenuePage() throws Exception {
		client.get().uri("/venues")
        .accept(MediaType.TEXT_HTML)
        .exchange()
        .expectStatus().isOk()
        .expectBody(String.class)
        .value(body -> {
            assertTrue(body.contains("<h1>All venues</h1>")); // Assuming the view contains a title
        });
	}
	
	@Test
	public void searchVenueNotFound() throws Exception {
		client.get()
		.uri(uriBuilder -> uriBuilder.path("/venues/search")
                .queryParam("inputSearch", "bean")
                .build())
        .accept(MediaType.TEXT_HTML)
        .exchange()
        .expectStatus().isOk()
        .expectBody(String.class)
        .value(body -> {
            assertTrue(body.contains("<h1>All venues</h1>")); // Assuming the view contains a title
        });
	}
	
	@Test
	public void searchVenueFound() throws Exception {
		client.get()
		.uri(uriBuilder -> uriBuilder.path("/venues/search")
                .queryParam("inputSearch", "Kilburn")
                .build())
        .accept(MediaType.TEXT_HTML)
        .exchange()
        .expectStatus().isOk()
        .expectBody(String.class)
        .value(body -> {
            assertTrue(body.contains("<h1>All venues</h1>")); // Assuming the view contains a title
        });
	}
	
	@Test
	public void venueUpdatePage() throws Exception {
		client.get().uri("/venues/edit/1")
        .accept(MediaType.TEXT_HTML)
        .exchange()
        .expectStatus().isOk()
        .expectBody(String.class)
        .value(body -> {
            assertTrue(body.contains("<h1>Edit Venue</h1>")); // Assuming the view contains a title
        });
	}
	
	@Test
	public void deleteVenuePageAsAdmin() throws Exception {
		client.get().uri("/venues/delete?id=1")
        .accept(MediaType.TEXT_HTML)
        .exchange()
        .expectStatus().isOk()
        .expectBody(String.class)
        .value(body -> {
            assertTrue(body.contains("Delete Venue")); // Assuming the view contains a title
        });
	}
	
	@Test
	public void venuePageValidId() throws Exception {
		client.get().uri("/venues/1")
        .accept(MediaType.TEXT_HTML)
        .exchange()
        .expectStatus().isOk()
        .expectBody(String.class)
        .value(body -> {
            assertTrue(body.contains("Kilburn")); // Assuming the view contains a title
        });
	}
}
