package ch.admin.bfs.sbg.transfer;

import java.io.Serializable;

/**
 * TODO Describe this class
 * 
 * @author $Author: lsc $
 * @version $Revision: 326 $
 */
public class ExportResult extends ResultBase implements Serializable {
    private static final long serialVersionUID = -6513696769789555648L;

    private byte[] _binaryExport;
    private String _filename;

    public ExportResult() {}

    public ExportResult(byte[] anExport) {

        _binaryExport = anExport;
        setState(OK);
    }

    public ExportResult(String message) {

        setExport(new byte[0]);
        setMessage(message);
        setState(FAILURE);
    }

    public byte[] getExport() {
        return _binaryExport;
    }

    public void setExport(byte[] anExport) {
        this._binaryExport = anExport;
    }

    public String getFilename() {
        return _filename;
    }

    public void setFilename(String filename) {
        this._filename = filename;
    }

    public void setMessage(String message) {
        super.setMessage(message);
        setState(FAILURE);
    }

}
