package com.gaizkaFrost.AES;

/**
 * <h2>Política de configuración para el módulo AES</h2>
 *
 * <p>
 * Esta clase define todos los parámetros, constantes y valores por defecto
 * utilizados en el sistema criptográfico del paquete
 * <code>com.gaizkaFrost.AES</code>.
 * </p>
 *
 * <p>
 * Centraliza:
 * </p>
 * <ul>
 *     <li>Algoritmos de cifrado y KDF.</li>
 *     <li>Tamaños de clave.</li>
 *     <li>Formatos del artefacto de cifrado.</li>
 *     <li>Versiones internas del protocolo.</li>
 *     <li>Longitudes de nonce, tag, sal e iteraciones recomendadas.</li>
 * </ul>
 *
 * <p>
 * Es una clase utilitaria: no puede instanciarse y todos sus valores son constantes.
 * </p>
 *
 * @since 2025
 * @version 1.0
 *
 * author Gaizka
 * author Diego
 */
public final class Policy {

    /**
     * Constructor privado para evitar instanciación.
     */
    private Policy() {}

    /**
     * <h3>Cabecera mágica para identificar archivos cifrados del sistema</h3>
     *
     * <p>
     * Los bytes representan la palabra "ENC".
     * Se utiliza para validar que un archivo contiene un artefacto
     * generado por este módulo.
     * </p>
     */
    public static final byte[] MAGIC = new byte[]{0x45, 0x4E, 0x43}; // "ENC"

    /**
     * Versión actual del formato de artefactos de cifrado.
     */
    public static final int VERSION = 0x01;

    /**
     * Identificador del modo AES-GCM dentro del protocolo.
     */
    public static final int MODE_ID_GCM = 0x01;

    /**
     * Identificador del algoritmo KDF PBKDF2-HMAC-SHA256.
     */
    public static final int KDF_ID_PBKDF2 = 0x02; // 2 = PBKDF2WithHmacSHA256

    /**
     * Tamaño de clave AES por defecto (en bits).
     * Puede ser 128 o 256 dependiendo de la fortaleza deseada.
     */
    public static final int KEY_BITS_DEFAULT = 256;

    /**
     * Longitud del nonce usado en AES-GCM (12 bytes = 96 bits).
     */
    public static final int GCM_NONCE_LEN = 12;

    /**
     * Longitud del tag GCM en bits.
     */
    public static final int GCM_TAG_BITS = 128;

    /**
     * Longitud del tag GCM en bytes.
     */
    public static final int GCM_TAG_LEN = GCM_TAG_BITS / 8;

    /**
     * Longitud de la sal usada en PBKDF2 (16 bytes = 128 bits).
     */
    public static final int SALT_LEN = 16;

    /**
     * Número de iteraciones recomendadas para PBKDF2 en 2025.
     */
    public static final int PBKDF2_ITERATIONS = 200_000;

    /**
     * Transformación de cifrado utilizada por defecto:
     * AES en modo GCM sin padding.
     */
    public static final String CIPHER_TRANSFORM = "AES/GCM/NoPadding";

    /**
     * Algoritmo de derivación de clave basado en PBKDF2-HMAC-SHA256.
     */
    public static final String KDF_ALGO = "PBKDF2WithHmacSHA256";

    /**
     * Algoritmo de clave simétrica utilizado: AES.
     */
    public static final String KEY_ALGO = "AES";
}
