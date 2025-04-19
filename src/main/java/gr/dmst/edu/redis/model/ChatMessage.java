package gr.dmst.edu.redis.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.redis.core.RedisHash;

@RedisHash("chat_message")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
    private static final long serialVersionUID = 1L;
    private String email;
    private String message;
    private Long timestamp;
}