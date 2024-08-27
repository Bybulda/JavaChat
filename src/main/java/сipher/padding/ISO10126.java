package сipher.padding;

import сipher.IPadding;

public class ISO10126 implements IPadding {
    @Override
    public byte[] makeWithPadding(byte[] text, int requiredSizeBytes) {
        return new byte[0];
    }

    @Override
    public byte[] removePadding(byte[] text) {
        return new byte[0];
    }
}
