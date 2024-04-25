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
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
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
	
	@GetMapping("/edit/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public String showUpdateForm(@PathVariable("id") long id, Model model) {
	    Venue venue = venueServices.findById(id);	
	    if (venue == null) {
	        throw new VenueNotFoundException(id);
	    }
	    model.addAttribute("venue", venue);
	    return "venues/edit_venue";
	}
	
	
	@GetMapping("{id}")
	public String showVenueDetails(@PathVariable("id") Long id, Model model) {
	    Venue venue = venueServices.findById(id);
	    if (venue == null) {
	        throw new VenueNotFoundException(id);
	    }
	    
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
	
	
	

	@PostMapping("/update/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public String updateVenue(@PathVariable("id") long id, @Valid Venue venue, BindingResult error, RedirectAttributes redirectAttributes, Model model) {
		if(error.hasErrors()) {
			String ErrorMessage= "Error: Please fix these problems : ";
			List<ObjectError> errors = error.getAllErrors();
			for (ObjectError e: errors) {
				ErrorMessage += e.getDefaultMessage() + " & ";
			}
			ErrorMessage = ErrorMessage.substring(0, ErrorMessage.length()-2);
			redirectAttributes.addFlashAttribute("errorMessage", ErrorMessage);
			return "redirect:/venues/edit/"+id;
		}
		else {
			venueServices.save(venue);
	       
		}
		return "redirect:/venues";
	}
	
	@GetMapping("/delete")
	@PreAuthorize("hasRole('ADMIN')")
	public String deleteVenuePage(@RequestParam(name = "id") String id, HttpServletRequest request, Model model) {
        model.addAttribute("id",id);
		return "venues/delete_venue";
	}
	
	@PostMapping("/deleted")
	public String deleteVenue(HttpServletRequest request, RedirectAttributes redirectAttributes) {

		
		try {		
			venueServices.deleteById(Long.parseLong(request.getParameter("venueID")));	
			return "redirect:/venues";
		}
		catch(Exception e){
		    redirectAttributes.addFlashAttribute("fail", "Venue is associated with existing events. Cannot delete.");
			return "redirect:/venues";

		}
		

		
	}
	
	@GetMapping("/add")
	@PreAuthorize("hasRole('ADMIN')")
	public String addVenuePage(HttpServletRequest request, Model model) {
		return "venues/add_venue";
	}
	
	@PostMapping("/added")
	public String addVenue(@Valid Venue venue, BindingResult error, RedirectAttributes redirectAttributes, Model model) {
			if(error.hasErrors()) {
				String ErrorMessage= "Error: Please fix these problems : ";
				List<ObjectError> errors = error.getAllErrors();
				for (ObjectError e: errors) {
					ErrorMessage += e.getDefaultMessage() + " & ";
				}
				ErrorMessage = ErrorMessage.substring(0, ErrorMessage.length()-2);
				redirectAttributes.addFlashAttribute("errorMessage", ErrorMessage);
				return "redirect:/venues/add";
			}
			else {
				venueServices.save(venue);
		       
			}
		return "redirect:/venues";
		
	}

	@GetMapping("/search")
	public String searchVenue(@RequestParam(name = "inputSearch") String searchTerm, Model model) {
		
		model.addAttribute("venues", venueServices.search(searchTerm));
		
		return "venues/index";
	}

	
}
