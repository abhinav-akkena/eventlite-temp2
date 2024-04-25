package uk.ac.man.cs.eventlite.controllers;

import static org.hamcrest.MatcherAssert.assertThat;

import static org.hamcrest.core.StringContains.containsString;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.handler;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.reactive.server.WebTestClient.*;

import java.util.Collections;

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
import uk.ac.man.cs.eventlite.entities.Event;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = EventLite.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("test")
public class EventsControllerIntegrationTest extends AbstractTransactionalJUnit4SpringContextTests {

	@LocalServerPort
	private int port;

	private WebTestClient client;

	@BeforeEach
	public void setup() {
		client = WebTestClient.bindToServer().baseUrl("http://localhost:" + port).build();
	}

	@Test
	public void testGetAllEvents() {
		client.get().uri("/events").accept(MediaType.TEXT_HTML).exchange().expectStatus().isOk();
	}

	@Test
	public void getEventNotFound() {
		client.get().uri("/events/99").accept(MediaType.TEXT_HTML).exchange().expectStatus().isNotFound().expectHeader()
				.contentTypeCompatibleWith(MediaType.TEXT_HTML).expectBody(String.class).consumeWith(result -> {
					assertThat(result.getResponseBody(), containsString("99"));
				});
	}
	
	@Test
	public void checkEventPage() throws Exception {
		client.get().uri("/events")
        .accept(MediaType.TEXT_HTML)
        .exchange()
        .expectStatus().isOk()
        .expectBody(String.class)
        .value(body -> {
            assertTrue(body.contains("<h1>All events</h1>")); // Assuming the view contains a title
        });
	}
	
	@Test
	public void searchEventNotFound() throws Exception {
		client.get()
		.uri(uriBuilder -> uriBuilder.path("/events/search")
                .queryParam("inputSearch", "beanSequel")
                .build())
        .accept(MediaType.TEXT_HTML)
        .exchange()
        .expectStatus().isOk()
        .expectBody(String.class)
        .value(body -> {
            assertTrue(body.contains("<h1>All events</h1>")); // Assuming the view contains a title
        });
	}
	
	
	@Test
	public void searchEventFound() throws Exception {
		client.get()
		.uri(uriBuilder -> uriBuilder.path("/events/search")
                .queryParam("inputSearch", "COMP")
                .build())
        .accept(MediaType.TEXT_HTML)
        .exchange()
        .expectStatus().isOk()
        .expectBody(String.class)
        .value(body -> {
            assertTrue(body.contains("<h1>All events</h1>")); // Assuming the view contains a title
        });
	}
	
	@Test
	public void eventPageValidId() throws Exception {
		client.get().uri("/events/1")
        .accept(MediaType.TEXT_HTML)
        .exchange()
        .expectStatus().isOk()
        .expectBody(String.class)
        .value(body -> {
            assertTrue(body.contains("COMP23412")); // Assuming the view contains a title
        });
	}
	
	@Test
	public void deleteEventPageAsAdmin() throws Exception {
		client.get().uri("/events/delete?id=1")
        .accept(MediaType.TEXT_HTML)
        .exchange()
        .expectStatus().isOk()
        .expectBody(String.class)
        .value(body -> {
            assertTrue(body.contains("Delete Event")); // Assuming the view contains a title
        });
	}

}
