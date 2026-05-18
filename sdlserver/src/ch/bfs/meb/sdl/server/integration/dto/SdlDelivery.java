/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sdlserver

 */
package ch.bfs.meb.sdl.server.integration.dto;

import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlTransient;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Where;

import ch.bfs.meb.util.CodegroupUtility;
import ch.bfs.meb.util.MebUtils;

/**
 * Persistence Object for the delivery data table
 * 
 * @author $Author$
 * @version $Revision$
 */
@Entity
@Table(name = "SDL_DELIVERIES")
@GenericGenerator(name = "deliveryseqgen", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator", parameters = {
        @Parameter(name = "sequence_name", value = "SDLSEQ"), @Parameter(name = "increment_size", value = "100"),
        @Parameter(name = "optimizer", value = "pooled-lo") })
public class SdlDelivery {
    // Constants
    public final static Long DELIVERY_NOT_LOCKED = 0L;
    public final static Long DELIVERY_LOCKED = 1L;

    public final static String WAITING_FOR_DELIVERY_MESSAGE = "delivery.waitingfordelivery";

    // Fields
    private Long _deliveryId;
    private Long _canton;
    private Long _version;
    private String _deliveryCode;
    private String _configDeliveryCode;
    private Long _deliveryStatus;
    private Long _plausiStatus;
    private String _creation_user;
    private Date _creation_date;
    private String _modification_user;
    private Date _modification_date;
    private String _prevalidation_user;
    private Date _prevalidation_date;
    private String _validation_user;
    private Date _validation_date;
    private String _userText;
    private String _confirmRules;
    private Long _isLocked;

    private Set<SdlPlausiError> _plausierrors = new LinkedHashSet<SdlPlausiError>();

    // Transient
    private Long _schoolPlausiOk;
    private Long _schoolPlausiNotOk;
    private Long _classPlausiOk;
    private Long _classPlausiTotal;
    private Long _learnerPlausiOk;
    private Long _learnerPlausiTotal;
    private boolean _creatingReport;

    public SdlDelivery() {
        super();
    }

    public SdlDelivery(SdlDelivery delivery, List<SdlPlausiError> plausiErrors) {
        setDeliveryId(delivery.getDeliveryId());
        setCanton(delivery.getCanton());
        setVersion(delivery.getVersion());
        setDeliveryCode(delivery.getDeliveryCode());
        setConfigDeliveryCode(delivery.getConfigDeliveryCode());
        setDeliveryStatus(delivery.getDeliveryStatus());
        setPlausiStatus(delivery.getPlausiStatus());
        setCreation_user(delivery.getCreation_user());
        setCreation_date(delivery.getCreation_date());
        setModification_user(delivery.getModification_user());
        setModification_date(delivery.getModification_date());
        setPrevalidation_user(delivery.getPrevalidation_user());
        setPrevalidation_date(delivery.getPrevalidation_date());
        setValidation_user(delivery.getValidation_user());
        setValidation_date(delivery.getValidation_date());
        setUserText(delivery.getUserText());
        setConfirmRules(delivery.getConfirmRules());
        setIsLocked(delivery.getIsLocked());

        if (plausiErrors != null) {
            for (SdlPlausiError plausiError : plausiErrors) {
                _plausierrors.add(new SdlPlausiError(plausiError));
            }
        }
    }

    public void clearDeliveryNumbers() {
        _schoolPlausiOk = null;
        _schoolPlausiNotOk = null;
        _classPlausiOk = null;
        _classPlausiTotal = null;
        _learnerPlausiOk = null;
        _learnerPlausiTotal = null;
    }

    public void resetDeliveryNumbers() {
        _schoolPlausiOk = 0L;
        _schoolPlausiNotOk = 0L;
        _classPlausiOk = 0L;
        _classPlausiTotal = 0L;
        _learnerPlausiOk = 0L;
        _learnerPlausiTotal = 0L;
    }

    public void addPlausiSchools(Long plausiStatus, Long status, Long nrSchools) {
        if ((plausiStatus != null)
                && (plausiStatus.equals(CodegroupUtility.MEB_PLAUSISTATUS_VALID) || plausiStatus.equals(CodegroupUtility.MEB_PLAUSISTATUS_CONFIRMED))) {
            _schoolPlausiOk += nrSchools;
        } else {
            _schoolPlausiNotOk += nrSchools;
        }
    }

    public void addPlausiClasses(Long plausiStatus, Long nrClasses) {
        if ((plausiStatus != null)
                && (plausiStatus.equals(CodegroupUtility.MEB_PLAUSISTATUS_VALID) || plausiStatus.equals(CodegroupUtility.MEB_PLAUSISTATUS_CONFIRMED))) {
            _classPlausiOk += nrClasses;
        } else {
            _classPlausiTotal += nrClasses;
        }
    }

    public void addPlausiLearners(Long plausiStatus, Long nrLearners) {
        if ((plausiStatus != null)
                && (plausiStatus.equals(CodegroupUtility.MEB_PLAUSISTATUS_VALID) || plausiStatus.equals(CodegroupUtility.MEB_PLAUSISTATUS_CONFIRMED))) {
            _learnerPlausiOk += nrLearners;
        } else {
            _learnerPlausiTotal += nrLearners;
        }
    }

    // Property accessors
    @Id
    @Column(name = "DELIVERYID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "deliveryseqgen")
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
    public String getConfigDeliveryCode() {
        return _configDeliveryCode;
    }

    public void setConfigDeliveryCode(String configDeliveryCode) {
        _configDeliveryCode = configDeliveryCode;
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
    public String getPrevalidation_user() {
        return _prevalidation_user;
    }

    public void setPrevalidation_user(String prevalidationUser) {
        _prevalidation_user = prevalidationUser;
    }

    @Column
    public Date getPrevalidation_date() {
        return _prevalidation_date;
    }

    public void setPrevalidation_date(Date prevalidationDate) {
        _prevalidation_date = prevalidationDate;
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

    public Long getIsLocked() {
        return _isLocked;
    }

    public void setIsLocked(Long isLocked) {
        _isLocked = isLocked;
    }

    /**
     * @return the plausierrors
     */
    @XmlTransient
    @OneToMany(mappedBy = "deliveryId")
    @Where(clause = "schoolId is null and isToDelete=0")
    @OrderBy("isConfirmed, plausi, errorId")
    public Set<SdlPlausiError> getPlausierrors() {
        return _plausierrors;
    }

    /**
     * @param plausierrors the plausierrors to set
     */
    public void setPlausierrors(Set<SdlPlausiError> plausierrors) {
        _plausierrors = plausierrors;
    }

    /**
     * @return the string for the school numbers
     */
    @Transient
    public String getNrPlausiSchool() {
        if (_schoolPlausiOk == null || _schoolPlausiNotOk == null) {
            return MebUtils.getDeliveryToBeLoadedMessage();
        } else if (getDeliveryStatus() == CodegroupUtility.MEB_DELIVERYSTATUS_IMPORTED && (_schoolPlausiOk + _schoolPlausiNotOk == 0L)) {
            // sobald erste schule eingelesen --> wird verarbeitet, ansonsten delivery noch in queue
            return MebUtils.getDeliveryQueuedMessage();
        }
        return _schoolPlausiOk + "/" + new Long(_schoolPlausiOk + _schoolPlausiNotOk);
    }

    /**
     * @param dummy
     */
    public void setNrPlausiSchool(String dummy) {}

    /**
     * @return the string for the class numbers
     */
    @Transient
    public String getNrPlausiClass() {
        if (_classPlausiOk == null || _classPlausiTotal == null) {
            return MebUtils.getDeliveryToBeLoadedMessage();
        } else if (getDeliveryStatus() == CodegroupUtility.MEB_DELIVERYSTATUS_IMPORTED && (_schoolPlausiOk + _schoolPlausiNotOk == 0L)) {
            // sobald erste schule eingelesen --> wird verarbeitet, ansonsten delivery noch in queue
            return "";
        }
        return _classPlausiOk + "/" + _classPlausiTotal;
    }

    /**
     * @param dummy
     */
    public void setNrPlausiClass(String dummy) {}

    /**
     * @return the string for the learner numbers
     */
    @Transient
    public String getNrPlausiLearner() {
        if (_learnerPlausiOk == null || _learnerPlausiTotal == null) {
            return MebUtils.getDeliveryToBeLoadedMessage();
        } else if (getDeliveryStatus() == CodegroupUtility.MEB_DELIVERYSTATUS_IMPORTED && (_schoolPlausiOk + _schoolPlausiNotOk == 0L)) {
            // sobald erste schule eingelesen --> wird verarbeitet, ansonsten delivery noch in queue
            return "";
        }
        return _learnerPlausiOk + "/" + _learnerPlausiTotal;
    }

    /**
     * @param dummy
     */
    public void setNrPlausiLearner(String dummy) {}

    /**
     * @return the creatingReport
     */
    @Transient
    public boolean isCreatingReport() {
        return _creatingReport;
    }

    /**
     * @param creatingReport the creatingReport to set
     */
    public void setCreatingReport(boolean creatingReport) {
        _creatingReport = creatingReport;
    }
}
