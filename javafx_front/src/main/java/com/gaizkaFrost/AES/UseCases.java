package com.gaizkaFrost.AES;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public final class UseCases {
    private UseCases() {}

    public static byte[] encryptWithPassword(byte[] plaintext, char[] password, byte[] aad) {
        int keyBits = Policy.KEY_BITS_DEFAULT;
        byte[] salt = RandomSource.nextBytes(Policy.SALT_LEN);
        KeyMaterial km = KeyDerivation.deriveFromPassword(password, salt, keyBits, Policy.PBKDF2_ITERATIONS);
        byte[] nonce = RandomSource.nextBytes(Policy.GCM_NONCE_LEN);

        CryptoEngine.EncryptResult res = CryptoEngine.encrypt(plaintext, km.key(), nonce, aad);
        CipherArtifact artifact = new CipherArtifact(
                Policy.VERSION,
                Policy.MODE_ID_GCM,
                Policy.KDF_ID_PBKDF2,
                keyBits,
                salt,
                nonce,
                aad != null ? aad : new byte[0],
                res.ciphertext,
                res.tag
        );
        return Serializer.encode(artifact);
    }

    public static byte[] decryptWithPassword(byte[] blob, char[] password) {
        CipherArtifact a = Serializer.decode(blob);
        KeyMaterial km = KeyDerivation.deriveFromPassword(password, a.salt, a.keyBits, Policy.PBKDF2_ITERATIONS);
        return CryptoEngine.decrypt(a.ciphertext, a.tag, km.key(), a.nonce, a.aad);
    }

    public static String encryptToBase64(byte[] plaintext, char[] password, String aadUtf8) {
        byte[] aad = aadUtf8 != null ? aadUtf8.getBytes(StandardCharsets.UTF_8) : new byte[0];
        byte[] blob = encryptWithPassword(plaintext, password, aad);
        return Base64.getEncoder().encodeToString(blob);
    }

    public static byte[] decryptFromBase64(String b64, char[] password) {
        byte[] blob = Base64.getDecoder().decode(b64);
        return decryptWithPassword(blob, password);
    }
}

