/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sbaserver

 */
package ch.bfs.meb.sba.server.integration.dto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.persistence.*;

import org.hibernate.annotations.GenericGenerator;

import ch.bfs.meb.server.commons.integration.dto.Parameter;
import ch.bfs.meb.server.commons.integration.dto.Plausi;

/**
 * Persistence Object for the plausi data table
 */
@Entity
@Table(name = "SBA_PLAUSIS")
@GenericGenerator(name = "plausiseqgen", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator", parameters = {
        @org.hibernate.annotations.Parameter(name = "sequence_name", value = "SBASEQ"),
        @org.hibernate.annotations.Parameter(name = "increment_size", value = "100"),
        @org.hibernate.annotations.Parameter(name = "optimizer", value = "pooled-lo") })
public class SbaPlausi extends Plausi {
    private List<SbaParameter> _parameters = new ArrayList<SbaParameter>();

    /**
     * Default constructor
     */
    public SbaPlausi() {}

    /**
     * Copy from dto
     */
    public SbaPlausi(Plausi dtoPlausi) {
        setPlausiId(dtoPlausi.getPlausiId());
        setId(dtoPlausi.getId());
        setType(dtoPlausi.getType());
        setName_de(dtoPlausi.getName_de());
        setName_fr(dtoPlausi.getName_fr());
        setName_it(dtoPlausi.getName_it());
        setDescription_de(dtoPlausi.getDescription_de());
        setDescription_fr(dtoPlausi.getDescription_fr());
        setDescription_it(dtoPlausi.getDescription_it());
        setSource(dtoPlausi.getSource());
        setObjectLevel(dtoPlausi.getObjectLevel());
        setIsActive(dtoPlausi.getIsActive());
        setIsConfirmable(dtoPlausi.getIsConfirmable());
        setValidFrom(dtoPlausi.getValidFrom());
        setValidTo(dtoPlausi.getValidTo());
        setPlausiOrder(dtoPlausi.getPlausiOrder());
    }

    @OneToMany(cascade = { CascadeType.ALL }, fetch = FetchType.EAGER, orphanRemoval = true)
    @JoinColumn(name = "plausiId")
    public List<SbaParameter> getSbaParameters() {
        return _parameters;
    }

    public void setSbaParameters(List<SbaParameter> value) {
        _parameters = value;
    }

    @Transient
    @Override
    public List<Parameter> getParameters() {
        return Collections.unmodifiableList(new ArrayList<Parameter>(_parameters));
    }
}
