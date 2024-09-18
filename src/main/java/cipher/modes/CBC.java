package cipher.modes;

import cipher.ICipherMode;
import cipher.ISymmCipher;
import cipher.algoritms.operations.BitOperations;

import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

public class CBC implements ICipherMode {
    @Override
    public byte[] encryptWithMode(byte[] text, byte[] IV, List<String> parameters, ISymmCipher algorithm, int blockSize) {
        byte[] prevBlock = IV;
        byte[] result = new byte[text.length];
        int blocksCount = text.length / blockSize;

        for (int i = 0; i < blocksCount; i++) {
            int idx = i * blockSize;
            byte[] block = Arrays.copyOfRange(text, idx, idx + blockSize);
            byte[] encryptedBlock = algorithm.encryptBlock(BitOperations.byteArrayXOR(prevBlock, block));
            System.arraycopy(encryptedBlock, 0, result, idx, encryptedBlock.length);
            prevBlock = encryptedBlock;
        }

        return result;
    }

    @Override
    public byte[] decryptWithMode(byte[] cipheredText, byte[] IV, List<String> parameters, ISymmCipher algorithm, int blockSize) {
        byte[] result = new byte[cipheredText.length];
        IntStream.range(0, cipheredText.length / blockSize)
                .parallel()
                .forEach(i -> {
                    int idx = i * blockSize;
                    byte[] prevBlock = (i == 0) ? IV : Arrays.copyOfRange(cipheredText, idx - blockSize, idx);
                    byte[] block = Arrays.copyOfRange(cipheredText, idx, idx + blockSize);
                    byte[] decryptedBlock = BitOperations.byteArrayXOR(prevBlock, algorithm.decryptBlock(block));
                    System.arraycopy(decryptedBlock, 0, result, idx, decryptedBlock.length);
                });
        return result;
    }
}
