package com.openclassrooms.mddapi.controller.subscription;

import com.openclassrooms.mddapi.dto.topic.TopicResponse;
import com.openclassrooms.mddapi.service.subscription.SubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import static com.openclassrooms.mddapi.config.OpenApiConfig.AUTH_COOKIE_SCHEME;

@RestController
@RequestMapping("/api/topics")
@Tag(name = "Abonnements", description = "Abonnement et désabonnement aux sujets")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    public SubscriptionController(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    @PostMapping("/{topicId}/subscribe")
    @Operation(summary = "S'abonner à un sujet")
    @SecurityRequirement(name = AUTH_COOKIE_SCHEME)
    @ApiResponse(responseCode = "201", description = "Abonnement créé")
    @ApiResponse(responseCode = "401", description = "Utilisateur non authentifié")
    @ApiResponse(responseCode = "404", description = "Sujet introuvable")
    @ApiResponse(responseCode = "409", description = "Utilisateur déjà abonné")
    // Abonne l'utilisateur connecté au thème demandé.
    public ResponseEntity<TopicResponse> subscribe(@PathVariable Long topicId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(subscriptionService.subscribe(topicId));
    }

    @DeleteMapping("/{topicId}/unsubscribe")
    @Operation(summary = "Se désabonner d'un sujet")
    @SecurityRequirement(name = AUTH_COOKIE_SCHEME)
    @ApiResponse(responseCode = "204", description = "Désabonnement effectué")
    @ApiResponse(responseCode = "401", description = "Utilisateur non authentifié")
    // Désabonne l'utilisateur connecté du thème demandé.
    public ResponseEntity<Void> unsubscribe(@PathVariable Long topicId) {
        subscriptionService.unsubscribe(topicId);
        return ResponseEntity.noContent().build();
    }
}
