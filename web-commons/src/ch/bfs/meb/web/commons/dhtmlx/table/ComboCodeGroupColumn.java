/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: web-commons

  $Id$

 */
package ch.bfs.meb.web.commons.dhtmlx.table;

import ch.bfs.meb.util.StringUtils;
import ch.bfs.meb.web.commons.dhtmlx.DhtmlxException;
import ch.bfs.meb.web.commons.dhtmlx.table.xmlbeans.ColumnDocument;
import ch.bfs.meb.web.commons.dhtmlx.table.xmlbeans.OptionDocument.Option;
import ch.bfs.meb.web.commons.exception.InputValidationException;
import ch.bfs.meb.web.commons.i18n.ILocalizedCode;
import ch.bfs.meb.web.commons.i18n.IWebLocalizationManager;
import ognl.Ognl;
import ognl.OgnlException;
import org.apache.xmlbeans.XmlCursor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Dhtmlx table column to display a column with a drop down selection box.
 * 
 * @author $Author$
 * @version $Revision$
 */
public class ComboCodeGroupColumn extends Column {

    private boolean addEmptyOption = false;
    private boolean _sortByKey;
    protected boolean _codeFirstFormat;
    protected String _codeGroup;
    protected Long _canton;
    protected Long _choiceFromCodegroupId = 0L;
    protected final List<Long> _codegroupIdList = new ArrayList<>();

    public ComboCodeGroupColumn(String name, String header, IWebLocalizationManager manager, String codeGroup, Long canton) throws DhtmlxException {
        this(name, header, manager, codeGroup, canton, true, 1);
    }

    public ComboCodeGroupColumn(String name, String header, IWebLocalizationManager manager, String codeGroup) throws DhtmlxException {
        this(name, header, manager, codeGroup, null, true, 1);
    }

    public ComboCodeGroupColumn(String name, String header, IWebLocalizationManager manager, String codeGroup, Long canton, boolean sortByKey)
            throws DhtmlxException {
        this(name, header, manager, codeGroup, canton, sortByKey, 1);
    }

    public ComboCodeGroupColumn(String name, String header, IWebLocalizationManager manager, String codeGroup, boolean sortByKey) throws DhtmlxException {
        this(name, header, manager, codeGroup, null, sortByKey, 1);
    }

    public ComboCodeGroupColumn(String name, String header, IWebLocalizationManager manager, String codeGroup, Long canton, int width) throws DhtmlxException {
        this(name, header, manager, codeGroup, canton, true, width);
    }

    public ComboCodeGroupColumn(String name, String header, IWebLocalizationManager manager, String codeGroup, int width, Long choiceFromCodegroupId)
            throws DhtmlxException {
        this(name, header, manager, codeGroup, null, true, width);
        _choiceFromCodegroupId = choiceFromCodegroupId;
    }

    public ComboCodeGroupColumn(String name, String header, IWebLocalizationManager manager, String codeGroup, int width) throws DhtmlxException {
        this(name, header, manager, codeGroup, null, true, width);
    }

    public ComboCodeGroupColumn(String name, String header, IWebLocalizationManager manager, String codeGroup, boolean sortByKey, int width)
            throws DhtmlxException {
        this(name, header, manager, codeGroup, null, sortByKey, width);
    }

    public ComboCodeGroupColumn(String name, String header, IWebLocalizationManager manager, String codeGroup, boolean sortByKey, int width, boolean addEmptyOption)
            throws DhtmlxException {
        this(name, header, manager, codeGroup, null, sortByKey, width);
        this.addEmptyOption = addEmptyOption;
    }

    public ComboCodeGroupColumn(String name, String header, IWebLocalizationManager manager, String codeGroup, Long canton, boolean sortByKey, int width)
            throws DhtmlxException {
        super(name, header, manager, width);

        _canton = canton;
        _sortByKey = sortByKey;

        setDefault(null);

        this._codeGroup = codeGroup;

        setEditorType(EDITOR.SELECTBOX);

        if (sortByKey) {
            setSort(SORT.INT);
        } else {
            setSort(SORT.STRING);
        }
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

    @Override
    public String getDisplayString(Object row) throws ognl.OgnlException {
        Object value = ognl.Ognl.getValue(expression, row);
        if (value == null) {
            return "";
        } else if (value instanceof Long) {
            Long longValue = (Long) value;
            String codeGroupValue = getLocalizationManager().getCodeGroupValueById(_codeGroup, (Long) value, _canton);
            if (StringUtils.isEmpty(codeGroupValue)) {
                return longValue.toString();
            } else {
                return codeGroupValue + " (" + longValue.toString() + ")";
            }
        }

        return "";
    }

    public void setCodegroupIdList(List<Long> codegroupIdList) {
        _codegroupIdList.clear();
        _codegroupIdList.addAll(codegroupIdList);
    }

    protected boolean keyChoosableInList(Long key) {
        if (_codegroupIdList.size() == 0) {
            return key >= _choiceFromCodegroupId;
        } else {
            return _codegroupIdList.contains(key);
        }
    }

    @Override
    public void createHeader(ColumnDocument.Column column) {
        super.createHeader(column);

        Collection<ILocalizedCode> options = getLocalizationManager().getCodeGroupAllValues(_codeGroup, _canton, _sortByKey);

        addEmptyOption(column);

        for (ILocalizedCode code : options) {
            if (keyChoosableInList(code.getKey())) {
                Option coption = column.addNewOption();
                coption.setValue(code.getKey().toString());
                XmlCursor optionCursor = coption.newCursor();
                optionCursor.setTextValue(getOptionText(code));
                optionCursor.dispose();
            }
        }
    }

    private void addEmptyOption(ColumnDocument.Column column) {
        if (this.addEmptyOption) {
            Option emptyOption = column.addNewOption();
            emptyOption.setValue("");
            XmlCursor optionCursor = emptyOption.newCursor();
            optionCursor.setTextValue("");
            optionCursor.dispose();
        }
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.web.commons.dhtmlx.table.Column#toValue(java.lang.Object)
     */
    @Override
    public Object toValue(Object row) throws OgnlException {
        Long code = (Long) ognl.Ognl.getValue(getExpression(), row);
        if (code == null || keyChoosableInList(code)) {
            return super.toValue(row);
        } else {
            String value = getLocalizationManager().getCodeGroupValueById(_codeGroup, code, _canton);
            if (value != null) {
                return value + " (" + code.toString() + ")";
            } else {
                return code;
            }
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(ComboCodeGroupColumn.class);

    public void toObject(Object object, Object value) throws DhtmlxException, OgnlException {
        try {
            logger.debug("toObject() appelé avec object={}, value={}", object, value);

            Object oldValue = Ognl.getValue(getExpression(), object);
            logger.debug("OGNL getValue réussie : expression='{}', oldValue={}", getExpression(), oldValue);

            if (oldValue == null || keyChoosableInList((Long) oldValue)) {
                logger.debug("Condition remplie (oldValue == null || keyChoosableInList), appel de super.toObject");
                super.toObject(object, value);
            } else {
                logger.debug("Clé non autorisée dans la liste : {}", oldValue);
            }

        } catch (OgnlException e) {
            logger.error("Erreur OGNL sur expression='{}', object={}, value={}",
                    getExpression(), object.getClass().getName(), value, e);

            if (e.getReason() != null) {
                throw new InputValidationException(
                        getLocalizationManager().getMessage("invalid.input.error.message", new String[] { getHeaderText() }),
                        e.getReason()
                );
            } else {
                throw new DhtmlxException("Could not map value for column " + getName(), e);
            }
        }
    }

    /**
     * Get the text for an option. There are two formatting options, depending
     * on the codeFirst parameter.
     * 
     * @return Returns the _option.
     */
    protected String getOptionText(ILocalizedCode code) {
        if (_codeFirstFormat) {
            return code.getKey() + ": " + code.getValue();
        } else {
            return code.getValue() + " (" + code.getKey() + ")";
        }

    }

    /**
     * Sets the formatting of the combo options. False is standard formatting
     * "name (code)", true is alternate formatting "code: name".
     * 
     * @param codeFirst
     *            The format to set.
     */
    public void setCodeFirstFormat(boolean codeFirst) {
        _codeFirstFormat = codeFirst;
    }

    public void setCanton(Long canton) {
        _canton = canton;
    }
}
