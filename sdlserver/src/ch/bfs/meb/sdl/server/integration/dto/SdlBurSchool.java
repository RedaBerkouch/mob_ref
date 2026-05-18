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

import ch.bfs.meb.server.commons.integration.dto.BurSchool;
import lombok.Data;

/**
 * Persistence Object for the config delivery data table
 */
@Data
@Entity
@Table(name = "SCHOOLS")
@GenericGenerator(name = "schoolseqgen", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator", parameters = {
        @Parameter(name = "sequence_name", value = "MEBSEQ"), @Parameter(name = "increment_size", value = "100"),
        @Parameter(name = "optimizer", value = "pooled-lo") })
public class SdlBurSchool extends BurSchool {
    @Column
    @ManyToMany(targetEntity = SdlConfigDelivery.class, fetch = FetchType.EAGER, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @JoinTable(name = "SDL_SCHOOLS_CONFIGDELIVERIES", joinColumns = @JoinColumn(name = "schoolId"), inverseJoinColumns = @JoinColumn(name = "deliveryId"))
    protected List<SdlConfigDelivery> configDeliveries;

    /**
     * Default constructor
     */
    public SdlBurSchool() {}

    /**
     * Copy from dto
     */
    public SdlBurSchool(BurSchool dtoBurSchool) {
        setSchoolId(dtoBurSchool.getSchoolId());
        setBurNr(dtoBurSchool.getBurNr());
        setCanton(dtoBurSchool.getCanton());
        setBur_canton(dtoBurSchool.getBur_canton());
        setLabel(dtoBurSchool.getLabel());
        setBur_label(dtoBurSchool.getBur_label());
        setMunicipality(dtoBurSchool.getMunicipality());
        setBur_municipality(dtoBurSchool.getBur_municipality());
        setActivityStatus(dtoBurSchool.getActivityStatus());
        setBur_activityStatus(dtoBurSchool.getBur_activityStatus());
        setSynchStatus_sdl(dtoBurSchool.getSynchStatus_sdl());
        setCantonalCode_sdl(dtoBurSchool.getCantonalCode_sdl());
        setBur_cantonalCode_sdl(dtoBurSchool.getBur_cantonalCode_sdl());
        set_sdl(dtoBurSchool.is_sdl());
        setBur_is_sdl(dtoBurSchool.isBur_is_sdl());
        setValidFrom_sdl_ssp(dtoBurSchool.getValidFrom_sdl_ssp());
        setBur_validFrom_sdl_ssp(dtoBurSchool.getBur_validFrom_sdl_ssp());
        setValidTo_sdl_ssp(dtoBurSchool.getValidTo_sdl_ssp());
        setBur_validTo_sdl_ssp(dtoBurSchool.getBur_validTo_sdl_ssp());
        setValidFrom_ssp(dtoBurSchool.getValidFrom_ssp());
        setBur_validFrom_ssp(dtoBurSchool.getBur_validFrom_ssp());
        setValidTo_ssp(dtoBurSchool.getValidTo_ssp());
        setBur_validTo_ssp(dtoBurSchool.getBur_validTo_ssp());
        setSynchStatus_ssp(dtoBurSchool.getSynchStatus_ssp());
        setCantonalCode_ssp(dtoBurSchool.getCantonalCode_ssp());
        setBur_cantonalCode_ssp(dtoBurSchool.getBur_cantonalCode_ssp());
        set_ssp(dtoBurSchool.is_ssp());
        setBur_is_ssp(dtoBurSchool.isBur_is_ssp());
        setSynchStatus_sba(dtoBurSchool.getSynchStatus_sba());
        setCantonalCode_sba(dtoBurSchool.getCantonalCode_sba());
        setBur_cantonalCode_sba(dtoBurSchool.getBur_cantonalCode_sba());
        set_sba(dtoBurSchool.is_sba());
        setBur_is_sba(dtoBurSchool.isBur_is_sba());
        setValidFrom_sba(dtoBurSchool.getValidFrom_sba());
        setBur_validFrom_sba(dtoBurSchool.getBur_validFrom_sba());
        setValidTo_sba(dtoBurSchool.getValidTo_sba());
        setBur_validTo_sba(dtoBurSchool.getBur_validTo_sba());
        setUserText(dtoBurSchool.getUserText());

        setDeliveryCode(dtoBurSchool.getDeliveryCode());
        setDeliveryId(dtoBurSchool.getDeliveryId());
        setVersion(dtoBurSchool.getVersion());

        setSynchStatusBur(dtoBurSchool.getSynchStatusBur());
        setNameBur(dtoBurSchool.getNameBur());
        setCantonBur(dtoBurSchool.getCantonBur());
        setMunicipalityBur(dtoBurSchool.getMunicipalityBur());
        setValidFromBur(dtoBurSchool.getValidFromBur());
        setValidToBur(dtoBurSchool.getValidToBur());

        setChar_publ_flg(dtoBurSchool.getChar_publ_flg());
        setChar_priv_sub_flg(dtoBurSchool.getChar_priv_sub_flg());
        setChar_priv_no_sub_flg(dtoBurSchool.getChar_priv_no_sub_flg());
        setIsSpecialSchool(dtoBurSchool.getIsSpecialSchool());
        setBur_char_publ_flg(dtoBurSchool.getBur_char_publ_flg());
        setBur_char_priv_sub_flg(dtoBurSchool.getBur_char_priv_sub_flg());
        setBur_char_priv_no_sub_flg(dtoBurSchool.getBur_char_priv_no_sub_flg());
        setIsSpecialSchoolBur(dtoBurSchool.getIsSpecialSchoolBur());
    }
}
