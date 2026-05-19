package chat.app.model;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection="messages")
@Data
public class Message {
    @Id
    private String id;
    private Long senderId;
    private Long receiverId;
    private String content;
    @CreationTimestamp
    private LocalDateTime timestamp;
    private boolean isRead;

}
