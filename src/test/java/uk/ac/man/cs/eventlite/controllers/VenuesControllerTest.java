package uk.ac.man.cs.eventlite.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.handler;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import java.util.Collections;
import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.DefaultCsrfToken;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

import uk.ac.man.cs.eventlite.config.Security;
import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Venue;
import uk.ac.man.cs.eventlite.exceptions.VenueNotFoundException;
import uk.ac.man.cs.eventlite.entities.Event;

@ExtendWith(SpringExtension.class)
@WebMvcTest(VenuesController.class)
@Import(Security.class)
@ActiveProfiles("test")
public class VenuesControllerTest {

	@Autowired
	private MockMvc mvc;
	
	@Autowired
	private WebApplicationContext context;

	@Mock
	private Venue venue;
	
	@Captor
	private ArgumentCaptor<Venue> venueCaptor;
	
	@Mock
	private Event event;
	
	@MockBean
	private VenueService venueService;

	@MockBean
	private EventService eventService;
	
	private Venue testVenue;
	@BeforeEach
	public void setup() {
	    testVenue = new Venue();
	    testVenue.setId(1L);
	    testVenue.setName("Test Venue");
	    testVenue.setCapacity(100);
	    testVenue.setAddress("123 Global");
	    testVenue.setPostcode("M14 987");
	    
	    mvc = MockMvcBuilders
	            .webAppContextSetup(context)
	            .apply(springSecurity())  // Ensure spring security is applied
	            .build();
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
	
	//Testing update functionality 
	@Test
	@WithMockUser(roles = {"ADMIN", "ADMINISTRATOR"})
	public void postUpdateVenueAsAdminUpdatesVenue() throws Exception {
	    long venueId = 1L;
	    String updatedName = "Updated Venue Name";
	    String updatedAddress = "Updated Address";
	    String updatedPostcode = "Updated Postcode";
	    int updatedCapacity = 200;

	    when(venueService.findById(venueId)).thenReturn(testVenue);

	    CsrfToken csrfToken = new DefaultCsrfToken("X-CSRF-TOKEN", "_csrf", "token-value");
	    
	    mvc.perform(post("/venues/update/{id}", venueId)
	        .sessionAttr("_csrf", csrfToken)
	        .param("_csrf", csrfToken.getToken())
	        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
	        .param("name", updatedName)
	        .param("address", updatedAddress)
	        .param("capacity", String.valueOf(updatedCapacity))
	        .param("postcode", updatedPostcode))
	        .andDo(print())
	        ;
	}

	
	@Test
	@WithMockUser(roles = "ADMIN")
	public void getEditVenueAsAdminShowsForm() throws Exception {
	    long venueId = 1L;
	    when(venueService.findById(venueId)).thenReturn(testVenue); // Assuming testVenue is already set up

	    mvc.perform(get("/venues/edit/{id}", venueId))
	            .andExpect(status().isOk())
	            .andExpect(view().name("venues/edit_venue"))
	            .andExpect(model().attributeExists("venue"))
	            .andExpect(model().attribute("venue", testVenue));

	    verify(venueService).findById(venueId);
	}

	//Test edit function is forbidden without admin perms
	@Test
	@WithMockUser
	public void updateVenuePermCheck() throws Exception {
	    mvc.perform(post("/venues/update/1")
	            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
	            .param("name", "Attempted Update Name"))
	            .andExpect(status().isForbidden());
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
