package сipher;

public interface IPadding {
    public byte[] makeWithPadding(byte[] text, int requiredSizeBytes);

    public byte[] removePadding(byte[] text);

    default byte[] makeArray(byte[] text, int requiredSizeBytes){
        int delim = text.length % requiredSizeBytes;
        int newLength = text.length + requiredSizeBytes;
        byte[] result = delim == 0 ? new byte[newLength] : new byte[newLength - delim];
        System.arraycopy(text, 0, result, 0, text.length);
        return result;
    }
}
