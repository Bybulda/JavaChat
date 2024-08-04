package application.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@AllArgsConstructor
@Builder
@Data
public class Channel {
    private long channelId;
    private long userId;
    private long buddyId;
    private String channelName;
    private String buddy;
}
