/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: web-commons

  $Id$

 */
package ch.bfs.meb.web.commons.dhtmlx.table;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.xmlbeans.XmlCursor;

import ch.bfs.meb.web.commons.dhtmlx.DhtmlxException;
import ch.bfs.meb.web.commons.dhtmlx.table.xmlbeans.OptionDocument.Option;
import ch.bfs.meb.web.commons.i18n.IWebLocalizationManager;

/**
 * A column with a combobox.
 * 
 * @author $Author$
 * @version $Revision$
 */
public class ComboColumn extends Column {
    private final Map<Long, String> _comboItems = new HashMap<Long, String>();

    public ComboColumn(String name, String header, IWebLocalizationManager manager, int width) throws DhtmlxException {
        super(name, header, manager, width);

        setDefault(null);

        setEditorType(EDITOR.SELECTBOX);
        setSort(SORT.INT);
    }

    /**
     * @return Returns the default value.
     */
    public Object getDefault() {

        // Generate default from option array
        if (defaultValue == null) {

            // ArrayList<ComboOption> options = getComboOptions();

            defaultValue = "";

            /*
              @todo Allow null values (null cannot be set otherwise at the
             *       moment
             */
            /*
             * if ( options.size() > 0 ) { _default =
             * ((ComboOption)options.get(0)).getCode(); } else {
             * LOGGER.info("No combo options for combobox '" + getName() +
             * " ' defined");
             * 
             * _default = ""; }
             */
        }

        return defaultValue;
    }

    @Override
    public void createHeader(ch.bfs.meb.web.commons.dhtmlx.table.xmlbeans.ColumnDocument.Column column) {
        super.createHeader(column);

        Set<Long> items = new TreeSet<Long>(_comboItems.keySet());

        for (Long item : items) {
            Option coption = column.addNewOption();
            coption.setValue(item.toString());
            XmlCursor optionCursor = coption.newCursor();
            optionCursor.setTextValue(getLocalizationManager().getMessage(_comboItems.get(item), null, _comboItems.get(item)));
            optionCursor.dispose();
        }
    }

    /**
     * @param Set
     *            the options for combo boxes.
     */
    public void addComboItem(Long key, String value) {
        _comboItems.put(key, value);
    }
}
