package cipher.algoritms.macguffin;

import cipher.algoritms.macguffin.constants.MagicConstants;
import cipher.IKeyExpansion;
import cipher.ISymmCipher;

public class MacGuffinCipher implements ISymmCipher, IKeyExpansion {

    private final int rounds = 32;

    private char[][] keysRounds = new char[32][3];

    public MacGuffinCipher() {}

    @Override
    public void setKey(byte[] key) {
        setKeyKey(key);
    }

    @Override
    public byte[] encryptBlock(byte[] initial) {
        return encryptBlock(initial, keysRounds);
    }

    @Override
    public byte[] decryptBlock(byte[] initial) {
        return decryptBlock(initial, keysRounds);
    }

    private byte getBitForPosition(char word, int position) {
        return (byte) ((word >> position) & 1);
    }

    public byte getOuterBits(byte word, int size) {
        int left = word >> (size - 1);
        int right = word & 1;
        return (byte) ((left << 1) | right);
    }

    public byte getInnerBits(byte word, int size) {
        return (byte) ((word >> 1) & 0b1111);
    }

    public byte getSixBitsFromSBox(char a, char b, char c, int sBoxNum) {
        byte result = 0;
        int count = 2;
        char[] collected = {a, b, c};
        int[] places = MagicConstants.sBits[sBoxNum];
        for (int i = 5; i >= 0; i -= 2) {
            char currentChar = collected[count--];
            result <<= 1;
            result |= getBitForPosition(currentChar, places[i]);
            result <<= 1;
            result |= getBitForPosition(currentChar, places[i - 1]);

        }
        return result;
    }

    // works consistent
    public char[] bytesToChars(byte[] byteArray) {
        if (byteArray.length != 8) {
            throw new IllegalArgumentException("Array must be exactly 8 bytes long.");
        }

        char[] chars = new char[4];

        for (int i = 0; i < 4; i++) {
            // Каждое слово char состоит из двух байт
            int index = i * 2;
            // Объединяем два байта в один char
            chars[i] = (char) (((byteArray[index] & 0xFF) << 8) | (byteArray[index + 1] & 0xFF));
        }

        return chars;
    }

    // works consistant
    public byte[] charsToBytes(char[] charArray) {
        // Массив байтов должен быть в два раза длиннее массива символов
        byte[] byteArray = new byte[charArray.length * 2];

        for (int i = 0; i < charArray.length; i++) {

            byteArray[i * 2] = (byte) ((charArray[i] >> 8) & 0xFF);

            byteArray[i * 2 + 1] = (byte) (charArray[i] & 0xFF);
        }

        return byteArray;
    }

    public long byteArrayToLong(byte[] byteArray) {
        if (byteArray.length != 8) {
            throw new IllegalArgumentException("Массив должен содержать 8 байт");
        }

        long result = 0;
        for (int i = 0; i < 8; i++) {
            result = (result << 8) | (byteArray[i] & 0xFF);
        }

        return result;
    }

    public byte[] encryptBlock(byte[] block, char[][] key) {
        char[] words = bytesToChars(block);
        char left = words[0], a = words[1], b = words[2], c = words[3];
        for (int i = 0; i < rounds; i++) {
            left = getLeftForRound(a, b, c, key, left, i);
            char tmp = left;
            left = a;
            a = b;
            b = c;
            c = tmp;
        }
        return charsToBytes(new char[]{left, a, b, c});
    }

    private byte changeBytes(byte word){
        return (byte) ((word >> 1) | ((word & 1) << 1));
    }

    public byte[] decryptBlock(byte[] block, char[][] key) {
        char[] words = bytesToChars(block);
        char left = words[3], a = words[0], b = words[1], c = words[2];
        for (int i = 31; i >= 0; i--) {
            left = getLeftForRound(a, b, c, key, left, i);
            char tmp = left;
            char tmpa = a;
            char tmpb = b;
            left = c;
            a = tmp;
            b = tmpa;
            c = tmpb;
        }
        return charsToBytes(new char[]{a, b, c, left});
    }

    // works consistant
    private char getLeftForRound(char a, char b, char c, char[][] key, char left, int index) {
        char t = 0;
        for (int j = 0; j < 8; j++) {
            byte positions = getSixBitsFromSBox((char) (a ^ key[index][0]), (char) (b ^ key[index][1]), (char) (c ^ key[index][2]), j);
            byte bitsToHav = (byte) MagicConstants.gufnSBox[j][positions];
            bitsToHav = changeBytes(bitsToHav);

            t |= (char) (bitsToHav << (j * 2));
        }
        left ^= t;
        return left;
    }

    // seem right
    public void setKeyKey(byte[] key){
        byte[] leftKey = new byte[8];
        byte[] rightKey = new byte[8];
        System.arraycopy(key, 0, leftKey, 0, 8);
        System.arraycopy(key, 8, rightKey, 0, 8);
        for (int i = 0; i < 32; i++) {
            leftKey = encryptBlock(leftKey, keysRounds);
            char[] keysChars = bytesToChars(leftKey);
            keysRounds[i][0] = keysChars[0];
            keysRounds[i][1] = keysChars[1];
            keysRounds[i][2] = keysChars[2];
        }
        for (int i = 0; i < 32; i++) {
            rightKey = encryptBlock(rightKey, keysRounds);
            char[] keysChars = bytesToChars(rightKey);
            keysRounds[i][0] = (char) (keysRounds[i][0] ^ keysChars[0]);
            keysRounds[i][1] = (char) (keysRounds[i][1] ^ keysChars[1]);
            keysRounds[i][2] = (char) (keysRounds[i][2] ^ keysChars[2]);
        }

    }

}
