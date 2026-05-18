package ch.bfs.meb.web.commons.dhtmlx.taglib;

import java.io.IOException;

import ch.bfs.meb.web.commons.dhtmlx.DhtmlxException;

public class ExtraHtmlTag extends DhtmlxTagBase {
    private static final long serialVersionUID = 5855726029579059165L;

    private String _partName;

    public void doTag() throws DhtmlxTagException {
        try {
            pageContext.getOut().print(getManager().getExtraHtml(_partName));
        } catch (IOException e) {

            throw new DhtmlxTagException(e);
        } catch (DhtmlxException e) {
            throw new DhtmlxTagException(e);
        }
    }

    public String getPartName() {
        return _partName;
    }

    public void setPartName(String partName) {
        _partName = partName;
    }
}
