package backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "rooms_info")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RoomsInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private long leftUser;
    private long rightUser;
    private String titleLeft;
    private String titleRight;
    private long cipherInfoId;
    private byte[] p;
    private byte[] g;

}
