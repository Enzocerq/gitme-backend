package br.com.tcc.github_poc.controller;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    @Value("${github.client.id}")
    private String clientId;

    @Value("${github.client.secret}")
    private String clientSecret;

    private final RestTemplate restTemplate;

    public record TokenRequest(String code) {}

    public record GithubUser(
            String login,
            String name,
            @JsonProperty("avatar_url") String avatarUrl,
            String email
    ) {}

    public record AuthResponse(String accessToken, GithubUser user) {}

    @PostMapping("/github")
    public ResponseEntity<?> exchangeCode(@RequestBody TokenRequest body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        Map<String, String> tokenParams = Map.of(
                "client_id", clientId,
                "client_secret", clientSecret,
                "code", body.code()
        );

        ResponseEntity<Map> tokenResp = restTemplate.postForEntity(
                "https://github.com/login/oauth/access_token",
                new HttpEntity<>(tokenParams, headers),
                Map.class
        );

        if (tokenResp.getBody() == null || !tokenResp.getBody().containsKey("access_token")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Failed to exchange code — token not returned by GitHub"));
        }

        String accessToken = (String) tokenResp.getBody().get("access_token");

        HttpHeaders userHeaders = new HttpHeaders();
        userHeaders.setBearerAuth(accessToken);
        userHeaders.set("User-Agent", "GitHealth-TCC");

        ResponseEntity<GithubUser> userResp = restTemplate.exchange(
                "https://api.github.com/user",
                HttpMethod.GET,
                new HttpEntity<>(userHeaders),
                GithubUser.class
        );

        return ResponseEntity.ok(new AuthResponse(accessToken, userResp.getBody()));
    }
}
