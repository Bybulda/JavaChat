package cipher;

public interface ICipherService {
    public byte[] encrypt(byte[] data);

    public byte[] decrypt(byte[] data);

    public void setCipherSettings(String paddings, String cipherModes, String ciphers, int blockSizeBits, int keySizeBits, int rounds, byte[] IV);

    public void setCipherKey(byte[] key);

    public byte[] getCipherKey();
}
