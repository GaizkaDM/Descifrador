package com.gaizkaFrost.AES;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * <h2>Serializador y deserializador del artefacto AES</h2>
 *
 * <p>
 * Esta clase se encarga de convertir instancias de {@link CipherArtifact} en un
 * bloque binario (byte[]) y viceversa.
 * Es una parte esencial del formato propio utilizado por el módulo AES para
 * almacenar cifrados, ya que implementa un protocolo binario compacto,
 * seguro y compatible con futuras versiones.
 * </p>
 *
 * <p>
 * El formato binario generado tiene la siguiente estructura:
 * </p>
 *
 * <pre>
 * MAGIC (3 bytes)
 * version (1 byte)
 * mode (1 byte)
 * kdf (1 byte)
 * keyBits (2 bytes, BE)
 * saltLen (2 bytes, BE)
 * salt (...)
 * nonceLen (1 byte)
 * nonce (...)
 * aadLen (2 bytes, BE)
 * aad (...)
 * ctLen (4 bytes, BE)
 * ciphertext (...)
 * tagLen (1 byte)
 * tag (...)
 * </pre>
 *
 * <p>
 * Todos los enteros se escriben en big-endian (BE), acorde con la política del módulo.
 * </p>
 *
 * @see CipherArtifact
 * @see Policy
 * @see CryptoFormatException
 * @see CryptoException
 *
 * @version 1.0
 * @since 2025
 *
 * author Gaizka
 * author Diego
 */
final class Serializer {

    /**
     * Constructor privado para impedir instanciación.
     * La clase únicamente expone métodos estáticos.
     */
    private Serializer() {}

    /**
     * <h3>Serializa un {@link CipherArtifact} a un array de bytes</h3>
     *
     * <p>
     * Escribe secuencialmente todos los campos del artefacto en orden big-endian,
     * generando un formato binario compacto y portatil.
     * </p>
     *
     * @param a Instancia de artefacto que se desea serializar.
     * @return Array de bytes que representa el artefacto serializado.
     *
     * @throws CryptoException Si ocurre cualquier error de E/S o ensamblado.
     */
    static byte[] encode(CipherArtifact a) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            // Cabecera mágica
            out.write(Policy.MAGIC);

            // Campos principales
            out.write((byte) a.version);
            out.write((byte) a.mode);
            out.write((byte) a.kdf);
            out.write(shortToBE((short) a.keyBits));

            // SALT
            out.write(shortToBE((short) a.salt.length));
            out.write(a.salt);

            // NONCE
            out.write((byte) a.nonce.length);
            out.write(a.nonce);

            // AAD
            out.write(shortToBE((short) a.aad.length));
            if (a.aad.length > 0) out.write(a.aad);

            // CIPHERTEXT
            out.write(intToBE(a.ciphertext.length));
            if (a.ciphertext.length > 0) out.write(a.ciphertext);

            // TAG
            out.write((byte) a.tag.length);
            out.write(a.tag);

            return out.toByteArray();

        } catch (Exception e) {
            throw new CryptoException("Error serializando: " + e.getMessage(), e);
        }
    }

    /**
     * <h3>Deserializa un bloque binario en un {@link CipherArtifact}</h3>
     *
     * <p>
     * Interpreta un array de bytes siguiendo el formato binario definido en
     * {@link Policy} y reconstruye todos los campos originales del artefacto.
     * </p>
     *
     * <p><b>Valida:</b></p>
     * <ul>
     *     <li>Cabecera MAGIC.</li>
     *     <li>Versión del formato.</li>
     *     <li>Longitudes de todos los elementos.</li>
     * </ul>
     *
     * @param blob Array de bytes que contiene la serialización del artefacto.
     * @return El artefacto reconstruido.
     *
     * @throws CryptoFormatException Si el formato es incorrecto, truncado o incompatible.
     */
    static CipherArtifact decode(byte[] blob) {
        try {
            ByteBuffer bb = ByteBuffer.wrap(blob).order(ByteOrder.BIG_ENDIAN);

            // Validación MAGIC
            byte m0 = bb.get(), m1 = bb.get(), m2 = bb.get();
            if (m0 != Policy.MAGIC[0] || m1 != Policy.MAGIC[1] || m2 != Policy.MAGIC[2])
                throw new CryptoFormatException("Magic incorrecto");

            int version = bb.get() & 0xFF;
            int mode = bb.get() & 0xFF;
            int kdf = bb.get() & 0xFF;
            int keyBits = bb.getShort() & 0xFFFF;

            // validación de versión del protocolo
            if (version != Policy.VERSION)
                throw new CryptoFormatException("Versión no soportada: v" + version);

            // SALT
            int saltLen = bb.getShort() & 0xFFFF;
            byte[] salt = new byte[saltLen];
            bb.get(salt);

            // NONCE
            int nonceLen = bb.get() & 0xFF;
            byte[] nonce = new byte[nonceLen];
            bb.get(nonce);

            // AAD
            int aadLen = bb.getShort() & 0xFFFF;
            byte[] aad = new byte[aadLen];
            if (aadLen > 0) bb.get(aad);

            // CIPHERTEXT
            int ctLen = bb.getInt();
            byte[] ct = new byte[ctLen];
            if (ctLen > 0) bb.get(ct);

            // TAG
            int tagLen = bb.get() & 0xFF;
            byte[] tag = new byte[tagLen];
            bb.get(tag);

            return new CipherArtifact(version, mode, kdf, keyBits, salt, nonce, aad, ct, tag);

        } catch (Exception e) {
            throw new CryptoFormatException("Blob inválido o truncado: " + e.getMessage(), e);
        }
    }

    /**
     * Convierte un entero corto (short) a big-endian.
     *
     * @param v Valor short.
     * @return Array de 2 bytes en orden big-endian.
     */
    private static byte[] shortToBE(short v) {
        return new byte[]{(byte) (v >> 8), (byte) v};
    }

    /**
     * Convierte un entero a big-endian.
     *
     * @param v Valor entero.
     * @return Array de 4 bytes en orden big-endian.
     */
    private static byte[] intToBE(int v) {
        return new byte[]{
                (byte) (v >> 24),
                (byte) (v >> 16),
                (byte) (v >> 8),
                (byte) v
        };
    }
}
