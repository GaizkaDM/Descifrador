package com.gaizkaFrost.AES;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.spec.KeySpec;
import java.util.Arrays;

final class KeyDerivation {
    private KeyDerivation() {}

    static KeyMaterial deriveFromPassword(char[] password, byte[] salt, int keyBits, int iterations) {
        try {
            SecretKeyFactory skf = SecretKeyFactory.getInstance(Policy.KDF_ALGO);
            KeySpec spec = new PBEKeySpec(password, salt, iterations, keyBits);
            byte[] keyBytes = skf.generateSecret(spec).getEncoded();
            SecretKey key = new SecretKeySpec(keyBytes, Policy.KEY_ALGO);
            Arrays.fill(keyBytes, (byte) 0); // limpiar
            return new KeyMaterial(key);
        } catch (Exception e) {
            throw new CryptoException("Fallo al derivar la clave: " + e.getMessage(), e);
        }
    }
}

