package chat.app.repository.mongodb;

import chat.app.model.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;


import java.util.List;

@Repository
public interface MessageRepository extends MongoRepository<Message,String> {
    Page<Message> findBySenderIdAndReceiverIdOrSenderIdAndReceiverId(
            Long senderId1, Long receiverId1,
            Long senderId2, Long receiverId2,
            Pageable pageable
    );
}
