package cipher.algoritms.rc5;


import org.javatuples.Pair;

import java.util.Map;



public class RC5service {
    private final int blockSizeInBits;
    private final int lenKeyInBits;
    private final int countRounds;
    private static final Map<Integer, Pair<Long, Long>> CONSTANTS_FOR_KEY = Map.of(
            16, new Pair<>(0xB7E1L, 0x9E37L),
            32, new Pair<>(0xB7E15163L, 0x9E3779B9L),
            64, new Pair<>(0xB7E151628AED2A6BL, 0x9E3779B97F4A7C15L)
    );

    public RC5service(int blockSizeInBits, int lenKeyInBits, int countRounds) {
        this.blockSizeInBits = blockSizeInBits;
        this.lenKeyInBits = lenKeyInBits;
        this.countRounds = countRounds;
    }

    public long[] expandKey(byte[] key) {
        long[] L_splitKey = splitKeyIntoWords(key); // массив L_i
        long[] S_extendedKeyTable = buildExtendedKeyTable(); // массив S_i
        int w = blockSizeInBits / 2; // размер блока (слова)
        int c = L_splitKey.length; // (lenKeyInBits + w - 1) / w
        int sizeExtendedKeyTable = L_splitKey.length; // 2 * (R + 1)

        long G = 0, H = 0;
        int i = 0, j = 0;
        for (int k = 0; k < Integer.max(3 * c, 3 * sizeExtendedKeyTable); k++) {
            G = S_extendedKeyTable[i] = BinaryOperations.leftCycleShift(
                    BinaryOperations.sumModule(
                            BinaryOperations.sumModule(S_extendedKeyTable[i], G, w),
                            H, w),
                    w,3);

            H = L_splitKey[j] = BinaryOperations.leftCycleShift(
                    BinaryOperations.sumModule(
                            BinaryOperations.sumModule(L_splitKey[j], G, w),
                            H, w),
                    w, BinaryOperations.sumModule(G, H, w));

            i = (i + 1) % sizeExtendedKeyTable;
            j = (j + 1) % c;
        }

        return S_extendedKeyTable;
    }

    private long[] splitKeyIntoWords(byte[] key) {
        // с = b / u; u = w / 8
        // => с = (b * 8) / w = (lenKeyInBits + w - 1) / w
        int w = blockSizeInBits / 2; // размер слова
        int c = (lenKeyInBits + w - 1) / w; // кол-во слов
        long[] result = new long[c];

        for (int i = 0; i < c; i++) {
            result[i] = BinaryOperations.getBits(key, i * w, w);
        }

        return result;
    }

    private long[] buildExtendedKeyTable() {
        int w = blockSizeInBits / 2; // размер блока (слова)
        long P = CONSTANTS_FOR_KEY.get(w).getValue0();
        long Q = CONSTANTS_FOR_KEY.get(w).getValue1();
        int c = 2 * (countRounds + 1); // размер таблицы S
        long[] result = new long[c];

        result[0] = P;
        for (int i = 1; i < c; i++) {
            result[i] = BinaryOperations.sumModule(result[i - 1], Q, w);
        }

        return result;
    }

    public long getLongFromHalfBlock(byte[] inputBlock, int start, int end) {
        byte[] result = new byte[end - start];
        System.arraycopy(inputBlock, start, result, 0, end - start);

        return BinaryOperations.bytesToLong(result);
    }

    public byte[] twoLongPartToByteArray(long left, long right, int sizeResult) {
        byte[] leftResult = new byte[sizeResult / 2];
        byte[] rightResult = new byte[sizeResult / 2];

        for (int i = 0; i < sizeResult / 2; i++) {
            leftResult[sizeResult / 2 - i - 1] = (byte) ((left >> (i * Byte.SIZE)) & ((1 << Byte.SIZE) - 1));
            rightResult[sizeResult / 2 - i - 1] = (byte) ((right >> (i * Byte.SIZE)) & ((1 << Byte.SIZE) - 1));
        }

        byte[] result = new byte[sizeResult];
        System.arraycopy(leftResult, 0, result, 0, sizeResult / 2);
        System.arraycopy(rightResult, 0, result, sizeResult / 2, sizeResult / 2);

        return result;
    }
}
