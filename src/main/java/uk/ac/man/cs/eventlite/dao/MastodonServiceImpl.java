package uk.ac.man.cs.eventlite.dao;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import uk.ac.man.cs.eventlite.entities.MastodonPost;

@Service
public class MastodonServiceImpl implements MastodonService {

    private final RestTemplate restTemplate;

    @Value("${mastodon.access-token}")
    private String accessToken;

    @Value("${mastodon.instance.url}")
    private String mastodonInstanceUrl;

    public MastodonServiceImpl() {
        this.restTemplate = new RestTemplate();
    }

    @Override
    public List<MastodonPost> fetchLastThreePosts() {
        String url = mastodonInstanceUrl + "/api/v1/timelines/public"; // Modify according to the actual API endpoint
        ResponseEntity<MastodonPost[]> response = restTemplate.getForEntity(url, MastodonPost[].class);
        
        System.out.println(response.toString());
        
        if (response.getStatusCode() == HttpStatus.OK) {
            return Arrays.stream(response.getBody())
                         .filter(post -> post.getCreatedAt() != null) // Filter out posts with null createdAt
                         .sorted(Comparator.comparing(MastodonPost::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                         .limit(3)
                         .collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }


    @Override
    public void shareStatus(String status) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBearerAuth(accessToken);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("status", status);

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(map, headers);
        
        ResponseEntity<String> response = restTemplate.postForEntity(mastodonInstanceUrl + "/api/v1/statuses", entity, String.class);
        
        if (response.getStatusCode() != HttpStatus.CREATED) {
            throw new Exception("Failed to post status to Mastodon: " + response.toString());
        }
    }
}
