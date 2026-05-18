/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010, 2011

  Projekt: web-commons

  $Id: ComboMunicipalityColumn.java 2081 2011-01-04 08:09:42Z msc $

 */
package ch.admin.bfs.sbg.dhtmlx.table;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.bfs.meb.sbg.web.service.IEventService;
import ch.bfs.meb.sbg.web.ws.sbgevent.KeyAspect;
import ch.bfs.meb.web.commons.dhtmlx.CommandConstants;
import ch.bfs.meb.web.commons.dhtmlx.DhtmlxException;
import ch.bfs.meb.web.commons.dhtmlx.FileHttpResult;
import ch.bfs.meb.web.commons.dhtmlx.table.Column;
import ch.bfs.meb.web.commons.i18n.IWebLocalizationManager;
import ognl.OgnlException;

/**
 * Dhtmlx table column to display a column with a drop down selection box of keyAspects depending on sbfiCode.
 * 
 * @author $Author: msc $
 * @version $Revision: 2081 $
 */
public class SbgKeyAspectDynamicComboColumn extends Column {
    protected IEventService _eventService;
    protected String _sbfiCodeId;
    protected String _getSbfiCodeGroupJavascript;
    protected String _table;
    protected final Map<Long, List<KeyAspect>> _keyAspects = new HashMap<Long, List<KeyAspect>>();

    public SbgKeyAspectDynamicComboColumn(String name, String header, IWebLocalizationManager manager, String sbfiCodeId, IEventService eventService,
            String getSbfiCodeGroupJavascript, String table) throws DhtmlxException {
        this(name, header, manager, sbfiCodeId, eventService, getSbfiCodeGroupJavascript, 6, table);
    }

    public SbgKeyAspectDynamicComboColumn(String name, String header, IWebLocalizationManager manager, String sbfiCodeId, IEventService eventService,
            String getSbfiCodeGroupJavascript, int width, String table) throws DhtmlxException {
        super(name, header, manager, width);

        setDefault(null);

        _eventService = eventService;
        _sbfiCodeId = sbfiCodeId;
        _getSbfiCodeGroupJavascript = getSbfiCodeGroupJavascript;
        _table = table;

        setEditorType(EDITOR.COMBOBOX_EX);
    }

    public static FileHttpResult createComboXml(String mask, Map<Long, String> keyAspects, IWebLocalizationManager localizationManager,
            String keyAspectCodeGroup) {
        String xml = "<?xml version=\"1.0\" ?><complete>";
        for (Long keyAspect : keyAspects.keySet()) {
            String keyAspectName = keyAspects.get(keyAspect);
            xml += "<option value=\"" + protectSpecialCharacters(keyAspectName) + " (" + keyAspect.toString() + ")\">" + protectSpecialCharacters(keyAspectName)
                    + " (" + keyAspect.toString() + ")</option>";
        }
        xml += "</complete>";
        return new FileHttpResult(xml.getBytes(), "keyAspect.xml") {
            public String getContentType() {
                return "application/xml";
            }
        };
    }

    /**
     * @return Returns the default value.
     */
    @Override
    public Object getDefault() {
        if (defaultValue == null) {
            defaultValue = "";
        }

        return defaultValue;
    }

    protected synchronized String getKeyAspect(Long sbfiCode, Long keyAspectCode) {
        if (sbfiCode == null) {
            return null;
        }

        if (!_keyAspects.containsKey(sbfiCode)) {
            List<KeyAspect> keyAspects = _eventService.getKeyAspectsForSbfiCode(sbfiCode).getKeyAspects();
            _keyAspects.put(sbfiCode, keyAspects);
        }

        List<KeyAspect> keyAspects = _keyAspects.get(sbfiCode);
        for (KeyAspect keyAspect : keyAspects) {
            if (keyAspect.getKeyAspectCode().equals(keyAspectCode)) {
                return getLocalizationManager().getLanguage().toLowerCase().equals("fr") ? keyAspect.getTextFr() : keyAspect.getTextDe();
            }
        }

        return null;
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.web.commons.dhtmlx.table.Column#toValue(java.lang.Object)
     */
    @Override
    public Object toValue(Object row) throws OgnlException {
        Long keyAspectCode = (Long) ognl.Ognl.getValue(getExpression(), row);
        Long sbfiCode = (Long) ognl.Ognl.getValue(ognl.Ognl.parseExpression(_sbfiCodeId), row);
        String value = getKeyAspect(sbfiCode, keyAspectCode);
        if (value != null) {
            return value + " (" + keyAspectCode + ")";
        } else {
            return keyAspectCode != null ? keyAspectCode : "";
        }
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.web.commons.dhtmlx.table.Column#toObject(java.lang.Object, java.lang.Object)
     */
    @Override
    public void toObject(Object object, Object value) throws DhtmlxException, OgnlException {
        if (value != null) {
            String s = (String) value;
            int closing = s.lastIndexOf(')');
            int opening = s.lastIndexOf('(');
            if (closing != -1 && opening != -1 && opening < closing) {
                value = s.substring(opening + 1, closing);
            }
        }

        super.toObject(object, value);
    }

    @Override
    public void createHeader(ch.bfs.meb.web.commons.dhtmlx.table.xmlbeans.ColumnDocument.Column column) {
        super.createHeader(column);

        column.setSource(_getSbfiCodeGroupJavascript + "^controller.do?control=" + _table + "&command=" + CommandConstants.KEYASPECT_XML);
        column.setAuto(true);
        column.setCache(false);
    }
}
