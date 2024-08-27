package сipher;

public interface IPadding {
    public byte[] makeWithPadding(byte[] text, int requiredSizeBytes);

    public byte[] removePadding(byte[] text);
}
