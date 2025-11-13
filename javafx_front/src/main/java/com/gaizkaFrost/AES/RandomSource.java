package com.gaizkaFrost.AES;

import java.security.SecureRandom;

/**
 * <h2>Generador seguro de bytes aleatorios</h2>
 *
 * <p>
 * Esta clase proporciona un punto centralizado para la generación de bytes aleatorios
 * criptográficamente seguros mediante {@link SecureRandom}.
 * Se utiliza en el módulo AES para generar:
 * </p>
 *
 * <ul>
 *     <li>Nonces (IV) para AES-GCM</li>
 *     <li>Sal para funciones KDF (PBKDF2)</li>
 *     <li>Material aleatorio necesario en el protocolo</li>
 * </ul>
 *
 * <p>
 * Solo expone un método estático y no permite instanciación.
 * El objeto {@link SecureRandom} se inicializa una única vez para evitar
 * overhead innecesario.
 * </p>
 *
 * @see SecureRandom
 * @since 2025
 * @version 1.0
 *
 * author Gaizka
 * author Diego
 */
final class RandomSource {

    /**
     * Instancia única de {@link SecureRandom} utilizada en todo el módulo.
     */
    private static final SecureRandom SR = new SecureRandom();

    /**
     * Constructor privado para evitar instanciación externa.
     */
    private RandomSource() {}

    /**
     * <h3>Genera un array de bytes aleatorios de longitud especificada</h3>
     *
     * <p>
     * Los bytes se obtienen mediante un generador criptográficamente seguro
     * acorde a los requisitos del módulo AES.
     * </p>
     *
     * @param n Número de bytes aleatorios a generar.
     * @return Array de {@code n} bytes aleatorios.
     */
    static byte[] nextBytes(int n) {
        byte[] out = new byte[n];
        SR.nextBytes(out);
        return out;
    }
}
