package uk.ac.man.cs.eventlite.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Iterator;

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
import uk.ac.man.cs.eventlite.entities.Venue;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = EventLite.class)
@DirtiesContext
@ActiveProfiles("test")
//@Disabled
public class VenueServiceTest extends AbstractTransactionalJUnit4SpringContextTests {

	@Autowired
	private VenueService venueService;

	// This class is here as a starter for testing any custom methods within the
	// VenueService. Note: It is currently @Disabled!
	
	
	@Test
	public void testSave() {
		Venue venue = new Venue();
		venue.setName("Academy 1");
		venue.setCapacity(500);
		
		
		long initialCount = venueService.count();
		venueService.save(venue);
		assertEquals(venueService.count(), initialCount+1);
	}
	
	@Test
	public void testOrderingOfVenuesWhenFindAll() {
		Venue venue1 = new Venue();
		venue1.setName("C");
		venue1.setCapacity(500);
		Venue venue2 = new Venue();
		venue2.setName("A");
		venue2.setCapacity(500);
		Venue venue3 = new Venue();
		venue3.setName("B");
		venue3.setCapacity(500);
		
		venueService.save(venue1);
		venueService.save(venue2);
		venueService.save(venue3);
		
		System.out.println(venueService.count());
		
		Iterator<Venue> venues = (venueService.findAll()).iterator();
		String lastStr = "";
		
		while(venues.hasNext()) {
			Venue next = venues.next();
			assertTrue(next.getName().compareTo(lastStr)>=0);
		}
		
		
	}
	
	@Test
	public void testFindAllByDateAscNameAsc() {
		Venue venue = new Venue();
		venue.setName("Academy 1");
		
		venue.setCapacity(500);
		
		
		long initialCount = venueService.count();
		venueService.save(venue);
		assertEquals(venueService.count(), initialCount+1);
	}
	
	
	@Test
	public void testVenueLongLatSet1() {
		Venue venue = new Venue();
		venue.setId(1);
		venue.setName("Academy 1");
		venue.setCapacity(500);
		venue.setAddress("Manchester University Students Union, Oxford Rd, Manchester");
		venue.setPostcode("M13 9PR");
		
		venueService.save(venue);
		
		venue = venueService.findById(1);
		
		double expectedLong = -2.231285012788957;
		double expectedLat = 53.4632662088948;
		
		double distance = Math.sqrt(Math.pow(expectedLong-venue.getLongitude(),2) + Math.pow(expectedLong-venue.getLongitude(),2));
		
		assertTrue(distance<4);
	}
	
	@Test
	public void testVenueLongLatSet2() {
		Venue venue = new Venue();
		venue.setId(1);
		venue.setName("O2 Apollo");
		venue.setCapacity(500);
		venue.setAddress("O2 Apollo, Stockport Rd, Manchester");
		venue.setPostcode("M12 6AP");
		
		venueService.save(venue);
		
		venue = venueService.findById(1);
		
		double expectedLong =-2.3045419;
		double expectedLat = 53.4695392;
		
		double distance = Math.sqrt(Math.pow(expectedLong-venue.getLongitude(),2) + Math.pow(expectedLong-venue.getLongitude(),2));
		
		assertTrue(distance<4);
	}
	
	@Test
	public void testVenueLongLatSet3() {
		Venue venue = new Venue();
		venue.setId(1);
		venue.setName("Lyceum Theatre");
		venue.setCapacity(500);
		venue.setAddress("Lyceum Theatre, Heath St, Crewe ");
		venue.setPostcode("CW1 2DA");
		
		venueService.save(venue);
		
		venue = venueService.findById(1);
		
		double expectedLong = -2.4403216;
		double expectedLat = 53.099048;
		
		double distance = Math.sqrt(Math.pow(expectedLong-venue.getLongitude(),2) + Math.pow(expectedLong-venue.getLongitude(),2));
		
		assertTrue(distance<4);
	}
	
	@Test
	public void testVenueLongLatSetWhenAddressNotSet() {
		Venue venue = new Venue();
		venue.setName("Academy 1");
		venue.setCapacity(500);
		venue.setAddress("");
		venue.setPostcode("");
		
		long initialCount = venueService.count();
		venueService.save(venue);
		assertEquals(venueService.count(), initialCount+1);
	}
}
