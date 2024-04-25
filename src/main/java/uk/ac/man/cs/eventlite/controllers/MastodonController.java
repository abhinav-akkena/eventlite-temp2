package uk.ac.man.cs.eventlite.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uk.ac.man.cs.eventlite.entities.MastodonPost;
import uk.ac.man.cs.eventlite.dao.MastodonService;

import java.util.List;

@Controller
@RequestMapping("/events")
public class MastodonController {

	@Autowired
    private final MastodonService mastodonService;

    @Autowired
    public MastodonController(MastodonService mastodonService) {
        this.mastodonService = mastodonService;
    }

    @PostMapping("/share-on-mastodon")
    @PreAuthorize("hasRole('ADMIN')")
    public String shareOnMastodon(@RequestParam("status") String status,
                                  @RequestParam("eventId") Long eventId,
                                  RedirectAttributes redirectAttributes) {
        try {
            mastodonService.shareStatus(status);
            redirectAttributes.addFlashAttribute("successMessage", "Post published successfully on Mastodon.  Content: " + status);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to publish post: " + e.getMessage());
        }

        return "redirect:/events/" + eventId;
    }
}
