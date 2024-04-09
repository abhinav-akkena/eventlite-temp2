package uk.ac.man.cs.eventlite.controllers;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.handler;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;


import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import uk.ac.man.cs.eventlite.assemblers.VenueModelAssembler;
import uk.ac.man.cs.eventlite.config.Security;
import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Venue;

@ExtendWith(SpringExtension.class)
@WebMvcTest(VenuesController.class)
@Import(Security.class)
public class VenuesControllerTest {

	@Autowired
	private MockMvc mvc;

	@Mock
	private Venue venue;

	@MockBean
	private VenueService venueService;

	@MockBean
	private EventService eventService;
	
	private Venue testVenue;
	@BeforeEach
	public void setup() {
	    testVenue = new Venue();
	    testVenue.setId(1L); // Use a specific ID matching your test cases
	    testVenue.setName("Test Venue");
	    testVenue.setCapacity(100);
	    testVenue.setAddress("123 Global");
	    testVenue.setPostcode("M14 987");
	}

	
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
	
	//Test update - need to fix roles 
	@Test
	@WithMockUser(roles = "ADMIN")
	public void updateVenue() throws Exception {
	    long venueId = 1L; //WHY DOES IT BREAK IF ITS NOT A LONG ??????? KMN
	    String updatedName = "Updated Venue Name";
	    String updatedAddress = "Updated Address";
	    int updatedCapacity = 200;
	    String updatedPostcode = "NEW123";

	    Venue updatedVenue = new Venue();
	    updatedVenue.setId(venueId);
	    updatedVenue.setName(updatedName);
	    updatedVenue.setAddress(updatedAddress);
	    updatedVenue.setCapacity(updatedCapacity);
	    updatedVenue.setPostcode(updatedPostcode);

	    when(venueService.findById(venueId)).thenReturn(testVenue);
	    
	    mvc.perform(post("/venues/update/1") // Make sure this matches the mapping in your controller
	            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
	            .param("name", "Updated Venue Name")
	            .param("address", "Updated Address")
	            .param("capacity", "200")
	            .param("postcode", "UPD 123")
	            .with(csrf())) // CSRF token included
	            .andExpect(status().is3xxRedirection())
	            .andExpect(redirectedUrl("/venues"));

	    ArgumentCaptor<Venue> venueArgumentCaptor = ArgumentCaptor.forClass(Venue.class);
	    verify(venueService).save(venueArgumentCaptor.capture());
	    Venue savedVenue = venueArgumentCaptor.getValue();

	    assertEquals(updatedName, savedVenue.getName());
	    assertEquals(updatedAddress, savedVenue.getAddress());
	    assertEquals(updatedCapacity, savedVenue.getCapacity());
	    assertEquals(updatedPostcode, savedVenue.getPostcode());
	}
	
	//Test perms
	@Test
	@WithMockUser
	public void updateVenuePermCheck() throws Exception {
	    mvc.perform(post("/venues/update/1")
	            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
	            .param("name", "Attempted Update Name"))
	            .andExpect(status().isForbidden());
	}

	
}
