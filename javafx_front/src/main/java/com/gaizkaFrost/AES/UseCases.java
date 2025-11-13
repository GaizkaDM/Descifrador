package com.gaizkaFrost.AES;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * <h2>Caso de uso de alto nivel para cifrado y descifrado AES</h2>
 *
 * <p>
 * Esta clase representa la API pública del módulo criptográfico.
 * Expone métodos sencillos y directos para cifrar y descifrar datos usando
 * una contraseña, generando y utilizando internamente:
 * </p>
 *
 * <ul>
 *     <li>KDF (PBKDF2-HMAC-SHA256)</li>
 *     <li>Nonce seguro generado aleatoriamente</li>
 *     <li>AAD opcional</li>
 *     <li>Cifrado AES-GCM autenticado</li>
 *     <li>Serialización compacta mediante {@link Serializer}</li>
 * </ul>
 *
 * <p>
 * Esta clase permite trabajar tanto con blobs binarios como con cadenas Base64,
 * lo que facilita el almacenamiento, transporte y uso del cifrado en aplicaciones externas.
 * </p>
 *
 * @version 1.0
 * @since 2025
 *
 * author Gaizka
 * author Diego
 */
public final class UseCases {

    /**
     * Constructor privado para evitar instanciación.
     */
    private UseCases() {}

    /**
     * <h3>Cifra datos binarios usando una contraseña</h3>
     *
     * <p>
     * El proceso ejecutado es el siguiente:
     * </p>
     * <ol>
     *     <li>Generar sal aleatoria.</li>
     *     <li>Derivar una clave AES mediante PBKDF2.</li>
     *     <li>Generar nonce seguro para AES-GCM.</li>
     *     <li>Cifrar utilizando {@link CryptoEngine}.</li>
     *     <li>Construir un {@link CipherArtifact} con toda la metadata.</li>
     *     <li>Serializar el artefacto mediante {@link Serializer}.</li>
     * </ol>
     *
     * @param plaintext Datos originales que se desean cifrar.
     * @param password  Contraseña usada para generar la clave.
     * @param aad       Datos adicionales autenticados (opcional).
     * @return Blob binario que contiene el artefacto cifrado completo.
     */
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

    /**
     * <h3>Descifra un blob binario generado por {@link #encryptWithPassword(byte[], char[], byte[])}</h3>
     *
     * <p>
     * El proceso incluye:
     * </p>
     * <ol>
     *     <li>Deserializar el artefacto con {@link Serializer#decode(byte[])}.</li>
     *     <li>Derivar la misma clave AES a partir de la contraseña y la sal almacenada.</li>
     *     <li>Descifrar usando {@link CryptoEngine#decrypt(byte[], byte[], SecretKey, byte[], byte[])}.</li>
     * </ol>
     *
     * @param blob     Blob cifrado en el formato del módulo AES.
     * @param password Contraseña utilizada para derivar la clave de descifrado.
     * @return Datos descifrados en su forma binaria original.
     */
    public static byte[] decryptWithPassword(byte[] blob, char[] password) {
        CipherArtifact a = Serializer.decode(blob);
        KeyMaterial km = KeyDerivation.deriveFromPassword(password, a.salt, a.keyBits, Policy.PBKDF2_ITERATIONS);
        return CryptoEngine.decrypt(a.ciphertext, a.tag, km.key(), a.nonce, a.aad);
    }

    /**
     * <h3>Cifra datos y devuelve el resultado en Base64</h3>
     *
     * <p>
     * Facilita la integración con sistemas que almacenan o transmiten cadenas.
     * </p>
     *
     * @param plaintext Datos en claro.
     * @param password  Contraseña para cifrar.
     * @param aadUtf8   Cadena que se usará como AAD (opcional), codificada en UTF-8.
     * @return Cadena Base64 que representa un artefacto cifrado completo.
     */
    public static String encryptToBase64(byte[] plaintext, char[] password, String aadUtf8) {
        byte[] aad = aadUtf8 != null ? aadUtf8.getBytes(StandardCharsets.UTF_8) : new byte[0];
        byte[] blob = encryptWithPassword(plaintext, password, aad);
        return Base64.getEncoder().encodeToString(blob);
    }

    /**
     * <h3>Descifra una cadena Base64 previamente cifrada con AES-GCM</h3>
     *
     * <p>
     * Equivalente a llamar a:
     * {@code decryptWithPassword(Base64.decode(b64), password)}.
     * </p>
     *
     * @param b64      Cadena en Base64 que contiene un artefacto cifrado.
     * @param password Contraseña usada para descifrar.
     * @return Datos binarios originales descifrados.
     */
    public static byte[] decryptFromBase64(String b64, char[] password) {
        byte[] blob = Base64.getDecoder().decode(b64);
        return decryptWithPassword(blob, password);
    }
}
