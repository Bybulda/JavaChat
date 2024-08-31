package cipher.algoritms.macguffin;

import cipher.algoritms.macguffin.constants.MagicConstants;
import cipher.IKeyExpansion;
import cipher.ISymmCipher;

public class MacGuffinCipher implements ISymmCipher, IKeyExpansion {

    private final int rounds = 32;

    private final int keySize = rounds * 3;

    private final char[] keys = new char[keySize];

    private final int tSize = 1 << 16;

    private final int[] sTable = new int[tSize];

    private int getIndexSBox(int i, int j){
        int result = 0;
        for (int k = 0; k < 6; k++) {
            result |= ((i >>> MagicConstants.sBits[j][k]) & 1) << k;
        }
        return result;
    }

    private void initTable(){
        for(int i = 0; i < tSize; i++){
            for (int j = 0; j < 8; j++) {
                sTable[i] |= MagicConstants.sBoxes[j][getIndexSBox(i, j)];
            }
        }
    }

    public MacGuffinCipher(byte[] key){
        setKey(key);
    }

    private char getRforRound(char a, char b, char c){
        return (char) (
                (MagicConstants.OUT0 & sTable[(a & MagicConstants.IN00) | (b & MagicConstants.IN00) | (c & MagicConstants.IN00)]) |
                (MagicConstants.OUT1 & sTable[(a & MagicConstants.IN10) | (b & MagicConstants.IN11) | (c & MagicConstants.IN12)]) |
                (MagicConstants.OUT2 & sTable[(a & MagicConstants.IN20) | (b & MagicConstants.IN21) | (c & MagicConstants.IN22)]) |
                (MagicConstants.OUT3 & sTable[(a & MagicConstants.IN30) | (b & MagicConstants.IN31) | (c & MagicConstants.IN32)])
        );
    }

    private byte[] getBytes(byte[] initial, char[] r) {
        for (int j = 0; j < 8; j++) {
            initial[j] = (j & 1) == 0 ? (byte) (r[j / 2]) : (byte) (r[(j - 1) / 2]);

        }
        return initial;
    }

    @Override
    public void setKey(byte[] key) {
        int i, j;
        byte[][] k = new byte[2][8];
        initTable();

        System.arraycopy(key, 0, k[0], 0, 8);
        System.arraycopy(key, 8, k[1], 0, 8);

        for (i = 0; i < keySize; i++) {
            keys[i] = 0;
        }

        for (i = 0; i < 2; i++) {
            for (j = 0; j < 32; j++) {
                encryptBlock(k[i]);
                keys[j * 3] ^= (char) ((k[i][0] & 0xFF) | ((k[i][1] & 0xFF) << 8));
                keys[j * 3 + 1] ^= (char) ((k[i][2] & 0xFF) | ((k[i][3] & 0xFF) << 8));
                keys[j * 3 + 2] ^= (char) ((k[i][4] & 0xFF) | ((k[i][5] & 0xFF) << 8));
            }
        }
    }

    @Override
    public byte[] encryptBlock(byte[] initial) {
        char[] r = new char[4];

        for (int i = 0; i < 4; i++) {
            r[i] = (char) (initial[i * 2] & 0xFF | (initial[i * 2 + 1] << 8));
        }
//        char r0 = (char) ((initial[0] | (initial[1] << 8)));
//        char r1= (char) ((initial[2] | (initial[3] << 8)));
//        char r2 = (char) (initial[4] | (initial[5] << 8));
//        char r3 = (char) (initial[6] | (initial[7] << 8))
        char a, b, c, i = 0, counter = 0;
        for (; i < (rounds / 4);i++) {
            for (int j = 0; j < 4; j++){
                a = (char) (r[(j + 1) % 4] & keys[counter++]);
                b = (char) (r[(j + 2) % 4] & keys[counter++]);
                c = (char) (r[(j + 3) % 4] & keys[counter++]);
                r[j] ^= getRforRound(a, b, c);
            }
//            a = (char) (r1 & keys[counter++]);
//            b = (char) (r2 & keys[counter++]);
//            c = (char) (r3 & keys[counter++]);
//            r0 ^= getRforRound(a, b, c);
//            a = (char) (r2 & keys[counter++]);
//            b = (char) (r3 & keys[counter++]);
//            c = (char) (r0 & keys[counter++]);
//            r1 ^= getRforRound(a, b, c);
//            a = (char) (r3 & keys[counter++]);
//            b = (char) (r0 & keys[counter++]);
//            c = (char) (r1 & keys[counter++]);
//            r2 ^= getRforRound(a, b, c);
//            a = (char) (r0 & keys[counter++]);
//            b = (char) (r1 & keys[counter++]);
//            c = (char) (r2 & keys[counter++]);
//            r3 ^= getRforRound(a, b, c);
        }
        return getBytes(initial, r);
    }

    @Override
    public byte[] decryptBlock(byte[] initial) {
        char[] r = new char[4];
        for (int i = 0; i < 4; i++) {
            r[i] = (char) (initial[i * 2] | (initial[i * 2 + 1] << 8));
        }
        char a, b, c, i = 0, counter = keySize;
        for (; i < (rounds / 4);i++) {
            for (int j = 3; j > -1; j--){
                a = (char) (r[(j + 1) % 4] & keys[--counter]);
                b = (char) (r[(j + 2) % 4] & keys[--counter]);
                c = (char) (r[(j + 3) % 4] & keys[--counter]);
                r[j] ^= getRforRound(a, b, c);
            }

        }
        return getBytes(initial, r);
    }
}
