package ch.bfs.meb.ssp.web.utils;

import ch.bfs.meb.web.commons.util.ValidationUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

@Slf4j
public final class FileUtils {

    private static final int BUFFER_SIZE = 8192;

    private FileUtils() {
        throw new AssertionError("Utility class");
    }

    /**
     * Crée les headers HTTP pour un export CSV
     *
     * @param filename Nom de base du fichier (sans extension)
     * @param length   Taille du contenu en bytes
     * @return Headers HTTP configurés pour un téléchargement CSV
     */
    public static HttpHeaders getHttpHeadersForCsvFile(String filename, long length) {
        return createHeaders(filename, length, "text/csv;charset=UTF-8", ".csv", true);
    }

    /**
     * Crée les headers HTTP pour un export Excel (XLSX)
     *
     * @param filename Nom de base du fichier (sans extension)
     * @param content    en bytes
     * @return Headers HTTP configurés pour un téléchargement Excel
     */
    public static HttpHeaders getHttpHeadersForXlsxFile(String filename, byte[] content) {
        boolean xlsFormat = ValidationUtils.isXlsFormat(content);
        return createHeaders(filename, content.length,
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", xlsFormat ? ".xls" : ".xlsx",
                true);
    }
    
    public static HttpHeaders getHttpHeaderForCustomFileType(String filename, byte[] content, String fileType) {
        return createHeaders(filename, content.length, fileType, null,false);
    }

    /**
     * Crée les headers HTTP pour un fichier ZIP
     */
    public static HttpHeaders getHttpHeadersForZipFile(String filename, long length) {
        return createHeaders(filename, length, "application/zip", ".zip", true);
    }

    /**
     * Crée les headers HTTP génériques pour un export de fichier
     *
     * @param filename    Nom de base du fichier (sans extension)
     * @param length      Taille du contenu en bytes
     * @param contentType Type MIME du fichier
     * @param extension   Extension du fichier (avec le point)
     * @param export      Indication pour savoir s'il s'agit d'un fichier d'export ({@code true}) ou non ({@code false})
     * @return Headers HTTP configurés pour le téléchargement
     */
    private static HttpHeaders createHeaders(String filename, long length,
                                             String contentType, String extension, boolean export) {
        HttpHeaders headers = new HttpHeaders();

        headers.add(HttpHeaders.CONTENT_TYPE, contentType);
        headers.add(HttpHeaders.CONTENT_LENGTH, String.valueOf(length));

        String fullFilename;
        
        if (export) {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            fullFilename = filename + "_export_" + timestamp + extension;
        }
        else {
            fullFilename = filename + (extension != null ? extension : "");
        }
        
        headers.add(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + fullFilename + "\"");

        headers.add(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate");
        headers.add(HttpHeaders.PRAGMA, "no-cache");
        headers.add(HttpHeaders.EXPIRES, "0");

        return headers;
    }

    /**
     * Extrait le contenu d'un fichier ZIP si nécessaire.
     * Si le contenu n'est pas un ZIP, le retourne tel quel.
     *
     * @param content Contenu potentiellement zippé
     * @return Contenu décompressé ou original
     * @throws IOException En cas d'erreur de lecture
     */
    public static byte[] extractFromZipIfNeeded(byte[] content) throws IOException {
        if (content == null || content.length == 0) {
            return content;
        }

        // Vérifier si c'est un fichier ZIP (magic bytes: 0x504B)
        if (!isZipFile(content)) {
            return content;
        }

        try (ByteArrayInputStream bais = new ByteArrayInputStream(content);
             ZipInputStream zis = new ZipInputStream(bais)) {

            ZipEntry entry = zis.getNextEntry();

            if (entry == null) {
                log.warn("ZIP file is empty, returning original content");
                return content;
            }

            log.info("Extracting file: {} (size: {} bytes)",
                    entry.getName(), entry.getSize());

            // Extraire le contenu
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;

            while ((bytesRead = zis.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
            }

            byte[] extracted = baos.toByteArray();
            log.info("Successfully extracted {} bytes", extracted.length);

            return extracted;

        } catch (ZipException e) {
            log.warn("Invalid ZIP format, returning original content: {}", e.getMessage());
            return content;
        } catch (IOException e) {
            log.error("Error extracting ZIP content", e);
            // En cas d'erreur, retourner le contenu original
            return content;
        }
    }

    /**
     * Vérifie si le contenu est un fichier ZIP en vérifiant les magic bytes
     */
    private static boolean isZipFile(byte[] content) {
        if (content.length < 4) {
            return false;
        }
        // ZIP magic bytes: 0x504B0304 (PK..)
        return content[0] == 0x50 && content[1] == 0x4B &&
                content[2] == 0x03 && content[3] == 0x04;
    }

    /**
     * Crée un fichier ZIP contenant un seul fichier Excel
     *
     * @param fileContent Contenu du fichier Excel (bytes)
     * @param filename    Nom du fichier dans le ZIP (ex: "export.xlsx")
     * @return Bytes du fichier ZIP créé
     */
    public static byte[] createZipFileResult(byte[] fileContent, String filename) {
        try {
            if (filename == null || filename.isEmpty()) {
                filename = "file.xlsx";
            }

            if (isZipFile(fileContent)) {
                log.info("Content is already a ZIP file, returning as-is ({} bytes)", fileContent.length);
                return fileContent; // ← Ne pas re-zipper !
            }

            log.info("Creating ZIP with file: {} ({} bytes)", filename, fileContent.length);
            return createZipBytes(fileContent, filename);

        } catch (IOException e) {
            log.error("Error creating ZIP", e);
            return fileContent;
        }
    }

    /**
     * Crée les bytes d'un ZIP contenant un seul fichier
     */
    private static byte[] createZipBytes(byte[] fileContent, String filename) throws IOException {
        if (fileContent == null || fileContent.length == 0) {
            throw new IllegalArgumentException("File content cannot be empty");
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            ZipEntry entry = new ZipEntry(filename);
            entry.setSize(fileContent.length);
            entry.setTime(System.currentTimeMillis());

            zos.putNextEntry(entry);
            zos.write(fileContent);
            zos.closeEntry();
            zos.finish();
        }

        byte[] zipContent = baos.toByteArray();
        log.info("ZIP created successfully: {} → {} bytes", filename, zipContent.length);

        return zipContent;
    }
}
