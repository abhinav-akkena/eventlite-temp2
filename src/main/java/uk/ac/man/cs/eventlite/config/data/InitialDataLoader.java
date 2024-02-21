package uk.ac.man.cs.eventlite.config.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Venue;
import uk.ac.man.cs.eventlite.entities.Event;

import java.util.ArrayList;
import java.util.Iterator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalTime;

@Configuration
@Profile("default")
public class InitialDataLoader {

	private final static Logger log = LoggerFactory.getLogger(InitialDataLoader.class);

	@Autowired
	private EventService eventService;

	@Autowired
	private VenueService venueService;
	
	private final static String DATA = "data/events.json";

	Venue venue1;
	@Bean
	CommandLineRunner initDatabase() {
		return args -> {
			if (venueService.count() > 0) {
				log.info("Database already populated with venues. Skipping venue initialization.");
			} else {
				venue1 = new Venue();
				venue1.setCapacity(500);
				venue1.setName("Academy 1");
				venue1.setId(1);
				venueService.save(venue1);;
			}

			if (eventService.count() > 0) {
				log.info("Database already populated with events. Skipping event initialization.");
			} else {
				// Build and save initial events here.
				eventService.save(new Event(1, "COMP23412 Showcase 01", LocalDate.of(2024, 05, 7), LocalTime.of(9, 0),venue1)); 
				eventService.save(new Event(2, "COMP23412 Showcase 02", LocalDate.of(2024, 05, 7), LocalTime.of(12, 0),venue1)); 
				eventService.save(new Event(3, "COMP23412 Showcase 03", LocalDate.of(2024, 05, 9), LocalTime.of(15, 0),venue1)); 
			}
			
		

		};
	}
}
