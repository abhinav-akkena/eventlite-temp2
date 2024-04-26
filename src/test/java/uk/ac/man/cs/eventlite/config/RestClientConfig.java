package uk.ac.man.cs.eventlite.config;

import org.springframework.boot.autoconfigure.web.client.RestTemplateBuilderConfigurer;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.client.RestTemplate;

import uk.ac.man.cs.eventlite.dao.MastodonService;

@Profile("test")
@Configuration
public class RestClientConfig {

//    @Bean("testRestTemplate")
//    public RestTemplate restTemplate() {
//        return new RestTemplate();
//    }
	
	public RestTemplateBuilder restTemplateBuilder() {
		RestTemplateBuilder builder = new RestTemplateBuilder();
//		return restTemplateBuilderConfigurer.configure(builder);
		return builder;
	}


	
//    
//	@MockBean
//	private MastodonService mastodonService;

}
