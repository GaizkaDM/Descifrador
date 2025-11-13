package com.gaizkaFrost.AES;

/**
 * <h2>Contenedor interno para artefactos del cifrado AES</h2>
 *
 * <p>
 * Esta clase representa la estructura completa generada o requerida por un proceso
 * de cifrado autenticado (AES-GCM u otros modos compatibles).
 * Se utiliza internamente en el paquete {@code com.gaizkaFrost.AES} como forma de
 * agrupar todos los parámetros necesarios para reconstruir el cifrado/descifrado.
 * </p>
 *
 * <p>
 * Un objeto de este tipo contiene:
 * <ul>
 *     <li><b>version</b>: versión del formato utilizado.</li>
 *     <li><b>mode</b>: modo de cifrado AES (por ejemplo: GCM).</li>
 *     <li><b>kdf</b>: función KDF utilizada para derivar claves.</li>
 *     <li><b>keyBits</b>: tamaño de la clave (128 / 192 / 256 bits).</li>
 *     <li><b>salt</b>: sal usada en la derivación de clave.</li>
 *     <li><b>nonce</b>: nonce o IV utilizado en el cifrado.</li>
 *     <li><b>aad</b>: datos autenticados adicionales.</li>
 *     <li><b>ciphertext</b>: datos cifrados.</li>
 *     <li><b>tag</b>: etiqueta de autenticación GCM.</li>
 * </ul>
 * </p>
 *
 * <p>
 * Esta clase no realiza operaciones criptográficas; solo almacena los datos necesarios
 * para que otras clases realicen el proceso correcto de cifrado/descifrado.
 * </p>
 *
 * @author Gaizka
 * @author Diego
 * @version 1.0
 * @since 2025
 */
final class CipherArtifact {

    /**
     * Versión del formato del artefacto.
     */
    final int version;

    /**
     * Modo de cifrado usado (por ejemplo, AES-GCM).
     */
    final int mode;

    /**
     * Identificador de la función KDF usada para derivar la clave.
     */
    final int kdf;

    /**
     * Tamaño de la clave AES en bits (128 / 256…).
     */
    final int keyBits;

    /**
     * Sal usada en el proceso de derivación de claves.
     */
    final byte[] salt;

    /**
     * Nonce (IV) utilizado en el cifrado.
     */
    final byte[] nonce;

    /**
     * Datos autenticados adicionales (AAD).
     * Si es {@code null}, se reemplaza por un array vacío.
     */
    final byte[] aad;

    /**
     * Datos cifrados generados por el proceso de cifrado.
     */
    final byte[] ciphertext;

    /**
     * Etiqueta de autenticación GCM (tag de integridad).
     */
    final byte[] tag;

    /**
     * <h3>Constructor del artefacto de cifrado</h3>
     *
     * <p>
     * Crea un contenedor inmutable con todos los campos necesarios
     * para representar un cifrado autenticado completo.
     * </p>
     *
     * @param version    Versión del formato.
     * @param mode       Modo de cifrado usado.
     * @param kdf        Identificador de la función KDF empleada.
     * @param keyBits    Tamaño de clave AES en bits.
     * @param salt       Sal para la derivación de clave.
     * @param nonce      Nonce o IV del cifrado.
     * @param aad        Datos autenticados adicionales.
     * @param ciphertext Datos cifrados.
     * @param tag        Tag de autenticación GCM.
     */
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


