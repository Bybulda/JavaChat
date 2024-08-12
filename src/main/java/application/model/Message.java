package application.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class Message {
    private String username;
    private Long userId;
    private MessageType type;
    private String text;

    private enum MessageType {
        TEXT,
        LINK,
        PICTURE
    }
}
