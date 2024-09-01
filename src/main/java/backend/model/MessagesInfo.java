package backend.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Table(name = "messages_info")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MessagesInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private long chatId;
    private long senderId;
    private long componentId;
    private String messageType;
    private byte[] message;
    private LocalDateTime timestamp;

}
