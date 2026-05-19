package chat.app.dto;

import chat.app.model.User;
import lombok.Data;

import java.time.LocalDateTime;
@Data
public class UserDTO {
    private Long id;
    private String username;
    private String email;
    private boolean isOnline;
    private LocalDateTime createdAt;

    public UserDTO(User user) {
        this.id = user.getId();
        this.username = user.getDisplayName(); // Use actual display name instead of email
        this.email = user.getEmail();
        this.isOnline = user.isOnline();       // boolean uses is prefix
        this.createdAt = user.getCreatedAt();
    }
}
