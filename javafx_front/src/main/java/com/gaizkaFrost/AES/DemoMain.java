package com.gaizkaFrost.AES;

import java.nio.charset.StandardCharsets;

class DemoMain {
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

