package cipher.algoritms;

import cipher.ICipherMode;
import cipher.ICipherService;
import cipher.IPadding;
import cipher.ISymmCipher;
import lombok.Setter;

@Setter
public class CipherService implements ICipherService {
    private ICipherMode cipherMode;
    private IPadding padding;
    private ISymmCipher cipher;
    private int blockSizeBits;
    private int keySizeBits;

    public CipherService(){}

    public void setCipherSettings(String paddings, String cipherModes, String ciphers, int blockSizeBits, int keySizeBits, byte[] IV){}



    @Override
    public byte[] encrypt(byte[] data) {
        return new byte[0];
    }

    @Override
    public byte[] decrypt(byte[] data) {
        return new byte[0];
    }
}
