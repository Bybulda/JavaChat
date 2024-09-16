package cipher;

public interface ICipherService {
    public byte[] encrypt(byte[] data);

    public byte[] decrypt(byte[] data);
}
