package com.gaizkaFrost.AES;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.spec.KeySpec;
import java.util.Arrays;

/**
 * <h2>Derivación de clave a partir de contraseña (KDF)</h2>
 *
 * <p>
 * Esta clase implementa la derivación de claves a partir de una contraseña mediante
 * un algoritmo KDF (Key Derivation Function), definido en {@link Policy#KDF_ALGO}.
 * </p>
 *
 * <p>
 * El proceso usa <b>PBE (Password-Based Encryption)</b> con iteraciones configurables
 * e incluye una sal para evitar ataques por diccionario y rainbow tables.
 * Tras derivar los bytes de la clave, esta se convierte en un {@link SecretKey}
 * del algoritmo especificado en {@link Policy#KEY_ALGO}.
 * </p>
 *
 * <p>
 * La clase es <b>package-private</b> y expone únicamente métodos estáticos,
 * ya que no está diseñada para creación de instancias.
 * </p>
 *
 * @see KeyMaterial
 * @see Policy
 * @see PBEKeySpec
 * @since 2025
 * @version 1.0
 *
 * author Gaizka
 * author Diego
 */
final class KeyDerivation {

    /**
     * Constructor privado para evitar instanciación.
     */
    private KeyDerivation() {}

    /**
     * <h3>Deriva una clave segura desde una contraseña</h3>
     *
     * <p>
     * Usa el algoritmo definido en {@link Policy#KDF_ALGO} y genera una clave
     * de tamaño {@code keyBits}.
     * Los parámetros incluyen:
     * </p>
     *
     * <ul>
     *     <li><b>password</b>: contraseña del usuario.</li>
     *     <li><b>salt</b>: sal aleatoria asociada a la contraseña.</li>
     *     <li><b>keyBits</b>: longitud deseada de la clave AES.</li>
     *     <li><b>iterations</b>: número de iteraciones para el algoritmo KDF.</li>
     * </ul>
     *
     * <p>
     * Tras generar la clave derivada, los bytes temporales se sobrescriben
     * para mejorar la seguridad.
     * </p>
     *
     * @param password   Contraseña de entrada (char[]).
     * @param salt       Sal utilizada en la derivación.
     * @param keyBits    Tamaño de clave deseado (en bits).
     * @param iterations Número de iteraciones del KDF.
     * @return Objeto {@link KeyMaterial} que contiene la clave derivada.
     *
     * @throws CryptoException Si ocurre algún error durante la derivación.
     */
    static KeyMaterial deriveFromPassword(char[] password, byte[] salt, int keyBits, int iterations) {
        try {
            SecretKeyFactory skf = SecretKeyFactory.getInstance(Policy.KDF_ALGO);
            KeySpec spec = new PBEKeySpec(password, salt, iterations, keyBits);
            byte[] keyBytes = skf.generateSecret(spec).getEncoded();
            SecretKey key = new SecretKeySpec(keyBytes, Policy.KEY_ALGO);

            Arrays.fill(keyBytes, (byte) 0); // limpiar clave temporal en memoria

            return new KeyMaterial(key);
        } catch (Exception e) {
            throw new CryptoException("Fallo al derivar la clave: " + e.getMessage(), e);
        }
    }
}
