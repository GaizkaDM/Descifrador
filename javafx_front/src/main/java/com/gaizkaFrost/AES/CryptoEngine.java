package com.gaizkaFrost.AES;

import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import java.security.GeneralSecurityException;
import java.util.Arrays;

final class CryptoEngine {
    private CryptoEngine() {}

    static EncryptResult encrypt(byte[] plaintext, SecretKey key, byte[] nonce, byte[] aad) {
        try {
            Cipher cipher = Cipher.getInstance(Policy.CIPHER_TRANSFORM);
            GCMParameterSpec spec = new GCMParameterSpec(Policy.GCM_TAG_BITS, nonce);
            cipher.init(Cipher.ENCRYPT_MODE, key, spec);
            if (aad != null && aad.length > 0) cipher.updateAAD(aad);
            byte[] ctAndTag = cipher.doFinal(plaintext);

            int tlen = Policy.GCM_TAG_LEN;
            byte[] ct = Arrays.copyOf(ctAndTag, ctAndTag.length - tlen);
            byte[] tag = Arrays.copyOfRange(ctAndTag, ctAndTag.length - tlen, ctAndTag.length);
            return new EncryptResult(ct, tag);
        } catch (GeneralSecurityException e) {
            throw new CryptoException("Fallo cifrando: " + e.getMessage(), e);
        }
    }

    static byte[] decrypt(byte[] ciphertext, byte[] tag, SecretKey key, byte[] nonce, byte[] aad) {
        try {
            Cipher cipher = Cipher.getInstance(Policy.CIPHER_TRANSFORM);
            GCMParameterSpec spec = new GCMParameterSpec(Policy.GCM_TAG_BITS, nonce);
            cipher.init(Cipher.DECRYPT_MODE, key, spec);
            if (aad != null && aad.length > 0) cipher.updateAAD(aad);

            byte[] ctAndTag = new byte[ciphertext.length + tag.length];
            System.arraycopy(ciphertext, 0, ctAndTag, 0, ciphertext.length);
            System.arraycopy(tag, 0, ctAndTag, ciphertext.length, tag.length);
            return cipher.doFinal(ctAndTag);
        } catch (AEADBadTagException bad) {
            throw new CryptoException("Contraseña incorrecta o datos corruptos");
        } catch (GeneralSecurityException e) {
            throw new CryptoException("Fallo descifrando: " + e.getMessage(), e);
        }
    }

    static final class EncryptResult {
        final byte[] ciphertext;
        final byte[] tag;
        EncryptResult(byte[] ciphertext, byte[] tag) {
            this.ciphertext = ciphertext;
            this.tag = tag;
        }
    }
}

