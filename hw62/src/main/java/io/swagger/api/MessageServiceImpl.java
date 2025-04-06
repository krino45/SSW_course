package io.swagger.api;

import io.swagger.model.Message;
import io.swagger.model.MessageCreate;
import io.swagger.model.MessageUpdate;
import javax.persistence.EntityNotFoundException;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MessageServiceImpl implements MessageService {

    private static final Logger logger = LoggerFactory.getLogger(MessageServiceImpl.class);
    private final MessageRepository messageRepository;

    @Autowired
    public MessageServiceImpl(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    @Override
    @Transactional
    public Message createMessage(MessageCreate messageCreate, UUID userId) {
        logger.info("Creating message for user: {}", userId);
        Message entity = new Message();
        entity.setText(messageCreate.getText());
        entity.setUserId(userId);
        entity.setParentId(null);
        entity.setRating(0);
        entity.setReplyCount(0);

        Message savedEntity = messageRepository.save(entity);
        logger.info("Message created with ID: {}", savedEntity.getId());
        return mapEntityToDto(savedEntity);
    }

    @Override
    @Transactional
    public Message createReply(UUID parentId, MessageCreate messageCreate, UUID userId) {
        logger.info("Creating reply to message ID: {} by user: {}", parentId, userId);
        Optional<Message> parentMessage = messageRepository.findById(parentId);

        if (!parentMessage.isPresent()) {
            logger.warn("Parent message not found: {}", parentId);
            throw new EntityNotFoundException("Parent message not found");
        }

        Message entity = new Message();
        entity.setText(messageCreate.getText());
        entity.setUserId(userId);
        entity.setParentId(parentId);
        entity.setRating(0);
        entity.setReplyCount(0);

        Message savedEntity = messageRepository.save(entity);
        messageRepository.incrementReplyCount(parentId);
        logger.info("Reply created with ID: {}", savedEntity.getId());
        return mapEntityToDto(savedEntity);
    }

    @Override
    @Transactional
    public void deleteMessage(UUID messageId) {
        logger.info("Deleting message with ID: {}", messageId);
        Message entity = messageRepository.findById(messageId)
                .orElseThrow(() -> {
                    logger.warn("Message not found: {}", messageId);
                    return new EntityNotFoundException("Message not found");
                });

        if (entity.getParentId() != null) {
            messageRepository.decrementReplyCount(entity.getParentId());
        }

        messageRepository.deleteById(messageId);
        logger.info("Message deleted: {}", messageId);
    }

    @Override
    public Optional<Message> getMessageById(UUID messageId) {
        logger.info("Fetching message with ID: {}", messageId);
        return messageRepository.findById(messageId)
                .map(this::mapEntityToDto);
    }

    @Override
    public List<Message> getMessageReplies(UUID messageId) {
        logger.info("Fetching replies for message ID: {}", messageId);
        messageRepository.findById(messageId)
                .orElseThrow(() -> {
                    logger.warn("Parent message not found: {}", messageId);
                    return new EntityNotFoundException("Parent message not found");
                });

        return messageRepository.findByParentId(messageId)
                .stream()
                .map(this::mapEntityToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<Message> getMessages(UUID userId, UUID parentId) {
        logger.info("Fetching messages for user: {} and parent: {}", userId, parentId);
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
        logger.info("Updating rating for message ID: {} with value: {}", messageId, value);
        Message entity = messageRepository.findById(messageId)
                .orElseThrow(() -> {
                    logger.warn("Message not found: {}", messageId);
                    return new EntityNotFoundException("Message not found");
                });

        entity.setRating(entity.getRating() + value);
        Message savedEntity = messageRepository.save(entity);
        logger.info("Updated rating for message ID: {}", messageId);
        return mapEntityToDto(savedEntity);
    }

    @Override
    @Transactional
    public Message updateMessage(UUID messageId, MessageUpdate messageUpdate) {
        logger.info("Updating message ID: {}", messageId);
        Message entity = messageRepository.findById(messageId)
                .orElseThrow(() -> {
                    logger.warn("Message not found: {}", messageId);
                    return new EntityNotFoundException("Message not found");
                });

        entity.setText(messageUpdate.getText());
        Message savedEntity = messageRepository.save(entity);
        logger.info("Message updated: {}", messageId);
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
