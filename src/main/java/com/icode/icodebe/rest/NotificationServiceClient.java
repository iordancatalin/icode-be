package com.icode.icodebe.rest;

import com.icode.icodebe.rest.model.EmailConfirmationModel;
import com.icode.icodebe.rest.model.NotificationRequest;
import com.icode.icodebe.rest.model.ResetPasswordModel;
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

    public Mono<ClientResponse> sendConfirmationEmail(EmailConfirmationModel emailConfirmationModel) {
        final var url = baseURL + "/api/v1/email-confirmation";

        return WebClient.create(url)
                .post()
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(emailConfirmationModel), EmailConfirmationModel.class)
                .exchange();
    }

    public Mono<ClientResponse> sendResetPassword(ResetPasswordModel resetPasswordModel) {
        final var url = baseURL + "/api/v1/reset-password";

        return WebClient.create(url)
                .post()
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(resetPasswordModel), ResetPasswordModel.class)
                .exchange();
    }

    public Mono<ClientResponse> createAppNotification(NotificationRequest notificationRequest) {
        final var url = baseURL + "/api/v1/notification";

        return WebClient.create(url)
                .post()
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(notificationRequest), NotificationRequest.class)
                .exchange();
    }

}
