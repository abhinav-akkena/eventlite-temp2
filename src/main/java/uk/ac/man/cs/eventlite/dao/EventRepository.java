package uk.ac.man.cs.eventlite.dao;
import org.springframework.data.repository.CrudRepository;
import java.util.Optional;


import uk.ac.man.cs.eventlite.entities.Event;

public interface EventRepository extends CrudRepository<Event, Long> {

	public Iterable<Event> findAll();

	public Event save(Event e);
	
	public Iterable<Event> findAllByOrderByDateAscTimeAsc();

   @Override
    Optional<Event> findById(Long id);
	
	public Iterable<Event> findByNameLike(String name);
}
