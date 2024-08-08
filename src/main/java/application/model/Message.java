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
    private Type type;
    private String text;

    public enum Type {
        TEXT,
        LINK,
        FILE,
        PICTURE
    }
}
