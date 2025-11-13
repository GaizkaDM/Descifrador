package com.gaizkaFrost.AES;

import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import java.security.GeneralSecurityException;
import java.util.Arrays;

/**
 * <h2>Motor criptográfico interno para AES-GCM</h2>
 *
 * <p>
 * Esta clase encapsula las operaciones de cifrado y descifrado usando
 * <b>AES en modo GCM</b>, gestionando internamente la AAD,
 * el nonce, el tag y la separación correcta entre los bytes cifrados
 * y la etiqueta de autenticación.
 * </p>
 *
 * <p>
 * La clase es <b>package-private</b> y no está pensada para ser utilizada
 * directamente por el usuario final, sino por las capas superiores del
 * paquete <code>com.gaizkaFrost.AES</code> (por ejemplo <code>UseCases</code>).
 * </p>
 *
 * <p>
 * En caso de error, se lanzan excepciones {@link CryptoException} para mantener
 * una interfaz uniforme dentro del módulo AES.
 * </p>
 *
 * @see Policy
 * @see javax.crypto.Cipher
 * @see GCMParameterSpec
 *
 * @since 2025
 * @version 1.0
 *
 * @author Gaizka
 * @author Diego
 */
final class CryptoEngine {

    /**
     * Constructor privado para evitar instanciación.
     * Esta clase solo expone métodos estáticos.
     */
    private CryptoEngine() {}

    /**
     * <h3>Cifrado AES-GCM</h3>
     *
     * <p>
     * Recibe los bytes en claro, la clave AES, el nonce (IV) y los datos AAD.
     * Devuelve un objeto {@link EncryptResult} compuesto por:
     * </p>
     *
     * <ul>
     *     <li><b>ciphertext</b>: datos cifrados.</li>
     *     <li><b>tag</b>: etiqueta de autenticación GCM.</li>
     * </ul>
     *
     * <p>
     * Internamente, Java combina <i>ciphertext</i> + <i>tag</i> en un único array;
     * esta función separa ambos para permitir almacenarlos de forma explícita.
     * </p>
     *
     * @param plaintext Datos originales sin cifrar.
     * @param key       Clave secreta AES.
     * @param nonce     Vector de inicialización (IV) para GCM.
     * @param aad       Datos autenticados adicionales.
     * @return Resultado del cifrado (ciphertext + tag separados).
     *
     * @throws CryptoException Si ocurre cualquier error criptográfico interno.
     */
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

    /**
     * <h3>Descifrado AES-GCM</h3>
     *
     * <p>
     * Reconstruye internamente un array conteniendo:
     * ciphertext + tag
     * y lo pasa a {@link Cipher#doFinal(byte[])} para verificar autenticidad
     * y obtener los datos en claro.
     * </p>
     *
     * @param ciphertext Datos cifrados sin la etiqueta GCM.
     * @param tag        Etiqueta GCM de autenticación.
     * @param key        Clave secreta AES.
     * @param nonce      Nonce/IV usado en el cifrado.
     * @param aad        Datos adicionales autenticados.
     * @return Los datos descifrados en claro.
     *
     * @throws CryptoException Si la etiqueta no coincide, si los datos están corruptos
     *                         o si la contraseña derivada es incorrecta.
     */
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

    /**
     * <h3>Resultado del cifrado AES-GCM</h3>
     *
     * <p>
     * Clase interna que modela la respuesta del motor de cifrado:
     * ciphertext y tag por separado.
     * </p>
     */
    static final class EncryptResult {
        /**
         * Datos cifrados.
         */
        final byte[] ciphertext;

        /**
         * Etiqueta de autenticación GCM.
         */
        final byte[] tag;

        /**
         * Construye el resultado del cifrado.
         *
         * @param ciphertext Datos cifrados.
         * @param tag        Etiqueta GCM.
         */
        EncryptResult(byte[] ciphertext, byte[] tag) {
            this.ciphertext = ciphertext;
            this.tag = tag;
        }
    }
}
