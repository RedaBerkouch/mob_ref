/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: server-commons

 */
package ch.bfs.meb.server.commons.integration.dto;

import javax.persistence.*;

@MappedSuperclass
public class Parameter {
    // Fields

    private Long _parameterId;
    private Long _filterId;
    private Long _plausiId;
    private Long _exportId;
    private String _uniqueName;
    private String _name_de;
    private String _name_fr;
    private String _name_it;
    private String _defaultValue;
    private Long _parameterOrder;

    // Property accessors
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "parameterseqgen")
    @Column(name = "PARAMETERID")
    public Long getParameterId() {
        return _parameterId;
    }

    public void setParameterId(Long parameterId) {
        _parameterId = parameterId;
    }

    @Column
    public Long getFilterId() {
        return _filterId;
    }

    public void setFilterId(Long filterId) {
        _filterId = filterId;
    }

    @Column
    public Long getPlausiId() {
        return _plausiId;
    }

    public void setPlausiId(Long plausiId) {
        _plausiId = plausiId;
    }

    @Column
    public Long getExportId() {
        return _exportId;
    }

    public void setExportId(Long exportId) {
        _exportId = exportId;
    }

    @Column
    public String getUniqueName() {
        return _uniqueName;
    }

    public void setUniqueName(String uniqueName) {
        _uniqueName = uniqueName;
    }

    @Column
    public String getName_de() {
        return _name_de;
    }

    public void setName_de(String name_de) {
        _name_de = name_de;
    }

    @Column
    public String getName_fr() {
        return _name_fr;
    }

    public void setName_fr(String name_fr) {
        _name_fr = name_fr;
    }

    @Column
    public String getName_it() {
        return _name_it;
    }

    public void setName_it(String name_it) {
        _name_it = name_it;
    }

    @Column
    public String getDefaultValue() {
        return _defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        _defaultValue = defaultValue;
    }

    @Column
    public Long getParameterOrder() {
        return _parameterOrder;
    }

    public void setParameterOrder(Long parameterOrder) {
        _parameterOrder = parameterOrder;
    }
}
