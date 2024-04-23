package uk.ac.man.cs.eventlite.dao;

import java.util.Optional;

import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

public interface EventService {

	public long count();

	public Iterable<Event> findAll();
	
	public Iterable<Event> findPast();
	
	public Iterable<Event> findFuture();
	
	public Event save(Event e);
	
	public Optional<Event> findById(Long id);

	
	public void deleteById(long id);

	public Iterable<Event> searchPast(String searchTerm);
	
	public Iterable<Event> searchFuture(String searchTerm);
	
	public Iterable<Event> getNextThreeEvents(Venue venue);
	
	public Iterable<Event> getEventsForVenue(Venue venue);
}
