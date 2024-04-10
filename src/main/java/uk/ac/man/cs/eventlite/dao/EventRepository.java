package uk.ac.man.cs.eventlite.dao;
import org.springframework.data.repository.CrudRepository;

import java.time.LocalDate;
import java.util.Optional;


import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

public interface EventRepository extends CrudRepository<Event, Long> {

	public Iterable<Event> findAll();

	public Event save(Event e);
	
	public Iterable<Event> findAllByOrderByDateAscTimeAsc();
	
	public void deleteById(long id);

    @Override
    Optional<Event> findById(Long id);
	
	public Iterable<Event> findByNameLike(String name);
	
	public Iterable<Event> findByNameLikeAndDateBefore(String name, LocalDate date);
	
	public Iterable<Event> findByVenueByDateAscTimeAsc(Venue venue);
	
}
