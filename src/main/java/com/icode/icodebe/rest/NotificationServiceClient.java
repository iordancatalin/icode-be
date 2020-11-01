package com.icode.icodebe.rest;

import com.icode.icodebe.exception.NotificationServiceException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class NotificationServiceClient {

    private final String baseURL;

    public NotificationServiceClient(@Value("${notification-service.baseURL}") String baseURL) {
        this.baseURL = baseURL;
    }

    public Mono<ClientResponse> sendConfirmationEmail(String to, String confirmationToken) {
        final var url = baseURL + "/api/v1/email-confirmation";

        return WebClient.create(url)
                .post()
                .contentType(MediaType.APPLICATION_JSON)
                .body(createBody(to, confirmationToken), EmailConfirmationModel.class)
                .exchange();
    }

    private Mono<EmailConfirmationModel> createBody(String to, String confirmationToken) {
        return Mono.just(new EmailConfirmationModel(to, confirmationToken));
    }
}
