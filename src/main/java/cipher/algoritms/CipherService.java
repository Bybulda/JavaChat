package cipher.algoritms;

import cipher.*;
import cipher.algoritms.macguffin.MacGuffinCipher;
import cipher.algoritms.rc5.RC5;
import cipher.algoritms.rc5.util.RC5Cipher;
import cipher.modes.*;
import cipher.padding.ANSIX923;
import cipher.padding.ISO10126;
import cipher.padding.PKCS7;
import cipher.padding.ZerosPadding;
import lombok.Setter;

import java.util.List;

@Setter
public class CipherService implements ICipherService {
    private ICipherMode cipherMode;
    private IPadding padding;
    private ISymmCipher cipher;
    private byte[] IV;
    private int rounds;
    private int blockSizeBits;
    private int keySizeBits;

    public CipherService(){}

    @Override
    public void setCipherSettings(String paddings, String cipherModes, String ciphers, int blockSizeBits, int keySizeBits, int rounds, byte[] IV){
        this.IV = IV;
        this.blockSizeBits = blockSizeBits;
        this.keySizeBits = keySizeBits;
        this.rounds = rounds;
        cipher = getSymmCipher(ciphers, blockSizeBits, keySizeBits, rounds);
        padding = getPadding(paddings);
        cipherMode = getCipherMode(cipherModes);
    }

    @Override
    public void setCipherKey(byte[] key) {
        ((IKeyExpansion)cipher).setKey(key);
    }

    private ISymmCipher getSymmCipher(String ciphers, int blockSizeBits, int keySizeBits, int rounds){
        if (ciphers.equals("MACGUFFIN")) {
            return new MacGuffinCipher();
        }
        return new RC5(blockSizeBits, rounds, keySizeBits);
    }

    private IPadding getPadding(String paddings){
        return switch (paddings) {
            case "ANSIX923" -> new ANSIX923();
            case "ISO10126" -> new ISO10126();
            case "PKCS7" -> new PKCS7();
            case "Zeros" -> new ZerosPadding();
            default -> null;
        };
    }

    private ICipherMode getCipherMode(String cipherModes){
        return switch (cipherModes){
            case "CBC" -> new CBC();
            case "ECB" -> new ECB();
            case "CFB" -> new CFB();
            case "CTR" -> new CTR();
            case "OFB" -> new OFB();
            case "PCBC" -> new PCBC();
            case "RANDOMDELTA" -> new RandomDelta();
            default -> null;
        };
    }

    @Override
    public byte[] encrypt(byte[] data) {
        byte[] paddedData = padding.makeWithPadding(data, blockSizeBits / 8);
        return cipherMode.encryptWithMode(paddedData, IV, List.of(""), cipher, blockSizeBits / 8);
    }

    @Override
    public byte[] decrypt(byte[] data) {
        byte[] decryptedData = cipherMode.decryptWithMode(data, IV, List.of(""), cipher, blockSizeBits / 8);
        return padding.removePadding(decryptedData);
    }
}
