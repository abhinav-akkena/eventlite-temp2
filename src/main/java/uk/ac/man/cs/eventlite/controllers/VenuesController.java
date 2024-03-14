package uk.ac.man.cs.eventlite.controllers;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.web.server.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;
import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;
import uk.ac.man.cs.eventlite.exceptions.EventNotFoundException;
import uk.ac.man.cs.eventlite.exceptions.VenueNotFoundException;
import uk.ac.man.cs.eventlite.entities.Venue;


@Controller
@RequestMapping(value = "/venues", produces = { MediaType.TEXT_HTML_VALUE })
public class VenuesController {

	@Autowired
	private VenueService venueServices;

	@Autowired
	private EventService eventService;


	@ExceptionHandler(VenueNotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public String eventNotFoundHandler(VenueNotFoundException ex, Model model) {
		model.addAttribute("not_found_id", ex.getId());

		return "venues/not_found";
	}
	
	@GetMapping
	public String getAllVenues(Model model) {
		model.addAttribute("venues", venueServices.findAll());
		return "venues/index";
	}
	
	
	@GetMapping("{id}")
	public String showVenueDetails(@PathVariable("id") Long id, Model model) {
	    Venue venue = venueServices.findById(id);
	    model.addAttribute("venue", venue);
	    
	    List<Event> required = new ArrayList<Event>();
	    
	    Iterable<Event> upcoming = eventService.findFuture();
	    
        for (Event e : upcoming) {
            if(e.getId() == id) {
                    required.add(e);
            }
        }
        model.addAttribute("events", required);
	    
	    return "venues/venueDetails"; 
	}
	
	
	

	@GetMapping("/search")
	public String searchVenue(@RequestParam(name = "inputSearch") String searchTerm, Model model) {
		
		model.addAttribute("venues", venueServices.search(searchTerm));
		
		return "venues/index";
	}

	
}
