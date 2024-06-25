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

    public void setSessionIdFromResponseHeaders(Map<String, List<String>> headers) {
        for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
            if ("Cookie".equalsIgnoreCase(entry.getKey())) {
                for (String cookie : entry.getValue()) {
                    if (cookie.startsWith("JSESSIONID")) {
                        sessionId.setSessionId(cookie.split(";")[0]);
                        log.info("Session ID setSessionIdFromResponseHeaders: {}", this.sessionId);
                        break;
                    }
                }
            }
        }
    }

    public String extractPart(String responseBody) {
        String[] parts = responseBody.split(":");
        log.info("{} responseBody", responseBody);
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
        log.info("Session ID: {}", sessionId.getSessionId());
        return List.of(users);
    }

    public String createUser(User user) {
        HttpEntity<User> request = getUserHttpEntity(user);
        ResponseEntity<String> response = restTemplate.postForEntity(URL, request, String.class);
        String responseBody = response.getBody();
        password.setFirstPart(extractPart(responseBody));
        log.info("{} firstPart", password.getFirstPart());
        return password.getFirstPart();
    }

    public String updateUser(User user) {
        HttpEntity<User> request = getUserHttpEntity(user);
        /*ResponseEntity<String> response = restTemplate.exchange(URL, HttpMethod.PUT, request, String.class);*/
        ResponseEntity<String> response = restTemplate.postForEntity(URL, request, String.class);
        String responseBody = response.getBody();
        password.setSecondPart(responseBody);
        log.info("{} secondPart", password.getSecondPart());
        return password.getSecondPart();
    }

    public String deleteUser(Long id) {
        HttpHeaders headers = new HttpHeaders();
        if (sessionId.getSessionId() != null) {
            headers.add("Cookie", "JSESSIONID=" + sessionId.getSessionId());
        }
        headers.add("Content-Type", "application/json");
        headers.setAccessControlRequestMethod(HttpMethod.DELETE);

        HttpEntity<String> request = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(URL + "/" + id,HttpMethod.DELETE,request,String.class);
        log.info(response.toString());
        String responseBody = response.getBody();
        password.setThirdPart(responseBody);
        log.info("{} thirdPart", password.getThirdPart());
        Password completePassword = new Password();
        completePassword.setFirstPart(password.getFirstPart());
        completePassword.setSecondPart(password.getSecondPart());
        completePassword.setThirdPart(password.getThirdPart());
        return completePassword.getFirstPart() + completePassword.getSecondPart() + completePassword.getThirdPart();
    }

    private HttpEntity<User> getUserHttpEntity(User user) {
        HttpHeaders headers = new HttpHeaders();
        if (sessionId.getSessionId() != null) {
            headers.add("Cookie", "JSESSIONID=" + sessionId.getSessionId());
        }
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(user, headers);
    }

    private RequestEntity<?> createRequestEntity(String url, HttpMethod method) {
        HttpHeaders headers = new HttpHeaders();
        if (sessionId != null) {
            headers.add("Cookie", "JSESSIONID=" + sessionId.getSessionId());
        }
        return new RequestEntity<>(headers, method, URI.create(url));
    }
}
