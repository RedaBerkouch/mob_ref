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
 * Data Transfer Object for plausierrors
 * 
 * @author $Author$
 * @version $Revision$
 */
@MappedSuperclass
public class PlausiError {
    // Fields
    private Long _errorId;
    private Long _cantonId;
    private Long _deliveryId;
    private String _errorMsg_de;
    private String _errorMsg_fr;
    private String _errorMsg_it;
    private String _reportData;
    private boolean _isConfirmed;
    private String _confirmId;
    private boolean _isToDelete;
    private String _modification_user;
    private Date _modification_date;

    // derived fields
    private boolean _isConfirmable;
    private String _plausiName_de;
    private String _plausiName_fr;
    private String _plausiName_it;

    // Property accessors
    @Id
    @Column(name = "ERRORID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "plausierrorseqgen")
    /*
      @return the _errorId
     */
    public Long getErrorId() {
        return _errorId;
    }

    /**
     * @param errorId the _errorId to set
     */
    public void setErrorId(Long errorId) {
        _errorId = errorId;
    }

    @Column
    /*
      @return the _cantonId
     */
    public Long getCantonId() {
        return _cantonId;
    }

    /**
     * @param cantonId the _cantonId to set
     */
    public void setCantonId(Long cantonId) {
        _cantonId = cantonId;
    }

    /**
     * @return the _deliveryId
     */
    public Long getDeliveryId() {
        return _deliveryId;
    }

    /**
     * @param deliveryId the _deliveryId to set
     */
    public void setDeliveryId(Long deliveryId) {
        _deliveryId = deliveryId;
    }

    /**
     * @return the _errorMsg_de
     */
    public String getErrorMsg_de() {
        return _errorMsg_de;
    }

    /**
     * @param errorMsgDe the _errorMsg_de to set
     */
    public void setErrorMsg_de(String errorMsgDe) {
        _errorMsg_de = errorMsgDe;
    }

    /**
     * @return the _errorMsg_fr
     */
    public String getErrorMsg_fr() {
        return _errorMsg_fr;
    }

    /**
     * @param errorMsgFr the _errorMsg_fr to set
     */
    public void setErrorMsg_fr(String errorMsgFr) {
        _errorMsg_fr = errorMsgFr;
    }

    /**
     * @return the _errorMsg_it
     */
    public String getErrorMsg_it() {
        return _errorMsg_it;
    }

    /**
     * @param errorMsgIt the _errorMsg_it to set
     */
    public void setErrorMsg_it(String errorMsgIt) {
        _errorMsg_it = errorMsgIt;
    }

    /**
     * @return the _reportData
     */
    public String getReportData() {
        return _reportData;
    }

    /**
     * @param reportData the _reportData to set
     */
    public void setReportData(String reportData) {
        _reportData = reportData;
    }

    /**
     * @return the _isConfirmed
     */
    public boolean getIsConfirmed() {
        return _isConfirmed;
    }

    /**
     * @param isConfirmed the _isConfirmed to set
     */
    public void setIsConfirmed(boolean isConfirmed) {
        _isConfirmed = isConfirmed;
    }

    /**
     * @return the _confirmId
     */
    public String getConfirmId() {
        return _confirmId;
    }

    /**
     * @param confirmId the _confirmId to set
     */
    public void setConfirmId(String confirmId) {
        _confirmId = confirmId;
    }

    /**
     * @return the _isToDelete
     */
    public boolean getIsToDelete() {
        return _isToDelete;
    }

    /**
     * @param isToDelete the _isToDelete to set
     */
    public void setIsToDelete(boolean isToDelete) {
        _isToDelete = isToDelete;
    }

    /**
     * @return the _modification_user
     */
    public String getModification_user() {
        return _modification_user;
    }

    /**
     * @param modificationUser the _modification_user to set
     */
    public void setModification_user(String modificationUser) {
        _modification_user = modificationUser;
    }

    /**
     * @return the _modification_date
     */
    public Date getModification_date() {
        return _modification_date;
    }

    /**
     * @param modificationDate the _modification_date to set
     */
    public void setModification_date(Date modificationDate) {
        _modification_date = modificationDate;
    }

    /**
     * @return the isConfirmable
     */
    @Transient
    public boolean isConfirmable() {
        return _isConfirmable;
    }

    /**
     * @param isConfirmable the isConfirmable to set
     */
    public void setConfirmable(boolean isConfirmable) {
        _isConfirmable = isConfirmable;
    }

    /**
     * @return the plausiName_de
     */
    @Transient
    public String getPlausiName_de() {
        return _plausiName_de;
    }

    /**
     * @param plausiNameDe the plausiName_de to set
     */
    public void setPlausiName_de(String plausiNameDe) {
        _plausiName_de = plausiNameDe;
    }

    /**
     * @return the plausiName_fr
     */
    @Transient
    public String getPlausiName_fr() {
        return _plausiName_fr;
    }

    /**
     * @param plausiNameFr the plausiName_fr to set
     */
    public void setPlausiName_fr(String plausiNameFr) {
        _plausiName_fr = plausiNameFr;
    }

    /**
     * @return the plausiName_it
     */
    @Transient
    public String getPlausiName_it() {
        return _plausiName_it;
    }

    /**
     * @param plausiNameIt the plausiName_it to set
     */
    public void setPlausiName_it(String plausiNameIt) {
        _plausiName_it = plausiNameIt;
    }
}
