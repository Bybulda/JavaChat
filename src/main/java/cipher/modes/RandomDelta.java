package cipher.modes;

import cipher.ICipherMode;
import cipher.ISymmCipher;
import cipher.algoritms.operations.BitOperations;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.IntStream;

public class RandomDelta implements ICipherMode {
    @Override
    public byte[] encryptWithMode(byte[] text, byte[] IV, List<String> parameters, ISymmCipher algorithm, int blockSize) {
        byte[] result = new byte[text.length];
        BigInteger delta = new BigInteger(Arrays.copyOf(IV, IV.length / 2));
        BigInteger initial = new BigInteger(IV);

        IntStream.range(0, text.length / blockSize)
                .parallel()
                .forEach(i -> {
                    int idx = i * blockSize;
                    byte[] block = Arrays.copyOfRange(text, idx, idx + blockSize);
                    BigInteger initialDelta = initial.add(delta.multiply(BigInteger.valueOf(i)));
                    byte[] encryptedBlock = algorithm.encryptBlock(BitOperations.byteArrayXOR(initialDelta.toByteArray(), block));
                    System.arraycopy(encryptedBlock, 0, result, idx, encryptedBlock.length);
                });

        return result;
    }

    @Override
    public byte[] decryptWithMode(byte[] cipheredText, byte[] IV, List<String> parameters, ISymmCipher algorithm, int blockSize) {
        byte[] result = new byte[cipheredText.length];
        BigInteger delta = new BigInteger(Arrays.copyOf(IV, IV.length / 2));
        BigInteger initial = new BigInteger(IV);

        IntStream.range(0, cipheredText.length / blockSize)
                .parallel()
                .forEach(i -> {
                    int idx = i * blockSize;
                    byte[] block = Arrays.copyOfRange(cipheredText, idx, idx + blockSize);
                    BigInteger initialDelta = initial.add(delta.multiply(BigInteger.valueOf(i)));
                    byte[] decryptedBlock = BitOperations.byteArrayXOR(algorithm.decryptBlock(block), initialDelta.toByteArray());
                    System.arraycopy(decryptedBlock, 0, result, idx, decryptedBlock.length);
                });


        return result;
    }

    private byte[] handler(byte[] cipheredText, byte[] IV, List<String> parameters, UnaryOperator<byte[]> cipherFunction, int blockSize){
        byte[] result = new byte[cipheredText.length];
        BigInteger delta = new BigInteger(Arrays.copyOf(IV, IV.length / 2));
        BigInteger initial = new BigInteger(IV);

        IntStream.range(0, cipheredText.length / blockSize)
                .parallel()
                .forEach(i -> {
                    int idx = i * blockSize;
                    byte[] block = Arrays.copyOfRange(cipheredText, idx, idx + blockSize);
                    BigInteger initialDelta = initial.add(delta.multiply(BigInteger.valueOf(i)));
                    byte[] decryptedBlock = BitOperations.byteArrayXOR(cipherFunction.apply(block), initialDelta.toByteArray());
                    System.arraycopy(decryptedBlock, 0, result, idx, decryptedBlock.length);
                });

        return result;
    }
}
