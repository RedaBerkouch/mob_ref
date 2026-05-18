/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: server-commons

 */
package ch.bfs.meb.server.commons.integration.dto;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.*;

/**
 * Data Transfer Object for the export data table
 */
@MappedSuperclass
public class Export {
    // Fields
    private Long _exportId;
    private String _id;
    private Long _type;
    private String _name_de;
    private String _name_fr;
    private String _name_it;
    private String _description_de;
    private String _description_fr;
    private String _description_it;
    private String _source;
    private Long _authorisationLevel;
    private boolean _isActive;
    private Long _exportOrder;
    private List<Parameter> _parameters;

    /**
     * Default constructor
     */
    public Export() {

    }

    /**
     * Create export from persistent export (copy constructor)
     * @param persistExport
     */
    public Export(Export persistExport) {
        setExportId(persistExport.getExportId());
        setType(persistExport.getType());
        setName_de(persistExport.getName_de());
        setName_fr(persistExport.getName_fr());
        setName_it(persistExport.getName_it());
        setDescription_de(persistExport.getDescription_de());
        setDescription_fr(persistExport.getDescription_fr());
        setDescription_it(persistExport.getDescription_it());
        setSource(persistExport.getSource());
        setAuthorisationLevel(persistExport.getAuthorisationLevel());
        setIsActive(persistExport.getIsActive());
        setExportOrder(persistExport.getExportOrder());
        // clone params
        List<Parameter> params = new ArrayList<Parameter>();
        for (Parameter persistParam : persistExport.getParameters()) {
            Parameter param = new Parameter();
            param.setParameterId(persistParam.getParameterId());
            param.setExportId(persistParam.getExportId());
            param.setUniqueName(persistParam.getUniqueName());
            param.setName_de(persistParam.getName_de());
            param.setName_fr(persistParam.getName_fr());
            param.setName_it(persistParam.getName_it());
            param.setDefaultValue(persistParam.getDefaultValue());
            param.setParameterOrder(persistParam.getParameterOrder());
        }
        setParameters(params);
    }

    // Property accessors
    @Id
    @Column(name = "EXPORTID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "exportseqgen")
    public Long getExportId() {
        return _exportId;
    }

    public void setExportId(Long exportId) {
        _exportId = exportId;
    }

    @Column
    public String getId() {
        return _id;
    }

    public void setId(String id) {
        _id = id;
    }

    @Column
    public Long getType() {
        return _type;
    }

    public void setType(Long type) {
        _type = type;
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
    public Long getExportOrder() {
        return _exportOrder;
    }

    public void setExportOrder(Long exportOrder) {
        _exportOrder = exportOrder;
    }
}
