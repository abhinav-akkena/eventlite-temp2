package uk.ac.man.cs.eventlite.controllers;

import static org.mockito.Mockito.verify;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.handler;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;

import java.util.Collections;
import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import uk.ac.man.cs.eventlite.assemblers.VenueModelAssembler;
import uk.ac.man.cs.eventlite.config.Security;
import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Venue;
import uk.ac.man.cs.eventlite.exceptions.EventNotFoundException;
import uk.ac.man.cs.eventlite.exceptions.VenueNotFoundException;
import uk.ac.man.cs.eventlite.entities.Event;

@ExtendWith(SpringExtension.class)
@WebMvcTest(VenuesController.class)
@Import(Security.class)
public class VenuesControllerTest {

	@Autowired
	private MockMvc mvc;

	@Mock
	private Venue venue;
	
	@Mock
	private Event event;
	
	

	@MockBean
	private VenueService venueService;

	@MockBean
	private EventService eventService;
	
	@Test
	public void getIndexWhenNoEvents() throws Exception {
		when(venueService.findAll()).thenReturn(Collections.<Venue>emptyList());

		mvc.perform(get("/venues").accept(MediaType.TEXT_HTML)).andExpect(status().isOk())
				.andExpect(view().name("venues/index")).andExpect(handler().methodName("getAllVenues"));

		verify(venueService).findAll();
		verifyNoInteractions(venue);
	}

	@Test
	public void getIndexWithEvents() throws Exception {
		when(venue.getName()).thenReturn("Kilburn Building");
		when(venueService.findAll()).thenReturn(Collections.<Venue>singletonList(venue));

		mvc.perform(get("/venues").accept(MediaType.TEXT_HTML)).andExpect(status().isOk())
				.andExpect(view().name("venues/index")).andExpect(handler().methodName("getAllVenues"));

		verify(venueService).findAll();
	}
	
	@Test
	public void searchVenues() throws Exception {
		when(venue.getName()).thenReturn("Kilburn Building");

		when(venueService.search("spaghetti")).thenReturn(Collections.<Venue>singletonList(venue));
		
		mvc.perform(get("/venues/search").accept(MediaType.TEXT_HTML).param("inputSearch", "spaghetti")).andExpect(status().isOk())
			.andExpect(view().name("venues/index")).andExpect(handler().methodName("searchVenue"));
	}
	
	@Test
	public void searchNoVenue() throws Exception {
		when(venue.getName()).thenReturn("Kilburn Building");

		when(venueService.search("spaghettiMeatballs")).thenReturn(Collections.<Venue>emptyList());
		
		mvc.perform(get("/venues/search").accept(MediaType.TEXT_HTML).param("inputSearch", "spaghettiMeatballs")).andExpect(status().isOk())
			.andExpect(view().name("venues/index")).andExpect(handler().methodName("searchVenue"));
	}
	
    @Test
    public void showExistingVenue() throws Exception {
        Long venueId = 1L;
        when(venueService.findById(venueId)).thenReturn(venue);
        when(eventService.findFuture()).thenReturn(Arrays.asList(event));
        when(event.getVenue()).thenReturn(venue);
        when(venue.getId()).thenReturn(venueId);

        mvc.perform(get("/venues/{id}", venueId))
                .andExpect(status().isOk())
                .andExpect(view().name("venues/venueDetails"))
                .andExpect(model().attributeExists("venue"))
                .andExpect(model().attributeExists("events"))
                .andExpect(handler().methodName("showVenueDetails"));

        verify(venueService).findById(venueId);
        verify(eventService).findFuture();
    }
    
    @Test
    public void showNonExistingVenue() throws Exception {
        long invalidVenueId = 999L;
        when(venueService.findById(invalidVenueId)).thenThrow(new VenueNotFoundException(invalidVenueId));

        mvc.perform(get("/venues/{id}", invalidVenueId))
                .andExpect(status().isNotFound())
                .andExpect(view().name("venues/not_found"))
                .andExpect(model().attributeExists("not_found_id"))
                .andExpect(handler().methodName("showVenueDetails"));

        verify(venueService).findById(invalidVenueId);
    }
   
	
}
