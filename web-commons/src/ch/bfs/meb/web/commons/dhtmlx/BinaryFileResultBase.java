package ch.bfs.meb.web.commons.dhtmlx;

import java.io.OutputStream;

public abstract class BinaryFileResultBase implements IHttpResult{

    public abstract void writeTo(OutputStream os);

    public abstract byte[] getContent();

}
