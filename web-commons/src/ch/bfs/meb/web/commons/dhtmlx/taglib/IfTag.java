package ch.bfs.meb.web.commons.dhtmlx.taglib;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;

import ch.bfs.meb.web.commons.dhtmlx.DhtmlxException;

public class IfTag extends DhtmlxTagBase {
    private static final long serialVersionUID = 5855726029579059165L;

    private String _condition;

    public int doStartTag() throws JspException {
        try {
            if (getManager().showIfTagBody(getCondition())) {
                return EVAL_BODY_INCLUDE;
            }
        } catch (DhtmlxException ex) {
            throw new JspTagException(new DhtmlxTagException("Manager was not registered", ex));
        }
        return SKIP_BODY;
    }

    public void doTag() throws DhtmlxTagException {
        // do nothing
    }

    public String getCondition() {
        return _condition;
    }

    public void setCondition(String condition) {
        _condition = condition;
    }
}
