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

/**
 * <h2>Servicio de cifrado AES para imágenes y archivos binarios</h2>
 *
 * <p>
 * Esta clase proporciona métodos para cifrar y descifrar imágenes (o cualquier archivo)
 * utilizando el estándar <b>AES-GCM</b>, que ofrece cifrado autenticado y protección
 * de integridad.
 * </p>
 *
 * <p>
 * El fichero cifrado generado contiene:
 * <ul>
 *     <li><b>IV</b> (vector de inicialización) de 12 bytes.</li>
 *     <li>Datos cifrados + etiqueta GCM de autenticación.</li>
 * </ul>
 * </p>
 *
 * <p>
 * Por simplicidad, la clave AES se deriva a partir de una contraseña de texto
 * usando SHA-256 y tomando los primeros 128 bits.
 * </p>
 *
 * @author Gaizka
 * @author Diego
 * @version 1.0
 * @since 2025
 */
public class AESImageService {

    /**
     * Transformación AES utilizada: modo GCM sin padding.
     */
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";

    /**
     * Longitud del IV (vector de inicialización) en bytes.
     */
    private static final int IV_LENGTH = 12;        // 12 bytes típico en GCM

    /**
     * Longitud del tag de autenticación GCM en bits.
     */
    private static final int TAG_LENGTH = 128;      // 128 bits de tag de autenticación

    /**
     * Clave secreta AES generada a partir de la contraseña del usuario.
     */
    private final SecretKey secretKey;

    /**
     * <h3>Constructor del servicio AES</h3>
     *
     * <p>
     * Crea una instancia del servicio derivando internamente la clave AES de 128 bits
     * a partir de la contraseña en texto recibida.
     * </p>
     *
     * @param key Contraseña introducida por el usuario.
     * @throws NoSuchAlgorithmException Si falla la obtención del algoritmo SHA-256.
     */
    public AESImageService(String key) throws NoSuchAlgorithmException {
        this.secretKey = deriveKeyFromString(key);
    }

    /**
     * <h3>Derivación de clave</h3>
     *
     * Deriva una clave AES a partir de una cadena utilizando SHA-256
     * y conservando solo los primeros 128 bits.
     *
     * @param key Contraseña en texto plano.
     * @return Clave AES de 128 bits.
     * @throws NoSuchAlgorithmException Si SHA-256 no está disponible.
     */
    private SecretKey deriveKeyFromString(String key) throws NoSuchAlgorithmException {
        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        byte[] keyBytes = sha.digest(key.getBytes(StandardCharsets.UTF_8));
        // Nos quedamos con 16 bytes = 128 bits
        keyBytes = Arrays.copyOf(keyBytes, 16);
        return new SecretKeySpec(keyBytes, "AES");
    }

    /**
     * <h3>Cifrar imagen o archivo</h3>
     *
     * Cifra un archivo binario usando AES-GCM.
     * El archivo generado contendrá el IV seguido de los datos cifrados.
     *
     * <p><b>Proceso:</b></p>
     * <ul>
     *     <li>Leer bytes del archivo original.</li>
     *     <li>Generar IV aleatorio y configurar GCM.</li>
     *     <li>Cifrar los datos usando AES.</li>
     *     <li>Escribir en el fichero destino: IV + bytes cifrados.</li>
     * </ul>
     *
     * @param inputImagePath Ruta del archivo original (imagen u otro binario).
     * @param outputEncryptedPath Ruta donde se guardará el archivo cifrado.
     * @throws Exception Si ocurre algún error durante el proceso de cifrado.
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
     * <h3>Descifrar archivo cifrado con AES-GCM</h3>
     *
     * Descifra un archivo anteriormente generado con {@link #encryptImage(Path, Path)}.
     * Se extraen primero los bytes del IV y posteriormente los datos cifrados.
     *
     * <p><b>Proceso:</b></p>
     * <ul>
     *     <li>Leer todo el archivo cifrado.</li>
     *     <li>Separar IV (primeros 12 bytes) y datos cifrados.</li>
     *     <li>Descifrar usando AES-GCM con autenticación.</li>
     *     <li>Reconstruir el archivo original en la ruta indicada.</li>
     * </ul>
     *
     * @param inputEncryptedPath Ruta del archivo cifrado generado previamente.
     * @param outputImagePath Ruta donde se guardará el archivo descifrado.
     * @throws Exception Si el archivo está corrupto o la clave no coincide.
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