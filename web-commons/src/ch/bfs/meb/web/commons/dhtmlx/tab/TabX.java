/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: web-commons

  $Id$

 */
package ch.bfs.meb.web.commons.dhtmlx.tab;

import org.apache.xmlbeans.XmlCursor;

import ch.bfs.meb.web.commons.dhtmlx.tab.xmlbeans.TabbarDocument.Tabbar.Row.Tab;
import ch.bfs.meb.web.commons.i18n.IWebLocalizationManager;

/**
 * TODO Describe this class
 * 
 * @author $Author$
 * @version $Revision$
 */
public class TabX {

    protected String _href;

    protected boolean _selected;

    protected String _id;

    protected String _width = "100px";

    protected String _label;

    protected String _node;

    protected String _content;

    protected final IWebLocalizationManager _localizationManager;

    public TabX(String id, String labelm, IWebLocalizationManager manager) {
        this(id, labelm, manager, null);
    }

    public TabX(String id, String label, IWebLocalizationManager manager, String node) {
        _id = id;
        _label = label;
        _node = node;
        _localizationManager = manager;
    }

    public void createTab(Tab tab) {
        tab.setId(getId());
        tab.setWidth(getWidth());

        if (_href != null) {
            tab.setHref(getHref());
        }

        if (isSelected()) {
            tab.setSelected(1);
        }

        if (getNode() != null) {
            tab.setNode(getNode());
        }

        if (getContent() != null) {
            XmlCursor contentCursor = tab.addNewContent().newCursor();
            contentCursor.setTextValue(getContent());
        }

        XmlCursor cursor = tab.newCursor();

        String labelValue = getLabel();
        if (_localizationManager != null) {
            labelValue = _localizationManager.getMessage(getLabel());

            if (labelValue == null) {
                labelValue = getLabel();
            }
        }
        cursor.setTextValue(labelValue);
    }

    /**
     * @return Returns the href.
     */
    public String getHref() {
        return _href;
    }

    /**
     * @param href
     *            The href to set.
     */
    public void setHref(String href) {
        this._href = href;
    }

    /**
     * @return Returns the id.
     */
    public String getId() {
        return _id;
    }

    /**
     * @param id
     *            The id to set.
     */
    public void setId(String id) {
        this._id = id;
    }

    /**
     * @return Returns the selected.
     */
    public boolean isSelected() {
        return _selected;
    }

    /**
     * @param selected
     *            The selected to set.
     */
    public void setSelected(boolean selected) {
        this._selected = selected;
    }

    /**
     * @return Returns the width.
     */
    public String getWidth() {
        return _width;
    }

    /**
     * @param width
     *            The width to set.
     */
    public void setWidth(String width) {
        this._width = width;
    }

    public boolean equals(Object obj) {

        if (obj instanceof TabX) {
            TabX tabX = (TabX) obj;

            return tabX.getId().equals(getId());
        }

        return super.equals(obj);
    }

    public String toString() {

        return "Tab {" + getId() + "," + getLabel() + "," + getHref() + "," + getWidth() + "," + isSelected() + "}";
    }

    /**
     * @return Returns the label.
     */
    public String getLabel() {
        return _label;
    }

    /**
     * @param label
     *            The label to set.
     */
    public void setLabel(String label) {
        this._label = label;
    }

    /**
     * @return Returns the node.
     */
    public String getNode() {
        return _node;
    }

    /**
     * @param node
     *            The node to set.
     */
    public void setNode(String node) {
        this._node = node;
    }

    /**
     * @return Returns the content.
     */
    public String getContent() {
        return _content;
    }

    /**
     * @param content
     *            The content to set.
     */
    public void setContent(String content) {
        this._content = content;
    }

}
