package сipher.padding;

import сipher.IPadding;

import java.util.Arrays;

public class PKCS7 implements IPadding {
    @Override
    public byte[] makeWithPadding(byte[] text, int requiredSizeBytes) {
        byte[] result = makeArray(text, requiredSizeBytes);
        for (int i = text.length; i < result.length; i++) {
            result[i] = (byte) (requiredSizeBytes - text.length % requiredSizeBytes);
        }
        return result;
    }

    @Override
    public byte[] removePadding(byte[] text) {
        return Arrays.copyOf(text, text.length - (text[text.length - 1] & 0xFF));
    }
}
