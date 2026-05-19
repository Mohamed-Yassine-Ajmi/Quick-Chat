package chat.app.config;

import chat.app.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.security.Principal;

@Configuration
@RequiredArgsConstructor
public class WebSocketAuthConfig implements WebSocketMessageBrokerConfigurer {
    private final JwtUtil jwtUtil;

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {

                StompHeaderAccessor accessor = MessageHeaderAccessor
                        .getAccessor(message, StompHeaderAccessor.class);

                if (StompCommand.CONNECT.equals(accessor.getCommand())) {

                    // 1. get header
                    String header = accessor.getFirstNativeHeader("Authorization");

                    // 2. null/format check
                    if (header == null || !header.startsWith("Bearer ")) return message;

                    // 3. extract token
                    String token = header.split(" ")[1];

                    // 4. validate token
                    if (!jwtUtil.validateToken(token)) return message;

                    // 5. extract email
                    String email = jwtUtil.extractEmail(token);

                    // 6. set Principal (user identity for this WebSocket session)
                    accessor.setUser(new Principal() {
                        @Override
                        public String getName() { return email; }
                    });
                }

                return message;
            }
        });
    }
}







//package chat.app.config;
//
//import chat.app.security.JwtFilter;
//import chat.app.security.JwtUtil;
//import lombok.RequiredArgsConstructor;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.messaging.Message;
//import org.springframework.messaging.MessageChannel;
//import org.springframework.messaging.simp.config.ChannelRegistration;
//import org.springframework.messaging.simp.stomp.StompCommand;
//import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
//import org.springframework.messaging.support.ChannelInterceptor;
//import org.springframework.messaging.support.MessageHeaderAccessor;
//import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
//
//import java.security.Principal;
//
//@Configuration
//@RequiredArgsConstructor
//public class WebSocketAuthConfig implements WebSocketMessageBrokerConfigurer {
//    private final JwtUtil jwtUtil;
//    private final JwtFilter jwtFilter;
//
//    @Override
//    public void configureClientInboundChannel(ChannelRegistration registration) {
//        registration.interceptors(new ChannelInterceptor() {
//            @Override
//            public Message<?> preSend(Message<?> message, MessageChannel channel) {
//                // 1. wrap the message
//                StompHeaderAccessor accessor = MessageHeaderAccessor
//                        .getAccessor(message, StompHeaderAccessor.class);
//                // 2. check if it's a CONNECT command
//                if (StompCommand.CONNECT.equals(accessor.getCommand())) {
//                    // extract token here
//                }
//                // 3. extract token from header
//                String header = accessor.getFirstNativeHeader("Authorization");
//// header = "Bearer eyJhbGc..."
//// how do you extract just the token? 💡
//
//                // 4. validate and set Principal
//                accessor.setUser(new Principal() {
//                    @Override
//                    public String getName() {
//                        return email; // email becomes the user's identity
//                    }
//                });
//                return message;
//            }
//        });
//    }
//}