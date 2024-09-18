package cipher.modes;

import cipher.ICipherMode;
import cipher.ISymmCipher;
import cipher.algoritms.operations.BitOperations;

import java.util.Arrays;
import java.util.List;
import java.util.function.UnaryOperator;

public class OFB implements ICipherMode {
    @Override
    public byte[] encryptWithMode(byte[] text, byte[] IV, List<String> parameters, ISymmCipher algorithm, int blockSize) {
//        byte[] result = new byte[text.length];
//        byte[] prevBlock = IV;
//        int blocksCount = text.length / blockSize;
//
//        for (int i = 0; i < blocksCount; i++) {
//            int idx = i * blockSize;
//            byte[] block = Arrays.copyOfRange(text, idx, idx + blockSize);
//            prevBlock = algorithm.encryptBlock(prevBlock);
//            byte[] encryptedBlock = BitOperations.byteArrayXOR(block, prevBlock);
//            System.arraycopy(encryptedBlock, 0, result, idx, encryptedBlock.length);
//        }

        return handler(text, IV, parameters, algorithm::encryptBlock, blockSize);
    }

    @Override
    public byte[] decryptWithMode(byte[] cipheredText, byte[] IV, List<String> parameters, ISymmCipher algorithm, int blockSize) {
//        byte[] result = new byte[cipheredText.length];
//        byte[] prevBlock = IV;
//        int blocksCount = cipheredText.length / blockSize;
//
//        for (int i = 0; i < blocksCount; i++) {
//            int idx = i * blockSize;
//            byte[] block = Arrays.copyOfRange(cipheredText, idx, idx + blockSize);
//            prevBlock = algorithm.encryptBlock(prevBlock);
//            byte[] decryptedBlock = BitOperations.byteArrayXOR(block, prevBlock);
//            System.arraycopy(decryptedBlock, 0, result, idx, decryptedBlock.length);
//        }

        return handler(cipheredText, IV, parameters, algorithm::encryptBlock, blockSize);
    }

    private byte[] handler(byte[] cipheredText, byte[] IV, List<String> parameters, UnaryOperator<byte[]> cipherFunction, int blockSize){
        byte[] result = new byte[cipheredText.length];
        byte[] prevBlock = IV;
        int blocksCount = cipheredText.length / blockSize;

        for (int i = 0; i < blocksCount; i++) {
            int idx = i * blockSize;
            byte[] block = Arrays.copyOfRange(cipheredText, idx, idx + blockSize);
            prevBlock = cipherFunction.apply(prevBlock);
            byte[] decryptedBlock = BitOperations.byteArrayXOR(block, prevBlock);
            System.arraycopy(decryptedBlock, 0, result, idx, decryptedBlock.length);
        }

        return result;
    }
}
