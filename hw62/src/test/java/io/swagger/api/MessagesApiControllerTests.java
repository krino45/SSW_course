package io.swagger.api;

import io.swagger.model.Message;
import io.swagger.model.MessageCreate;
import io.swagger.model.MessageUpdate;
import io.swagger.model.MessageIdRateBody;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MessagesApiControllerTests {

    @Mock
    private MessageService messageService;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private MessagesApiController messagesApiController;

    private final UUID demoUserId = UUID.fromString("136da200-243d-41bd-91ed-fd18dca3e129");
    private final UUID demoMessageId = UUID.randomUUID();

    @BeforeEach
    public void setUp() {
        when(request.getHeader("Accept")).thenReturn("application/json");
    }

    @Test
    public void testCreateMessageEndpoint() {
        MessageCreate messageCreate = new MessageCreate();
        messageCreate.setText("Test message");

        Message mockMessage = new Message();
        mockMessage.setText("Test message");
        mockMessage.setId(demoMessageId);

        when(messageService.createMessage(any(MessageCreate.class), any(UUID.class)))
                .thenReturn(mockMessage);

        ResponseEntity<Message> response = messagesApiController.createMessage(messageCreate);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Test message", response.getBody().getText());
    }

    @Test
    public void testGetMessageByIdEndpoint() {
        Message mockMessage = new Message();
        mockMessage.setText("Message for get by id");
        mockMessage.setId(demoMessageId);
        mockMessage.setUserId(demoUserId);

        when(messageService.getMessageById(demoMessageId))
                .thenReturn(Optional.of(mockMessage));

        ResponseEntity<Message> response = messagesApiController.getMessageById(demoMessageId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(demoMessageId, response.getBody().getId());
        assertEquals("Message for get by id", response.getBody().getText());
    }

    @Test
    public void testGetMessageByIdNotFound() {
        when(messageService.getMessageById(demoMessageId))
                .thenReturn(Optional.empty());

        ResponseEntity<Message> response = messagesApiController.getMessageById(demoMessageId);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    public void testDeleteMessageEndpoint() {
        doNothing().when(messageService).deleteMessage(demoMessageId);

        ResponseEntity<Void> response = messagesApiController.deleteMessage(demoMessageId);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(messageService, times(1)).deleteMessage(demoMessageId);
    }

    @Test
    public void testUpdateMessageEndpoint() {
        MessageUpdate update = new MessageUpdate();
        update.setText("Updated text");

        Message mockMessage = new Message();
        mockMessage.setText("Updated text");
        mockMessage.setId(demoMessageId);

        when(messageService.updateMessage(eq(demoMessageId), any(MessageUpdate.class)))
                .thenReturn(mockMessage);

        ResponseEntity<Message> response = messagesApiController.updateMessage(demoMessageId, update);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Updated text", response.getBody().getText());
    }

    @Test
    public void testCreateReplyEndpoint() {
        MessageCreate replyCreate = new MessageCreate();
        replyCreate.setText("This is a reply");

        Message mockReply = new Message();
        mockReply.setText("This is a reply");
        mockReply.setId(demoMessageId);
        mockReply.setParentId(demoMessageId);

        when(messageService.createReply(eq(demoMessageId), any(MessageCreate.class), any(UUID.class)))
                .thenReturn(mockReply);

        ResponseEntity<Message> response = messagesApiController.createReply(demoMessageId, replyCreate);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("This is a reply", response.getBody().getText());
        assertEquals(demoMessageId, response.getBody().getParentId());
    }

    @Test
    public void testRateMessageEndpoint() {
        MessageIdRateBody rateBody = new MessageIdRateBody();
        rateBody.setValue(MessageIdRateBody.ValueEnum.NUMBER_1);

        Message mockMessage = new Message();
        mockMessage.setId(demoMessageId);
        mockMessage.setRating(1);

        when(messageService.rateMessage(eq(demoMessageId), anyInt()))
                .thenReturn(mockMessage);

        ResponseEntity<Message> response = messagesApiController.rateMessage(demoMessageId, rateBody);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(Integer.valueOf(1), response.getBody().getRating());
    }
}