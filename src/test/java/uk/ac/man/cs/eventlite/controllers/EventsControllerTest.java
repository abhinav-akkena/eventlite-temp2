package uk.ac.man.cs.eventlite.controllers;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.handler;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.client.RestTemplateAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import uk.ac.man.cs.eventlite.EventLite;
import uk.ac.man.cs.eventlite.config.RestClientConfig;
import uk.ac.man.cs.eventlite.config.Security;
import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.MastodonService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;
import uk.ac.man.cs.eventlite.exceptions.EventNotFoundException;

@ExtendWith(SpringExtension.class)
@WebMvcTest(EventsController.class)
@Import({Security.class, EventsController.class})
@ContextConfiguration(classes = RestTemplateAutoConfiguration.class)
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
	
	@MockBean
	private MastodonService mastodonService;

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
		when(eventService.searchFuture("bean")).thenReturn(Collections.<Event>singletonList(event));
		when(eventService.searchPast("bean")).thenReturn(Collections.emptyList());
		
		mvc.perform(get("/events/search").accept(MediaType.TEXT_HTML).param("inputSearch", "bean")).andExpect(status().isOk())
			.andExpect(view().name("events/index")).andExpect(handler().methodName("searchEvent"));
	}
	
	@Test
	public void searchNoEvents() throws Exception {
		when(venue.getName()).thenReturn("Kilburn Building");

		when(event.getVenue()).thenReturn(venue);
		when(eventService.searchFuture("bean")).thenReturn(Collections.emptyList());
		when(eventService.searchPast("bean")).thenReturn(Collections.emptyList());
		
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
    public void testAccessAdd() throws Exception {
    	
    	List<Venue> venues = new ArrayList<>();
        Venue venue1 = new Venue();
        venue1.setName("Venue 1");
        Venue venue2 = new Venue();
        venue2.setName("Venue 2");
        venues.add(venue1);
        venues.add(venue2);
        when(venueService.findAll()).thenReturn(venues);
        mvc.perform(MockMvcRequestBuilders.get("/events/add"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("events/add_event"))
                .andExpect(MockMvcResultMatchers.model().attributeExists("venues"))
                .andExpect(MockMvcResultMatchers.model().attribute("venues", venues));
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "ADMINISTRATOR"})
    public void testAddEventNoNameErrors() throws Exception {
    	List<Venue> venues = new ArrayList<>();
    	Venue venue1 = new Venue();
    	venue1.setId(1);
		venue1.setCapacity(120);
		venue1.setName("Kilburn Building");
		venue1.setAddress("Kilburn Building, Oxford Rd, Manchester");
		venue1.setPostcode("M13 9PL");
		venues.add(venue1);		
		
		when(venueService.findAll()).thenReturn(venues);

	    mvc.perform(post("/events/added")
	    		.param("venue.id", "1")
                .param("date", "2024-11-07")
                .with(csrf()))
        		.andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/events/add"))
	    		.andExpect(flash().attributeExists("errorMessage"));

        verifyNoInteractions(eventService);
    }
    
    @Test
    @WithMockUser(roles = {"ADMIN", "ADMINISTRATOR"})
    public void testAddSuccess() throws Exception {
    	List<Venue> venues = new ArrayList<>();
    	Venue venue1 = new Venue();
    	venue1.setId(1);
		venue1.setCapacity(120);
		venue1.setName("Kilburn Building");
		venue1.setAddress("Kilburn Building, Oxford Rd, Manchester");
		venue1.setPostcode("M13 9PL");
		venues.add(venue1);		
		
		when(venueService.findAll()).thenReturn(venues);

	    mvc.perform(post("/events/added")
	    		.param("name", "Test Event")
                .param("venue.id", "1")
                .param("date", "2024-11-07")
                .with(csrf()))
        		.andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/events"));

	    verify(eventService).save(any(Event.class));
    }
    
    @Test
    @WithMockUser(roles = {"ADMIN", "ADMINISTRATOR"})
    public void testAddNoVenueError() throws Exception {
    	List<Venue> venues = new ArrayList<>();
    	Venue venue1 = new Venue();
    	venue1.setId(1);
		venue1.setCapacity(120);
		venue1.setName("Kilburn Building");
		venue1.setAddress("Kilburn Building, Oxford Rd, Manchester");
		venue1.setPostcode("M13 9PL");
		venues.add(venue1);		
		
		when(venueService.findAll()).thenReturn(venues);

	    mvc.perform(post("/events/added")
	    		.param("name", "Test Event")
                .param("venue", "1")
                .param("date", "2024-11-07")
                .with(csrf()))
        		.andExpect(status().is3xxRedirection())
        		.andExpect(redirectedUrl("/events/add"))
	    		.andExpect(flash().attributeExists("errorMessage"));

        verifyNoInteractions(eventService);
    }
    
    @Test
    @WithMockUser(roles = {"ADMIN", "ADMINISTRATOR"})
    public void testAddNoDateError() throws Exception {
    	List<Venue> venues = new ArrayList<>();
    	Venue venue1 = new Venue();
    	venue1.setId(1);
		venue1.setCapacity(120);
		venue1.setName("Kilburn Building");
		venue1.setAddress("Kilburn Building, Oxford Rd, Manchester");
		venue1.setPostcode("M13 9PL");
		venues.add(venue1);		
		
		when(venueService.findAll()).thenReturn(venues);

	    mvc.perform(post("/events/added")
	    		.param("name", "Test Event")
                .param("venue.id", "1")
                .with(csrf()))
        		.andExpect(status().is3xxRedirection())
        		.andExpect(redirectedUrl("/events/add"))
	    		.andExpect(flash().attributeExists("errorMessage"));

        verifyNoInteractions(eventService);
    }
    
    @Test
    @WithMockUser(roles = {"ADMIN", "ADMINISTRATOR"})
    public void testAddNameLongError() throws Exception {
    	List<Venue> venues = new ArrayList<>();
    	Venue venue1 = new Venue();
    	venue1.setId(1);
		venue1.setCapacity(120);
		venue1.setName("Kilburn Building");
		venue1.setAddress("Kilburn Building, Oxford Rd, Manchester");
		venue1.setPostcode("M13 9PL");
		venues.add(venue1);		
		
		String newName = "";
		for(int i = 0; i<=255; i++) {
			newName += 'c';//Making a name longer than 255 characters
		}
		
		when(venueService.findAll()).thenReturn(venues);

	    mvc.perform(post("/events/added")
	    		.param("name", newName)
                .param("venue.id", "1")
                .param("date", "2024-11-07")
                .with(csrf()))
        		.andExpect(status().is3xxRedirection())
        		.andExpect(redirectedUrl("/events/add"))
	    		.andExpect(flash().attributeExists("errorMessage"));

        verifyNoInteractions(eventService);
    }
    
    @Test
    @WithMockUser(roles = {"ADMIN", "ADMINISTRATOR"})
    public void testAddDescriptionLongError() throws Exception {
    	List<Venue> venues = new ArrayList<>();
    	Venue venue1 = new Venue();
    	venue1.setId(1);
		venue1.setCapacity(120);
		venue1.setName("Kilburn Building");
		venue1.setAddress("Kilburn Building, Oxford Rd, Manchester");
		venue1.setPostcode("M13 9PL");
		venues.add(venue1);		
		
		String newDescription = "";
		for(int i = 0; i<=500; i++) {
			newDescription += 'c';//Making a description longer than 500 characters
		}
		
		when(venueService.findAll()).thenReturn(venues);

	    mvc.perform(post("/events/added")
	    		.param("name", "Test Event")
                .param("venue.id", "1")
                .param("date", "2024-11-07")
                .param("description", newDescription)
                .with(csrf()))
        		.andExpect(status().is3xxRedirection())
        		.andExpect(redirectedUrl("/events/add"))
	    		.andExpect(flash().attributeExists("errorMessage"));

        verifyNoInteractions(eventService);
    }



	@Test
    @WithMockUser()
    public void testAddEventNoAuth() throws Exception {
    	
		List<Venue> venues = new ArrayList<>();
    	Venue venue1 = new Venue();
    	venue1.setId(1);
		venue1.setCapacity(120);
		venue1.setName("Kilburn Building");
		venue1.setAddress("Kilburn Building, Oxford Rd, Manchester");
		venue1.setPostcode("M13 9PL");
		venues.add(venue1);		
		
		when(venueService.findAll()).thenReturn(venues);
		
        mvc.perform(post("/event/added")
                .param("name", "Test Event")
                .param("venue.id", "1")
                .param("date", "2024-11-07")
                .with(csrf()))
                .andExpect(status().isForbidden());
        verify(eventService, never()).save(any(Event.class));
    }
	
	@Test
    @WithMockUser()
    public void testUpdateEventNoAuth() throws Exception {
    	
		List<Venue> venues = new ArrayList<>();
    	Venue venue1 = new Venue();
    	venue1.setId(1);
		venue1.setCapacity(120);
		venue1.setName("Kilburn Building");
		venue1.setAddress("Kilburn Building, Oxford Rd, Manchester");
		venue1.setPostcode("M13 9PL");
		venues.add(venue1);		
		
		when(venueService.findAll()).thenReturn(venues);
		
        mvc.perform(post("/event/edit/1")
                .param("name", "Test Event")
                .param("venue.id", "1")
                .param("date", "2024-11-07")
                .with(csrf()))
                .andExpect(status().isForbidden());
        verify(eventService, never()).save(any(Event.class));
    }
	
	@Test
    @WithMockUser(roles = {"ADMIN", "ADMINISTRATOR"})
    public void testAccessUpdate() throws Exception {
		List<Venue> venues = new ArrayList<>();
    	Venue venue1 = new Venue();
    	venue1.setId(1);
		venue1.setCapacity(120);
		venue1.setName("Kilburn Building");
		venue1.setAddress("Kilburn Building, Oxford Rd, Manchester");
		venue1.setPostcode("M13 9PL");
		venues.add(venue1);		
		
		when(venueService.findAll()).thenReturn(venues);
		
		Event mockEvent = new Event(1, "Test Event", LocalDate.of(2024, 07, 7), LocalTime.of(9, 0),venue1,"Best event");
		when(eventService.findById(1L)).thenReturn(Optional.of(mockEvent));

		mvc.perform(MockMvcRequestBuilders.get("/events/edit/1"))
	    .andExpect(MockMvcResultMatchers.status().isOk())
	    .andExpect(MockMvcResultMatchers.view().name("events/edit_event"))
	    .andExpect(MockMvcResultMatchers.model().attributeExists("venues"))
	    .andExpect(MockMvcResultMatchers.model().attribute("venues", venues))
	    .andExpect(MockMvcResultMatchers.model().attributeExists("event"))
	    .andExpect(MockMvcResultMatchers.model().attribute("event", mockEvent));
    }
	
	@Test
    @WithMockUser(roles = {"ADMIN", "ADMINISTRATOR"})
    public void testUpdateSuccess() throws Exception {
		List<Venue> venues = new ArrayList<>();
    	Venue venue1 = new Venue();
    	venue1.setId(1);
		venue1.setCapacity(120);
		venue1.setName("Kilburn Building");
		venue1.setAddress("Kilburn Building, Oxford Rd, Manchester");
		venue1.setPostcode("M13 9PL");
		Venue venue2 = new Venue();
    	venue2.setId(1);
		venue2.setCapacity(220);
		venue2.setName("Engineering Building");
		venue2.setAddress("Kilburn Building, Oxford Rd, Manchester");
		venue2.setPostcode("M13 9QL");
		venues.add(venue2);		
		
		when(venueService.findAll()).thenReturn(venues);
		
		Event mockEvent = new Event(1, "Test Event", LocalDate.of(2024, 07, 7), LocalTime.of(9, 0),venue1,"Best event");
		when(eventService.findById(1L)).thenReturn(Optional.of(mockEvent));

		mvc.perform(post("/events/update/1")
	    		.param("name", "New Test Event")
                .param("venue.id", "2")
                .param("date", "2024-12-07")
                .with(csrf()))
        		.andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/events"));

	    verify(eventService).save(any(Event.class));
    }
	
	
	@Test
    @WithMockUser(roles = {"ADMIN", "ADMINISTRATOR"})
    public void testUpdateNoNameFailure() throws Exception {
		List<Venue> venues = new ArrayList<>();
    	Venue venue1 = new Venue();
    	venue1.setId(1);
		venue1.setCapacity(120);
		venue1.setName("Kilburn Building");
		venue1.setAddress("Kilburn Building, Oxford Rd, Manchester");
		venue1.setPostcode("M13 9PL");
		Venue venue2 = new Venue();
    	venue2.setId(1);
		venue2.setCapacity(220);
		venue2.setName("Engineering Building");
		venue2.setAddress("Kilburn Building, Oxford Rd, Manchester");
		venue2.setPostcode("M13 9QL");
		venues.add(venue2);		
		
		when(venueService.findAll()).thenReturn(venues);
		
		Event mockEvent = new Event(1, "Test Event", LocalDate.of(2024, 07, 7), LocalTime.of(9, 0),venue1,"Best event");
		when(eventService.findById(1L)).thenReturn(Optional.of(mockEvent));

		mvc.perform(post("/events/update/1")
	    		.param("name", "")
                .param("venue.id", "2")
                .param("date", "2024-12-07")
                .with(csrf()))
		.andExpect(status().is3xxRedirection())
		.andExpect(redirectedUrl("/events/edit/1"))
		.andExpect(flash().attributeExists("errorMessage"));

verifyNoInteractions(eventService);

	    
    }
	
	
	@Test
    @WithMockUser(roles = {"ADMIN", "ADMINISTRATOR"})
    public void testUpdateNoVenue() throws Exception {
		List<Venue> venues = new ArrayList<>();
    	Venue venue1 = new Venue();
    	venue1.setId(1);
		venue1.setCapacity(120);
		venue1.setName("Kilburn Building");
		venue1.setAddress("Kilburn Building, Oxford Rd, Manchester");
		venue1.setPostcode("M13 9PL");
		Venue venue2 = new Venue();
    	venue2.setId(1);
		venue2.setCapacity(220);
		venue2.setName("Engineering Building");
		venue2.setAddress("Kilburn Building, Oxford Rd, Manchester");
		venue2.setPostcode("M13 9QL");
		venues.add(venue2);		
		
		when(venueService.findAll()).thenReturn(venues);
		
		Event mockEvent = new Event(1, "Test Event", LocalDate.of(2024, 07, 7), LocalTime.of(9, 0),venue1,"Best event");
		when(eventService.findById(1L)).thenReturn(Optional.of(mockEvent));

		mvc.perform(post("/events/update/1")
	    		.param("name", "New test")
                .param("venue", "2")
                .param("date", "2024-12-07")
                .with(csrf()))
		.andExpect(status().is3xxRedirection())
		.andExpect(redirectedUrl("/events/edit/1"))
		.andExpect(flash().attributeExists("errorMessage"));

			verifyNoInteractions(eventService);

	    
    }
	
	@Test
    @WithMockUser(roles = {"ADMIN", "ADMINISTRATOR"})
    public void testUpdateNoDate() throws Exception {
		List<Venue> venues = new ArrayList<>();
    	Venue venue1 = new Venue();
    	venue1.setId(1);
		venue1.setCapacity(120);
		venue1.setName("Kilburn Building");
		venue1.setAddress("Kilburn Building, Oxford Rd, Manchester");
		venue1.setPostcode("M13 9PL");
		Venue venue2 = new Venue();
    	venue2.setId(1);
		venue2.setCapacity(220);
		venue2.setName("Engineering Building");
		venue2.setAddress("Kilburn Building, Oxford Rd, Manchester");
		venue2.setPostcode("M13 9QL");
		venues.add(venue2);		
		
		when(venueService.findAll()).thenReturn(venues);
		
		Event mockEvent = new Event(1, "Test Event", LocalDate.of(2024, 07, 7), LocalTime.of(9, 0),venue1,"Best event");
		when(eventService.findById(1L)).thenReturn(Optional.of(mockEvent));

		mvc.perform(post("/events/update/1")
	    		.param("name", "New test")
                .param("venue.id", "2")
                .param("date", "")
                .with(csrf()))
		.andExpect(status().is3xxRedirection())
		.andExpect(redirectedUrl("/events/edit/1"))
		.andExpect(flash().attributeExists("errorMessage"));

			verifyNoInteractions(eventService);

	    
    }
	
	@Test
    @WithMockUser(roles = {"ADMIN", "ADMINISTRATOR"})
    public void testUpdateNameLongError() throws Exception {
		List<Venue> venues = new ArrayList<>();
    	Venue venue1 = new Venue();
    	venue1.setId(1);
		venue1.setCapacity(120);
		venue1.setName("Kilburn Building");
		venue1.setAddress("Kilburn Building, Oxford Rd, Manchester");
		venue1.setPostcode("M13 9PL");
		Venue venue2 = new Venue();
    	venue2.setId(1);
		venue2.setCapacity(220);
		venue2.setName("Engineering Building");
		venue2.setAddress("Kilburn Building, Oxford Rd, Manchester");
		venue2.setPostcode("M13 9QL");
		venues.add(venue2);		
		String newName = "";
		for(int i = 0; i<=255; i++) {
			newName += 'c';//Making a name longer than 255 characters
		}
		
		when(venueService.findAll()).thenReturn(venues);
		
		Event mockEvent = new Event(1, "Test Event", LocalDate.of(2024, 07, 7), LocalTime.of(9, 0),venue1,"Best event");
		when(eventService.findById(1L)).thenReturn(Optional.of(mockEvent));

		mvc.perform(post("/events/update/1")
	    		.param("name", newName)
                .param("venue.id", "2")
                .param("date", "2024-12-07")
                .with(csrf()))
		.andExpect(status().is3xxRedirection())
		.andExpect(redirectedUrl("/events/edit/1"))
		.andExpect(flash().attributeExists("errorMessage"));

			verifyNoInteractions(eventService);

	    
    }
	
	@Test
    @WithMockUser(roles = {"ADMIN", "ADMINISTRATOR"})
    public void testUpdateDescriptionLongError() throws Exception {
		List<Venue> venues = new ArrayList<>();
    	Venue venue1 = new Venue();
    	venue1.setId(1);
		venue1.setCapacity(120);
		venue1.setName("Kilburn Building");
		venue1.setAddress("Kilburn Building, Oxford Rd, Manchester");
		venue1.setPostcode("M13 9PL");
		Venue venue2 = new Venue();
    	venue2.setId(1);
		venue2.setCapacity(220);
		venue2.setName("Engineering Building");
		venue2.setAddress("Kilburn Building, Oxford Rd, Manchester");
		venue2.setPostcode("M13 9QL");
		venues.add(venue2);		
		String newDescription = "";
		for(int i = 0; i<=500; i++) {
			newDescription += 'c';//Making a description longer than 500 characters
		}
		
		when(venueService.findAll()).thenReturn(venues);
		
		Event mockEvent = new Event(1, "Test Event", LocalDate.of(2024, 07, 7), LocalTime.of(9, 0),venue1,"Best event");
		when(eventService.findById(1L)).thenReturn(Optional.of(mockEvent));

		mvc.perform(post("/events/update/1")
	    		.param("name", "Test Name")
                .param("venue.id", "2")
                .param("date", "2024-12-07")
                .param("description", newDescription)
                .with(csrf()))
		.andExpect(status().is3xxRedirection())
		.andExpect(redirectedUrl("/events/edit/1"))
		.andExpect(flash().attributeExists("errorMessage"));

			verifyNoInteractions(eventService);

	    
    }
	

    @Test	
    @WithMockUser(roles = {"ADMIN", "ADMINISTRATOR"})
    public void deleteEventAsAdmin() throws Exception {
	    long eventId = 1L;
	    Venue venue1 = new Venue();
		venue1.setCapacity(120);
		venue1.setName("Kilburn Building");
		venue1.setAddress("Kilburn Building, Oxford Rd, Manchester");
		venue1.setPostcode("M13 9PL");
		venue1.setId(1);
		venueService.save(venue1);
        Event mockEvent =(new Event(1, "Test Event", LocalDate.of(2024, 10, 7), LocalTime.of(9, 0),venue1,"Test description")); 
        
        eventService.save(mockEvent);

        when(eventService.findById(eventId)).thenReturn(Optional.of(mockEvent));
	    
	    mvc.perform(post("/events/deleted")
	    		.param("eventID", "1")
	            .with(csrf()))
	    .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/events"));   
    }
    
    @Test
	@WithMockUser(roles = "ADMIN")
	public void getDeleteFormAsAdminShowsForm() throws Exception {
	    long venueId = 1L;	
	    long eventId = 1L;
	    Venue venue1 = new Venue();
		venue1.setCapacity(120);
		venue1.setName("Kilburn Building");
		venue1.setAddress("Kilburn Building, Oxford Rd, Manchester");
		venue1.setPostcode("M13 9PL");
		venue1.setId(1);
		venueService.save(venue1);
        Event mockEvent =(new Event(1, "Test Event", LocalDate.of(2024, 10, 7), LocalTime.of(9, 0),venue1,"Test description")); 
        
        eventService.save(mockEvent);

        when(eventService.findById(eventId)).thenReturn(Optional.of(mockEvent));
	    mvc.perform(get("/events/delete")
	    		.param("id", "1")
	            .with(csrf()))
	            .andExpect(status().isOk())
	                    .andExpect(view().name("events/delete_event"))
	                    .andExpect(model().attribute("id", "1"));
	}	
}
