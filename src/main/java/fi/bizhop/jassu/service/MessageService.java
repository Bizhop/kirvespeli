package fi.bizhop.jassu.service;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MessageService {
    final SimpMessagingTemplate simpMessagingTemplate;

    public void send(String topic, String message) {
        this.simpMessagingTemplate.convertAndSend(topic, message);
    }
}
