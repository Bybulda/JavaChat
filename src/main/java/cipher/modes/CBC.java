package cipher.modes;

import cipher.ICipherMode;
import cipher.ISymmCipher;

import java.util.List;

public class CBC implements ICipherMode {
    @Override
    public byte[] encryptWithMode(byte[] text, byte[] IV, List<String> parameters, ISymmCipher algorithm, int blockSize) {
        return new byte[0];
    }

    @Override
    public byte[] decryptWithMode(byte[] cipheredText, byte[] IV, List<String> parameters, ISymmCipher algorithm, int blockSize) {
        return new byte[0];
    }
}
