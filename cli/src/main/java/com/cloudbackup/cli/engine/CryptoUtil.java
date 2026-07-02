package com.cloudsafe.cli.engine;

import javax.crypto.*;
import javax.crypto.spec.*;
import java.io.*;
import java.nio.file.*;
import java.security.*;

/**
 * AES-256/GCM encryption utilities for the Backup Engine.
 * GCM mode provides authenticated encryption (integrity + confidentiality).
 *
 * File format on disk:
 *   [12 bytes IV][ciphertext + 16 byte GCM auth tag]
 */
public final class CryptoUtil {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int    GCM_IV_LEN  = 12;  // 96 bits recommended for GCM
    private static final int    GCM_TAG_LEN = 128; // bits

    private CryptoUtil() {}

    // ===== AES Key Generation =====

    /** Generates a cryptographically random 256-bit AES key. */
    public static byte[] generateAesKey() {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(256, new SecureRandom());
            return keyGen.generateKey().getEncoded();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("AES not available", e);
        }
    }

    // ===== Encryption =====

    /**
     * Encrypts inputFile → outputFile using AES-256/GCM.
     * Prepends the random 12-byte IV to the output file.
     */
    public static void encryptAes256Gcm(Path inputFile, Path outputFile,
                                        byte[] keyBytes) throws Exception {
        SecretKeySpec key = new SecretKeySpec(keyBytes, "AES");
        byte[] iv = new byte[GCM_IV_LEN];
        new SecureRandom().nextBytes(iv);

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_LEN, iv));

        try (InputStream  in  = new BufferedInputStream(Files.newInputStream(inputFile));
             OutputStream out = new BufferedOutputStream(Files.newOutputStream(outputFile))) {
            // Write IV first
            out.write(iv);
            // Stream encryption
            byte[] buffer = new byte[8192];
            int read;
            while ((read = in.read(buffer)) != -1) {
                byte[] encrypted = cipher.update(buffer, 0, read);
                if (encrypted != null) out.write(encrypted);
            }
            out.write(cipher.doFinal());
        }
    }

    // ===== Decryption =====

    /**
     * Decrypts inputFile → outputFile using AES-256/GCM.
     * Reads the 12-byte IV from the beginning of the file.
     */
    public static void decryptAes256Gcm(Path inputFile, Path outputFile,
                                        byte[] keyBytes) throws Exception {
        SecretKeySpec key = new SecretKeySpec(keyBytes, "AES");

        try (InputStream  in  = new BufferedInputStream(Files.newInputStream(inputFile));
             OutputStream out = new BufferedOutputStream(Files.newOutputStream(outputFile))) {
            // Read IV
            byte[] iv = in.readNBytes(GCM_IV_LEN);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_LEN, iv));

            // For GCM we must feed all ciphertext before doFinal (tag at end)
            ByteArrayOutputStream cipherBuf = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int read;
            while ((read = in.read(buffer)) != -1) {
                cipherBuf.write(buffer, 0, read);
            }
            out.write(cipher.doFinal(cipherBuf.toByteArray()));
        }
    }

    // ===== Hashing =====

    /** Computes SHA-256 digest of a file and returns lowercase hex string. */
    public static String sha256Hex(Path file) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        try (InputStream in = new BufferedInputStream(Files.newInputStream(file))) {
            byte[] buf = new byte[8192];
            int read;
            while ((read = in.read(buf)) != -1) {
                md.update(buf, 0, read);
            }
        }
        byte[] digest = md.digest();
        StringBuilder sb = new StringBuilder(64);
        for (byte b : digest) sb.append(String.format("%02x", b));
        return sb.toString();
    }

    /** Verifies a file's SHA-256 hash matches the expected value. */
    public static boolean verifySha256(Path file, String expectedHex) throws Exception {
        return sha256Hex(file).equalsIgnoreCase(expectedHex);
    }
}
