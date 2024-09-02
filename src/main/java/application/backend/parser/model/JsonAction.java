package application.backend.parser.model;


import application.backend.model.CipherInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JsonAction {
    private long senderId;
    private String chatName;
    private CipherInfo cipher;
    private byte[] p;
    private byte[] g;
}
