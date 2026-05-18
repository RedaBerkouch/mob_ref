/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: web-commons

 */
package ch.bfs.meb.web.commons.dhtmlx.table;

public class WebParameter {
    private Long _parameterId;
    private Long _filterId;
    private Long _plausiId;
    private Long _exportId;
    private String _uniqueName;
    private String _nameDe;
    private String _nameFr;
    private String _nameIt;
    private String _defaultValue;
    private Long _parameterOrder;

    public Long getParameterId() {
        return _parameterId;
    }

    public void setParameterId(Long parameterId) {
        _parameterId = parameterId;
    }

    public Long getFilterId() {
        return _filterId;
    }

    public void setFilterId(Long filterId) {
        _filterId = filterId;
    }

    public Long getPlausiId() {
        return _plausiId;
    }

    public void setPlausiId(Long plausiId) {
        _plausiId = plausiId;
    }

    public Long getExportId() {
        return _exportId;
    }

    public void setExportId(Long exportId) {
        _exportId = exportId;
    }

    public String getUniqueName() {
        return _uniqueName;
    }

    public void setUniqueName(String uniqueName) {
        _uniqueName = uniqueName;
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

    public String getDefaultValue() {
        return _defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        _defaultValue = defaultValue;
    }

    public Long getParameterOrder() {
        return _parameterOrder;
    }

    public void setParameterOrder(Long parameterOrder) {
        _parameterOrder = parameterOrder;
    }
}
