package com.gaizkaFrost.AES;

import java.security.SecureRandom;

final class RandomSource {
    private static final SecureRandom SR = new SecureRandom();

    private RandomSource() {}

    static byte[] nextBytes(int n) {
        byte[] out = new byte[n];
        SR.nextBytes(out);
        return out;
    }
}

