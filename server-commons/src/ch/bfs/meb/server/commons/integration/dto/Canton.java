/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: server-commons

 */
package ch.bfs.meb.server.commons.integration.dto;

import java.util.Date;

import javax.persistence.*;

import ch.bfs.meb.util.CodegroupUtility;

/**
 * Data Transfer Object for the canton data table
 */
@MappedSuperclass
public class Canton {
    // Fields
    private Long _cantonId;
    private Long _canton;
    private Long _version;
    private Long _deliveryStatus;
    private Long _plausiStatus;
    private String _plausi_user;
    private Date _plausi_date;
    private String _creation_user;
    private Date _creation_date;
    private String _modification_user;
    private Date _modification_date;
    private String _validation_user;
    private Date _validation_date;
    private String _finalisation_user;
    private Date _finalisation_date;
    private String _userText;
    private String _confirmRules;

    public Canton() {
        setDeliveryStatus(CodegroupUtility.MEB_CANTONSTATUS_INITIALIZED);
        setPlausiStatus(CodegroupUtility.MEB_PLAUSISTATUS_UNDEFINED);
        setUserText("");
    }

    // Property accessors
    @Id
    @Column(name = "CANTONID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "cantonseqgen")
    public Long getCantonId() {
        return _cantonId;
    }

    public void setCantonId(Long cantonId) {
        _cantonId = cantonId;
    }

    @Column
    public Long getCanton() {
        return _canton;
    }

    public void setCanton(Long canton) {
        _canton = canton;
    }

    @Column
    public Long getVersion() {
        return _version;
    }

    public void setVersion(Long version) {
        _version = version;
    }

    @Column
    public Long getDeliveryStatus() {
        return _deliveryStatus;
    }

    public void setDeliveryStatus(Long deliveryStatus) {
        _deliveryStatus = deliveryStatus;
    }

    @Column
    public Long getPlausiStatus() {
        return _plausiStatus;
    }

    public void setPlausiStatus(Long plausiStatus) {
        _plausiStatus = plausiStatus;
    }

    @Column
    public String getPlausi_user() {
        return _plausi_user;
    }

    public void setPlausi_user(String plausi_user) {
        _plausi_user = plausi_user;
    }

    @Column
    public Date getPlausi_date() {
        return _plausi_date;
    }

    public void setPlausi_date(Date plausi_date) {
        _plausi_date = plausi_date;
    }

    @Column
    public String getCreation_user() {
        return _creation_user;
    }

    public void setCreation_user(String creationUser) {
        _creation_user = creationUser;
    }

    @Column
    public Date getCreation_date() {
        return _creation_date;
    }

    public void setCreation_date(Date creationDate) {
        _creation_date = creationDate;
    }

    @Column
    public String getModification_user() {
        return _modification_user;
    }

    public void setModification_user(String modificationUser) {
        _modification_user = modificationUser;
    }

    @Column
    public Date getModification_date() {
        return _modification_date;
    }

    public void setModification_date(Date modificationDate) {
        _modification_date = modificationDate;
    }

    @Column
    public String getValidation_user() {
        return _validation_user;
    }

    public void setValidation_user(String validationUser) {
        _validation_user = validationUser;
    }

    @Column
    public Date getValidation_date() {
        return _validation_date;
    }

    public void setValidation_date(Date validationDate) {
        _validation_date = validationDate;
    }

    @Column
    public String getFinalisation_user() {
        return _finalisation_user;
    }

    public void setFinalisation_user(String finalisationUser) {
        _finalisation_user = finalisationUser;
    }

    @Column
    public Date getFinalisation_date() {
        return _finalisation_date;
    }

    public void setFinalisation_date(Date finalisationDate) {
        _finalisation_date = finalisationDate;
    }

    @Column
    public String getUserText() {
        return _userText;
    }

    public void setUserText(String userText) {
        _userText = userText;
    }

    @Column
    public String getConfirmRules() {
        return _confirmRules;
    }

    public void setConfirmRules(String confirmRules) {
        _confirmRules = confirmRules;
    }
}
