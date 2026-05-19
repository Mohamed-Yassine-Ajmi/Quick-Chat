package chat.app.config;

import chat.app.repository.jpa.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
@RequiredArgsConstructor
public class WebSocketEventListener {
    private final UserRepository userRepository;

    @EventListener
    public void handleConnect(SessionConnectedEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());

        // null check first!
        if (accessor.getUser() == null) return;

        String email = accessor.getUser().getName();

        userRepository.findByEmail(email).ifPresent(user -> {
            user.setOnline(true);
            userRepository.save(user);
        });
    }

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());

        // null check first!
        if (accessor.getUser() == null) return;

        String email = accessor.getUser().getName();

        userRepository.findByEmail(email).ifPresent(user -> {
            user.setOnline(false);
            userRepository.save(user);
        });
    }
}