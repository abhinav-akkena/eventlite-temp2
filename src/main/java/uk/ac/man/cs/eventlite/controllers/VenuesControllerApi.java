package uk.ac.man.cs.eventlite.controllers;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import uk.ac.man.cs.eventlite.assemblers.EventModelAssembler;
import uk.ac.man.cs.eventlite.assemblers.VenueModelAssembler;
import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;
import uk.ac.man.cs.eventlite.exceptions.VenueNotFoundException;

@RestController
@RequestMapping(value = "/api/venues", produces = { MediaType.APPLICATION_JSON_VALUE, MediaTypes.HAL_JSON_VALUE })
public class VenuesControllerApi {

	private static final String NOT_FOUND_MSG = "{ \"error\": \"%s\", \"id\": %d }";

	@Autowired
	private VenueService venueService;
	
	@Autowired
	private EventService eventService;

	@Autowired
	private VenueModelAssembler venueAssembler;
	
	@Autowired
	private EventModelAssembler eventAssembler;

	@ExceptionHandler(VenueNotFoundException.class)
	public ResponseEntity<?> venueNotFoundHandler(VenueNotFoundException ex) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
				.body(String.format(NOT_FOUND_MSG, ex.getMessage(), ex.getId()));
	}

	@GetMapping("/{id}")
	public EntityModel<Venue> getVenue(@PathVariable("id") long id) {
		Venue venue = venueService.findById(id);
		
		if (venue == null) {
			throw new VenueNotFoundException(id);
		}
				
		
		EntityModel<Venue> em =  venueAssembler.toModel(venue);
		
		em.removeLinks();
		// TODO: ADD /venues/<id>/events REL HERE

		em.add(
				linkTo(methodOn(VenuesControllerApi.class).getVenue(id)).withSelfRel(),
				linkTo(methodOn(VenuesControllerApi.class).getVenue(id)).withRel("venue"),
				linkTo(methodOn(VenuesControllerApi.class).getNextThreeEvents(id)).withRel("next3events")
				);
		
		return em;
	}

	@GetMapping
	public CollectionModel<EntityModel<Venue>> getAllVenues() {
		String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
		String profileLink = baseUrl+"/api/profile/venues";
		return venueAssembler.toCollectionModel(venueService.findAll())
				.add(
						linkTo(methodOn(VenuesControllerApi.class).getAllVenues()).withSelfRel(),
						 Link.of(profileLink).withRel("profile")
						 );
	}
	
	@GetMapping("/{id}/next3events")
	public CollectionModel<EntityModel<Event>> getNextThreeEvents(@PathVariable("id") long id) {
		Venue venue = venueService.findById(id);
		
		if (venue == null) {
			throw new VenueNotFoundException(id);
		}
		
		
		return eventAssembler.toCollectionModel(eventService.getNextThreeEvents(venue))
				.add(linkTo(methodOn(VenuesControllerApi.class).getNextThreeEvents(id)).withSelfRel());
	}
}
