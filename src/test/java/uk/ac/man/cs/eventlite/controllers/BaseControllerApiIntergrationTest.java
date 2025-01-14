package uk.ac.man.cs.eventlite.controllers;

import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.StringContains.containsString;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.autoconfigure.web.client.RestTemplateAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.web.reactive.server.WebTestClient;

import uk.ac.man.cs.eventlite.EventLite;
import uk.ac.man.cs.eventlite.dao.MastodonService;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = EventLite.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@ContextConfiguration(classes = RestTemplateAutoConfiguration.class)
public class BaseControllerApiIntergrationTest extends AbstractTransactionalJUnit4SpringContextTests {

	@LocalServerPort
	private int port;

	private WebTestClient client;
	
    @MockBean
    private MastodonService mastodonService;
	

	@BeforeEach
	public void setup() {
		client = WebTestClient.bindToServer().baseUrl("http://localhost:" + port + "/api").build();
	}

	@Test
	public void testApiRoot() {
	  client.get().uri("/").accept(MediaType.APPLICATION_JSON).exchange().expectStatus().isOk().expectHeader()
	          .contentType(MediaType.APPLICATION_JSON).expectBody()
	          .jsonPath("$._links.profile.href").value(endsWith("/api/profile"))
	          .jsonPath("$._links.venues.href").value(endsWith("/api/venues"))
	          .jsonPath("$._links.events.href").value(endsWith("/api/events"));
	}


}
