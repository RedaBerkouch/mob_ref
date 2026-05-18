/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: web-commons

  $Id$

 */
package ch.bfs.meb.web.commons.dhtmlx.table;

import java.math.BigDecimal;

import ch.bfs.meb.web.commons.dhtmlx.DhtmlxException;
import ch.bfs.meb.web.commons.exception.InputValidationException;
import ch.bfs.meb.web.commons.i18n.IWebLocalizationManager;
import ognl.OgnlException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class describes a dhtmlxGrid column with a BigDecimal number
 * 
 * @author $Author$
 * @version $Revision$
 */
public class BigDecimalColumn extends Column {
    private static final Logger logger = LoggerFactory.getLogger(BigDecimalColumn.class);
    public BigDecimalColumn(String name, String header, IWebLocalizationManager manager, int width) throws DhtmlxException {
        super(name, header, manager, width);
        setAlign(ALIGN.RIGHT);
        setSort(SORT.INT);
    }

    public void toObject(Object object, Object value) throws DhtmlxException, OgnlException {
        BigDecimal number = null;
        if (value != null) {
            try {
                number = new BigDecimal((String) value);
            } catch (NumberFormatException e) {
                logger.warn("Erreur de conversion BigDecimal pour la valeur '{}'. Expression: {}, Objet: {}", value, getExpression(), object.getClass().getName(), e);
                throw new InputValidationException(
                        localizationManager.getMessage("invalid.input.error.message", new String[] { getHeaderText() }),
                        e
                );
            }
        }
        super.toObject(object, number);
    }

}
