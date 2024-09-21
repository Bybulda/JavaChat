package cipher.modes;

import cipher.ICipherMode;
import cipher.ISymmCipher;
import cipher.algoritms.operations.BitOperations;
import cipher.algoritms.rc5.BinaryOperations;

import javax.crypto.Cipher;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.IntStream;

public class CTR implements ICipherMode {
    @Override
    public byte[] encryptWithMode(byte[] text, byte[] IV, List<String> parameters, ISymmCipher algorithm, int blockSize) {
//        byte[] result = new byte[text.length];
//        int length = blockSize / 2;
//
//        IntStream.range(0, text.length / blockSize)
//                .parallel()
//                .forEach(i -> {
//                    int idx = i * blockSize;
//                    byte[] block = Arrays.copyOfRange(text, idx, idx + blockSize);
//                    byte[] toEncrypt = new byte[blockSize];
//                    System.arraycopy(IV, 0, toEncrypt, 0, length);
//
//                    System.arraycopy(BitOperations.longToBytes(i, 4), 0, toEncrypt, toEncrypt.length - Integer.BYTES, length);
//                    byte[] encryptedBlock = BitOperations.byteArrayXOR(block, algorithm.encryptBlock(toEncrypt));
//                    System.arraycopy(encryptedBlock, 0, result, idx, encryptedBlock.length);
//                });

        return handler(text, IV, parameters, algorithm::encryptBlock, blockSize);
    }

    @Override
    public byte[] decryptWithMode(byte[] cipheredText, byte[] IV, List<String> parameters, ISymmCipher algorithm, int blockSize) {
//        byte[] result = new byte[cipheredText.length];
//        int length = blockSize / 2;
//
//        IntStream.range(0, cipheredText.length / blockSize)
//                .parallel()
//                .forEach(i -> {
//                    int idx = i * blockSize;
//                    byte[] block = Arrays.copyOfRange(cipheredText, idx, idx + blockSize);
//                    byte[] toDecrypt = new byte[blockSize];
//                    System.arraycopy(IV, 0, toDecrypt, 0, length);
//
//                    System.arraycopy(BitOperations.longToBytes(i, 4), 0, toDecrypt, toDecrypt.length - Integer.BYTES, length);
//                    byte[] decryptedBlock = BitOperations.byteArrayXOR(block, algorithm.encryptBlock(toDecrypt));
//                    System.arraycopy(decryptedBlock, 0, result, idx, decryptedBlock.length);
//                });

        return handler(cipheredText, IV, parameters, algorithm::encryptBlock, blockSize);
    }

    private byte[] handler(byte[] cipheredText, byte[] IV, List<String> parameters, UnaryOperator<byte[]> cipherFunction, int blockSize){
        byte[] result = new byte[cipheredText.length];
        int length = blockSize / 2;

        IntStream.range(0, cipheredText.length / blockSize)
                .parallel()
                .forEach(i -> {
                    int idx = i * blockSize;
                    byte[] block = Arrays.copyOfRange(cipheredText, idx, idx + blockSize);
                    byte[] toDecrypt = new byte[blockSize];
                    System.arraycopy(IV, 0, toDecrypt, 0, length);
                    byte[] counter = new byte[length];
                    for (int j = 0; j < length; j++) {
                        counter[j] = (byte) (i >> ((length - 1) - j) * 8);
                    }

                    System.arraycopy(counter, 0, toDecrypt, length, length);
                    byte[] decryptedBlock = BitOperations.byteArrayXOR(block, cipherFunction.apply(toDecrypt));
                    System.arraycopy(decryptedBlock, 0, result, idx, decryptedBlock.length);
                });

        return result;
    }
}
