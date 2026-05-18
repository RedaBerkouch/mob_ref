/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: web-commons

  $Id$

 */
package ch.bfs.meb.web.commons.dhtmlx.table;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.xmlbeans.XmlCursor;

import ch.bfs.meb.web.commons.dhtmlx.DhtmlxException;
import ch.bfs.meb.web.commons.exception.InputValidationException;
import ch.bfs.meb.web.commons.i18n.IWebLocalizationManager;
import ognl.Ognl;
import ognl.OgnlException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A column of a dhtmlx table.
 * 
 * @author $Author$
 * @version $Revision$
 */
public class Column {
    private static final Logger logger = LoggerFactory.getLogger(Column.class);
    public static final class EDITOR {

        /**
         * ReadOnly (ro) - cell can't be edited
         */
        public static final String READ_ONLY = "rotxt";

        /**
         * Simple Editor (ed) - text is edited inside cell
         */
        public static final String SIMPLE = "edtxt";

        /**
         * Text Editor (txt) - text is edited in popup multiline textarea
         */
        public static final String FULL = "txt";

        /**
         * Checkbox (ch) - standard checkbox
         */
        public static final String CHECKBOX = "ch";

        /**
         * Radiobutton (ra) - column oriented radiobutton
         */
        public static final String RADIOBUTTON = "ra";

        /**
         * Select box (coro) - simple selectbox
         */
        public static final String SELECTBOX = "corotxt";

        /**
         * Select box (cororo) - Adesso read-only selectbox
         */
        public static final String SELECTBOX_RO = "corotxtfix";

        /**
         * Select box (co) - simple combobox
         */
        public static final String COMBOBOX = "co";

        /**
         * Enhanced combo box (co) - enhanced combobox
         */
        public static final String COMBOBOX_EX = "combo";

        /**
         * Image (img) - not editable. Value considered as url of image
         */
        public static final String IMAGE = "img";

        /**
         * Link button - not editable. Value considered as url of link button
         */
        public static final String LINK = "link";

        /**
         * Color picker (cp) - simple color picker (just for example). Value
         * considered as color code or name
         */
        public static final String COLORPICKER = "cp";

        /**
         * Calendar (dhxCalendarA) - shows calendar control
         */
        public static final String CALENDAR = "dhxCalendarA";

        /**
         * Price oriented (price) - shows $ before _value, all values eql 0
         * shown as na in red color
         */
        public static final String PRICE = "price";

        /**
         * Dynamic of Sales (dyn) - shows up/down icon depending on _value. Also
         * color coding available (green/red)
         */
        public static final String DYNAMIC = "dyn";

        /**
         * Multiline read-only "pure" Text Editor, no interpretation of special
         * characters SBG special, will probably be integrated in
         * dhtmlXGridCell?
         */
        public static final String HTML_MULTILINE_READ_ONLY = "rotxttxt";

        /**
         * Parameter editor (paramlist) - SBG special
         */
        public static final String PARAMLIST = "paramlist";

        /**
         * Plausi error editor (plausierror) - SBG special
         */
        public static final String PLAUSIERROR = "plausierror";

        /**
         * Read only plausi error editor (roplausierror) - SBG special
         */
        public static final String PLAUSIERROR_READ_ONLY = "roplausierror";
    }

    public static final class SORT {
        public static final String STRING = "str";
        public static final String INT = "int";
        public static final String DATE = "date";
        public static final String NO_SORT = "na";
    }

    public static final class ALIGN {
        public static final String LEFT = "left";
        public static final String RIGHT = "right";
        public static final String CENTER = "center";
    }

    public static final class COLOR {
        public static final String WHITE = "white";
        public static final String BLACK = "black";
        public static final String LIGHTGREY = "#eeeeee";
    }

    public enum EditType {
        editable, editwheninserted, readonly
    };

    public EditType editType = EditType.editable;

    protected String name;

    protected int width = 20;

    protected String editorType = EDITOR.SIMPLE;

    protected String color = null;

    protected String align = ALIGN.LEFT;

    protected String sort = SORT.STRING;

    protected Object defaultValue = "";

    protected String header = "";

    protected Object expression;

    protected boolean visible = true;
    protected boolean hidden = false;
    protected boolean identity = false;

    protected IWebLocalizationManager localizationManager = null;

    private Column(String name, String header) throws DhtmlxException {
        this.name = name;
        this.header = header;

        try {
            setExpression(getName());
        } catch (OgnlException e) {
            throw new DhtmlxException("Error creating column'" + this.name + "'", e);
        }
    }

    public Column(String name, String header, IWebLocalizationManager manager) throws DhtmlxException {
        this(name, header);
        this.localizationManager = manager;
    }

    public Column(String name, String header, IWebLocalizationManager manager, int width) throws DhtmlxException {

        this(name, header, manager);
        this.width = width;
    }

    public IWebLocalizationManager getLocalizationManager() {
        return this.localizationManager;
    }

    /**
     * @return Returns the align.
     */
    public String getAlign() {
        return this.align;
    }

    /**
     * @param align
     *            The align to set.
     */
    public void setAlign(String align) {
        this.align = align;
    }

    /**
     * @return Returns the Color.
     */
    public String getColor() {
        return this.color;
    }

    /**
     * @param color
     *            The color to set.
     */
    public void setColor(String color) {
        this.color = color;
    }

    /**
     * @return Returns the editorType.
     */
    public String getEditorType() {
        return this.editorType;
    }

    /**
     * @param type
     *            The editorType to set.
     */
    public void setEditorType(String type) {
        this.editorType = type;
    }

    /**
     * @return Returns the header.
     */
    public String getHeader() {
        return this.header;
    }

    /**
     * @param this.header The header to set.
     */
    public void setHeader(String header) {
        this.header = header;
    }

    /**
     * Set the parsed expression
     * 
     * @param expression
     * @throws OgnlException 
     */
    protected void setExpression(String name) throws OgnlException {
        this.expression = ognl.Ognl.parseExpression(name);
    }

    /**
     * NOTE: Always use this method to access the expression object so it can be overwritten for subclasses (e.g. MultilanguageReadOnlyColumn)
     * 
     * @return the used expression
     * @throws OgnlException 
     */
    protected Object getExpression() throws OgnlException {
        return this.expression;
    }

    /**
     * @return Returns the name.
     */
    public String getName() {
        return this.name;
    }

    /**
     * @param name
     *            The name to set.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return Returns the sort type.
     */
    public String getSort() {
        return this.sort;
    }

    /**
     * @param sort
     *            The sort type to set.
     */
    public void setSort(String sort) {
        this.sort = sort;
    }

    /**
     * @return Returns the columns width.
     */
    public int getWidth() {
        return this.width;
    }

    /**
     * @param _width
     *            The columns width to set.
     */
    public void setWidth(int width) {
        this.width = width;
    }

    public String getHeaderText() {
        return getLocalizationManager().getMessage(getHeader());
    }

    public void createHeader(ch.bfs.meb.web.commons.dhtmlx.table.xmlbeans.ColumnDocument.Column column) {
        column.setWidth("" + getWidth());
        column.setType(getEditorType());
        column.setAlign(ch.bfs.meb.web.commons.dhtmlx.table.xmlbeans.ColumnDocument.Column.Align.Enum.forString(getAlign()));
        column.setSort(ch.bfs.meb.web.commons.dhtmlx.table.xmlbeans.ColumnDocument.Column.Sort.Enum.forString(getSort()));
        column.setColor(getColor());
        column.setTitle(getHeaderText());
        if (isHidden()) {
            column.setHidden(true);
        }

        XmlCursor cursor = column.newCursor();
        cursor.setTextValue(getHeaderText());
        cursor.dispose();
    }

    public String getDisplayString(Object row) throws OgnlException {
        return toValue(row).toString();
    }

    public Object toValue(Object row) throws OgnlException {

        Object value = ognl.Ognl.getValue(getExpression(), row);

        if (value == null) {
            return "";
        }

        return value;

    }



    @SuppressWarnings("unchecked")
    public void toObject(Object object, Object value) throws DhtmlxException, OgnlException {
        try {
            logger.debug("toObject() appelé avec object de type '{}', value = {}",
                    object != null ? object.getClass().getName() : "null", value);

            String expression = String.valueOf(getExpression());
            Object oldValue = Ognl.getValue(expression, object);
            logger.debug("Résultat de Ognl.getValue('{}', object) : {}", expression, oldValue);

            if (oldValue instanceof Collection) {
                Collection<Object> oldCollection = (Collection<Object>) oldValue;
                oldCollection.clear();
                logger.debug("Collection existante vidée.");
                if (value instanceof Collection) {
                    logger.debug("Ajout de {} éléments à la collection via addAll.", ((Collection<?>) value).size());
                    oldCollection.addAll((Collection<?>) value);
                }
            } else {
                // Conversion automatique de Number vers String
                if (oldValue instanceof String && value instanceof Number) {
                    logger.debug("Conversion automatique de Number {} vers String pour la propriété '{}'", value, expression);
                    value = String.valueOf(value);
                }

                // Conversion automatique de String vers Boolean si nécessaire
                if (oldValue instanceof Boolean && value instanceof String) {
                    logger.debug("Conversion automatique de String '{}' vers Boolean pour la propriété '{}'", value, expression);
                    value = Boolean.parseBoolean((String) value);
                }

                logger.debug("Objet simple, appel de Ognl.setValue('{}', object, value)", expression);
                Ognl.setValue(expression, object, value);
            }

        } catch (OgnlException e) {
            String safeHeader = escapeMessageFormat(getHeaderText());
            logger.error("Erreur lors du mapping OGNL - Expression: '{}', Object: '{}', Value: '{}'",
                    getExpression(), object, value, e);

            if (e.getReason() != null) {
                logger.error("Cause interne OGNL : {}", e.getReason().getMessage(), e.getReason());
                throw new InputValidationException(
                        localizationManager.getMessage("invalid.input.error.message", new String[] { safeHeader }),
                        e.getReason());
            } else {
                throw new DhtmlxException("Could not map value for column " + getName(), e);
            }
        }
    }


    public String escapeMessageFormat(String input) {
        if (input == null) return "";
        return input.replace("'", "''")
                .replace("{", "'{'")
                .replace("}", "'}'");
    }


    /**
     * Non-visible columns are not sent to the grid.
     * 
     * @return Returns the visible.
     */
    public boolean isVisible() {
        return visible;
    }

    /**
     * Non-visible columns are not sent to the grid.
     * 
     * @param visible	The visible to set.
     */
    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    /**
     * Hidden columns are sent to the grid, but not displayed.
     * 
     * @return Returns the hidden.
     */
    public boolean isHidden() {
        return hidden;
    }

    /**
     * Hidden columns are sent to the grid, but not displayed.
     * 
     * @param hidden	The visible to set.
     */
    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    /**
     * @return Returns the identity.
     */
    public boolean isIdentity() {
        return identity;
    }

    /**
     * @param identity
     *            The identity to set.
     */
    public void setIdentity(boolean identity) {
        this.identity = identity;
    }

    /**
     * @return Returns the editType.
     */
    public EditType getEditType() {
        return this.editType;
    }

    /**
     * @param type
     *            The editType to set.
     */
    public void setEditType(EditType type) {
        this.editType = type;
    }

    /**
     * @return Returns the default value.
     */
    public Object getDefault() {
        return this.defaultValue;
    }

    /**
     * @param default The _default to set.
     */
    public void setDefault(Object defaultValue) {
        this.defaultValue = defaultValue;
    }

    public boolean hasUserData() {
        return false;
    }

    public Map<String, String> getUserData(Object row) throws OgnlException {
        return new HashMap<String, String>();
    }

    public void userDataToObject(Object object, ParameterList params) throws DhtmlxException {}

    public boolean hasTooltip() {
        return false;
    }

    public String getTooltip(Object row) {
        return null;
    }

    public boolean equals(Object obj) {
        if (obj instanceof Column) {
            Column column = (Column) obj;

            return column.getName().equals(getName());
        }

        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        if (getName() != null) {
            return getName().hashCode();
        }
        return super.hashCode();
    }

    public String toString() {

        return "Column {" + getName() + "," + getAlign() + "," + getEditorType() + "," + getColor() + "," + getSort() + "," + getWidth() + "}";
    }

    protected static String protectSpecialCharacters(String originalUnprotectedString) {
        if (originalUnprotectedString == null) {
            return null;
        }
        boolean anyCharactersProtected = false;
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0; i < originalUnprotectedString.length(); i++) {
            char ch = originalUnprotectedString.charAt(i);
            boolean controlCharacter = ch < 32;
            boolean unicodeButNotAscii = ch > 126;
            boolean characterWithSpecialMeaningInXML = ch == '<' || ch == '&' || ch == '>';
            if (characterWithSpecialMeaningInXML || unicodeButNotAscii || controlCharacter) {
                stringBuffer.append("&#" + (int) ch + ";");
                anyCharactersProtected = true;
            } else {
                stringBuffer.append(ch);
            }
        }
        if (anyCharactersProtected == false) {
            return originalUnprotectedString;
        }
        return stringBuffer.toString();
    }

    protected static boolean isNumber(String value) {
        for (int i = 0; i < value.length(); ++i) {
            if (value.charAt(i) < '0' || value.charAt(i) > '9') {
                return false;
            }
        }
        return true;
    }
}
