package application.backend.parser.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class JsonMessage {
    private long chatId;
    private long id;
    private long senderId;
    // actions type: disconnect, connect, delete, text, image, file
    private String messageType;
    private byte[] message;
    private LocalDateTime timestamp;
    private byte[] keyWord;
}
