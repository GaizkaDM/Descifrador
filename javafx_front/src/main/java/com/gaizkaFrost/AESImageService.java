package com.gaizkaFrost;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;

public class AESImageService {

    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int IV_LENGTH = 12;        // 12 bytes típico en GCM
    private static final int TAG_LENGTH = 128;      // 128 bits de tag de autenticación

    private final SecretKey secretKey;

    /**
     * Crea el servicio a partir de una clave en texto (contraseña).
     * Se deriva una clave AES de 128 bits a partir del string.
     */
    public AESImageService(String key) throws NoSuchAlgorithmException {
        this.secretKey = deriveKeyFromString(key);
    }

    private SecretKey deriveKeyFromString(String key) throws NoSuchAlgorithmException {
        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        byte[] keyBytes = sha.digest(key.getBytes(StandardCharsets.UTF_8));
        // Nos quedamos con 16 bytes = 128 bits
        keyBytes = Arrays.copyOf(keyBytes, 16);
        return new SecretKeySpec(keyBytes, "AES");
    }

    /**
     * Cifra una imagen (o cualquier archivo) con AES-GCM.
     * El fichero de salida contiene IV + datos cifrados.
     */
    public void encryptImage(Path inputImagePath, Path outputEncryptedPath) throws Exception {
        byte[] inputBytes = Files.readAllBytes(inputImagePath);

        // Generar IV aleatorio
        byte[] iv = new byte[IV_LENGTH];
        SecureRandom random = new SecureRandom();
        random.nextBytes(iv);

        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        GCMParameterSpec gcmSpec = new GCMParameterSpec(TAG_LENGTH, iv);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmSpec);

        byte[] encryptedBytes = cipher.doFinal(inputBytes);

        // Guardamos IV + datos cifrados en el mismo fichero
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(iv);
        baos.write(encryptedBytes);

        Files.write(outputEncryptedPath, baos.toByteArray());
    }

    /**
     * Descifra un archivo previamente cifrado con encryptImage.
     * Reconstruye los bytes originales de la imagen.
     */
    public void decryptImage(Path inputEncryptedPath, Path outputImagePath) throws Exception {
        byte[] fileBytes = Files.readAllBytes(inputEncryptedPath);

        if (fileBytes.length < IV_LENGTH) {
            throw new IllegalArgumentException("El archivo cifrado es demasiado pequeño o está corrupto.");
        }

        // Extraemos IV y datos cifrados
        byte[] iv = Arrays.copyOfRange(fileBytes, 0, IV_LENGTH);
        byte[] encryptedBytes = Arrays.copyOfRange(fileBytes, IV_LENGTH, fileBytes.length);

        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        GCMParameterSpec gcmSpec = new GCMParameterSpec(TAG_LENGTH, iv);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec);

        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);

        Files.write(outputImagePath, decryptedBytes);
    }
}

