package uk.ac.man.cs.eventlite.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;

import uk.ac.man.cs.eventlite.EventLite;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = EventLite.class)
@DirtiesContext
@ActiveProfiles("test")
public class EventServiceTestWithMocking extends AbstractTransactionalJUnit4SpringContextTests {

	@Autowired
	private EventService eventService;
	
	@MockBean
	private EventRepository eventRepository;

	// This class is here as a starter for testing any custom methods within the
	// EventService. Note: It is currently @Disabled!

	
	@Test
	public void testSearchFuture() throws Exception{
		Venue venue = new Venue();
		Event e1 = new Event(1, "Kilburn 1", LocalDate.now().minusDays(1), LocalTime.of(12, 0), venue, ""); 
		Event e2 = new Event(1, "Kilburn 2", LocalDate.now().minusDays(1), LocalTime.of(13, 0), venue, "");
		Event e3 = new Event(1, "Kilburn 3", LocalDate.now().plusDays(1), LocalTime.of(12, 0), venue, "");
		Event e4 = new Event(1, "Kilburn 4", LocalDate.now().plusDays(1), LocalTime.of(13, 0), venue, "");
		
		List<Event> returnedEvents = new ArrayList<Event>();
		returnedEvents.add(e1);
		returnedEvents.add(e2);
		returnedEvents.add(e3);
		returnedEvents.add(e4);
		
		when(eventRepository.findByNameLike("%Kilburn%")).thenReturn(returnedEvents);
		
		List<Event> result = (List<Event>) eventService.searchFuture("Kilburn");
		
		assertTrue(result.contains(e3));
		assertTrue(result.contains(e4));
		
	}
	
	@Test
	public void testSearchPast() throws Exception{
		Venue venue = new Venue();
		Event e1 = new Event(1, "Kilburn 1", LocalDate.now().minusDays(1), LocalTime.of(12, 0), venue, ""); 
		Event e2 = new Event(1, "Kilburn 2", LocalDate.now().minusDays(1), LocalTime.of(13, 0), venue, "");
		
		List<Event> returnedEvents = new ArrayList<Event>();
		returnedEvents.add(e1);
		returnedEvents.add(e2);
		
		when(eventRepository.findByNameLikeAndDateBefore(eq("%Kilburn%"), any(LocalDate.class))).thenReturn(returnedEvents);
		
		List<Event> result = (List<Event>) eventService.searchPast("Kilburn");
		
		assertTrue(result.contains(e1));
		assertTrue(result.contains(e2));
		
	}
	
	@Test
	public void testGetNextThreeEvents() {
		Venue venue = new Venue();
		Event e1 = new Event(1, "Kilburn 1", LocalDate.now().minusDays(1), LocalTime.of(12, 0), venue, ""); 
		Event e2 = new Event(1, "Kilburn 2", LocalDate.now().minusDays(1), LocalTime.of(13, 0), venue, "");
		Event e3 = new Event(1, "Kilburn 3", LocalDate.now().plusDays(1), LocalTime.of(12, 0), venue, "");
		Event e4 = new Event(1, "Kilburn 4", LocalDate.now().plusDays(1), LocalTime.of(13, 0), venue, "");
		Event e5 = new Event(1, "Kilburn 4", LocalDate.now().plusDays(2), LocalTime.of(12, 0), venue, "");
		
		List<Event> returnedEvents = new ArrayList<Event>();
		returnedEvents.add(e1);
		returnedEvents.add(e2);
		returnedEvents.add(e3);
		returnedEvents.add(e4);
		returnedEvents.add(e5);
		
		when(eventRepository.findByVenueOrderByDateAscTimeAsc(venue)).thenReturn(returnedEvents);
		
		List<Event> result = (List<Event>) eventService.getNextThreeEvents(venue);
		
		assertTrue(result.contains(e3));
		assertTrue(result.contains(e4));
		assertTrue(result.contains(e5));
		
		
	}
}
