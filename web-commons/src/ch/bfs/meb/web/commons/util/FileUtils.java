package ch.bfs.meb.web.commons.util;

import ch.bfs.meb.web.commons.dhtmlx.FileHttpResult;

public class FileUtils {
    private FileUtils() {
        // Empêche l’instanciation
    }

    /**
     * Crée le fichier de résultats au format FileHttpResult
     *
     * @param fileData le contenu binaire du fichier
     * @param filename le nom du fichier (sans extension)
     *
     * @return le fichier créé au format FileHttpResult
     */
    public static FileHttpResult createFileHttpResult(byte[] fileData, String filename) {
        return new FileHttpResult(
                fileData,
                filename + (ValidationUtils.isXlsFormat(fileData) ? ".xls" : ".xlsx")
        );
    }

}
