package ch.bfs.meb.server.commons.util;

public final class ValidationUtils {

    private ValidationUtils() {
        // Empêche l’instanciation
    }

    /**
     * Vérifie si un fichier correspond au format binaire XLS (Excel 97-2003).
     * Reconnaît les fichiers de type OLE2 (D0 CF 11 E0 …).
     *
     * @param fileData le contenu binaire du fichier
     * @return true si le fichier semble être un .xls
     */
    public static boolean isXlsFormat(byte[] fileData) {
        return fileData != null &&
                fileData.length > 8 &&
                fileData[0] == (byte) 0xD0 &&
                fileData[1] == (byte) 0xCF &&
                fileData[2] == (byte) 0x11 &&
                fileData[3] == (byte) 0xE0;
    }
}
