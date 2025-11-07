package com.gaizkaFrost.AES;

final class CipherArtifact {
    final int version;
    final int mode;
    final int kdf;
    final int keyBits;

    final byte[] salt;
    final byte[] nonce;
    final byte[] aad;
    final byte[] ciphertext;
    final byte[] tag;

    CipherArtifact(int version, int mode, int kdf, int keyBits,
                   byte[] salt, byte[] nonce, byte[] aad, byte[] ciphertext, byte[] tag) {
        this.version = version;
        this.mode = mode;
        this.kdf = kdf;
        this.keyBits = keyBits;
        this.salt = salt;
        this.nonce = nonce;
        this.aad = aad != null ? aad : new byte[0];
        this.ciphertext = ciphertext;
        this.tag = tag;
    }
}

