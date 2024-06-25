package by.st.resttemplateexercise.configuration;

import by.st.resttemplateexercise.model.Password;
import by.st.resttemplateexercise.model.SessionId;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfiguration {
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
    @Bean
    public Password password(){
        return new Password();
    }
    @Bean
    public SessionId sessionId(){
        return new SessionId();
    }
}
