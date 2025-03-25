package io.swagger.api;

import io.swagger.model.Message;
import io.swagger.model.MessageCreate;
import io.swagger.model.MessageUpdate;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class MessageServiceImplITests {

    @Autowired
    private MessageService messageService;

    // Assume MessageRepository is available to help with cleanup/verification.
    @Autowired
    private MessageRepository messageRepository;

    @BeforeEach
    public void setUp() {
        messageRepository.deleteAll();
    }

    @Test
    public void testCreateMessage() {
        MessageCreate messageCreate = new MessageCreate();
        messageCreate.setText("Hello world");
        UUID userId = UUID.randomUUID();

        Message message = messageService.createMessage(messageCreate, userId);
        assertNotNull(message.getId());
        Assertions.assertEquals("Hello world", message.getText());
        Assertions.assertEquals(userId, message.getUserId());
    }

    @Test
    public void testCreateReply() {
        MessageCreate parentCreate = new MessageCreate();
        parentCreate.setText("Parent message");
        UUID userId = UUID.randomUUID();
        Message parent = messageService.createMessage(parentCreate, userId);

        MessageCreate replyCreate = new MessageCreate();
        replyCreate.setText("Reply message");
        Message reply = messageService.createReply(parent.getId(), replyCreate, userId);
        assertNotNull(reply.getId());
        Assertions.assertEquals("Reply message", reply.getText());
        Assertions.assertEquals(parent.getId(), reply.getParentId());

        Optional<Message> updatedParentOpt = messageService.getMessageById(parent.getId());
        assertTrue(updatedParentOpt.isPresent());
        Message updatedParent = updatedParentOpt.get();
        Assertions.assertEquals(Optional.of(1), Optional.ofNullable(updatedParent.getReplyCount()));
    }

    @Test
    public void testDeleteMessage() {
        MessageCreate messageCreate = new MessageCreate();
        messageCreate.setText("Message to be deleted");
        UUID userId = UUID.randomUUID();

        Message message = messageService.createMessage(messageCreate, userId);
        UUID messageId = message.getId();

        messageService.deleteMessage(messageId);
        Optional<Message> deleted = messageService.getMessageById(messageId);
        assertFalse(deleted.isPresent());
    }

    @Test
    public void testGetMessageByIdNotFound() {
        UUID randomId = UUID.randomUUID();
        Optional<Message> message = messageService.getMessageById(randomId);
        assertFalse(message.isPresent());
    }

    @Test
    public void testGetMessageRepliesForNonexistentParent() {
        UUID randomId = UUID.randomUUID();
        assertThrows(EntityNotFoundException.class, () -> {
            messageService.getMessageReplies(randomId);
        });
    }

    @Test
    public void testGetMessagesFiltering() {
        UUID userId1 = UUID.randomUUID();
        UUID userId2 = UUID.randomUUID();

        MessageCreate create1 = new MessageCreate();
        create1.setText("User1 message");
        messageService.createMessage(create1, userId1);

        MessageCreate create2 = new MessageCreate();
        create2.setText("User2 message");
        messageService.createMessage(create2, userId2);

        List<Message> allMessages = messageService.getMessages(null, null);
        Assertions.assertEquals(2, allMessages.size());

        List<Message> user1Messages = messageService.getMessages(userId1, null);
        Assertions.assertEquals(1, user1Messages.size());
        Assertions.assertEquals(userId1, user1Messages.get(0).getUserId());
    }

    @Test
    public void testRateMessage() {
        MessageCreate messageCreate = new MessageCreate();
        messageCreate.setText("Message to rate");
        UUID userId = UUID.randomUUID();

        Message message = messageService.createMessage(messageCreate, userId);
        int initialRating = message.getRating();

        Message ratedMessage = messageService.rateMessage(message.getId(), 1);
        Assertions.assertEquals(Optional.of(initialRating + 1), Optional.of(ratedMessage.getRating()));
    }

    @Test
    public void testUpdateMessage() {
        MessageCreate messageCreate = new MessageCreate();
        messageCreate.setText("Old text");
        UUID userId = UUID.randomUUID();

        Message message = messageService.createMessage(messageCreate, userId);

        MessageUpdate update = new MessageUpdate();
        update.setText("Updated text");

        Message updatedMessage = messageService.updateMessage(message.getId(), update);
        Assertions.assertEquals("Updated text", updatedMessage.getText());
    }
}
