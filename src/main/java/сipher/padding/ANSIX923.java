package сipher.padding;

import сipher.IPadding;

import java.util.Arrays;

public class ANSIX923 implements IPadding {
    @Override
    public byte[] makeWithPadding(byte[] text, int requiredSizeBytes) {
        byte[] result = makeArray(text, requiredSizeBytes);
        result[result.length - 1] = (byte) (requiredSizeBytes - text.length % requiredSizeBytes);
        return result;
    }

    @Override
    public byte[] removePadding(byte[] text) {
        return Arrays.copyOf(text, text.length - (text[text.length - 1] & 0xFF));
    }
}
