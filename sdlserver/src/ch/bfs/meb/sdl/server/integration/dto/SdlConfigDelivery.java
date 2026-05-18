/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sdlserver

 */
package ch.bfs.meb.sdl.server.integration.dto;

import java.util.List;

import javax.persistence.*;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import ch.bfs.meb.server.commons.integration.dto.ConfigDelivery;

/**
 * Persistence Object for the config delivery data table
 */
@Entity
@Table(name = "SDL_CONFIGDELIVERIES")
@GenericGenerator(name = "configdeliveryseqgen", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator", parameters = {
        @Parameter(name = "sequence_name", value = "SDLSEQ"), @Parameter(name = "increment_size", value = "100"),
        @Parameter(name = "optimizer", value = "pooled-lo") })
public class SdlConfigDelivery extends ConfigDelivery {
    protected List<SdlBurSchool> _burSchools;

    /** Default constructor */
    public SdlConfigDelivery() {}

    /** Copy from dto */
    public SdlConfigDelivery(ConfigDelivery dtoConfigDelivery) {
        setDeliveryId(dtoConfigDelivery.getDeliveryId());
        setCanton(dtoConfigDelivery.getCanton());
        setVersion(dtoConfigDelivery.getVersion());
        setDeliveryCode(dtoConfigDelivery.getDeliveryCode());
        setIsDefault(dtoConfigDelivery.getIsDefault());
        setDl_users(dtoConfigDelivery.getDl_users());
        setRo_users(dtoConfigDelivery.getRo_users());
        setReferenceDate(dtoConfigDelivery.getReferenceDate());
        setDueDate(dtoConfigDelivery.getDueDate());
        setCreation_user(dtoConfigDelivery.getCreation_user());
        setCreation_date(dtoConfigDelivery.getCreation_date());
        setModification_user(dtoConfigDelivery.getModification_user());
        setModification_date(dtoConfigDelivery.getModification_date());
        setUserText(dtoConfigDelivery.getUserText());
    }

    @Column
    @ManyToMany(targetEntity = SdlBurSchool.class, fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @JoinTable(name = "SDL_SCHOOLS_CONFIGDELIVERIES", joinColumns = @JoinColumn(name = "deliveryId"), inverseJoinColumns = @JoinColumn(name = "schoolId"))
    public List<SdlBurSchool> getBurSchools() {
        return _burSchools;
    }

    public void setBurSchools(List<SdlBurSchool> burSchools) {
        _burSchools = burSchools;
    }
}
