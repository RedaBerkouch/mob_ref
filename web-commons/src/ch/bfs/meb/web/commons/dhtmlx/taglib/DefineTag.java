/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sdlweb

  $Id$
 */
package ch.bfs.meb.web.commons.dhtmlx.taglib;

import java.io.IOException;

import ch.bfs.meb.web.commons.dhtmlx.DhtmlxException;

/**
 * TODO Describe this class
 * 
 * @author $Author$
 * @version $Revision$
 */
public class DefineTag extends DhtmlxTagBase {
    private static final long serialVersionUID = 3229880900242045754L;

    public void doTag() throws DhtmlxTagException {
        try {
            pageContext.getOut().print(getManager().getScriptingPart());
        } catch (IOException e) {

            throw new DhtmlxTagException(e);
        } catch (DhtmlxException e) {
            throw new DhtmlxTagException(e);
        }
    }
}
