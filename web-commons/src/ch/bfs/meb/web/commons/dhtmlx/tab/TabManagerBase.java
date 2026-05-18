/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: web-commons

  $Id$

 */
package ch.bfs.meb.web.commons.dhtmlx.tab;

import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ch.bfs.meb.web.commons.dhtmlx.DhtmlxException;
import ch.bfs.meb.web.commons.dhtmlx.DhtmlxManagerBase;
import ch.bfs.meb.web.commons.dhtmlx.tab.xmlbeans.TabbarDocument;
import ch.bfs.meb.web.commons.dhtmlx.tab.xmlbeans.TabbarDocument.Tabbar;
import ch.bfs.meb.web.commons.dhtmlx.tab.xmlbeans.TabbarDocument.Tabbar.Row;
import ch.bfs.meb.web.commons.dhtmlx.tab.xmlbeans.TabbarDocument.Tabbar.Row.Tab;

/**
 * TODO Describe this class
 * 
 * @author $Author$
 * @version $Revision$
 */
public abstract class TabManagerBase extends DhtmlxManagerBase {

    public static final String SUFFIX = "Control";

    private static final Log LOGGER = LogFactory.getLog(TabManagerBase.class);

    public static class TABPOSTION {

        public static final String TOP = "top";

        public static final String BOTTOM = "bottom";

        public static String LEFT = "left";

        public static String RIGHT = "right";
    }

    protected String _tabPosition = TABPOSTION.TOP;

    protected final ArrayList<TabX> _tabs = new ArrayList<TabX>();

    public void addTab(TabX tab) {
        _tabs.add(tab);
    }

    public void removeTab(TabX tab) {
        _tabs.remove(tab);
    }

    public String getControlName() {
        return getName() + SUFFIX;
    }

    /**
     * Returns the scripting part that initializes the fhtmris and the callbacks
     * 
     * @return Javascript
     */
    public String getScriptingPart() throws DhtmlxException {

        StringBuffer buf = new StringBuffer();

        buf.append("<script>");

        // Init tab control
        buf.append(getControlName()).append("= new dhtmlXTabBar(").append('"').append(getName()).append('"').append(",").append('"').append(getTabPosition())
                .append('"').append(");");
        buf.append(getControlName()).append(".setImagePath(").append('"').append("imgs/").append('"').append(");");
        buf.append(getControlName()).append(".loadXML(").append('"').append("controller.do?control=").append(getControlName()).append("&command=init")
                .append('"').append(");");

        // Base scripts
        super.getScriptingPart();

        buf.append("</script>");

        return buf.toString();
    }

    public DhtmlxXMLBase toXMLStream() throws DhtmlxException {

        TabbarDocument document = TabbarDocument.Factory.newInstance();

        Tabbar tabbar = document.addNewTabbar();

        // Tabbar options
        tabbar.setAlign(Tabbar.Align.RIGHT);

        Row row = tabbar.addNewRow();

        for (TabX tabX : _tabs) {

            Tab tab = row.addNewTab();

            LOGGER.debug("create:" + tabX.toString());

            tabX.createTab(tab);
        }

        LOGGER.debug(document);

        if (!validateXml(document)) {

            throw new DhtmlxException("Document not valid");
        }

        return new DhtmlxTabXML(document.toString());
    }

    /**
     * @return Returns the _tabPosition.
     */
    public String getTabPosition() {
        return _tabPosition;
    }

    /**
     * @param _tabPosition
     *            The _tabPosition to set.
     */
    public void setTabPosition(String tabPosition) {
        this._tabPosition = tabPosition;
    }
}
