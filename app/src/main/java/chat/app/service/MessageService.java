package chat.app.service;

import chat.app.exception.InvalidMessageException;
import chat.app.model.Message;
import chat.app.repository.mongodb.MessageRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MessageService {
    MessageRepository messageRepository;

    public MessageService(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    public Message sendMessage(Message message) throws InvalidMessageException {

        // 1. Verify sender and receiver
        if (message.getSenderId() == null || message.getReceiverId() == null) {
            throw new InvalidMessageException("Sender and receiver cannot be null");
        }

        // 2. Verify content
        if (message.getContent() == null || message.getContent().isBlank()) {
            throw new InvalidMessageException("Message content cannot be empty");
        }

        // 3. Set timestamp and isRead
        message.setTimestamp(LocalDateTime.now());
        message.setRead(false);

        // 4. Save and return
        return messageRepository.save(message);
    }

    public List<Message> getConversation(Long userId1, Long userId2 ,int page){
        Pageable pageable = PageRequest.of(page, 20, Sort.by("timestamp").descending());
        Page<Message> result=messageRepository.findBySenderIdAndReceiverIdOrSenderIdAndReceiverId(
                userId1,userId2,
                userId2,userId1,
                pageable
        );
        return result.getContent();
    }

    public void markAsRead(List<Message> list){
        if (list!=null){
            for(Message msg :list) {
                msg.setRead(true);
            }
            messageRepository.saveAll(list);
        }

    }
}
