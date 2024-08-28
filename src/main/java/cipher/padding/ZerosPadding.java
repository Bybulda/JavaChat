package cipher.padding;

import cipher.IPadding;

import java.util.Arrays;

public class ZerosPadding implements IPadding {
    @Override
    public byte[] makeWithPadding(byte[] text, int requiredSizeBytes) {
        return makeArray(text, requiredSizeBytes);
    }

    @Override
    public byte[] removePadding(byte[] text) {
        int removeIndex = 0;
        for (int i = text.length - 1; i >= 0; i--) {
            if(text[i] != 0){
                removeIndex = i;
                break;
            }
        }
        return Arrays.copyOf(text, removeIndex + 1);
    }
}
