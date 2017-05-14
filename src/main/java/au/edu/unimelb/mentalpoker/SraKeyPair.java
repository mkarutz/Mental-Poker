package au.edu.unimelb.mentalpoker;

import java.math.BigInteger;
import java.util.Random;

import static com.google.common.base.Preconditions.checkArgument;

public class SraKeyPair {
    private static final int DEFAULT_NUM_BITS = 2048;

    private final BigInteger prime;
    private final BigInteger secret;
    private final BigInteger secretInverse;

    private SraKeyPair(BigInteger prime, BigInteger secret, BigInteger secretInverse) {
        this.prime = prime;
        this.secret = secret;
        this.secretInverse = secretInverse;
    }

    public static SraKeyPair create(BigInteger prime, BigInteger secret) {
        checkArgument(secret.gcd(prime.subtract(BigInteger.ONE)).equals(BigInteger.ONE));
        final BigInteger d = secret.modInverse(prime.subtract(BigInteger.ONE));
        return new SraKeyPair(prime, secret, d);
    }

    public static SraKeyPair create(BigInteger prime) {
        return create(prime, new Random());
    }

    public static SraKeyPair create(BigInteger prime, Random random) {
        return create(DEFAULT_NUM_BITS, prime, random);
    }

    public static SraKeyPair create(int numBits, BigInteger prime, Random random) {
        final BigInteger secret = generateEncryptionKey(prime, numBits, random);
        final BigInteger secretInverse = secret.modInverse(prime.subtract(BigInteger.ONE));
        return new SraKeyPair(prime, secret, secretInverse);
    }

    private static BigInteger generateEncryptionKey(BigInteger p, int numBits, Random random) {
        BigInteger phiP = p.subtract(BigInteger.ONE);

        // Choose a key that is invertible in mod phi(prime)
        BigInteger k = new BigInteger(numBits, random);
        while (!k.gcd(phiP).equals(BigInteger.ONE)) {
            k = new BigInteger(numBits, random);
        }

        return k;
    }

    public BigInteger encrypt(BigInteger message) {
        return message.modPow(secret, prime);
    }

    public BigInteger decrypt(BigInteger cypher) {
        return cypher.modPow(secretInverse, prime);
    }

    public BigInteger getSecret() {
        return secret;
    }
}
