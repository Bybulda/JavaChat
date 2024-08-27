package сipher.padding;

import сipher.IPadding;

import java.util.Arrays;

public class ZerosPadding implements IPadding {
    @Override
    public byte[] makeWithPadding(byte[] text, int requiredSizeBytes) {
        int delim = text.length % requiredSizeBytes;
        int newLength = text.length + requiredSizeBytes;
        byte[] result = delim == 0 ? new byte[newLength] : new byte[newLength - delim];
        System.arraycopy(text, 0, result, 0, text.length);
        return result;
    }

    @Override
    public byte[] removePadding(byte[] text) {
        int removeIndex = 0;
        for (int i = text.length - 1; i >= 0; i++) {
            if(text[i] != 0){
                removeIndex = i;
                break;
            }
        }
        return Arrays.copyOf(text, removeIndex + 1);
    }
}
