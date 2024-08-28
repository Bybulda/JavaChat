package cipher.padding;

import cipher.IPadding;

import java.util.Arrays;
import java.util.Random;

public class ISO10126 implements IPadding {
    @Override
    public byte[] makeWithPadding(byte[] text, int requiredSizeBytes) {
        byte[] result = makeArray(text, requiredSizeBytes);
        int paddedLength = (requiredSizeBytes - text.length % requiredSizeBytes);
        Random randomizer = new Random();
        byte[] randomBytes = new byte[paddedLength];
        randomizer.nextBytes(randomBytes);
        System.arraycopy(randomBytes, 0, result, text.length, randomBytes.length);
        result[result.length - 1] = (byte) paddedLength;
        return result;
    }

    @Override
    public byte[] removePadding(byte[] text) {
        return Arrays.copyOf(text, text.length - (text[text.length - 1] & 0xFF));
    }
}
