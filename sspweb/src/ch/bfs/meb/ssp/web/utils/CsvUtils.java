package ch.bfs.meb.ssp.web.utils;

public final class CsvUtils {

    private CsvUtils() {
        throw new AssertionError("Utility class");
    }

    /**
     * Ajoute le BOM UTF-8 au début du contenu CSV
     * Nécessaire pour une bonne compatibilité avec Excel
     */
    public static byte[] addUtf8Bom(byte[] content) {
        byte[] bom = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
        byte[] result = new byte[bom.length + content.length];
        System.arraycopy(bom, 0, result, 0, bom.length);
        System.arraycopy(content, 0, result, bom.length, content.length);
        return result;
    }
}
