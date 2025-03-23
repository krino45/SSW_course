package io.swagger.api;

import io.swagger.model.Message;
import io.swagger.model.MessageCreate;
import io.swagger.model.MessageIdRateBody;
import io.swagger.model.MessageUpdate;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.persistence.EntityNotFoundException;
import javax.validation.Valid;
import java.util.List;

@RequestMapping("/messages")
@RestController
public class MessagesApiController {

    private static final Logger log = LoggerFactory.getLogger(MessagesApiController.class);
    private final MessageService messageService;

    @Autowired
    public MessagesApiController(MessageService messageService) {
        this.messageService = messageService;
    }

    @PostMapping
    public ResponseEntity<Message> createMessage(@Valid @RequestBody MessageCreate body) {
        try {
            UUID userId = UUID.fromString("136da200-243d-41bd-91ed-fd18dca3e129"); // Demo user ID

            Message message = messageService.createMessage(body, userId);
            return new ResponseEntity<>(message, HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("Error creating message", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/{messageId}/reply")
    public ResponseEntity<Message> createReply(@PathVariable("messageId") UUID messageId, @Valid @RequestBody MessageCreate body) {
        try {
            UUID userId = UUID.fromString("136da200-243d-41bd-91ed-fd18dca3e129"); // Demo user ID

            Message message = messageService.createReply(messageId, body, userId);
            return new ResponseEntity<>(message, HttpStatus.CREATED);
        } catch (EntityNotFoundException e) {
            log.error("Parent message not found", e);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            log.error("Error creating reply", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @DeleteMapping
    public ResponseEntity<Void> deleteMessage(@PathVariable("messageId") UUID messageId) {
        try {
            messageService.deleteMessage(messageId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (EntityNotFoundException e) {
            log.error("Message not found", e);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            log.error("Error deleting message", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @GetMapping("/{messageId}")
    public ResponseEntity<Message> getMessageById(@PathVariable("messageId") UUID messageId) {
        try {
            return messageService.getMessageById(messageId)
                    .map(message -> new ResponseEntity<>(message, HttpStatus.OK))
                    .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
        } catch (Exception e) {
            log.error("Error retrieving message", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @GetMapping("/{messageId}/replies")
    public ResponseEntity<List<Message>> getMessageReplies(@PathVariable("messageId") UUID messageId) {
        try {
            List<Message> replies = messageService.getMessageReplies(messageId);
            return new ResponseEntity<>(replies, HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            log.error("Parent message not found", e);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            log.error("Error retrieving replies", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @GetMapping
    public ResponseEntity<List<Message>> getMessages(
            @Valid @RequestParam(value = "userId", required = false) UUID userId,
            @Valid @RequestParam(value = "parentId", required = false) UUID parentId) {
        try {
            List<Message> messages = messageService.getMessages(userId, parentId);
            return new ResponseEntity<>(messages, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error retrieving messages", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @PostMapping("/{messageId}/rate")
    public ResponseEntity<Message> rateMessage(
            @PathVariable("messageId") UUID messageId,
            @Valid @RequestBody MessageIdRateBody body) {
        try {
            Message message = messageService.rateMessage(messageId, body.getValue().ordinal());
            return new ResponseEntity<>(message, HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            log.error("Message not found", e);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            log.error("Error rating message", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @PutMapping("/{messageId}")
    public ResponseEntity<Message> updateMessage(
            @PathVariable("messageId") UUID messageId,
            @Valid @RequestBody MessageUpdate body) {
        try {
            Message message = messageService.updateMessage(messageId, body);
            return new ResponseEntity<>(message, HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            log.error("Message not found", e);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            log.error("Error updating message", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}