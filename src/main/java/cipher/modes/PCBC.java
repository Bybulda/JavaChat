package cipher.modes;

import cipher.ICipherMode;
import cipher.ISymmCipher;
import cipher.algoritms.operations.BitOperations;

import java.util.Arrays;
import java.util.List;

public class PCBC implements ICipherMode {
    @Override
    public byte[] encryptWithMode(byte[] text, byte[] IV, List<String> parameters, ISymmCipher algorithm, int blockSize) {
        byte[] result = new byte[text.length];
        byte[] prevBlock = IV;
        int blocksCount = text.length / blockSize;

        for (int i = 0; i < blocksCount; i++) {
            int idx = i * blockSize;
            byte[] block = Arrays.copyOfRange(text, idx, idx + blockSize);
            byte[] encryptedBlock = algorithm.encryptBlock(BitOperations.byteArrayXOR(block, prevBlock));
            System.arraycopy(encryptedBlock, 0, result, idx, encryptedBlock.length);
            prevBlock = BitOperations.byteArrayXOR(encryptedBlock, block);
        }

        return result;
    }

    @Override
    public byte[] decryptWithMode(byte[] cipheredText, byte[] IV, List<String> parameters, ISymmCipher algorithm, int blockSize) {
        byte[] result = new byte[cipheredText.length];
        byte[] prevBlock = IV;
        int blocksCount = cipheredText.length / blockSize;

        for (int i = 0; i < blocksCount; i++) {
            int idx = i * blockSize;
            byte[] block = Arrays.copyOfRange(cipheredText, idx, idx + blockSize);
            byte[] decryptedBlock = BitOperations.byteArrayXOR(algorithm.decryptBlock(block), prevBlock);
            System.arraycopy(decryptedBlock, 0, result, idx, decryptedBlock.length);
            prevBlock =BitOperations.byteArrayXOR(decryptedBlock, block);
        }

        return result;
    }

}
