package au.edu.unimelb.mentalpoker;

import java.math.BigInteger;
import java.util.Random;

public class SraKeyPair {
    private static final int DEFAULT_NUM_BITS = 10;

    private final BigInteger p;
    private final BigInteger k;
    private final BigInteger d;

    private SraKeyPair(BigInteger p, BigInteger k, BigInteger d) {
        this.p = p;
        this.k = k;
        this.d = d;
    }

    public static SraKeyPair create(BigInteger q) {
        return create(q, new Random());
    }

    public static SraKeyPair create(BigInteger q, Random random) {
        return create(DEFAULT_NUM_BITS, q, random);
    }

    public static SraKeyPair create(int numBits, BigInteger q, Random random) {
        final BigInteger p = q.shiftLeft(1).add(BigInteger.ONE);

        final BigInteger k = generateEncryptionKey(p, numBits, random);
        final BigInteger d = k.modInverse(p.subtract(BigInteger.ONE));

        return new SraKeyPair(p, k, d);
    }

    private static BigInteger generateEncryptionKey(BigInteger p, int numBits, Random random) {
        BigInteger phiP = p.subtract(BigInteger.ONE);

        // Choose a key that is invertible in mod phi(p)
        BigInteger k = new BigInteger(numBits, random);
        while (!k.gcd(phiP).equals(BigInteger.ONE)) {
            k = new BigInteger(numBits, random);
        }

        return k;
    }

    public BigInteger encrypt(BigInteger message) {
        return message.modPow(k, p);
    }

    public BigInteger decrypt(BigInteger cypher) {
        return cypher.modPow(d, p);
    }
}
