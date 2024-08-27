package сipher.padding;

import сipher.IPadding;

public class PKCS7 implements IPadding {
    @Override
    public byte[] makeWithPadding(byte[] text, int requiredSizeBytes) {
        int delim = text.length % requiredSizeBytes;
        int newLength = text.length + requiredSizeBytes;
        int paddedLength = requiredSizeBytes - delim;
        byte[] result = delim == 0 ? new byte[newLength] : new byte[newLength - delim];
        System.arraycopy(text, 0, result, 0, text.length);
        for (int i = text.length; i < result.length; i++) {
            result[i] = (byte) paddedLength;
        }
        return result;
    }

    // TODO: удалить паддинг
    @Override
    public byte[] removePadding(byte[] text) {
        return new byte[0];
    }
}
