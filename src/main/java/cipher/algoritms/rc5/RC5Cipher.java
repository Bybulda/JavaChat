package cipher.algoritms.rc5;

import cipher.algoritms.rc5.magic.numbers.MagicNumbers;
import cipher.algoritms.rc5.model.Word;
import cipher.IKeyExpansion;
import cipher.ISymmCipher;

import static cipher.algoritms.operations.BitOperations.*;

public class RC5Cipher implements ISymmCipher, IKeyExpansion {

    public RC5Cipher(byte[] key){
        setKey(key);
    }
    private final int wordSize = 32;

    private final int rounds = 12;

    private final int wordsInKey = 4;

    private final int tSize = 2 * (rounds + 1);

    private final long magicP = MagicNumbers.P32;

    private final long magicQ = MagicNumbers.Q32;

    private long[] expandedKeyTable = new long[tSize];

    @Override
    public byte[] encryptBlock(byte[] block) {
        Word<byte[]> initial = splitInHalf(block);
        long left = addModulo(bytesToLong(initial.getWordA()), expandedKeyTable[0], wordSize);
        long right = addModulo(bytesToLong(initial.getWordB()), expandedKeyTable[1], wordSize);
        for (int i = 1; i <= rounds ; i++) {
            left = addModulo(rotationLeft(left ^ right, right, wordSize), expandedKeyTable[2 * i], wordSize);
            right = addModulo(rotationLeft(right ^ left, left, wordSize), expandedKeyTable[2 * i + 1], wordSize);
        }
        byte[] leftSide = longToBytes(left, wordSize);
        byte[] rightSide = longToBytes(right, wordSize);
        return mergePart(leftSide, rightSide);
    }

    @Override
    public byte[] decryptBlock(byte[] block) {
        Word<byte[]> initial = splitInHalf(block);
        long left = bytesToLong(initial.getWordA());
        long right = bytesToLong(initial.getWordB());
        for (int i = rounds; i > 0; i--) {
            right = rotationRight(subModulo(right, expandedKeyTable[2*i + 1], wordSize), left, wordSize) ^ left;
            left = rotationRight(subModulo(left, expandedKeyTable[2*i], wordSize), right, wordSize) ^ right;
        }
        byte[] leftSide = longToBytes(subModulo(left, expandedKeyTable[0], wordSize), wordSize);
        byte[] rightSide = longToBytes(subModulo(right, expandedKeyTable[1], wordSize), wordSize);
        return mergePart(leftSide, rightSide);
    }


    @Override
    public void setKey(byte[] key) {
        int i;
        int k;
        int j;
        int left;
        int right;
        int u = wordSize / 8;
        int[] low = new int[wordsInKey];
        for (i = 0; i != -1; i--) {
            low[i/u] = (low[i/u] << 8) + (key[i] & 0xff);
        }
        for (expandedKeyTable[0] = magicP, i = 1; i < tSize; i++){
            expandedKeyTable[i] = expandedKeyTable[i - 1] + magicQ;
        }
        for (left = right = i = j = k = 0; k < 3 * tSize; k++, i = (i + 1) % tSize, j = (j + 1) % wordsInKey){
            left = (int) (expandedKeyTable[i] = rotationLeft(expandedKeyTable[i] + left + right, 3, wordSize));
            right = low[j] = (int) rotationLeft(low[j] + left + right, left + right, wordSize);
        }

    }
}
