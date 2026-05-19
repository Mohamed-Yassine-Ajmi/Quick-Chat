package chat.app.security;

import chat.app.model.User;
import chat.app.repository.jpa.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@Component
public class JwtFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    public JwtFilter(UserRepository userRepository, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        System.out.println("=== JWT FILTER RUNNING ===");
        System.out.println("Authorization header: " + header);

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.split(" ")[1];
            System.out.println("Token found: " + token);

            if (jwtUtil.validateToken(token)) {
                System.out.println("Token is valid!");

                String email = jwtUtil.extractEmail(token);
                System.out.println("Email extracted: " + email);

                Optional<User> userOpt = userRepository.findByEmail(email);
                System.out.println("User found in DB: " + userOpt.isPresent());

                if (userOpt.isPresent()) {
                    User user = userOpt.get();
                    System.out.println("User role: " + user.getRole());
                    System.out.println("User authorities: " + user.getAuthorities());

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    user,
                                    null,
                                    user.getAuthorities()
                            );
                    authentication.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    System.out.println("Authentication set: " +
                            SecurityContextHolder.getContext().getAuthentication());
                }
            } else {
                System.out.println("Token is INVALID!");
            }
        }

        filterChain.doFilter(request, response);
    }
}