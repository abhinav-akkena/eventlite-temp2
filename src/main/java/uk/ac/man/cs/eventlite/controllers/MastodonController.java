package uk.ac.man.cs.eventlite.controllers;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/events")
public class MastodonController {

    private final RestTemplate restTemplate;

    @Value("${mastodon.access-token}")
    private String accessToken;

    @Value("${mastodon.instance.url}")
    private String mastodonInstanceUrl;

    public MastodonController() {
        this.restTemplate = new RestTemplate();
    }

    private ResponseEntity<String> postStatus(String status) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBearerAuth(accessToken);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("status", status);

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(map, headers);
        return restTemplate.postForEntity(mastodonInstanceUrl + "/api/v1/statuses", entity, String.class);
    }

    @PostMapping("/share-on-mastodon")
    @PreAuthorize("hasRole('ADMIN')")
    public String shareOnMastodon(@RequestParam("status") String status, @RequestParam("eventId") Long eventId, RedirectAttributes redirectAttributes) {
        try {
            ResponseEntity<String> response = postStatus(status);
            redirectAttributes.addFlashAttribute("successMessage", "Post published successfully on Mastodon.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to publish post: " + e.getMessage());
        }

        return "redirect:/events/" + eventId;
    }
}
