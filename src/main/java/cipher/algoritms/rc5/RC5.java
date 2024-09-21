package cipher.algoritms.rc5;
import cipher.IKeyExpansion;
import cipher.ISymmCipher;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RC5 implements ISymmCipher, IKeyExpansion {
    private final int inputBlockSizeInBits;
    private final int keyBlockSizeInBits;
    private final int countRounds;
    private long[] roundKeys;
    private RC5service service;

    public RC5(int inputBlockSizeInBits, int countRounds, int lenKeyInBits) {
        if (!(inputBlockSizeInBits == 32 || inputBlockSizeInBits == 64 ||inputBlockSizeInBits == 128)) {
            throw new IllegalArgumentException("Error size block in bits!");
        }

        if (countRounds <= 0 || countRounds > 255) {
            throw new IllegalArgumentException("Error count rounds!");
        }

        if (lenKeyInBits <= 0 || lenKeyInBits > 256) {
            throw new IllegalArgumentException("Error size key in bits!");
        }

        this.inputBlockSizeInBits = inputBlockSizeInBits;
        this.countRounds = countRounds;
        this.keyBlockSizeInBits = lenKeyInBits;
    }

    @Override
    public byte[] encryptBlock(byte[] inputBlock) {
        long A = service.getLongFromHalfBlock(inputBlock, 0, inputBlock.length / 2);
        long B = service.getLongFromHalfBlock(inputBlock, inputBlock.length / 2, inputBlock.length);

        A = BinaryOperations.sumModule(A, roundKeys[0], inputBlockSizeInBits / 2);
        B = BinaryOperations.sumModule(B, roundKeys[1], inputBlockSizeInBits / 2);

        for (int i = 1; i < countRounds; i++) {
            A = BinaryOperations.sumModule(BinaryOperations.leftCycleShift((A^B), inputBlockSizeInBits / 2, B), roundKeys[2*i], inputBlockSizeInBits / 2);
            B = BinaryOperations.sumModule(BinaryOperations.leftCycleShift((A^B), inputBlockSizeInBits / 2, A), roundKeys[2*i + 1], inputBlockSizeInBits / 2);
        }

        return service.twoLongPartToByteArray(A, B, inputBlock.length);
    }
    @Override
    public byte[] decryptBlock(byte[] inputBlock) {
        long B = service.getLongFromHalfBlock(inputBlock, inputBlock.length / 2, inputBlock.length);
        long A = service.getLongFromHalfBlock(inputBlock, 0, inputBlock.length / 2);

        for (int i = countRounds - 1; i > 0; i--) {
            B = BinaryOperations.rightCycleShift(BinaryOperations.subtractModule(B, roundKeys[2*i + 1], inputBlockSizeInBits / 2), inputBlockSizeInBits / 2, A) ^ A;
            A = BinaryOperations.rightCycleShift(BinaryOperations.subtractModule(A, roundKeys[2*i], inputBlockSizeInBits / 2), inputBlockSizeInBits / 2, B) ^ B;
        }

        B = BinaryOperations.subtractModule(B, roundKeys[1], inputBlockSizeInBits / 2);
        A = BinaryOperations.subtractModule(A, roundKeys[0], inputBlockSizeInBits / 2);

        return service.twoLongPartToByteArray(A, B, inputBlock.length);
    }

    @Override
    public void setKey(byte[] key) {
        this.service = new RC5service(inputBlockSizeInBits, keyBlockSizeInBits, countRounds);
        this.roundKeys = service.expandKey(key);
    }
}
