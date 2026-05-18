/* ----------------------------------------------------------------------------
 * 
 * SBG-Projekt
 * 
 * Copyright (c) 2006 GLANCE AG, Switzerland
 * 
 * $Id: Filter.java 48 2007-05-30 23:15:56Z lsc $
 *
 * ------------------------------------------------------------------------- */
package ch.bfs.meb.sbg.server.integration.dto;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.*;

import org.hibernate.annotations.GenericGenerator;

import ch.bfs.meb.server.commons.integration.dto.Filter;
import ch.bfs.meb.server.commons.integration.dto.Parameter;
import lombok.Data;

/**
 * Data Transfer Object for the filter data table
 *
 * @author $Author: sim
 */
@Data
@Entity
@Table(name = "FILTER")
@AttributeOverrides({ @AttributeOverride(name = "name_de", column = @Column(name = "NAME_D")),
        @AttributeOverride(name = "name_fr", column = @Column(name = "NAME_F")), @AttributeOverride(name = "name_it", column = @Column(name = "NAME_I")),
        @AttributeOverride(name = "description_de", column = @Column(name = "DESCRIPTION_D")),
        @AttributeOverride(name = "description_fr", column = @Column(name = "DESCRIPTION_F")),
        @AttributeOverride(name = "description_it", column = @Column(name = "DESCRIPTION_I")) })
@GenericGenerator(name = "filterseqgen", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator", parameters = {
        @org.hibernate.annotations.Parameter(name = "sequence_name", value = "SBGSEQ"),
        @org.hibernate.annotations.Parameter(name = "increment_size", value = "100"),
        @org.hibernate.annotations.Parameter(name = "optimizer", value = "pooled-lo") })
public class SbgFilter extends Filter {
    @Column
    private String modUser;
    @Column
    private Date modDate;

    private List<SbgParameter> sbgParameters = new ArrayList<SbgParameter>();

    /**
     * Default constructor
     */
    public SbgFilter() {}

    /**
     * Copy from dto
     */
    public SbgFilter(Filter dtoFilter) {
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

    @Transient
    @Override
    public List<Parameter> getParameters() {
        // set ParameterList from PersistSet
        super.setParameters(new ArrayList<Parameter>(getSbgParameters()));
        return super.getParameters();
    }

    public void setParameters(List<Parameter> parameters) {
        // override the PersistSet
        parameters.clear();
        for (Parameter transferParameter : parameters) {
            parameters.add((SbgParameter) transferParameter);
        }

        super.setParameters(parameters);
    }

    /**
     * For Hibernate only.
     *
     * @return SbgParameters
     */
    @OneToMany(cascade = { CascadeType.ALL }, fetch = FetchType.EAGER, orphanRemoval = true)
    @JoinColumn(name = "filterId")
    @OrderBy("parameterOrder, parameterId")
    public List<SbgParameter> getSbgParameters() {
        return this.sbgParameters;
    }

    /**
     * For Hibernate only.
     *
     * @param value
     */
    public void setSbgParameters(List<SbgParameter> value) {
        this.sbgParameters = value;
    }
}
