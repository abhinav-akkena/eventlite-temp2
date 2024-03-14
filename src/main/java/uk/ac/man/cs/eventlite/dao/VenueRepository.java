package uk.ac.man.cs.eventlite.dao;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

public interface VenueRepository extends CrudRepository<Venue, Long> {
	public Iterable<Venue> findAll();
	
	public Iterable<Venue> findAllByOrderByNameAsc();

	public Iterable<Venue> findByNameLike(String name);
}
