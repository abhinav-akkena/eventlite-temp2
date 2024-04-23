package uk.ac.man.cs.eventlite.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;

import uk.ac.man.cs.eventlite.EventLite;
import uk.ac.man.cs.eventlite.entities.Event;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = EventLite.class)
@DirtiesContext
@ActiveProfiles("test")
public class EventServiceTest extends AbstractTransactionalJUnit4SpringContextTests {

	@Autowired
	private EventService eventService;

	// This class is here as a starter for testing any custom methods within the
	// EventService. Note: It is currently @Disabled!
	
	@Test
	public void testSave() {
		Event event = new Event();
		event.setName("Event 1");
		event.setDescription("Best event of the year");
			
		
		long initialCount = eventService.count();
		eventService.save(event);
		assertEquals(eventService.count(), initialCount+1);
	}
	
	@Test
	public void testDeleteById() {
		Event event = new Event();
		event.setName("Event 1");
		event.setDescription("Best event of the year");
		
		
		long initialCount = eventService.count();
		event = eventService.save(event);
		assertEquals(eventService.count(), initialCount+1);
		
		eventService.deleteById(event.getId());
		assertEquals(eventService.count(), initialCount);
	}
}
