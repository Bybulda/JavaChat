package backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "cipher_info")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CipherInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String algorithm;
    private String mode;
    private String padding;
    private int keySizeBits;
    private int blockSizeBits;
    private byte[] iv;
}
