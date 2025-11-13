package com.gaizkaFrost.AES;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

final class Serializer {
    private Serializer() {}

    static byte[] encode(CipherArtifact a) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            out.write(Policy.MAGIC);
            out.write((byte) a.version);
            out.write((byte) a.mode);
            out.write((byte) a.kdf);
            out.write(shortToBE((short) a.keyBits));

            out.write(shortToBE((short) a.salt.length));
            out.write(a.salt);

            out.write((byte) a.nonce.length);
            out.write(a.nonce);

            out.write(shortToBE((short) a.aad.length));
            if (a.aad.length > 0) out.write(a.aad);

            out.write(intToBE(a.ciphertext.length));
            if (a.ciphertext.length > 0) out.write(a.ciphertext);

            out.write((byte) a.tag.length);
            out.write(a.tag);

            return out.toByteArray();
        } catch (Exception e) {
            throw new CryptoException("Error serializando: " + e.getMessage(), e);
        }
    }

    static CipherArtifact decode(byte[] blob) {
        try {
            ByteBuffer bb = ByteBuffer.wrap(blob).order(ByteOrder.BIG_ENDIAN);

            byte m0 = bb.get(), m1 = bb.get(), m2 = bb.get();
            if (m0 != Policy.MAGIC[0] || m1 != Policy.MAGIC[1] || m2 != Policy.MAGIC[2])
                throw new CryptoFormatException("Magic incorrecto");

            int version = bb.get() & 0xFF;
            int mode = bb.get() & 0xFF;
            int kdf = bb.get() & 0xFF;
            int keyBits = bb.getShort() & 0xFFFF;

            if (version != Policy.VERSION)
                throw new CryptoFormatException("Versión no soportada: v" + version);

            int saltLen = bb.getShort() & 0xFFFF;
            byte[] salt = new byte[saltLen];
            bb.get(salt);

            int nonceLen = bb.get() & 0xFF;
            byte[] nonce = new byte[nonceLen];
            bb.get(nonce);

            int aadLen = bb.getShort() & 0xFFFF;
            byte[] aad = new byte[aadLen];
            if (aadLen > 0) bb.get(aad);

            int ctLen = bb.getInt();
            byte[] ct = new byte[ctLen];
            if (ctLen > 0) bb.get(ct);

            int tagLen = bb.get() & 0xFF;
            byte[] tag = new byte[tagLen];
            bb.get(tag);

            return new CipherArtifact(version, mode, kdf, keyBits, salt, nonce, aad, ct, tag);
        } catch (Exception e) {
            throw new CryptoFormatException("Blob inválido o truncado: " + e.getMessage(), e);
        }
    }

    private static byte[] shortToBE(short v) {
        return new byte[]{(byte) (v >> 8), (byte) v};
    }

    private static byte[] intToBE(int v) {
        return new byte[]{
                (byte) (v >> 24),
                (byte) (v >> 16),
                (byte) (v >> 8),
                (byte) v
        };
    }
}

