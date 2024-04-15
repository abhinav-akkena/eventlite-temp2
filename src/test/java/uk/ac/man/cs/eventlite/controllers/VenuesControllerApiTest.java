package uk.ac.man.cs.eventlite.controllers;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.handler;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import uk.ac.man.cs.eventlite.assemblers.EventModelAssembler;
import uk.ac.man.cs.eventlite.assemblers.VenueModelAssembler;
import uk.ac.man.cs.eventlite.config.Security;
import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

@ExtendWith(SpringExtension.class)
@WebMvcTest(VenuesControllerApi.class)
@Import({ Security.class, VenueModelAssembler.class, EventModelAssembler.class })
public class VenuesControllerApiTest {

	@Autowired
	private MockMvc mvc;

	@MockBean
	private VenueService venueService;
	
	@MockBean
	private EventService eventService;

	@Test
	public void getIndexWhenNoVenues() throws Exception {
		when(venueService.findAll()).thenReturn(Collections.<Venue>emptyList());

		mvc.perform(get("/api/venues").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(handler().methodName("getAllVenues")).andExpect(jsonPath("$.length()", equalTo(1)))
				.andExpect(jsonPath("$._links.self.href", endsWith("/api/venues")));

		verify(venueService).findAll();
	}

	@Test
	public void getIndexWithVenues() throws Exception {
		Venue e = new Venue();
		e.setId(0);
		e.setName("Venue1");
		e.setCapacity(1000);
		when(venueService.findAll()).thenReturn(Collections.<Venue>singletonList(e));

		mvc.perform(get("/api/venues").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(handler().methodName("getAllVenues")).andExpect(jsonPath("$.length()", equalTo(2)))
				.andExpect(jsonPath("$._links.self.href", endsWith("/api/venues")))
				.andExpect(jsonPath("$._embedded.venues.length()", equalTo(1)));

		verify(venueService).findAll();
	}
	
	@Test
	public void getIndexWithVenuesWithLinks() throws Exception {
		Venue e = new Venue();
		e.setId(0);
		e.setName("Venue1");
		e.setCapacity(1000);
		when(venueService.findAll()).thenReturn(Collections.<Venue>singletonList(e));

		mvc.perform(get("/api/venues").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(handler().methodName("getAllVenues")).andExpect(jsonPath("$.length()", equalTo(2)))
				.andExpect(jsonPath("$._links.self.href", endsWith("/api/venues")))
				.andExpect(jsonPath("$._embedded.venues.length()", equalTo(1)))
				.andExpect(jsonPath("$._links.self.href", endsWith("/api/venues")))
				.andExpect(jsonPath("$._links.profile.href", endsWith("/api/profile/venues")));
				

		verify(venueService).findAll();
	}

	@Test
	public void getVenueNotFound() throws Exception {
		mvc.perform(get("/api/venues/99").accept(MediaType.APPLICATION_JSON)).andExpect(status().isNotFound())
				.andExpect(jsonPath("$.error", containsString("venue 99"))).andExpect(jsonPath("$.id", equalTo(99)))
				.andExpect(handler().methodName("getVenue"));
	}
	
	
	@Test 
	public void getNextThreeEvents() throws Exception {
		Event event1 = new Event();
		event1.setId(0);
		event1.setName("Event 1");
		event1.setDate(LocalDate.of(2024, 4, 8));
		
		Event event2 = new Event();
		event2.setId(0);
		event2.setName("Event 2");
		event2.setDate(LocalDate.of(2024, 4, 9));
		
		Event event3 = new Event();
		event3.setId(0);
		event3.setName("Event 3");
		event3.setDate(LocalDate.of(2024, 4, 10));
		
		Venue venue = new Venue();
		venue.setId(0);
		venue.setName("Venue 1");
		venue.setCapacity(1000);
		
		when(venueService.findById(0)).thenReturn(venue);
		
		when(eventService.getNextThreeEvents(venue)).thenReturn(List.of(event1, event2, event3));

		mvc.perform(get("/api/venues/0/next3events").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(handler().methodName("getNextThreeEvents")).andExpect(jsonPath("$.length()", equalTo(2)))
				.andExpect(jsonPath("$._links.self.href", endsWith("/api/venues/0/next3events")))
				.andExpect(jsonPath("$._embedded.events.length()", equalTo(3)));

		verify(eventService).getNextThreeEvents(venue);
	}
	
	@Test 
	public void getNextThreeEventsButTwo() throws Exception {
		Event event1 = new Event();
		event1.setId(0);
		event1.setName("Event 1");
		event1.setDate(LocalDate.of(2024, 4, 8));
		
		Event event2 = new Event();
		event2.setId(0);
		event2.setName("Event 2");
		event2.setDate(LocalDate.of(2024, 4, 9));
		
		Venue venue = new Venue();
		venue.setId(0);
		venue.setName("Venue 1");
		venue.setCapacity(1000);
		
		when(venueService.findById(0)).thenReturn(venue);
		
		when(eventService.getNextThreeEvents(venue)).thenReturn(List.of(event1, event2));

		mvc.perform(get("/api/venues/0/next3events").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(handler().methodName("getNextThreeEvents")).andExpect(jsonPath("$.length()", equalTo(2)))
				.andExpect(jsonPath("$._links.self.href", endsWith("/api/venues/0/next3events")))
				.andExpect(jsonPath("$._embedded.events.length()", equalTo(2)));

		verify(eventService).getNextThreeEvents(venue);
	}
}

