package cipher.diffie_hellman;

import java.math.BigInteger;
import java.util.Random;

public class DiffieHellman {
    private DiffieHellman() {

    }

    private static final Random RANDOM = new Random();
    private static final int[] VALUES = new int[]{2, 3, 5, 7, 11, 13, 17};

    public static BigInteger[] generateDiffieHellman(int bits) {
        BigInteger g = BigInteger.valueOf(VALUES[RANDOM.nextInt(7)]);
        BigInteger p = BigInteger.probablePrime(bits, RANDOM);
        while (!g.modPow(p.subtract(BigInteger.ONE), p).equals(BigInteger.ONE)) {
            p = BigInteger.probablePrime(bits, RANDOM);
        }
        return new BigInteger[]{p, g};

    }

    public static byte[] getIV(int sizeBytes){
        byte[] iv = new byte[sizeBytes];
        RANDOM.nextBytes(iv);
        return iv;
    }

    public static BigInteger generateRandomWord(){
        return BigInteger.valueOf(RANDOM.nextInt(100));
    }
}
