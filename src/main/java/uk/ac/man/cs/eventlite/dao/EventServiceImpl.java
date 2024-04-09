package uk.ac.man.cs.eventlite.dao;

import java.io.InputStream;
import java.time.LocalDate;

import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.dao.EventRepository;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Service
public class EventServiceImpl implements EventService {

	private final static Logger log = LoggerFactory.getLogger(EventServiceImpl.class);

	@Autowired
	private EventRepository eventRepository;
	
	@Override
	public Iterable<Event> findAll(){
		return eventRepository.findAllByOrderByDateAscTimeAsc();
	}
	
	@Override
    public Iterable<Event> findPast(){
        Iterable<Event> allEvents = eventRepository.findAllByOrderByDateAscTimeAsc();
        LocalDate today = LocalDate.now();
        List<Event> res = new ArrayList<Event>();

        for (Event e : allEvents) {
                if(e.getDate().compareTo(today) < 0) {
                        res.add(e);
                }
        }
        return res;
    }
	
	@Override
    public Iterable<Event> findFuture(){
        Iterable<Event> allEvents = eventRepository.findAllByOrderByDateAscTimeAsc();
        LocalDate today = LocalDate.now();
        List<Event> res = new ArrayList<Event>();

        for (Event e : allEvents) {
                if(e.getDate().compareTo(today) >= 0) {
                        res.add(e);
                }
        }
        return res;
    }
                

	@Override
	public long count() {
		return eventRepository.count();
	}

	@Override
	public Event save(Event e) {
		return eventRepository.save(e);
	}

	@Override
	public Iterable<Event> search(String searchTerm) {
		return eventRepository.findByNameLike("%" + searchTerm + "%");
	}
	
	@Override
	public void deleteById(long id) {
		eventRepository.deleteById(id);
	}

	@Override
	public Optional<Event> findById(Long id) {
		// TODO Auto-generated method stub
		return eventRepository.findById(id);
	}

	@Override
	public Iterable<Event> searchPast(String searchTerm) {
		// TODO Auto-generated method stub
		return eventRepository.findByNameLikeAndDateBefore("%" + searchTerm + "%", LocalDate.now());
	}

	@Override
	public Iterable<Event> searchFuture(String searchTerm) {
		// TODO Auto-generated method stub
		Iterable<Event> allEvents = eventRepository.findByNameLike("%" + searchTerm + "%");
        LocalDate today = LocalDate.now();
        List<Event> res = new ArrayList<Event>();

        for (Event e : allEvents) {
                if(e.getDate().compareTo(today) >= 0) {
                        res.add(e);
                }
        }
        return res;
	}

	@Override
	public Iterable<Event> getNextThreeEvents(Long venueID) {
		// TODO Auto-generated method stub
		return null;
	}
}
