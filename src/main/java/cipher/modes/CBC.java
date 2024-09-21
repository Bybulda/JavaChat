package cipher.modes;

import cipher.ICipherMode;
import cipher.ISymmCipher;
import cipher.algoritms.operations.BitOperations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
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
            byte[] encryptedBlock = algorithm.encryptBlock(BitOperations.byteArrayXOR(block, prevBlock));
            System.arraycopy(encryptedBlock, 0, result, idx, encryptedBlock.length);
            prevBlock = encryptedBlock;
        }

        return result;
    }

//    @Override
//    public byte[] decryptWithMode(byte[] cipheredText, byte[] IV, List<String> parameters, ISymmCipher algorithm, int blockSize) {
//        byte[] result = new byte[cipheredText.length];
//        IntStream.range(0, cipheredText.length / blockSize)
//                .parallel()
//                .forEach(i -> {
//                    int idx = i * blockSize;
//                    int dop = (i - 1) * blockSize;
//                    byte[] prevBlock = (i == 0) ? IV : Arrays.copyOfRange(cipheredText, dop, idx);
//                    byte[] block = Arrays.copyOfRange(cipheredText, idx, idx + blockSize);
//                    byte[] decryptedBlock = BitOperations.byteArrayXOR(prevBlock, algorithm.decryptBlock(block));
//                    System.arraycopy(decryptedBlock, 0, result, idx, decryptedBlock.length);
//                });
//        return result;
//    }
private void decryptProcess(byte[] input, byte[] output, int i, int lengthBlock, byte[] IV, ISymmCipher algorithm) {
    byte[] prev = new byte[lengthBlock];
    if (i == 0) {
        prev = IV;
    } else {
        System.arraycopy(
                input, (i - 1) * lengthBlock,
                prev, 0,
                lengthBlock
        );
    }
    int offset = i * lengthBlock;
    byte[] block = new byte[lengthBlock];
    System.arraycopy(input, offset, block, 0, lengthBlock);
    byte[] decryptedBlock = BitOperations.byteArrayXOR(prev, algorithm.decryptBlock(block));
    System.arraycopy(decryptedBlock, 0, output, offset, decryptedBlock.length);
}

    @Override
    public byte[] decryptWithMode(byte[] data, byte[] IV, List<String> parameters, ISymmCipher algorithm, int blockSize) {
        try  {
            var executor = Executors.newFixedThreadPool(
                    Runtime.getRuntime().availableProcessors() - 1);
            byte[] result = new byte[data.length];
            List<CompletableFuture<Void>> futures = new ArrayList<>();
            for (int i = 0; i < data.length / blockSize; i++) {
                int finalI = i;
                futures.add(
                        CompletableFuture.runAsync(
                                () -> decryptProcess(data, result, finalI, blockSize, IV, algorithm), executor
                        ));
            }
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

            executor.shutdown();
            try {
                if (!executor.awaitTermination(2, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
                return result;
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
                return result;
            }
        } catch (Exception e) {
            System.out.println("Error: " + e);
            return data;
        }
    }
}
