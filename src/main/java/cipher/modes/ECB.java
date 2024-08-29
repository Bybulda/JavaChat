package cipher.modes;

import cipher.ICipherMode;
import cipher.ISymmCipher;

import java.util.Arrays;
import java.util.List;
import java.util.function.UnaryOperator;
import java.util.stream.IntStream;

public class ECB implements ICipherMode {
    @Override
    public byte[] encryptWithMode(byte[] text, byte[] IV, List<String> parameters, ISymmCipher algorithm, int blockSize) {
//        byte[] result = new byte[text.length];
//        IntStream.range(0, text.length / blockSize)
//                .parallel()
//                .forEach(i -> {
//                    int idx = i * blockSize;
//                    byte[] block = Arrays.copyOfRange(text, idx, idx + blockSize);
//                    byte[] encryptedBlock = algorithm.encryptBlock(block);
//                    System.arraycopy(encryptedBlock, 0, result, idx, encryptedBlock.length);
//                });
        return handler(text, IV, parameters, algorithm::encryptBlock, blockSize);
    }

    @Override
    public byte[] decryptWithMode(byte[] cipheredText, byte[] IV, List<String> parameters, ISymmCipher algorithm, int blockSize) {
//        byte[] result = new byte[cipheredText.length];
//        IntStream.range(0, cipheredText.length / blockSize)
//                .parallel()
//                .forEach(i -> {
//                    int idx = i * blockSize;
//                    byte[] block = Arrays.copyOfRange(cipheredText, idx, idx + blockSize);
//                    byte[] decryptedBlock = algorithm.decryptBlock(block);
//                    System.arraycopy(decryptedBlock, 0, result, idx, decryptedBlock.length);
//                });
        return handler(cipheredText, IV, parameters, algorithm::decryptBlock, blockSize);
    }

    private byte[] handler(byte[] cipheredText, byte[] IV, List<String> parameters, UnaryOperator<byte[]> cipherFunction, int blockSize){
        byte[] result = new byte[cipheredText.length];
        IntStream.range(0, cipheredText.length / blockSize)
                .parallel()
                .forEach(i -> {
                    int idx = i * blockSize;
                    byte[] block = Arrays.copyOfRange(cipheredText, idx, idx + blockSize);
                    byte[] decryptedBlock = cipherFunction.apply(block);
                    System.arraycopy(decryptedBlock, 0, result, idx, decryptedBlock.length);
                });
        return result;
    }
}
