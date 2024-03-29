package fi.bizhop.kirves;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/api");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/refresh").setAllowedOrigins("http://localhost:1234", "http://localhost:8080", "https://kirvespeli.herokuapp.com", "https://kirvespeli-web.fly.dev");
        registry.addEndpoint("/refresh").setAllowedOrigins("http://localhost:1234", "http://localhost:8080", "https://kirvespeli.herokuapp.com", "https://kirvespeli-web.fly.dev").withSockJS();
    }
}
