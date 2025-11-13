package com.gaizkaFrost;

import com.gaizkaFrost.AES.CryptoException;
import com.gaizkaFrost.AES.UseCases;

import java.nio.charset.StandardCharsets;

/**
 * Fachada sencilla para cifrar/descifrar con AES usando la librería interna.
 * Desde el controlador solo deberías usar estos dos métodos.
 */
public class AESCryptoService {

    // AAD fijo para etiquetar/versionar el cifrado
    private static final String AAD = "app=Encriptador;v=1";

    public static String cifrar(String textoPlano, String password) throws CryptoException {
        byte[] plainBytes = nonNull(textoPlano).getBytes(StandardCharsets.UTF_8);
        char[] pwdChars = nonNull(password).toCharArray();

        return UseCases.encryptToBase64(plainBytes, pwdChars, AAD);
    }

    public static String descifrar(String cifradoBase64, String password) throws CryptoException {
        char[] pwdChars = nonNull(password).toCharArray();
        byte[] plainBytes = UseCases.decryptFromBase64(nonNull(cifradoBase64), pwdChars);
        return new String(plainBytes, StandardCharsets.UTF_8);
    }

    private static String nonNull(String value) {
        return value == null ? "" : value;
    }
}

