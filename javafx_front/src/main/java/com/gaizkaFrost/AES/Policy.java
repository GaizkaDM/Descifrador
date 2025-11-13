package com.gaizkaFrost.AES;

public final class Policy {
    private Policy() {}

    public static final byte[] MAGIC = new byte[]{0x45, 0x4E, 0x43}; // "ENC"
    public static final int VERSION = 0x01;
    public static final int MODE_ID_GCM = 0x01;
    public static final int KDF_ID_PBKDF2 = 0x02; // 2 = PBKDF2WithHmacSHA256

    public static final int KEY_BITS_DEFAULT = 256; // 128 o 256 válidos
    public static final int GCM_NONCE_LEN = 12; // 96 bits
    public static final int GCM_TAG_BITS = 128; // 16 bytes
    public static final int GCM_TAG_LEN = GCM_TAG_BITS / 8;

    public static final int SALT_LEN = 16; // 128 bits
    public static final int PBKDF2_ITERATIONS = 200_000; // 2025 recomendado

    public static final String CIPHER_TRANSFORM = "AES/GCM/NoPadding";
    public static final String KDF_ALGO = "PBKDF2WithHmacSHA256";
    public static final String KEY_ALGO = "AES";
}

