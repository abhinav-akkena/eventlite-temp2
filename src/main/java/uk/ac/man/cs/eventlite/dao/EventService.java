package uk.ac.man.cs.eventlite.dao;

import java.util.Optional;

import uk.ac.man.cs.eventlite.entities.Event;

public interface EventService {

	public long count();

	public Iterable<Event> findAll();
	
	public Event save(Event e);
	
	public Optional<Event> findById(Long id);

	
	public void deleteById(long id);
		
	public Iterable<Event> search(String searchTerm);
}
