package uk.ac.man.cs.eventlite.controllers;

import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.StringContains.containsString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.handler;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;

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

import uk.ac.man.cs.eventlite.EventLite;
import uk.ac.man.cs.eventlite.entities.Venue;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = EventLite.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("test")
public class VenuesControllerApiIntegrationTest extends AbstractTransactionalJUnit4SpringContextTests {

	@LocalServerPort
	private int port;

	private WebTestClient client;

	@BeforeEach
	public void setup() {
		client = WebTestClient.bindToServer().baseUrl("http://localhost:" + port + "/api").build();
	}

	@Test
	public void testGetAllVenues() {
		client.get().uri("/venues").accept(MediaType.APPLICATION_JSON).exchange().expectStatus().isOk().expectHeader()
				.contentType(MediaType.APPLICATION_JSON).expectBody().jsonPath("$._links.self.href")
				.value(endsWith("/api/venues")).jsonPath("$._embedded.venues.length()").value(equalTo(3));
	}

	@Test
	public void getVenueNotFound() {
		client.get().uri("/venues/99").accept(MediaType.APPLICATION_JSON).exchange().expectStatus().isNotFound()
				.expectHeader().contentType(MediaType.APPLICATION_JSON).expectBody().jsonPath("$.error")
				.value(containsString("venue 99")).jsonPath("$.id").isEqualTo(99);
	}
	
	@Test
	public void getVenuesOrderedByName() throws Exception {
		client.get().uri("/venues").accept(MediaType.APPLICATION_JSON).exchange().expectStatus().isOk().expectHeader()
			.contentType(MediaType.APPLICATION_JSON).expectBody()
			.jsonPath("$._links.self.href").value(endsWith("/api/venues"))
			.jsonPath("$._embedded.venues.length()").value(equalTo(3))
			.jsonPath("$._embedded.venues[0].name").value(equalTo("AMBS Building"))
			.jsonPath("$._embedded.venues[1].name").value(equalTo("Kilburn Building"))
			.jsonPath("$._embedded.venues[2].name").value(equalTo("Online"));
	}
	
	@Test
	public void getVenueFound() {
		client.get().uri("/venues/1").accept(MediaType.APPLICATION_JSON).exchange().expectStatus().isOk()
		.expectHeader().contentType(MediaType.APPLICATION_JSON).expectBody()
		.jsonPath("$._links.self.href").value(endsWith("/api/venues/1"));
	}
	
	@Test
	public void getVenueEvents() {
		client.get().uri("/venues/1/events").accept(MediaType.APPLICATION_JSON).exchange().expectStatus().isOk()
		.expectHeader().contentType(MediaType.APPLICATION_JSON).expectBody()
		.jsonPath("$._links.self.href").value(endsWith("/api/venues/1/events"))
		.jsonPath("$._embedded.events.length()").value(equalTo(3));
	}
	
	@Test
	public void getVenueNextThreeEvents() {
		client.get().uri("/venues/1/next3events").accept(MediaType.APPLICATION_JSON).exchange().expectStatus().isOk()
		.expectHeader().contentType(MediaType.APPLICATION_JSON).expectBody()
		.jsonPath("$._links.self.href").value(endsWith("/api/venues/1/next3events"))
		.jsonPath("$._embedded.events.length()").value(equalTo(3));
	}
}
