package io.swagger.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;

import org.springframework.validation.annotation.Validated;
import io.swagger.configuration.NotUndefined;

import javax.persistence.*;
import lombok.*;



/**
 * Message
 */
@Setter
@Getter
@Validated
@NotUndefined
@Entity
@Table(name = "messages")
@Data
@NoArgsConstructor

public class Message implements Serializable {
  private static final long serialVersionUID = 1L;
  @JsonProperty("id")
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private UUID id = null;

  @JsonProperty("userId")
  @Column(name = "user_id", nullable = false)
  private UUID userId = null;

  @JsonProperty("text")
  @Column(nullable = false, length = 2000)
  private String text = null;

  @JsonProperty("rating")
  @Column(nullable = false)
  private Integer rating = null;

  @JsonProperty("parentId")
  @Column(name = "parent_id")
  private UUID parentId = null;

  @JsonProperty("replyCount")
  @Column(name = "reply_count", nullable = false)
  private Integer replyCount = null;

  @JsonProperty("createdAt")
  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt = null;

  @JsonProperty("updatedAt")
  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt = null;

  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
    updatedAt = LocalDateTime.now();
  }

  @PreUpdate
  protected void onUpdate() {
    updatedAt = LocalDateTime.now();
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Message message = (Message) o;
    return Objects.equals(this.id, message.id) &&
            Objects.equals(this.userId, message.userId) &&
            Objects.equals(this.text, message.text) &&
            Objects.equals(this.rating, message.rating) &&
            Objects.equals(this.parentId, message.parentId) &&
            Objects.equals(this.replyCount, message.replyCount) &&
            Objects.equals(this.createdAt, message.createdAt) &&
            Objects.equals(this.updatedAt, message.updatedAt);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, userId, text, rating, parentId, replyCount, createdAt, updatedAt);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Message {\n");

    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    userId: ").append(toIndentedString(userId)).append("\n");
    sb.append("    text: ").append(toIndentedString(text)).append("\n");
    sb.append("    rating: ").append(toIndentedString(rating)).append("\n");
    sb.append("    parentId: ").append(toIndentedString(parentId)).append("\n");
    sb.append("    replyCount: ").append(toIndentedString(replyCount)).append("\n");
    sb.append("    createdAt: ").append(toIndentedString(createdAt)).append("\n");
    sb.append("    updatedAt: ").append(toIndentedString(updatedAt)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  private String toIndentedString(java.lang.Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}
