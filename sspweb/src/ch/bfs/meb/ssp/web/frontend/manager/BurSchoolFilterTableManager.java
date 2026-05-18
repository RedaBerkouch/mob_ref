/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sspweb
 */
package ch.bfs.meb.ssp.web.frontend.manager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import ch.bfs.meb.util.CodegroupUtility;
import ch.bfs.meb.web.commons.dhtmlx.table.FilterTableManagerBase;
import ch.bfs.meb.web.commons.dhtmlx.table.WebFilterListResult;
import ch.bfs.meb.web.commons.util.IFilterService;

@Scope("session")
@Component("burSchoolFilterTableManager")
public class BurSchoolFilterTableManager extends FilterTableManagerBase {
    @SuppressWarnings("unused")
    private static final Log LOGGER = LogFactory.getLog(BurSchoolFilterTableManager.class);

    public static final String MANAGER_NAME = "burSchoolFilter";

    public static final String CONTROL_NAME = MANAGER_NAME + SUFFIX;

    @Autowired
    private IFilterService _filterService;

    /**
     * Return the name of the manager
     * 
     * @return the managers name
     */
    public String getName() {
        return MANAGER_NAME;
    }

    /**
     * Return the control name of the manager
     * 
     * @return the managers control name
     */
    public String getControlName() {
        return CONTROL_NAME;
    }

    public WebFilterListResult getAllRows() {

        return _filterService.getFiltersForRefObject(CodegroupUtility.SSP_OBJECTTYPE_BURSCHOOL);
    }
}
