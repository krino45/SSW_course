package io.swagger.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.validation.annotation.Validated;
import io.swagger.configuration.NotUndefined;
import javax.validation.constraints.*;

/**
 * MessageUpdate
 */
@Validated
@NotUndefined


public class MessageUpdate   {
  @JsonProperty("text")

  private String text = null;


  public MessageUpdate text(String text) { 

    this.text = text;
    return this;
  }

  /**
   * Get text
   * @return text
   **/
  

  @NotNull
@Size(min=1,max=2000)   public String getText() {  
    return text;
  }



  public void setText(String text) { 

    this.text = text;
  }

  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    MessageUpdate messageUpdate = (MessageUpdate) o;
    return Objects.equals(this.text, messageUpdate.text);
  }

  @Override
  public int hashCode() {
    return Objects.hash(text);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class MessageUpdate {\n");
    
    sb.append("    text: ").append(toIndentedString(text)).append("\n");
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
