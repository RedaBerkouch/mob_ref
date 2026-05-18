package ch.bfs.meb.web.commons.dhtmlx;

import java.io.IOException;
import java.io.OutputStream;

public class FileDownloadResult extends BinaryFileResultBase{

    private final byte[] _documentContent;
    private final String _documentType;
    private final String _filename;




    public FileDownloadResult(byte[] documentContent, String filename, String documentType) {
        this._documentContent = documentContent;
        this._filename = filename;
        this._documentType = documentType;
    }

    @Override
    public String getDocument() {
        return "";
    }

    @Override
    public String getContentType() {
        return _documentType;
    }

    @Override
    public String getContentDisposition() {
        return "attachment; filename=\"" + _filename +"\"";
    }

    @Override
    public void writeTo(OutputStream os) {
        try {
            os.write(_documentContent);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] getContent() {
        return _documentContent;
    }
}
