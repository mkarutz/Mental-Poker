package au.edu.unimelb.mentalpoker;

import au.edu.unimelb.mentalpoker.crypto.SraKeyPair;
import org.junit.Test;

import java.math.BigInteger;
import java.util.Random;

import static org.junit.Assert.*;

public class SraKeyPairTest {
    private static final Random random = new Random();

    @Test
    public void testEncryptDecrypt() {
        BigInteger message = new BigInteger("1234567");
        BigInteger p = new BigInteger("100012421");
        SraKeyPair keyPair = SraKeyPair.create(p, random);
        BigInteger c = keyPair.encrypt(message);
        BigInteger actual = keyPair.decrypt(c);
        assertEquals(message, actual);
    }

    @Test
    public void testCommutativeEncryption() {
        BigInteger p = new BigInteger("100012421");

        SraKeyPair keyPairA = SraKeyPair.create(p, random);
        SraKeyPair keyPairB = SraKeyPair.create(p, random);

        BigInteger message = new BigInteger("1234567");
        BigInteger cAB = keyPairB.encrypt(keyPairA.encrypt(message));
        BigInteger cBA = keyPairA.encrypt(keyPairB.encrypt(message));

        assertEquals(cAB, cBA);
    }

    @Test
    public void testCommutativeDecryption() {
        BigInteger p = new BigInteger("100012421");

        SraKeyPair keyPairA = SraKeyPair.create(p, random);
        SraKeyPair keyPairB = SraKeyPair.create(p, random);

        BigInteger message = new BigInteger("1234567");
        BigInteger c = keyPairB.encrypt(keyPairA.encrypt(message));

        BigInteger mAB = keyPairB.decrypt(keyPairA.decrypt(c));
        BigInteger mBA = keyPairA.decrypt(keyPairB.decrypt(c));

        assertEquals(mAB, mBA);
    }
}
