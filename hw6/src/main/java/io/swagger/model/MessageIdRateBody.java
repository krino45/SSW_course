package io.swagger.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.springframework.validation.annotation.Validated;
import io.swagger.configuration.NotUndefined;
import javax.validation.constraints.*;

/**
 * MessageIdRateBody
 */
@Validated
@NotUndefined


public class MessageIdRateBody   {
  /**
   * Rating value (1 for upvote, -1 for downvote)
   */
  public enum ValueEnum {
    NUMBER_MINUS_1(-1),
    
    NUMBER_1(1);

    private Integer value;

    ValueEnum(Integer value) {
      this.value = value;
    }

    @Override
    @JsonValue
    public String toString() {
      return String.valueOf(value);
    }

    @JsonCreator
    public static ValueEnum fromValue(String text) {
      for (ValueEnum b : ValueEnum.values()) {
        if (String.valueOf(b.value).equals(text)) {
          return b;
        }
      }
      return null;
    }
  }
  @JsonProperty("value")

  private ValueEnum value = null;


  public MessageIdRateBody value(ValueEnum value) { 

    this.value = value;
    return this;
  }


  @NotNull
  public ValueEnum getValue() {
    return value;
  }

  public void setValue(ValueEnum value) { 

    this.value = value;
  }

  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    MessageIdRateBody messageIdRateBody = (MessageIdRateBody) o;
    return Objects.equals(this.value, messageIdRateBody.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(value);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class MessageIdRateBody {\n");
    
    sb.append("    value: ").append(toIndentedString(value)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(java.lang.Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}
