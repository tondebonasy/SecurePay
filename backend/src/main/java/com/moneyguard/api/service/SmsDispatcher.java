package com.moneyguard.api.service;

import com.moneyguard.api.config.AppProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
@org.springframework.context.annotation.Primary
public class SmsDispatcher implements SmsService {

    private static final Logger log = LoggerFactory.getLogger(SmsDispatcher.class);

    private final AppProperties props;
    private final WebClient webClient;
    private final MockSmsService mock;

    public SmsDispatcher(AppProperties props, WebClient.Builder webClientBuilder, MockSmsService mock) {
        this.props = props;
        this.webClient = webClientBuilder.build();
        this.mock = mock;
    }

    @Override
    public void send(String phone, String message) {
        String provider = props.getSms().getProvider();
        if (provider == null || provider.equalsIgnoreCase("mock")) {
            mock.send(phone, message);
            return;
        }
        if (provider.equalsIgnoreCase("twilio")) {
            sendViaTwilio(phone, message);
            return;
        }
        log.warn("Unknown SMS provider={}, falling back to mock", provider);
        mock.send(phone, message);
    }

    private void sendViaTwilio(String to, String body) {
        var tw = props.getSms().getTwilio();
        if (tw.getAccountSid() == null || tw.getAuthToken() == null || tw.getFromNumber() == null) {
            log.warn("Twilio not configured; falling back to mock");
            mock.send(to, body);
            return;
        }

        String url = "https://api.twilio.com/2010-04-01/Accounts/" + tw.getAccountSid() + "/Messages.json";
        String auth = tw.getAccountSid() + ":" + tw.getAuthToken();
        String basic = "Basic " + Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));

        var form = new LinkedMultiValueMap<String, String>();
        form.add("To", to);
        form.add("From", tw.getFromNumber());
        form.add("Body", body);

        webClient.post()
                .uri(url)
                .header("Authorization", basic)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(form))
                .retrieve()
                .bodyToMono(String.class)
                .doOnError(e -> log.error("Twilio send failed", e))
                .block();
    }
}
