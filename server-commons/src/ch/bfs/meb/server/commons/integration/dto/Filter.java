/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: server-commons

  $Id: Filter.java 104 2009-11-05 14:26:27Z dzw $
 */
package ch.bfs.meb.server.commons.integration.dto;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.*;

/**
 * Data Transfer Object for the filter data table
 * 
 * @author $Author: dzw $
 * @version $Revision: 104 $
 */
@MappedSuperclass
public class Filter {
    // Fields
    private Long _filterId;
    private String _name_de;
    private String _name_fr;
    private String _name_it;
    private String _description_de;
    private String _description_fr;
    private String _description_it;
    private Long _refObject;
    private String _source;
    private Long _authorisationLevel;
    private boolean _isActive;
    private boolean _isDefault;
    private Long _filterOrder;
    private List<Parameter> _parameters;

    /**
     * Default constructor
     */
    public Filter() {

    }

    /**
     * Copy constructor
     * @param persistFilter
     */
    public Filter(Filter persistFilter) {
        setFilterId(persistFilter.getFilterId());
        setName_de(persistFilter.getName_de());
        setName_fr(persistFilter.getName_fr());
        setName_it(persistFilter.getName_it());
        setDescription_de(persistFilter.getDescription_de());
        setDescription_fr(persistFilter.getDescription_fr());
        setDescription_it(persistFilter.getDescription_it());
        setRefObject(persistFilter.getRefObject());
        setSource(persistFilter.getSource());
        setAuthorisationLevel(persistFilter.getAuthorisationLevel());
        setIsActive(persistFilter.getIsActive());
        setIsDefault(persistFilter.getIsDefault());
        setFilterOrder(persistFilter.getFilterOrder());
        // clone params
        List<Parameter> params = new ArrayList<Parameter>();
        for (Parameter persistParam : persistFilter.getParameters()) {
            Parameter param = new Parameter();
            param.setParameterId(persistParam.getParameterId());
            param.setFilterId(persistParam.getExportId());
            param.setUniqueName(persistParam.getUniqueName());
            param.setName_de(persistParam.getName_de());
            param.setName_fr(persistParam.getName_fr());
            param.setName_it(persistParam.getName_it());
            param.setDefaultValue(persistParam.getDefaultValue());
            param.setParameterOrder(persistParam.getParameterOrder());
            params.add(param);//FIXME Nothing will happen with the param missing params.add(param); MANTIS 2287
        }
        setParameters(params);
    }

    // Property accessors
    @Id
    @Column(name = "FILTERID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "filterseqgen")
    public Long getFilterId() {
        return _filterId;
    }

    public void setFilterId(Long filterId) {
        _filterId = filterId;
    }

    @Column
    public String getDescription_de() {
        return _description_de;
    }

    public void setDescription_de(String description_de) {
        _description_de = description_de;
    }

    @Column
    public String getDescription_fr() {
        return _description_fr;
    }

    public void setDescription_fr(String description_fr) {
        _description_fr = description_fr;
    }

    @Column
    public String getDescription_it() {
        return _description_it;
    }

    public void setDescription_it(String description_it) {
        _description_it = description_it;
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
    public Long getRefObject() {
        return _refObject;
    }

    public void setRefObject(Long refObject) {
        _refObject = refObject;
    }

    @Column
    public String getSource() {
        return _source;
    }

    public void setSource(String source) {
        _source = source;
    }

    @Column
    public Long getAuthorisationLevel() {
        return _authorisationLevel;
    }

    public void setAuthorisationLevel(Long authorisationLevel) {
        _authorisationLevel = authorisationLevel;
    }

    @Transient
    public List<Parameter> getParameters() {
        if (_parameters == null) {
            _parameters = new ArrayList<Parameter>();
        }
        return _parameters;
    }

    public void setParameters(List<Parameter> parameters) {
        _parameters = parameters;
    }

    @Column
    public boolean getIsActive() {
        return _isActive;
    }

    public void setIsActive(boolean isActive) {
        _isActive = isActive;
    }

    @Column
    public boolean getIsDefault() {
        return _isDefault;
    }

    public void setIsDefault(boolean isDefault) {
        _isDefault = isDefault;
    }

    @Column
    public Long getFilterOrder() {
        return _filterOrder;
    }

    public void setFilterOrder(Long filterOrder) {
        _filterOrder = filterOrder;
    }
}
