package cipher;


public interface ISymmCipher {

    public byte[] encryptBlock(byte[] initial);

    public byte[] decryptBlock(byte[] initial);
}
