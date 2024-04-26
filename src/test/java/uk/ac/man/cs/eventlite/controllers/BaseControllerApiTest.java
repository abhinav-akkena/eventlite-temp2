package uk.ac.man.cs.eventlite.controllers;

import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.handler;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.client.RestTemplateAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import uk.ac.man.cs.eventlite.config.Security;

@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@WebMvcTest(BaseControllerApi.class)
@Import({Security.class, BaseControllerApi.class})
@ContextConfiguration(classes = RestTemplateAutoConfiguration.class)
public class BaseControllerApiTest {

    @Autowired
    private MockMvc mvc;
    

    @Test
    public void getApiRootTest() throws Exception {
        mvc.perform(get("/api").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(handler().methodName("getApiRoot"))
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$._links.venues.href", endsWith("/api/venues")))
            .andExpect(jsonPath("$._links.events.href", endsWith("/api/events")))
            .andExpect(jsonPath("$._links.profile.href", endsWith("/api/profile")));
    }
}
