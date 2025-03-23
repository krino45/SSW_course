package io.swagger.api;

import io.swagger.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MessageRepository extends JpaRepository<Message, UUID> {
    List<Message> findByUserId(UUID userId);

    List<Message> findByParentId(UUID parentId);

    List<Message> findByUserIdAndParentId(UUID userId, UUID parentId);

    @Modifying
    @Query("UPDATE Message m SET m.replyCount = m.replyCount + 1 WHERE m.id = :messageId")
    void incrementReplyCount(UUID messageId);

    @Modifying
    @Query("UPDATE Message m SET m.replyCount = m.replyCount - 1 WHERE m.id = :messageId")
    void decrementReplyCount(UUID messageId);
}