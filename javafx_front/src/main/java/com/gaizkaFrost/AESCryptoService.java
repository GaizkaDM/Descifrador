package com.gaizkaFrost;

import com.gaizkaFrost.AES.CryptoException;
import com.gaizkaFrost.AES.UseCases;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

public class AESCryptoService {

    private static final Logger logger = LoggerFactory.getLogger(AESCryptoService.class);
    private static final String AAD = "app=Encriptador;v=1";

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

    private static String nonNull(String value) {
        return value == null ? "" : value;
    }
}




