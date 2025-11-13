package com.gaizkaFrost.AES;

import java.nio.charset.StandardCharsets;

/**
 * <h2>Clase de demostración del módulo AES</h2>
 *
 * <p>
 * Esta clase contiene un método {@code main} pensado exclusivamente para
 * realizar pruebas manuales del sistema de cifrado y descifrado implementado
 * en {@link UseCases}.
 * </p>
 *
 * <p>
 * Permite comprobar rápidamente que:
 * <ul>
 *     <li>La función de cifrado genera texto en Base64 correctamente.</li>
 *     <li>El descifrado devuelve el mensaje original.</li>
 *     <li>La contraseña y el AAD funcionan como se espera.</li>
 * </ul>
 * </p>
 *
 * <p><b>No forma parte del flujo principal de la aplicación.</b></p>
 *
 * @see UseCases
 * @since 2025
 * @version 1.0
 *
 * @author Gaizka
 * @author Diego
 */
class DemoMain {

    /**
     * <h3>Método principal</h3>
     *
     * <p>
     * Realiza una demostración de cifrado y descifrado AES:
     * </p>
     *
     * <ol>
     *     <li>Define un mensaje en texto plano.</li>
     *     <li>Construye una contraseña segura.</li>
     *     <li>Especifica datos adicionales autenticados (AAD).</li>
     *     <li>Cifra el mensaje usando {@link UseCases#encryptToBase64(byte[], char[], String)}.</li>
     *     <li>Descifra el resultado con {@link UseCases#decryptFromBase64(String, char[])}.</li>
     *     <li>Imprime ambos resultados por consola.</li>
     * </ol>
     *
     * @param args Argumentos de línea de comandos (no utilizados).
     */
    public static void main(String[] args) {
        String msg = "Hola Claudia, el examen es el lunes.";
        char[] pwd = "super-segura-2025".toCharArray();
        String aad = "app=Encriptador;v=1";

        String b64 = UseCases.encryptToBase64(msg.getBytes(StandardCharsets.UTF_8), pwd, aad);
        System.out.println("CIFRADO (Base64):\n" + b64);

        byte[] plain = UseCases.decryptFromBase64(b64, pwd);
        System.out.println("DESCIFRADO:\n" + new String(plain, StandardCharsets.UTF_8));
    }
}
