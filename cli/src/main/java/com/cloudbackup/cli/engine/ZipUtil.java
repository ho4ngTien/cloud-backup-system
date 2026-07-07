package com.cloudsafe.cli.engine;

import java.io.*;
import java.nio.file.*;
import java.util.zip.*;

/**
 * ZIP compression utilities for the Backup Engine.
 */
public final class ZipUtil {

    private ZipUtil() {}

    /**
     * Compresses a single file into a ZIP archive at the target path.
     *
     * @param sourceFile  the file to compress
     * @param targetZip   output ZIP file path
     */
    public static void compressFile(Path sourceFile, Path targetZip) throws IOException {
        try (ZipOutputStream zos = new ZipOutputStream(
                new BufferedOutputStream(Files.newOutputStream(targetZip)))) {
            zos.setLevel(Deflater.BEST_COMPRESSION);
            ZipEntry entry = new ZipEntry(sourceFile.getFileName().toString());
            zos.putNextEntry(entry);
            Files.copy(sourceFile, zos);
            zos.closeEntry();
        }
    }

    /**
     * Decompresses a ZIP archive to the given target directory.
     * Prevents Zip Slip attacks by validating entry paths.
     *
     * @param zipFile   the ZIP archive to extract
     * @param targetDir directory to extract into
     */
    public static void decompress(Path zipFile, Path targetDir) throws IOException {
        Files.createDirectories(targetDir);
        try (ZipInputStream zis = new ZipInputStream(
                new BufferedInputStream(Files.newInputStream(zipFile)))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                Path outPath = targetDir.resolve(entry.getName()).normalize();
                // Zip Slip protection
                if (!outPath.startsWith(targetDir)) {
                    throw new IOException("Zip Slip detected: " + entry.getName());
                }
                if (entry.isDirectory()) {
                    Files.createDirectories(outPath);
                } else {
                    Files.createDirectories(outPath.getParent());
                    Files.copy(zis, outPath, StandardCopyOption.REPLACE_EXISTING);
                }
                zis.closeEntry();
            }
        }
    }
}
