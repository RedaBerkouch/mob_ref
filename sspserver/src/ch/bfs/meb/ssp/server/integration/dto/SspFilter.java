/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sspserver

 */
package ch.bfs.meb.ssp.server.integration.dto;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.*;

import org.hibernate.annotations.GenericGenerator;

import ch.bfs.meb.server.commons.integration.dto.Filter;
import ch.bfs.meb.server.commons.integration.dto.Parameter;

/**
 * Persistence Object for the filter data table
 */
@Entity
@Table(name = "SSP_FILTERS")
@GenericGenerator(name = "filterseqgen", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator", parameters = {
        @org.hibernate.annotations.Parameter(name = "sequence_name", value = "SSPSEQ"),
        @org.hibernate.annotations.Parameter(name = "increment_size", value = "100"),
        @org.hibernate.annotations.Parameter(name = "optimizer", value = "pooled-lo") })
public class SspFilter extends Filter {
    private List<SspParameter> _parameters = new ArrayList<SspParameter>();

    /** Default constructor */
    public SspFilter() {}

    /** Copy from dto */
    public SspFilter(Filter dtoFilter) {
        setFilterId(dtoFilter.getFilterId());
        setName_de(dtoFilter.getName_de());
        setName_fr(dtoFilter.getName_fr());
        setName_it(dtoFilter.getName_it());
        setDescription_de(dtoFilter.getDescription_de());
        setDescription_fr(dtoFilter.getDescription_fr());
        setDescription_it(dtoFilter.getDescription_it());
        setRefObject(dtoFilter.getRefObject());
        setSource(dtoFilter.getSource());
        setAuthorisationLevel(dtoFilter.getAuthorisationLevel());
        setIsActive(dtoFilter.getIsActive());
        setIsDefault(dtoFilter.getIsDefault());
        setFilterOrder(dtoFilter.getFilterOrder());
    }

    @OneToMany(cascade = { CascadeType.ALL }, fetch = FetchType.EAGER, orphanRemoval = true)
    @JoinColumn(name = "filterId")
    public List<SspParameter> getSspParameters() {
        return _parameters;
    }

    public void setSspParameters(List<SspParameter> value) {
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
            _parameters.add((SspParameter) transferParameter);
        }

        super.setParameters(parameters);
    }
}
