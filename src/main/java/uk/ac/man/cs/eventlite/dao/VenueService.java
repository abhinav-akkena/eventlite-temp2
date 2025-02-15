package uk.ac.man.cs.eventlite.dao;

import uk.ac.man.cs.eventlite.entities.Venue;

public interface VenueService {

	public long count();

	public Iterable<Venue> findAll();
	
	public Venue save(Venue venue);

	public Venue findById(long parseLong);
	
	public Iterable<Venue> search(String searchTerm);

	public void deleteById(long id);

}
