package com.gaizkaFrost;

import com.gaizkaFrost.AES.CryptoException;
import com.gaizkaFrost.AES.UseCases;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

/**
 * <h2>Servicio de cifrado y descifrado AES (versión alternativa)</h2>
 *
 * <p>
 * Esta clase actúa como una capa de servicio que utiliza las clases
 * {@link UseCases} y {@link CryptoException} para cifrar y descifrar texto
 * mediante AES, empleando además un valor AAD (Additional Authenticated Data)
 * para reforzar la seguridad en el modo autenticado.
 * </p>
 *
 * <p>
 * A diferencia de {@link AESImageService}, este servicio está orientado al
 * cifrado/descifrado de cadenas de texto utilizando algoritmos de más alto nivel,
 * encapsulados en la librería personalizada del paquete <b>com.gaizkaFrost.AES</b>.
 * </p>
 *
 * <p>
 * Todos los procesos se registran mediante SLF4J, permitiendo depuración detallada
 * y trazabilidad de errores.
 * </p>
 *
 * @author Gaizka
 * @author Diego
 * @version 1.0
 * @since 2025
 */
public class AESCryptoService {

    /**
     * Logger de la clase para trazabilidad de operaciones AES.
     */
    private static final Logger logger = LoggerFactory.getLogger(AESCryptoService.class);

    /**
     * Additional Authenticated Data (AAD) usada para reforzar integridad/autenticidad.
     * Este valor se incluye durante la operación AES-GCM en la librería AES del proyecto.
     */
    private static final String AAD = "app=Encriptador;v=1";

    /**
     * <h3>Cifra un texto utilizando AES</h3>
     *
     * <p>
     * Convierte el texto plano a UTF-8, deriva internamente la clave y delega la operación
     * en {@link UseCases#encryptToBase64(byte[], char[], String)}.
     * Devuelve el resultado en Base64 listo para transporte o almacenamiento.
     * </p>
     *
     * @param textoPlano Texto original que se desea cifrar. Si es {@code null}, se usa cadena vacía.
     * @param password   Contraseña utilizada para generar la clave AES.
     * @return El texto cifrado codificado en Base64.
     * @throws CryptoException Si ocurre un error criptográfico (clave incorrecta, fallo interno…).
     */
    public static String cifrar(String textoPlano, String password) throws CryptoException {
        String seguroTexto = nonNull(textoPlano);
        String seguraPassword = nonNull(password);

        logger.info("Cifrando texto con AES (longitud={} caracteres)", seguroTexto.length());

        try {
            byte[] plainBytes = seguroTexto.getBytes(StandardCharsets.UTF_8);
            char[] pwdChars = seguraPassword.toCharArray();

            String resultado = UseCases.encryptToBase64(plainBytes, pwdChars, AAD);

            logger.info("Cifrado AES completado correctamente (Base64 length={})", resultado.length());
            return resultado;

        } catch (CryptoException e) {
            logger.error("Error criptográfico durante el cifrado AES", e);
            throw e; // dejamos pasar la CryptoException tal cual
        } catch (Exception e) {
            logger.error("Error inesperado durante el cifrado AES", e);
            throw new RuntimeException("Error inesperado durante el cifrado AES", e);
        }
    }

    /**
     * <h3>Descifra un texto cifrado mediante AES</h3>
     *
     * <p>
     * Recibe texto codificado en Base64, utiliza la librería AES del proyecto para
     * descifrarlo y devuelve el contenido en texto plano UTF-8.
     * </p>
     *
     * @param cifradoBase64 Texto cifrado en Base64. Si es {@code null}, se usa cadena vacía.
     * @param password      Contraseña necesaria para descifrar correctamente.
     * @return Texto plano descifrado.
     * @throws CryptoException Si la contraseña es incorrecta o los datos están corruptos.
     */
    public static String descifrar(String cifradoBase64, String password) throws CryptoException {
        String seguroCifrado = nonNull(cifradoBase64);
        String seguraPassword = nonNull(password);

        logger.info("Descifrando texto con AES (Base64 length={})", seguroCifrado.length());

        try {
            char[] pwdChars = seguraPassword.toCharArray();
            byte[] plainBytes = UseCases.decryptFromBase64(seguroCifrado, pwdChars);

            logger.info("Descifrado AES completado correctamente");
            return new String(plainBytes, StandardCharsets.UTF_8);

        } catch (CryptoException e) {
            logger.warn("Falló el descifrado AES: contraseña incorrecta o datos corruptos", e);
            throw e;
        } catch (Exception e) {
            logger.error("Error inesperado durante el descifrado AES", e);
            throw new RuntimeException("Error inesperado durante el descifrado AES", e);
        }
    }

    /**
     * Devuelve una cadena vacía cuando el valor es {@code null}, evitando
     * excepciones innecesarias.
     *
     * @param value Cadena recibida que podría ser {@code null}.
     * @return La propia cadena, o {@code ""} si era nula.
     */
    private static String nonNull(String value) {
        return value == null ? "" : value;
    }
}