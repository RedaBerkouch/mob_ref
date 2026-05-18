/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: web-commons

  $Id$

 */
package ch.bfs.meb.web.commons.dhtmlx.table;

/**
 * Constants for http parameter
 * 
 * @author $Author$
 * @version $Revision$
 */
public class ParameterConstants {

    /**
     * File (Multipart request)
     */
    public final static String PARAM_FILENAME = "fileName";

    /**
     * Selected row id
     */
    public final static String PARAM_ROWID = "gr_id";

    public final static String PARAM_ROWS_LOADED = "posStart";
    public final static String PARAM_COUNT = "count";

    public final static String PARAM_SORT_STATE = "sortState";

    public final static String PARAM_PLAUSIERROR = "plausierror";

    public final static String PARAM_RAW_DATA = "data";

    public final static String PARAM_NATIVE_EDITOR_STATUS = "!nativeeditor_status";

    public final static String PARAM_COMMAND = "command";

    public final static String PARAM_GOAL = "goal";

    public final static String PARAM_SELECTED_ROW_IDS = "selRowId";

    public final static String PARAM_SELECTED_MASTER = "selMaster";

    public final static String PARAM_NO_PLAUSI = "noPlausi";

    /** serialized Grid constants */
    public final static String PARAM_WHEREFILTERDATA = "whereFilterData";
    public final static String PARAM_PREDEFINEDFILTERDATA = "predefinedFilterData";
    public final static String PARAM_EXPORTDATA = "exportData";
    public final static String PARAM_FILTERVERSION = "filterVersion";
    public final static String PARAM_FILTERCANTON = "filterCanton";
    public final static String PARAM_FILTERCOMMAND = "filterCommand";
}
