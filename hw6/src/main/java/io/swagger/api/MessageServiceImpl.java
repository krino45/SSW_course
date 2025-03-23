package io.swagger.api;

import io.swagger.model.Message;
import io.swagger.model.MessageCreate;
import io.swagger.model.MessageUpdate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class MessageServiceImpl implements MessageService {

    private final MessageRepository messageRepository;

    @Autowired
    public MessageServiceImpl(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    @Override
    @Transactional
    public Message createMessage(MessageCreate messageCreate, UUID userId) {
        Message entity = new Message();
        entity.setText(messageCreate.getText());
        entity.setUserId(userId);
        entity.setParentId(null);
        entity.setRating(0);
        entity.setReplyCount(0);

        Message savedEntity = messageRepository.save(entity);
        return mapEntityToDto(savedEntity);
    }

    @Override
    @Transactional
    public Message createReply(UUID parentId, MessageCreate messageCreate, UUID userId) {
        messageRepository.findById(parentId)
                .orElseThrow(() -> new EntityNotFoundException("Parent message not found"));

        Message entity = new Message();
        entity.setText(messageCreate.getText());
        entity.setUserId(userId);
        entity.setParentId(parentId);
        entity.setRating(0);
        entity.setReplyCount(0);

        Message savedEntity = messageRepository.save(entity);

        messageRepository.incrementReplyCount(parentId);

        return mapEntityToDto(savedEntity);
    }

    @Override
    @Transactional
    public void deleteMessage(UUID messageId) {
        Message entity = messageRepository.findById(messageId)
                .orElseThrow(() -> new EntityNotFoundException("Message not found"));

        // If this is a reply, decrement parent's reply count
        if (entity.getParentId() != null) {
            messageRepository.decrementReplyCount(entity.getParentId());
        }

        messageRepository.deleteById(messageId);
    }

    @Override
    public Optional<Message> getMessageById(UUID messageId) {
        return messageRepository.findById(messageId)
                .map(this::mapEntityToDto);
    }

    @Override
    public List<Message> getMessageReplies(UUID messageId) {
        // Verify parent exists
        messageRepository.findById(messageId)
                .orElseThrow(() -> new EntityNotFoundException("Parent message not found"));

        return messageRepository.findByParentId(messageId)
                .stream()
                .map(this::mapEntityToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<Message> getMessages(UUID userId, UUID parentId) {
        List<Message> entities;

        if (userId != null && parentId != null) {
            entities = messageRepository.findByUserIdAndParentId(userId, parentId);
        } else if (userId != null) {
            entities = messageRepository.findByUserId(userId);
        } else if (parentId != null) {
            entities = messageRepository.findByParentId(parentId);
        } else {
            entities = messageRepository.findAll();
        }

        return entities.stream()
                .map(this::mapEntityToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Message rateMessage(UUID messageId, int value) {
        Message entity = messageRepository.findById(messageId)
                .orElseThrow(() -> new EntityNotFoundException("Message not found"));

        entity.setRating(entity.getRating() + value);

        Message savedEntity = messageRepository.save(entity);
        return mapEntityToDto(savedEntity);
    }

    @Override
    @Transactional
    public Message updateMessage(UUID messageId, MessageUpdate messageUpdate) {
        Message entity = messageRepository.findById(messageId)
                .orElseThrow(() -> new EntityNotFoundException("Message not found"));

        entity.setText(messageUpdate.getText());
        // Updated timestamp will be handled by @PreUpdate

        Message savedEntity = messageRepository.save(entity);
        return mapEntityToDto(savedEntity);
    }

    private Message mapEntityToDto(Message entity) {
        Message message = new Message();
        message.setId(entity.getId());
        message.setUserId(entity.getUserId());
        message.setText(entity.getText());
        message.setRating(entity.getRating());
        message.setParentId(entity.getParentId());
        message.setReplyCount(entity.getReplyCount());
        message.setCreatedAt(entity.getCreatedAt());
        message.setUpdatedAt(entity.getUpdatedAt());
        return message;
    }
}