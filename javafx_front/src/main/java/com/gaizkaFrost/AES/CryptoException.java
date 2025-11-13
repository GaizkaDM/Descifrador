package com.gaizkaFrost.AES;

/**
 * <h2>Excepción criptográfica genérica</h2>
 *
 * <p>
 * Esta excepción se utiliza para señalar errores durante operaciones de cifrado
 * o descifrado dentro del módulo AES.
 * Es una {@link RuntimeException}, por lo que no requiere ser declarada o capturada
 * explícitamente, permitiendo que las capas superiores decidan cómo manejarla.
 * </p>
 *
 * <p>
 * Normalmente es lanzada por clases como {@link CryptoEngine} o {@link UseCases}.
 * </p>
 *
 * @author Gaizka
 * @author Diego
 * @version 1.0
 * @since 2025
 */
public class CryptoException extends RuntimeException {

    /**
     * Crea una nueva excepción criptográfica con un mensaje descriptivo.
     *
     * @param m Mensaje de error.
     */
    CryptoException(String m) { super(m); }

    /**
     * Crea una excepción criptográfica con mensaje y causa anidada.
     *
     * @param m Mensaje explicativo del error.
     * @param c Causa original de la excepción.
     */
    CryptoException(String m, Throwable c) { super(m, c); }
}

/**
 * <h2>Excepción para errores de formato criptográfico</h2>
 *
 * <p>
 * Esta excepción indica que los datos recibidos no cumplen el formato esperado
 * para ser procesados por el sistema criptográfico (por ejemplo:
 * estructuras incompletas, bytes alterados, longitudes incorrectas, etc.).
 * </p>
 *
 * <p>
 * Se emplea durante la fase de parsing o interpretación de artefactos
 * cifrados en clases del paquete <code>com.gaizkaFrost.AES</code>.
 * </p>
 *
 * @see CipherArtifact
 * @see CryptoEngine
 */
class CryptoFormatException extends RuntimeException {

    /**
     * Crea una excepción de formato criptográfico con un mensaje explicativo.
     *
     * @param m Descripción del error detectado.
     */
    CryptoFormatException(String m) { super(m); }

    /**
     * Crea una excepción de formato criptográfico con mensaje y causa asociada.
     *
     * @param m Mensaje explicativo del error.
     * @param c Excepción original que produjo el fallo.
     */
    CryptoFormatException(String m, Throwable c) { super(m, c); }
}
