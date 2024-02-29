package uk.ac.man.cs.eventlite.controllers;

import java.time.LocalDate;
import java.time.LocalTime;

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
import uk.ac.man.cs.eventlite.dao.VenueServiceImpl;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.exceptions.EventNotFoundException;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;


@Controller
@RequestMapping(value = "/events", produces = { MediaType.TEXT_HTML_VALUE })
public class EventsController {

	@Autowired
	private EventService eventService;

	@Autowired
	private VenueService venueServices;


	@ExceptionHandler(EventNotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public String eventNotFoundHandler(EventNotFoundException ex, Model model) {
		model.addAttribute("not_found_id", ex.getId());

		return "events/not_found";
	}
	

//	@GetMapping("/{id}")
//	public String getEvent(@PathVariable("id") long id, Model model) {
//		throw new EventNotFoundException(id);
//	}

	
	@GetMapping
	public String getAllEvents(Model model) {

		model.addAttribute("events", eventService.findAll());

		return "events/index";
	}
	
	@GetMapping("/add")
	@PreAuthorize("hasRole('ADMIN')")
	public String addEventPage(HttpServletRequest request, Model model) {
		Iterable<Venue> venues = venueServices.findAll();
		CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        model.addAttribute("_csrf", csrfToken);
        model.addAttribute("venues", venues);
		return "events/add_event";
	}
	
	@PostMapping("/added")
	public String addEvent(HttpServletRequest request) {
		Event event = new Event();
		Iterable<Event> events = eventService.findAll();
		long max = 0;
        for (Event ev : events) {
            if (ev.getId() > max) {
                max = ev.getId()+1;
            }
        }
        event.setId(max);
		event.setName(request.getParameter("name"));
		event.setDate(LocalDate.parse(request.getParameter("date")));
		event.setTime(LocalTime.parse(request.getParameter("time")));
		event.setVenue(venueServices.findById(Long.parseLong(request.getParameter("venue"))));
		
		event.setDescription(request.getParameter("description"));
		eventService.save(event);
		
		return "redirect:/events";
		
	}
	
	@GetMapping("/edit/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public String showUpdateForm(@PathVariable("id") long id, HttpServletRequest request, Model model) {
	    Event event = eventService.findById(id).orElseThrow(() -> new EventNotFoundException(id));
	    Iterable<Venue> venues = venueServices.findAll();
	    model.addAttribute("event", event);
	    model.addAttribute("venues", venues);
	    CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        model.addAttribute("_csrf", csrfToken);
	    
	    return "events/edit_event";
	}

	@PostMapping("/update/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public String updateEvent(@PathVariable("id") long id, HttpServletRequest request, RedirectAttributes redirectAttributes) {
	    Event event = eventService.findById(id).orElseThrow(() -> new EventNotFoundException(id));
	    
	    // Update event properties from request parameters
	    event.setName(request.getParameter("name"));
	    event.setDate(LocalDate.parse(request.getParameter("date")));
	    event.setTime(LocalTime.parse(request.getParameter("time")));
	    event.setVenue(venueServices.findById(Long.parseLong(request.getParameter("venue"))));
	    event.setDescription(request.getParameter("description"));

	    eventService.save(event);
	    redirectAttributes.addFlashAttribute("success", "Event updated successfully!");
	    
	    return "redirect:/events";
	}
	
	@GetMapping("/delete")
	@PreAuthorize("hasRole('ADMIN')")
	public String deleteEventPage(@RequestParam(name = "id") String id, HttpServletRequest request, Model model) {
		CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        model.addAttribute("_csrf", csrfToken);
        model.addAttribute("id",id);
		return "events/delete_event";
	}
	
	@PostMapping("/deleted")
	public String deleteEvent(HttpServletRequest request) {
		eventService.deleteById(Long.parseLong(request.getParameter("eventID")));		
		return "redirect:/events";

		
	}
	
	
	@GetMapping("{eventId}")
	public String showEventDetails(@PathVariable("eventId") Long eventId, Model model) {
	    Event event = eventService.findById(eventId).orElseThrow(() -> new EventNotFoundException(eventId));
	    model.addAttribute("event", event);
	    return "events/eventDetails"; 
	}

	
	
	@GetMapping("/search")
	public String searchEvent(@RequestParam(name = "inputSearch") String searchTerm, Model model) {
		
		model.addAttribute("events", eventService.search(searchTerm));
		
		return "events/index";
	}

}
