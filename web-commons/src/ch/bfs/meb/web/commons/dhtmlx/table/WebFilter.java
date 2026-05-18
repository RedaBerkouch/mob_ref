/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: web-commons

 */
package ch.bfs.meb.web.commons.dhtmlx.table;

import java.util.ArrayList;
import java.util.List;

public class WebFilter {
    private Long _filterId;
    private String _nameDe;
    private String _nameFr;
    private String _nameIt;
    private String _descriptionDe;
    private String _descriptionFr;
    private String _descriptionIt;
    private Long _refObject;
    private String _source;
    private Long _authorisationLevel;
    private boolean _isActive;
    private boolean _isDefault;
    private Long _filterOrder;
    private List<WebParameter> _parameters = new ArrayList<WebParameter>();

    public Long getFilterId() {
        return _filterId;
    }

    public void setFilterId(Long filterId) {
        _filterId = filterId;
    }

    public String getDescriptionDe() {
        return _descriptionDe;
    }

    public void setDescriptionDe(String descriptionDe) {
        _descriptionDe = descriptionDe;
    }

    public String getDescriptionFr() {
        return _descriptionFr;
    }

    public void setDescriptionFr(String descriptionFr) {
        _descriptionFr = descriptionFr;
    }

    public String getDescriptionIt() {
        return _descriptionIt;
    }

    public void setDescriptionIt(String descriptionIt) {
        _descriptionIt = descriptionIt;
    }

    public String getNameDe() {
        return _nameDe;
    }

    public void setNameDe(String nameDe) {
        _nameDe = nameDe;
    }

    public String getNameFr() {
        return _nameFr;
    }

    public void setNameFr(String nameFr) {
        _nameFr = nameFr;
    }

    public String getNameIt() {
        return _nameIt;
    }

    public void setNameIt(String nameIt) {
        _nameIt = nameIt;
    }

    public Long getRefObject() {
        return _refObject;
    }

    public void setRefObject(Long refObject) {
        _refObject = refObject;
    }

    public String getSource() {
        return _source;
    }

    public void setSource(String source) {
        _source = source;
    }

    public Long getAuthorisationLevel() {
        return _authorisationLevel;
    }

    public void setAuthorisationLevel(Long authorisationLevel) {
        _authorisationLevel = authorisationLevel;
    }

    public List<WebParameter> getParameters() {
        return _parameters;
    }

    public void setParameters(List<WebParameter> parameters) {
        if (parameters == null) {
            _parameters.clear();
        } else {
            _parameters = parameters;
        }
    }

    public boolean getIsActive() {
        return _isActive;
    }

    public void setIsActive(boolean isActive) {
        _isActive = isActive;
    }

    public boolean getIsDefault() {
        return _isDefault;
    }

    public void setIsDefault(boolean isDefault) {
        _isDefault = isDefault;
    }

    public Long getFilterOrder() {
        return _filterOrder;
    }

    public void setFilterOrder(Long filterOrder) {
        _filterOrder = filterOrder;
    }
}
