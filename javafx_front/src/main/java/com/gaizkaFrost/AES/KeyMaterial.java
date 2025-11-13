package com.gaizkaFrost.AES;

import javax.crypto.SecretKey;

/**
 * <h2>Contenedor inmutable para claves AES derivadas</h2>
 *
 * <p>
 * Esta clase encapsula un objeto {@link SecretKey} que representa una clave AES
 * derivada mediante el proceso definido en {@link KeyDerivation}.
 * Su propósito es proporcionar un tipo seguro y explícito para transportar claves
 * dentro del módulo criptográfico, evitando exponer directamente implementaciones
 * concretas de {@link SecretKey}.
 * </p>
 *
 * <p>
 * La clase es <b>inmutable</b>: la clave solo puede establecerse en el constructor
 * y no puede ser modificada posteriormente.
 * </p>
 *
 * @see KeyDerivation
 * @see SecretKey
 *
 * @since 2025
 * @version 1.0
 *
 * author Gaizka
 * author Diego
 */
final class KeyMaterial {

    /**
     * Clave AES derivada mediante KDF.
     */
    private final SecretKey key;

    /**
     * <h3>Constructor</h3>
     *
     * Crea una nueva instancia conteniendo una clave AES derivada.
     *
     * @param key Clave AES generada mediante {@link KeyDerivation}.
     */
    KeyMaterial(SecretKey key) {
        this.key = key;
    }

    /**
     * <h3>Obtiene la clave AES</h3>
     *
     * @return La clave almacenada internamente.
     */
    SecretKey key() {
        return key;
    }
}
