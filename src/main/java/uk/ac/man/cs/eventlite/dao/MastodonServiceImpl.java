package uk.ac.man.cs.eventlite.dao;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import uk.ac.man.cs.eventlite.entities.MastodonPost;

@Service
public class MastodonServiceImpl implements MastodonService {

	@Autowired
    private RestTemplate restTemplate;

    @Value("${mastodon.access-token}")
    private String accessToken;

    @Value("${mastodon.instance.url}")
    private String mastodonInstanceUrl;

    @Value("${mastodon.user-id}")
    private String mastodonUserId;
    
    @Override
    public List<MastodonPost> fetchLastThreePosts() {
        String url = mastodonInstanceUrl + "/api/v1/accounts/" + mastodonUserId + "/statuses";

        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        ObjectMapper mapper = new ObjectMapper();
        try {
            MastodonPost[] posts = mapper.readValue(response.getBody(), MastodonPost[].class);
            
            //Sorts by date
            Arrays.sort(posts, new Comparator<MastodonPost>() {
                @Override
                public int compare(MastodonPost p1, MastodonPost p2) {
                    return p2.getCreatedAt().compareTo(p1.getCreatedAt());
                }
            });
            
            return Arrays.asList(Arrays.copyOfRange(posts, 0, 3));
        } 
        catch (Exception e) {
            e.printStackTrace();
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
    }
}
