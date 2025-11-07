package com.gaizkaFrost.AES;

import javax.crypto.SecretKey;

final class KeyMaterial {
    private final SecretKey key;

    KeyMaterial(SecretKey key) {
        this.key = key;
    }

    SecretKey key() {
        return key;
    }
}

