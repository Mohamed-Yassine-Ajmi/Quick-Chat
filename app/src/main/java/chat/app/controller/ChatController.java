package chat.app.controller;

import chat.app.model.Message;
import chat.app.model.User;
import chat.app.exception.InvalidMessageException;
import chat.app.repository.jpa.UserRepository;
import chat.app.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/messages")
@RequiredArgsConstructor
public class ChatController {

    private final MessageService messageService;
    private final SimpMessagingTemplate messagingTemplate;
    // ↑ Spring's tool for pushing messages to specific users via WebSocket
    // No need to create it — Spring provides it automatically when WebSocket is configured

    private final UserRepository userRepository;
    // ↑ needed to convert email → User object (to get the sender's ID)

    // ─────────────────────────────────────────
    // 1. WEBSOCKET — Send a message
    // Frontend sends to: /app/chat.send
    // ─────────────────────────────────────────
    @MessageMapping("/chat.send")
    public void sendMessage(Message message, Principal principal) {
        // Principal = the authenticated WebSocket user (set in WebSocketAuthConfig)
        // principal.getName() = sender's EMAIL

        // Step 1 — Get sender from DB using email
        // We DON'T trust senderId from frontend — we set it ourselves from the token!
        User sender = userRepository.findByEmail(principal.getName()).get();
        message.setSenderId(sender.getId()); // override whatever frontend sent

        try {
            // Step 2 — Save message to MongoDB
            Message saved = messageService.sendMessage(message);

            // We must use the user's EMAIL for WebSockets because the STOMP Principal 
            // is configured to use the email as its identity in WebSocketAuthConfig.java
            User receiver = userRepository.findById(message.getReceiverId()).orElse(null);

            // Step 3 — Push to RECEIVER's private channel
            if (receiver != null) {
                messagingTemplate.convertAndSendToUser(
                        receiver.getEmail(),                      // use EMAIL, not ID
                        "/queue/messages",                        // their channel
                        saved                                     // the message data
                );
            }

            // Step 4 — Push back to SENDER too
            messagingTemplate.convertAndSendToUser(
                    sender.getEmail(),               // use EMAIL, not ID
                    "/queue/messages",               // same channel
                    saved
            );

        } catch (InvalidMessageException e) {
            // push error back to sender only
            messagingTemplate.convertAndSendToUser(
                    sender.getEmail(),
                    "/queue/errors",   // separate error channel
                    e.getMessage()
            );
        }
    }

    // ─────────────────────────────────────────
    // 2. REST — Get conversation history
    // GET /messages/{userId}?page=0
    // ─────────────────────────────────────────
    @GetMapping("/{userId}")
    public ResponseEntity<?> getConversation(
            @PathVariable Long userId,
            // ↑ the OTHER user's ID (from the URL)
            @RequestParam(defaultValue = "0") int page,
            // ↑ pagination: page=0 is latest, page=1 is older, etc.
            Authentication authentication) {

        // get current logged-in user from JWT
        User currentUser = (User) authentication.getPrincipal();

        // load conversation between current user and the other user
        List<Message> messages = messageService.getConversation(
                currentUser.getId(), // current user
                userId,              // the other user
                page
        );

        return ResponseEntity.ok(messages);
    }

    // ─────────────────────────────────────────
    // 3. REST — Mark messages as read
    // PUT /messages/read/{senderId}
    // Called when user OPENS a conversation
    // ─────────────────────────────────────────
    @PutMapping("/read/{senderId}")
    public ResponseEntity<?> markAsRead(
            @PathVariable Long senderId,
            // ↑ whose messages to mark as read (the other person's ID)
            Authentication authentication) {

        User currentUser = (User) authentication.getPrincipal();

        // get the conversation (page 0 = latest messages)
        List<Message> messages = messageService.getConversation(
                currentUser.getId(),
                senderId,
                0
        );

        // mark them all as read
        messageService.markAsRead(messages);

        // Notify the SENDER that their messages have been read (real-time read receipts)
        User sender = userRepository.findById(senderId).orElse(null);
        if (sender != null) {
            // Send a read-receipt event to the sender via WebSocket
            messagingTemplate.convertAndSendToUser(
                    sender.getEmail(),
                    "/queue/read-receipt",
                    java.util.Map.of(
                            "readBy", currentUser.getId(),
                            "conversationWith", currentUser.getId()
                    )
            );
        }

        return ResponseEntity.ok("Messages marked as read");
    }
}