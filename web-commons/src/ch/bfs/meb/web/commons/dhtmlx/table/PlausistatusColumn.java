/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: web-commons

  $Id$

 */
package ch.bfs.meb.web.commons.dhtmlx.table;

import ch.bfs.meb.util.CodegroupUtility;
import ch.bfs.meb.web.commons.dhtmlx.DhtmlxException;
import ch.bfs.meb.web.commons.i18n.IWebLocalizationManager;
import ognl.OgnlException;

/**
 * Dhtmlx table column to display the plausistatus.
 * 
 * @author $Author$
 * @version $Revision$
 */
public class PlausistatusColumn extends Column {
    protected String _codegroupPlausiStatus = CodegroupUtility.MEB_PLAUSISTATUS;

    public PlausistatusColumn(String name, String header, String codeGroup, IWebLocalizationManager manager, int width) throws DhtmlxException {
        super(name, header, manager, width);
        _codegroupPlausiStatus = codeGroup;
        setEditorType(EDITOR.PLAUSIERROR);
        setDefault(getLocalizationManager().getCodeGroupValueById(_codegroupPlausiStatus, CodegroupUtility.MEB_PLAUSISTATUS_UNDEFINED));
    }

    public PlausistatusColumn(String name, String header, IWebLocalizationManager manager, int width) throws DhtmlxException {
        this(name, header, CodegroupUtility.MEB_PLAUSISTATUS, manager, width);
    }

    @Override
    public void toObject(Object object, Object value) throws DhtmlxException {
        // These columns are read only, so no need to set new value.
    }

    @Override
    public Object toValue(Object row) throws OgnlException {
        Object value = ognl.Ognl.getValue(expression, row);
        if (value != null && value instanceof Long) {
            String codeText = getLocalizationManager().getCodeGroupValueById(_codegroupPlausiStatus, (Long) value);
            return (codeText == null || codeText.trim().length() == 0) ? value.toString() : codeText;
        } else {
            return "";
        }
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.web.commons.dhtmlx.table.Column#hasTooltip()
     */
    @Override
    public boolean hasTooltip() {
        return true;
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.web.commons.dhtmlx.table.Column#getTooltip(java.lang.Object)
     */
    @Override
    public String getTooltip(Object row) {
        return getLocalizationManager().getMessage("tableColumn.plausistatus.tooltip");
    }
}
