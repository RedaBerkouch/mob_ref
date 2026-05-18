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

/**
 * Data Transfer Object for the configdelivery data table
 */
@MappedSuperclass
public class ConfigDelivery {
    // Fields
    private Long _deliveryId;
    private Long _canton;
    private Long _version;
    private String _deliveryCode;
    private boolean _isDefault;
    private String _dl_users;
    private String _ro_users;
    private Date _referenceDate;
    private Date _dueDate;
    private String _creation_user;
    private Date _creation_date;
    private String _modification_user;
    private Date _modification_date;
    private String _userText;

    // Property accessors
    @Id
    @Column(name = "DELIVERYID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "configdeliveryseqgen")
    public Long getDeliveryId() {
        return _deliveryId;
    }

    public void setDeliveryId(Long deliveryId) {
        _deliveryId = deliveryId;
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
    public String getDeliveryCode() {
        return _deliveryCode;
    }

    public void setDeliveryCode(String deliveryCode) {
        _deliveryCode = deliveryCode;
    }

    @Column
    public boolean getIsDefault() {
        return _isDefault;
    }

    public void setIsDefault(boolean isDefault) {
        _isDefault = isDefault;
    }

    @Column
    public String getDl_users() {
        return _dl_users;
    }

    public void setDl_users(String dlUsers) {
        _dl_users = dlUsers;
    }

    @Column
    public String getRo_users() {
        return _ro_users;
    }

    public void setRo_users(String roUsers) {
        _ro_users = roUsers;
    }

    @Column
    public Date getReferenceDate() {
        return _referenceDate;
    }

    public void setReferenceDate(Date referenceDate) {
        _referenceDate = referenceDate;
    }

    @Column
    public Date getDueDate() {
        return _dueDate;
    }

    public void setDueDate(Date dueDate) {
        _dueDate = dueDate;
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
    public String getUserText() {
        return _userText;
    }

    public void setUserText(String userText) {
        _userText = userText;
    }
}
