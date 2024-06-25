package by.st.resttemplateexercise.service;

import by.st.resttemplateexercise.model.Password;
import by.st.resttemplateexercise.model.SessionId;
import by.st.resttemplateexercise.model.User;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;

import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.List;
import java.util.Map;

@Service
@Log4j2
public class UserService {

    private final String URL = "http://94.198.50.185:7081/api/users";

    private final RestTemplate restTemplate;
    private final Password password;
    private final SessionId sessionId;


    @Autowired
    public UserService(RestTemplate restTemplate, Password password, SessionId sessionId) {
        this.restTemplate = restTemplate;
        this.password = password;
        this.sessionId = sessionId;
    }

    public  void setSessionIdFromResponseHeaders(Map<String, List<String>> headers) {
        for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
            if ("Set-Cookie".equalsIgnoreCase(entry.getKey())) {
                for (String cookie : entry.getValue()) {
                    if (cookie.startsWith("JSESSIONID")) {
                        sessionId.setSessionId(cookie.split(";")[0]);
                        log.info("Session ID setSessionIdFromResponseHeaders: " + this.sessionId);
                        break;
                    }
                }
            }
        }
    }

    public  String extractPart(String responseBody) {
        String[] parts = responseBody.split(":");
        log.info(responseBody + " responseBody");
        if (parts.length > 0) {
            return parts[0].trim();
        } else {
            return "";
        }
    }

    public List<User> getAllUsers() {
        RequestEntity<?> requestEntity = createRequestEntity(URL, HttpMethod.GET);
        ResponseEntity<User[]> responseEntity = restTemplate.exchange(requestEntity, User[].class);
        User[] users = responseEntity.getBody();
        HttpHeaders responseHeaders = responseEntity.getHeaders();
        setSessionIdFromResponseHeaders(responseHeaders);
        log.info("Session ID: " + sessionId.getSessionId());
        return List.of(users);
    }

    public String createUser(User user) {
        HttpHeaders headers = new HttpHeaders();
        if (sessionId.getSessionId() != null) {
            headers.add("Cookie", "JSESSIONID=" + sessionId.getSessionId());
        }
        HttpEntity<User> request = new HttpEntity<>(user, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(URL, request, String.class);
        String responseBody = response.getBody();
        log.info(responseBody);
        password.setFirstPart(extractPart(responseBody));
        log.info("{}firstPart", password.getFirstPart());
        return password.getFirstPart();
    }

    public String updateUser(User user) {
        HttpHeaders headers = new HttpHeaders();
        if (sessionId.getSessionId() != null) {
            headers.add("Cookie", "JSESSIONID=" + sessionId.getSessionId());
        }
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<User> request = new HttpEntity<>(user, headers);
        ResponseEntity<String> response = restTemplate.exchange(URL, HttpMethod.PUT, request, String.class);
        if (response.getStatusCode() == HttpStatus.OK) {
            String updatedUser = response.getBody();
            String secondPart = extractPart(updatedUser); // Извлекаем вторую часть кода
            log.info("Second part of code: {}", secondPart);
            return secondPart;
        } else {
            log.error("Error updating user: {}", response.getBody());
            return null;
        }
    }



    public void deleteUser(Long id) {
        RequestEntity<?> requestEntity = createRequestEntity(URL + id, HttpMethod.DELETE);
        restTemplate.exchange(requestEntity, Void.class);
    }

    private RequestEntity<?> createRequestEntity(String url, HttpMethod method) {
        HttpHeaders headers = new HttpHeaders();
        if (sessionId != null) {
            headers.add("Cookie", "JSESSIONID=" + sessionId.getSessionId());
        }
        return new RequestEntity<>(headers, method, URI.create(url));
    }
}
