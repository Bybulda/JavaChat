package application.backend.parser.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JsonMessageAction {
    private long senderId;
    private String action;
    private byte[] aOrB;
}
