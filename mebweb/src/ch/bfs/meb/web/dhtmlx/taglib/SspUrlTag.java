/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: mebweb

  $Id: SspUrlTag.java 980 2010-03-10 07:52:24Z dzw $
 */
package ch.bfs.meb.web.dhtmlx.taglib;

import ch.bfs.meb.web.commons.dhtmlx.DhtmlxException;
import ch.bfs.meb.web.configuration.IMebWebConfiguration;

/**
 * Get the url of the ssp web application
 * 
 * @author $Author: dzw $
 * @version $Revision: 980 $
 */
public class SspUrlTag extends AbstractUrlTag {
    private static final long serialVersionUID = 7178933863813681435L;

    @Override
    protected String getUrl(IMebWebConfiguration configuration) throws DhtmlxException {
        return convertMebUrl(configuration.getSspWebURL());
    }
}
