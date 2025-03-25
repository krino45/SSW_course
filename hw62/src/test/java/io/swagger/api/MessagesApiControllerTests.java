package io.swagger.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.model.Message;
import io.swagger.model.MessageCreate;
import io.swagger.model.MessageUpdate;
import io.swagger.model.MessageIdRateBody;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class MessagesApiControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private final UUID demoUserId = UUID.fromString("136da200-243d-41bd-91ed-fd18dca3e129");

    @BeforeEach
    public void setUp() {
        messageRepository.deleteAll();
    }

    @Test
    public void testCreateMessageEndpoint() throws Exception {
        MessageCreate messageCreate = new MessageCreate();
        messageCreate.setText("Test message");

        mockMvc.perform(post("/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Accept", "application/json")
                        .content(objectMapper.writeValueAsString(messageCreate)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.text").value("Test message"));
    }

    @Test
    public void testGetMessageByIdEndpoint() throws Exception {
        Message message = new Message();
        message.setText("Message for get by id");
        message.setUserId(demoUserId);
        message.setRating(0);
        message = messageRepository.save(message);

        mockMvc.perform(get("/messages/" + message.getId())
                        .header("Accept", "application/json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(message.getId().toString()))
                .andExpect(jsonPath("$.text").value("Message for get by id"));
    }

    @Test
    public void testDeleteMessageEndpoint() throws Exception {
        Message message = new Message();
        message.setText("Message to delete");
        message.setUserId(demoUserId);
        message.setRating(0);
        message = messageRepository.save(message);

        mockMvc.perform(delete("/messages/" + message.getId())
                        .header("Accept", "application/json"))
                .andExpect(status().isNoContent());
    }

    @Test
    public void testUpdateMessageEndpoint() throws Exception {
        Message message = new Message();
        message.setText("Initial text");
        message.setUserId(demoUserId);
        message.setRating(0);
        message = messageRepository.save(message);

        MessageUpdate update = new MessageUpdate();
        update.setText("Updated text");

        mockMvc.perform(put("/messages/" + message.getId())
                        .header("Accept", "application/json")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("Updated text"));
    }

    @Test
    public void testCreateReplyEndpoint() throws Exception {
        // First, create a parent message
        Message parentMessage = new Message();
        parentMessage.setText("Parent message");
        parentMessage.setUserId(demoUserId);
        parentMessage.setRating(0);
        parentMessage = messageRepository.save(parentMessage);

        MessageCreate replyCreate = new MessageCreate();
        replyCreate.setText("This is a reply");

        mockMvc.perform(post("/messages/" + parentMessage.getId() + "/reply")
                        .header("Accept", "application/json")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(replyCreate)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.text").value("This is a reply"))
                .andExpect(jsonPath("$.parentId").value(parentMessage.getId().toString()));

        Message updatedParent = messageRepository.findById(parentMessage.getId()).get();
        assert updatedParent.getReplyCount() == 1;
    }

    @Test
    public void testGetMessageRepliesEndpoint() throws Exception {
        // Create a parent message
        Message parentMessage = new Message();
        parentMessage.setText("Parent for replies");
        parentMessage.setUserId(demoUserId);
        parentMessage.setRating(0);
        parentMessage = messageRepository.save(parentMessage);

        // Create two replies
        for (int i = 0; i < 2; i++) {
            MessageCreate replyCreate = new MessageCreate();
            replyCreate.setText("Reply " + i);
            mockMvc.perform(post("/messages/" + parentMessage.getId() + "/reply")
                            .header("Accept", "application/json")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(replyCreate)))
                    .andExpect(status().isCreated());
        }

        // Retrieve replies
        mockMvc.perform(get("/messages/" + parentMessage.getId() + "/replies")
                        .header("Accept", "application/json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    public void testGetMessagesEndpoint() throws Exception {
        Message message1 = new Message();
        message1.setText("User1 message");
        message1.setUserId(UUID.randomUUID());
        message1.setRating(0);
        message1 = messageRepository.save(message1);

        Message message2 = new Message();
        message2.setText("User2 message");
        message2.setUserId(UUID.randomUUID());
        message2.setRating(0);
        message2 = messageRepository.save(message2);

        MessageCreate replyCreate = new MessageCreate();
        replyCreate.setText("Reply to user1");
        mockMvc.perform(post("/messages/" + message1.getId() + "/reply")
                        .header("Accept", "application/json")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(replyCreate)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/messages")
                        .header("Accept", "application/json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));

        mockMvc.perform(get("/messages")
                        .param("userId", message1.getUserId().toString())
                        .header("Accept", "application/json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(message1.getUserId().toString()));
    }

    @Test
    public void testRateMessageEndpoint() throws Exception {
        Message message = new Message();
        message.setText("Message to rate");
        message.setUserId(demoUserId);
        message.setRating(0);
        message = messageRepository.save(message);

        MessageIdRateBody rateBody = new MessageIdRateBody();
        // Assuming the enum ordinal for +1 is 1.
        rateBody.setValue(MessageIdRateBody.ValueEnum.NUMBER_1);

        mockMvc.perform(post("/messages/" + message.getId() + "/rate")
                        .header("Accept", "application/json")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rateBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rating").value(1));
    }
}
