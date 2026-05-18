/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sdlweb

  $Id: SetDeliveryButtonStatesTag.java 976 2010-03-09 14:52:44Z dzw $
 */
package ch.admin.bfs.sbg.dhtmlx.taglib;

import org.springframework.security.core.context.SecurityContextHolder;

import ch.bfs.meb.security.MebUser;
import ch.bfs.meb.util.SecurityConstants;
import ch.bfs.meb.web.commons.dhtmlx.taglib.DhtmlxTagBase;
import ch.bfs.meb.web.commons.dhtmlx.taglib.DhtmlxTagException;

/**
 * Disables buttons on delivery tab according to role
 * 
 * @author $Author: dzw $
 * @version $Revision: 976 $
 */
public class SetDeliveryButtonStatesTag extends DhtmlxTagBase {
    private static final long serialVersionUID = -7517997129759819600L;

    public void doTag() throws DhtmlxTagException {
        try {
            StringBuilder buf = new StringBuilder();

            MebUser user = (MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (!user.isInRole(SecurityConstants.ROLE_SBG_DL)) {
                // user is SDL_RO
                buf.append("var uploadButton=document.getElementById('fileUploadButton');");
                buf.append("uploadButton.disabled=true;");
                buf.append("var fileWidget=document.getElementById('fileWidget');");
                buf.append("fileWidget.disabled=true;");
                pageContext.getOut().append("<script>");
                pageContext.getOut().print(buf.toString());
                pageContext.getOut().append("</script>");
            }
        } catch (Exception e) {
            throw new DhtmlxTagException(e);
        }
    }
}
