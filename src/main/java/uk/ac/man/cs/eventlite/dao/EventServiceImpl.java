package uk.ac.man.cs.eventlite.dao;

import java.io.InputStream;
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
	public long count() {
		return eventRepository.count();
	}

	@Override
	public Event save(Event e) {
		return eventRepository.save(e);
	}
	
	@Override
	public Optional<Event> findById(Long id) {
	    return eventRepository.findById(id);
	}	
}
