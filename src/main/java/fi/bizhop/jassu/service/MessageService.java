package fi.bizhop.jassu.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class MessageService {
    final
    SimpMessagingTemplate simpMessagingTemplate;

    public MessageService(SimpMessagingTemplate simpMessagingTemplate) {
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    public void send(String topic, String message) {
        simpMessagingTemplate.convertAndSend(topic, message);
    }
}
