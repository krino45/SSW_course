package io.swagger.api;

import io.swagger.model.Message;
import io.swagger.model.MessageCreate;
import io.swagger.model.MessageUpdate;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MessageService {
    Message createMessage(MessageCreate messageCreate, UUID userId);
    Message createReply(UUID parentId, MessageCreate messageCreate, UUID userId);
    void deleteMessage(UUID messageId);
    Optional<Message> getMessageById(UUID messageId);
    List<Message> getMessageReplies(UUID messageId);
    List<Message> getMessages(UUID userId, UUID parentId);
    Message rateMessage(UUID messageId, int value);
    Message updateMessage(UUID messageId, MessageUpdate messageUpdate);
}