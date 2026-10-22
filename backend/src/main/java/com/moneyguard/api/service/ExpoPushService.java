package com.moneyguard.api.service;

import com.moneyguard.api.config.AppProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Service
public class ExpoPushService {

    private static final Logger log = LoggerFactory.getLogger(ExpoPushService.class);

    private final AppProperties props;
    private final WebClient webClient;

    public ExpoPushService(AppProperties props, WebClient.Builder builder) {
        this.props = props;
        this.webClient = builder.build();
    }

    public void send(List<String> expoTokens, String title, String body, Map<String, Object> data) {
        if (!props.getExpo().isEnabled()) {
            log.info("[EXPO_DISABLED] would send to={} title={} body={}", expoTokens, title, body);
            return;
        }
        var payload = expoTokens.stream().map(t -> Map.of(
                "to", t,
                "title", title,
                "body", body,
                "data", data == null ? Map.of() : data
        )).toList();

        var req = webClient.post()
                .uri("https://exp.host/--/api/v2/push/send")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload);

        if (props.getExpo().getAccessToken() != null && !props.getExpo().getAccessToken().isBlank()) {
            req = req.header("Authorization", "Bearer " + props.getExpo().getAccessToken());
        }

        String resp = req.retrieve().bodyToMono(String.class)
                .doOnError(e -> log.error("Expo push failed", e))
                .block();

        log.info("Expo push response={}", resp);
    }
}
