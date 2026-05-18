/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sbaserver

 */
package ch.bfs.meb.sba.server.integration.dto;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.*;

import org.hibernate.annotations.GenericGenerator;

import ch.bfs.meb.server.commons.integration.dto.Export;
import ch.bfs.meb.server.commons.integration.dto.Parameter;

/**
 * Persistence Object for the export data table
 */
@Entity
@Table(name = "SBA_EXPORTS")
@GenericGenerator(name = "exportseqgen", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator", parameters = {
        @org.hibernate.annotations.Parameter(name = "sequence_name", value = "SBASEQ"),
        @org.hibernate.annotations.Parameter(name = "increment_size", value = "100"),
        @org.hibernate.annotations.Parameter(name = "optimizer", value = "pooled-lo") })
public class SbaExport extends Export {
    private List<SbaParameter> _parameters = new ArrayList<SbaParameter>();

    /** Default constructor */
    public SbaExport() {}

    /** Copy from dto */
    public SbaExport(Export dtoExport) {
        setExportId(dtoExport.getExportId());
        setId(dtoExport.getId());
        setType(dtoExport.getType());
        setName_de(dtoExport.getName_de());
        setName_fr(dtoExport.getName_fr());
        setName_it(dtoExport.getName_it());
        setDescription_de(dtoExport.getDescription_de());
        setDescription_fr(dtoExport.getDescription_fr());
        setDescription_it(dtoExport.getDescription_it());
        setSource(dtoExport.getSource());
        setAuthorisationLevel(dtoExport.getAuthorisationLevel());
        setIsActive(dtoExport.getIsActive());
        setExportOrder(dtoExport.getExportOrder());
    }

    @OneToMany(cascade = { CascadeType.ALL }, fetch = FetchType.EAGER, orphanRemoval = true)
    @JoinColumn(name = "exportId")
    public List<SbaParameter> getSbaParameters() {
        return _parameters;
    }

    public void setSbaParameters(List<SbaParameter> value) {
        _parameters = value;
    }

    @Transient
    @Override
    public List<Parameter> getParameters() {
        // set ParameterList from PersistSet
        super.setParameters(new ArrayList<Parameter>(_parameters));
        return super.getParameters();
    }

    public void setParameters(List<Parameter> parameters) {
        // override the PersistSet
        _parameters.clear();
        for (Parameter transferParameter : parameters) {
            _parameters.add((SbaParameter) transferParameter);
        }

        super.setParameters(parameters);
    }
}
