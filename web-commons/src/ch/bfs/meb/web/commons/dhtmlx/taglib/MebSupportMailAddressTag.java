package ch.bfs.meb.web.commons.dhtmlx.taglib;

import java.io.IOException;

import ch.bfs.meb.util.MebUtils;

public class MebSupportMailAddressTag extends DhtmlxTagBase {
    private static final long serialVersionUID = 364822907672807648L;

    public void doTag() throws DhtmlxTagException {
        try {
            pageContext.getOut().print(MebUtils.getMebSupportMailAddress());
        } catch (IOException e) {
            throw new DhtmlxTagException(e);
        }
    }
}
