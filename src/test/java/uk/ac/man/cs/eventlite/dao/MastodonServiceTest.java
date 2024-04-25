package uk.ac.man.cs.eventlite.dao;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import uk.ac.man.cs.eventlite.EventLite;
import uk.ac.man.cs.eventlite.config.RestClientConfig;
import uk.ac.man.cs.eventlite.entities.MastodonPost;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@ExtendWith(MockitoExtension.class)
@SpringBootTest(classes = {EventLite.class, RestClientConfig.class})
@DirtiesContext
@ActiveProfiles("test")
//@Disabled
public class MastodonServiceTest extends AbstractTransactionalJUnit4SpringContextTests{

	@Mock
    private RestTemplate restTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private MastodonServiceImpl mastodonService = new MastodonServiceImpl();
        
    @Test
    public void testFetchLastThreePosts() throws IOException {
    	String jsonData = "[{\"id\":\"112332359946672958\",\"created_at\":\"2024-04-25T14:36:25.319Z\",\"content\":\"Yo Ben make sure to put to this fire event\"}, {\"id\":\"112332347018198397\",\"created_at\":\"2024-04-25T14:33:08.045Z\",\"content\":\"sadf\"}, {\"id\":\"112332245795116306\",\"created_at\":\"2024-04-25T14:07:23.504Z\",\"content\":\"asdf\"}, {\"id\":\"112332243367494354\",\"created_at\":\"2024-04-25T14:06:46.463Z\",\"content\":\"sa\"}, {\"id\":\"112332226616197821\",\"created_at\":\"2024-04-25T14:02:30.855Z\",\"content\":\"COMP23412Showcase 03 is happening on 2024-05-09\"}]";

        ResponseEntity<String> responseEntity = new ResponseEntity<>(jsonData, HttpStatus.OK);
        
        MastodonPost[] postsArray = {
        		new MastodonPost(),
        		new MastodonPost(),
        		new MastodonPost(),
        		new MastodonPost(),
        		new MastodonPost()
        };
        		
        postsArray[0].setId("112332359946672958");
        postsArray[1].setId("112332347018198397");
        postsArray[2].setId("112332245795116306");
        postsArray[3].setId("112332243367494354");
        postsArray[4].setId("112332226616197821");
        
        postsArray[0].setCreatedAtViaString("2024-04-25T14:36:25.319Z");
        postsArray[1].setCreatedAtViaString("2024-04-25T14:33:08.045Z");
        postsArray[2].setCreatedAtViaString("2024-04-25T14:07:23.504Z");
        postsArray[3].setCreatedAtViaString("2024-04-25T14:06:46.463Z");
        postsArray[4].setCreatedAtViaString("2024-04-25T14:02:30.855Z");
        
        postsArray[0].setContent("Yo Ben make sure to put to this fire event");
        postsArray[1].setContent("sadf");
        postsArray[2].setContent("asdf");
        postsArray[3].setContent("sa");
        postsArray[4].setContent("COMP23412Showcase 03 is happening on 2024-05-09");

        Mockito.when(restTemplate.getForEntity(anyString(), eq(String.class))).thenReturn(responseEntity);
        
        List<MastodonPost> posts = mastodonService.fetchLastThreePosts();
        
        // Assert the correct number of posts returned
        assertEquals(3, posts.size());

        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date date = Date.from(Instant.parse("2024-04-25T14:07:23.504Z"));
        
        String formattedTime = timeFormat.format(date);
        String formattedDate = dateFormat.format(date);
        
        // Assert the posts are in the correct order, based on the 'created_at' date
        assertEquals("112332359946672958", posts.get(0).getId());
        assertEquals("sadf", posts.get(1).getContent());
        assertEquals(formattedTime, posts.get(2).getFormattedTime());
        assertEquals(formattedDate, posts.get(2).getFormattedDate());
        

    }
}

    