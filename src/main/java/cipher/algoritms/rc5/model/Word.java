package cipher.algoritms.rc5.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class Word<T> {
    private T wordA;
    private T wordB;

    public static Word<byte[]> of(byte[] left, byte[] right) {
        return Word.<byte[]>builder().wordA(left).wordB(right).build();
    }
}
