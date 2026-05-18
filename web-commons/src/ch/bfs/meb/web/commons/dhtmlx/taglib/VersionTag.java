package ch.bfs.meb.web.commons.dhtmlx.taglib;

import java.io.IOException;

import ch.bfs.meb.version.VersionReader;

public class VersionTag extends DhtmlxTagBase {
    private static final long serialVersionUID = 6073451351646471775L;

    public void doTag() throws DhtmlxTagException {
        try {
            pageContext.getOut().print("v" + VersionReader.getVersion());
        } catch (IOException e) {
            throw new DhtmlxTagException(e);
        }
    }
}
