package ch.bfs.meb.web.commons.dhtmlx.table;

import java.text.Collator;
import java.util.*;

import ch.bfs.meb.web.commons.dhtmlx.DhtmlxException;
import ch.bfs.meb.web.commons.dhtmlx.FileHttpResult;
import ch.bfs.meb.web.commons.i18n.ILocalizedCode;
import ch.bfs.meb.web.commons.i18n.IWebLocalizationManager;
import ognl.OgnlException;

/**
 * Dhtmlx table column to display a column with a drop down selection box of a specific codegroup and searchable on both key and value.
 */
public class DoubleSearchableCodegroupCombo extends Column {
    protected String codeGroup;
    protected String table;
    protected String xmlCommand;

    public DoubleSearchableCodegroupCombo(String name, String header, IWebLocalizationManager manager, String codeGroup, String table, String xmlCommand) throws DhtmlxException {
        this(name, header, manager, codeGroup, 1, table, xmlCommand);
    }

    public DoubleSearchableCodegroupCombo(String name, String header, IWebLocalizationManager manager, String codeGroup, int width, String table, String xmlCommand)
            throws DhtmlxException {
        super(name, header, manager, width);

        setDefault(null);

        this.codeGroup = codeGroup;
        this.table = table;
        this.xmlCommand = xmlCommand;

        setEditorType(EDITOR.COMBOBOX_EX);
    }

    public static FileHttpResult createComboXml(String mask, String codeGroup, IWebLocalizationManager localizationManager) {
        int count = 0;
        mask = mask.toLowerCase();

        ArrayList<ILocalizedCode> codes = new ArrayList<ILocalizedCode>();
        if (isNumber(mask)) {
            for (ILocalizedCode code : localizationManager.getCodeGroupAllValues(codeGroup, null, true)) {
                String entry = "" + code.getKey();
                if (entry.contains(mask)) {
                    codes.add(code);
                    if (++count == 50) {
                        break;
                    }
                }
            }
        } else {
            for (ILocalizedCode code : localizationManager.getCodeGroupAllValues(codeGroup, null, true)) {
                String entry = code.getValue().toLowerCase() + " (" + code.getKey() + ")";
                if (entry.contains(mask)) {
                    codes.add(code);
                    if (++count == 50) {
                        break;
                    }
                }
            }
        }

        final Collator collator = Collator.getInstance(localizationManager.getLocale());
        collator.setStrength(Collator.SECONDARY);
        Collections.sort(codes, (code1, code2) -> collator.compare(code1.getValue(), code2.getValue()));

        StringBuilder xml = new StringBuilder("<?xml version=\"1.0\" ?><complete>");
        for (ILocalizedCode code : codes) {
            xml.append("<option value=\"").append(protectSpecialCharacters(code.getValue())).append(" (").append(code.getKey()).append(")\">")
                    .append(protectSpecialCharacters(code.getValue())).append(" (").append(code.getKey()).append(")</option>");
        }
        xml.append("</complete>");
        return new FileHttpResult(xml.toString().getBytes(), "results.xml") {
            public String getContentType() {
                return "application/xml";
            }
        };
    }

    @Override
    public Object getDefault() {
        return defaultValue == null ? "" : defaultValue;
    }

    @Override
    public Object toValue(Object row) throws OgnlException {
        Long code = (Long) ognl.Ognl.getValue(getExpression(), row);
        if (code == null) {
            return "";
        }

        String value = getLocalizationManager().getCodeGroupValueById(codeGroup, code, null);
        return value == null ? code : value + " (" + code + ")";
    }

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

        column.setSource("controller.do?control=" + table + "&command=" + xmlCommand + "&codeGroup=" + codeGroup);
        column.setAuto(true);
        column.setCache(false);
    }

}
