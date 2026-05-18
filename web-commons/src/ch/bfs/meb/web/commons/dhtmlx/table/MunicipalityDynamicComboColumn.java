/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010, 2011

  Projekt: web-commons

  $Id: ComboMunicipalityColumn.java 2081 2011-01-04 08:09:42Z msc $

 */
package ch.bfs.meb.web.commons.dhtmlx.table;

import java.text.Collator;
import java.util.*;

import ch.bfs.meb.web.commons.dhtmlx.CommandConstants;
import ch.bfs.meb.web.commons.dhtmlx.DhtmlxException;
import ch.bfs.meb.web.commons.dhtmlx.FileHttpResult;
import ch.bfs.meb.web.commons.i18n.ILocalizedCode;
import ch.bfs.meb.web.commons.i18n.IWebLocalizationManager;
import ognl.OgnlException;


/**
 * Dhtmlx table column to display a column with a drop down selection box of municipalities.
 * 
 * @author $Author: msc $
 * @version $Revision: 2081 $
 */
public class MunicipalityDynamicComboColumn extends Column {
    protected String _codeGroup;
    protected Long _canton;
    protected String _table;

    public MunicipalityDynamicComboColumn(String name, String header, IWebLocalizationManager manager, String codeGroup, Long canton, String table)
            throws DhtmlxException {
        this(name, header, manager, codeGroup, canton, 1, table);
    }

    public MunicipalityDynamicComboColumn(String name, String header, IWebLocalizationManager manager, String codeGroup, String table) throws DhtmlxException {
        this(name, header, manager, codeGroup, null, 1, table);
    }

    public MunicipalityDynamicComboColumn(String name, String header, IWebLocalizationManager manager, String codeGroup, int width, String table)
            throws DhtmlxException {
        this(name, header, manager, codeGroup, null, width, table);
    }

    public MunicipalityDynamicComboColumn(String name, String header, IWebLocalizationManager manager, String codeGroup, Long canton, int width, String table)
            throws DhtmlxException {
        super(name, header, manager, width);

        setCanton(canton);

        setDefault(null);

        _codeGroup = codeGroup;
        _table = table;

        setEditorType(EDITOR.COMBOBOX_EX);
    }

    public static FileHttpResult createComboXml(String mask, final Long canton, String codeGroup, IWebLocalizationManager localizationManager) {
        int count = 0;
        final HashMap<Long, Long> cantons = new HashMap<Long, Long>();
        mask = mask.toLowerCase();

        ArrayList<ILocalizedCode> codes = new ArrayList<ILocalizedCode>();
        if (isNumber(mask)) {
            for (ILocalizedCode code : localizationManager.getCodeGroupAllValues(codeGroup, canton, true)) {
                String entry = "" + code.getKey();
                if (entry.startsWith(mask)) {
                    codes.add(code);
                    cantons.put(code.getKey(), canton);
                    if (++count == 50) {
                        break;
                    }
                }
            }
            if (count < 50) {
                for (long c = 1; c <= 26; ++c) {
                    if (c != canton) {
                        for (ILocalizedCode code : localizationManager.getCodeGroupAllValues(codeGroup, c, true)) {
                            String entry = "" + code.getKey();
                            if (entry.startsWith(mask)) {
                                codes.add(code);
                                cantons.put(code.getKey(), c);
                                if (++count == 50) {
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        } else {
            for (ILocalizedCode code : localizationManager.getCodeGroupAllValues(codeGroup, canton, true)) {
                String entry = code.getValue().toLowerCase() + " (" + code.getKey() + ")";
                if (entry.startsWith(mask)) {
                    codes.add(code);
                    cantons.put(code.getKey(), canton);
                    if (++count == 50) {
                        break;
                    }
                }
            }
            if (count < 50) {
                for (long c = 1; c <= 26; ++c) {
                    if (c != canton) {
                        for (ILocalizedCode code : localizationManager.getCodeGroupAllValues(codeGroup, c, true)) {
                            String entry = code.getValue().toLowerCase() + " (" + code.getKey() + ")";
                            if (entry.startsWith(mask)) {
                                codes.add(code);
                                cantons.put(code.getKey(), c);
                                if (++count == 50) {
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }

        final Collator collator = Collator.getInstance(Locale.GERMAN);
        collator.setStrength(Collator.SECONDARY);
        Collections.sort(codes, new Comparator<ILocalizedCode>() {
            @Override
            public int compare(ILocalizedCode code1, ILocalizedCode code2) {
                long canton1 = cantons.get(code1.getKey());
                long canton2 = cantons.get(code2.getKey());
                if (canton1 == canton && canton2 != canton) {
                    return -1;
                } else if (canton1 != canton && canton2 == canton) {
                    return 1;
                } else {
                    return collator.compare(code1.getValue(), code2.getValue());
                }
            }
        });

        String xml = "<?xml version=\"1.0\" ?><complete>";
        for (ILocalizedCode code : codes) {
            xml += "<option value=\"" + protectSpecialCharacters(code.getValue()) + " (" + code.getKey() + ")\">" + protectSpecialCharacters(code.getValue())
                    + " (" + code.getKey() + ")</option>";
        }
        xml += "</complete>";
        return new FileHttpResult(xml.getBytes(), "municipality.xml") {
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

    /* (non-Javadoc)
     * @see ch.bfs.meb.web.commons.dhtmlx.table.Column#toValue(java.lang.Object)
     */
    @Override
    public Object toValue(Object row) throws OgnlException {
        Long code = (Long) ognl.Ognl.getValue(getExpression(), row);
        String value = getLocalizationManager().getCodeGroupValueById(_codeGroup, code, _canton);
        if (value != null) {
            return value + " (" + code + ")";
        } else {
            value = getLocalizationManager().searchValueInAllCantons(_codeGroup, code);
            if (value != null) {
                return value + " (" + code + ")";
            } else {
                return code != null ? code : "";
            }
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

        column.setSource("controller.do?control=" + _table + "&command=" + CommandConstants.MUNICIPALITY_XML + "&codeGroup=" + _codeGroup);
        column.setAuto(true);
        column.setCache(false);
    }

    public void setCanton(Long canton) {
        if (canton < 1L) {
            _canton = 1L;
        } else {
            _canton = canton;
        }
    }
}
