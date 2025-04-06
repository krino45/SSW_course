package io.swagger.api;

import io.swagger.model.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class MessageServiceImplITests {

    private static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15.2")
            .withDatabaseName("messagedb")
            .withUsername("test")
            .withPassword("test");

    @Autowired
    private MessageService messageService;

    @Autowired
    private MessageRepository messageRepository;

    private final UUID testUserId1 = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private final UUID testUserId2 = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private Message testParentMessage;

    @BeforeAll
    public static void startContainer() {
        postgres.start();
        System.setProperty("spring.datasource.url", postgres.getJdbcUrl());
        System.setProperty("spring.datasource.username", postgres.getUsername());
        System.setProperty("spring.datasource.password", postgres.getPassword());
    }

    @AfterAll
    public static void stopContainer() {
        postgres.stop();
    }

    @BeforeEach
    public void setUp() {
        messageRepository.deleteAll();

        MessageCreate parentCreate = new MessageCreate();
        parentCreate.setText("Test parent message");
        testParentMessage = messageService.createMessage(parentCreate, testUserId1);
    }

    @Test
    public void testCreateMessage() {
        MessageCreate messageCreate = new MessageCreate();
        messageCreate.setText("Hello world");

        Message message = messageService.createMessage(messageCreate, testUserId1);

        assertNotNull(message.getId());
        assertEquals("Hello world", message.getText());
        assertEquals(testUserId1, message.getUserId());

        // Verify it was actually persisted
        Optional<Message> foundMessage = messageRepository.findById(message.getId());
        assertTrue(foundMessage.isPresent());
    }

    @Test
    public void testCreateReply() {
        MessageCreate replyCreate = new MessageCreate();
        replyCreate.setText("Reply message");

        Message reply = messageService.createReply(testParentMessage.getId(), replyCreate, testUserId2);

        assertNotNull(reply.getId());
        assertEquals("Reply message", reply.getText());
        assertEquals(testParentMessage.getId(), reply.getParentId());

        // Verify parent's reply count was updated
        Optional<Message> updatedParent = messageRepository.findById(testParentMessage.getId());
        assertTrue(updatedParent.isPresent());
        assertEquals(Integer.valueOf(1), updatedParent.get().getReplyCount());
    }

    @Test
    public void testDeleteMessage() {
        MessageCreate messageCreate = new MessageCreate();
        messageCreate.setText("Message to be deleted");
        Message message = messageService.createMessage(messageCreate, testUserId1);

        messageService.deleteMessage(message.getId());

        assertFalse(messageRepository.existsById(message.getId()));
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
        // Create additional test messages
        messageService.createMessage(new MessageCreate().text("User1 message"), testUserId1);
        messageService.createMessage(new MessageCreate().text("User2 message"), testUserId2);

        List<Message> allMessages = messageService.getMessages(null, null);
        assertEquals(3, allMessages.size()); // Includes the parent message from setUp

        List<Message> user1Messages = messageService.getMessages(testUserId1, null);
        assertEquals(2, user1Messages.size());
        assertEquals(testUserId1, user1Messages.get(0).getUserId());
    }

    @Test
    public void testRateMessage() {
        Message message = messageService.createMessage(
                new MessageCreate().text("Message to rate"),
                testUserId1
        );
        int initialRating = message.getRating();

        Message ratedMessage = messageService.rateMessage(message.getId(), 1);
        assertEquals(Integer.valueOf(initialRating + 1), ratedMessage.getRating());

        // Verify rating persists
        Optional<Message> reloaded = messageRepository.findById(message.getId());
        assertTrue(reloaded.isPresent());
        assertEquals(Integer.valueOf(initialRating + 1), reloaded.get().getRating());
    }

    @Test
    public void testUpdateMessage() {
        MessageUpdate update = new MessageUpdate();
        update.setText("Updated text");

        Message updatedMessage = messageService.updateMessage(testParentMessage.getId(), update);
        assertEquals("Updated text", updatedMessage.getText());

        // Verify update persists
        Optional<Message> reloaded = messageRepository.findById(testParentMessage.getId());
        assertTrue(reloaded.isPresent());
        assertEquals("Updated text", reloaded.get().getText());
    }
}