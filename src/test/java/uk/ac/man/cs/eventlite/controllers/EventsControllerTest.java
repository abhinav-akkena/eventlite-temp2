package uk.ac.man.cs.eventlite.controllers;
import java.time.LocalDate;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.handler;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;


import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.DefaultCsrfToken;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import uk.ac.man.cs.eventlite.assemblers.EventModelAssembler;
import uk.ac.man.cs.eventlite.config.Security;
import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;
import uk.ac.man.cs.eventlite.exceptions.EventNotFoundException;


@ExtendWith(SpringExtension.class)
@WebMvcTest(EventsController.class)
@Import(Security.class)
public class EventsControllerTest {

	@Autowired
	private MockMvc mvc;

	@Mock
	private Event event;

	@Mock
	private Venue venue;

	@MockBean
	private EventService eventService;

	@MockBean
	private VenueService venueService;

	private Optional<Event> testEvent;
	@Test
	public void getIndexWhenNoEvents() throws Exception {
		when(eventService.findAll()).thenReturn(Collections.<Event>emptyList());
//		when(venueService.findAll()).thenReturn(Collections.<Venue>emptyList());

		mvc.perform(get("/events").accept(MediaType.TEXT_HTML)).andExpect(status().isOk())
				.andExpect(view().name("events/index")).andExpect(handler().methodName("getAllEvents"));

		verify(eventService).findAll();
//		verify(venueService).findAll();
		verifyNoInteractions(event);
		verifyNoInteractions(venue);
	}

	@Test
	public void getIndexWithEvents() throws Exception {
		when(venue.getName()).thenReturn("Kilburn Building");
//		when(venueService.findAll()).thenReturn(Collections.<Venue>singletonList(venue));

		when(event.getVenue()).thenReturn(venue);
		when(eventService.findAll()).thenReturn(Collections.<Event>singletonList(event));

		mvc.perform(get("/events").accept(MediaType.TEXT_HTML)).andExpect(status().isOk())
				.andExpect(view().name("events/index")).andExpect(handler().methodName("getAllEvents"));

		verify(eventService).findAll();
//		verify(venueService).findAll();
	}

	@Test
	public void getEventNotFound() throws Exception {
		mvc.perform(get("/events/99").accept(MediaType.TEXT_HTML)).andExpect(status().isNotFound())
				.andExpect(view().name("events/not_found")).andExpect(handler().methodName("showEventDetails"));
	}
	
	@Test
	public void searchEvents() throws Exception {
		when(venue.getName()).thenReturn("Kilburn Building");

		when(event.getVenue()).thenReturn(venue);
		when(eventService.search("bean")).thenReturn(Collections.<Event>singletonList(event));
		
		mvc.perform(get("/events/search").accept(MediaType.TEXT_HTML).param("inputSearch", "bean")).andExpect(status().isOk())
			.andExpect(view().name("events/index")).andExpect(handler().methodName("searchEvent"));
	}
	
	@Test
	public void searchNoEvents() throws Exception {
		when(venue.getName()).thenReturn("Kilburn Building");

		when(event.getVenue()).thenReturn(venue);
		when(eventService.search("beanSequel")).thenReturn(Collections.<Event>emptyList());
		
		mvc.perform(get("/events/search").accept(MediaType.TEXT_HTML).param("inputSearch", "beanSequel")).andExpect(status().isOk())
			.andExpect(view().name("events/index")).andExpect(handler().methodName("searchEvent"));
	}
	
	
	
    @Test
    public void showEventDetailsWithValidId() throws Exception {
        Venue mockVenue = new Venue();
        
        Long eventId = 1L;
        Event mockEvent = new Event();
        mockEvent.setId(eventId);
        mockEvent.setName("Sample Event");
        mockEvent.setVenue(mockVenue);

        when(eventService.findById(eventId)).thenReturn(Optional.of(mockEvent));

        mvc.perform(get("/events/{eventId}", eventId))
                .andExpect(status().isOk())
                .andExpect(view().name("events/eventDetails"))
                .andExpect(model().attributeExists("event"))
                .andExpect(model().attribute("event", mockEvent));
    }
    

    @Test
    public void showEventDetailsWithInvalidId() throws Exception {
        long invalidEventId = 999L;
        when(eventService.findById(invalidEventId)).thenThrow(new EventNotFoundException(invalidEventId));

        mvc.perform(get("/events/{eventId}", invalidEventId))
                .andExpect(status().isNotFound())
                .andExpect(view().name("events/not_found"))
                .andExpect(model().attributeExists("not_found_id"))
                .andExpect(handler().methodName("showEventDetails"));

        verify(eventService).findById(invalidEventId);
    }
	

    @Test	
    @WithMockUser(roles = {"ADMIN", "ADMINISTRATOR"})
    public void deleteEventAsAdmin() throws Exception {
	    long eventId = 1L;
        Event mockEvent = new Event();
        mockEvent.setId(eventId);
        mockEvent.setName("Sample Event");

        when(eventService.findById(eventId)).thenReturn(Optional.of(mockEvent));
	    
	    MvcResult result = mvc.perform(get("/events/delete?id={id}", eventId)
	            .with(csrf()))
	            .andReturn();
	    assertNotNull(result);
	    


    }
	
}
