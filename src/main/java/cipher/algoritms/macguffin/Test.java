package cipher.algoritms.macguffin;

import cipher.ISymmCipher;
import cipher.algoritms.rc5.util.RC5Cipher;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class Test {
    public static void main(String[] args) throws NoSuchAlgorithmException {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(128); // Устанавливаем длину ключа на 128 бит

        // Генерация ключа
        SecretKey secretKey = keyGen.generateKey();

        // Получаем ключ как массив байт
        // 0b00111111
//        BigInteger bytes = new BigInteger("106097987032490124347019119078893606399");
//        BigInteger input = new BigInteger("956906766452501266");
//        byte[] inpt = "12345678".getBytes();
//        System.out.println(Arrays.toString(inpt));
//        MacGuffinCipher macGuffinCipher = new MacGuffinCipher(bytes.toByteArray());
//        byte[] encrypted = macGuffinCipher.encryptBlock(inpt);
//        System.out.println(new BigInteger(encrypted));
//        byte[] decrypted = macGuffinCipher.decryptBlock(encrypted);
//        System.out.println(Arrays.toString(decrypted));
        byte[] key = "1234".getBytes();
        byte[] word = "1234".getBytes();
        ISymmCipher cipher = new RC5Cipher(key);
        byte[] encrypted = cipher.encryptBlock(word);
        System.out.println(Arrays.toString(encrypted));
        byte[] decrypted = cipher.decryptBlock(encrypted);
        System.out.println(Arrays.toString(decrypted));
    }
}
