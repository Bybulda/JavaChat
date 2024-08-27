package сipher;


public interface ISymmCipher {

    public byte[] encrypt(byte[] initial);

    public byte[] decrypt(byte[] initial);
}
