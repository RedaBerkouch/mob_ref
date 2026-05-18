/*
 * MEB Portal
 * Bundesamt für Statistik
 *
 * adesso Schweiz AG
 * Copyright (c) 2009, 2010
 *
 * Projekt: sspserver
 *
 */
package ch.bfs.meb.ssp.server.integration.dto;

import java.util.*;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlTransient;

import lombok.AllArgsConstructor;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.springframework.security.core.context.SecurityContextHolder;

import ch.bfs.meb.security.MebUser;
import ch.bfs.meb.server.commons.integration.dto.PlausiError;

/**
 * Persistence Object for the plausi error table
 *
 * @author $Author: lsc $
 * @version $Revision: 547 $
 */
@Entity
@Table(name = "SSP_PLAUSIERRORS")
@GenericGenerator(name = "plausierrorseqgen", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator", parameters = {
        @Parameter(name = "sequence_name", value = "SSPSEQ"), @Parameter(name = "increment_size", value = "100"),
        @Parameter(name = "optimizer", value = "pooled-lo") })
public class SspPlausiError extends PlausiError {

    @AllArgsConstructor
    public static class SchoolInfo {
        Long personId;
        String schoolIdType;
        String schoolId;
        String label;
    }

    // Fields
    private Long _personId;
    private Long _activityId;
    @XmlTransient
    private SspPlausi _plausi;

    // Transient
    private String _person_idType;
    private String _person_id;
    private String _person_origDeliveryData;
    private String _school_idType;
    private String _school_id;
    private String _school_label;
    private String _activity_id;
    private List<SchoolInfo> _school_Info;

    public SspPlausiError() {}

    public SspPlausiError(PlausiError plausierror) {
        setErrorId(plausierror.getErrorId());
        setCantonId(plausierror.getCantonId());
        setDeliveryId(plausierror.getDeliveryId());
        setErrorMsg_de(plausierror.getErrorMsg_de());
        setErrorMsg_fr(plausierror.getErrorMsg_fr());
        setErrorMsg_it(plausierror.getErrorMsg_it());
        setReportData(plausierror.getReportData());
        setIsConfirmed(plausierror.getIsConfirmed());
        setConfirmId(plausierror.getConfirmId());
        setIsToDelete(plausierror.getIsToDelete());
        setModification_user(plausierror.getModification_user());
        setModification_date(plausierror.getModification_date());
        setConfirmable(plausierror.isConfirmable());
        setPlausiName_de(plausierror.getPlausiName_de());
        setPlausiName_fr(plausierror.getPlausiName_fr());
        setPlausiName_it(plausierror.getPlausiName_it());
    }

    public SspPlausiError(SspPlausiError plausierror) {
        this((PlausiError) plausierror);

        // Fields SspPlausiError
        setPersonId(plausierror.getPersonId());
        setActivityId(plausierror.getActivityId());
        setPlausi(plausierror.getPlausi());
    }

    public static List<SspPlausiError> updatePlausiErrorsData(Set<SspPlausiError> psistPlausiErrors, List<PlausiError> dataPlausiErrors) {
        List<SspPlausiError> newPlausiErrors = new ArrayList<>();
        HashMap<Long, PlausiError> dataPlausiErrorsMap = new HashMap<>();
        if (dataPlausiErrors != null) {
            for (PlausiError plausiError : dataPlausiErrors) {
                dataPlausiErrorsMap.put(plausiError.getErrorId(), plausiError);
            }
        }

        String userEmail = ((MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getEmail();
        for (SspPlausiError psistPlausiError : psistPlausiErrors) {
            if (dataPlausiErrors != null) {
                PlausiError plausiError = dataPlausiErrorsMap.get(psistPlausiError.getErrorId());
                if (plausiError != null) {
                    SspPlausiError newPlausiError = new SspPlausiError(plausiError);
                    newPlausiError.setPersonId(psistPlausiError.getPersonId());
                    newPlausiError.setActivityId(psistPlausiError.getActivityId());
                    newPlausiError.setPlausi(psistPlausiError.getPlausi());
                    // confirmed state changed, update modification data
                    if (plausiError.getIsConfirmed() != psistPlausiError.getIsConfirmed()) {
                        newPlausiError.setModification_user(userEmail);
                        newPlausiError.setModification_date(new Date());
                    }
                    newPlausiErrors.add(newPlausiError);
                } else {
                    newPlausiErrors.add(new SspPlausiError(psistPlausiError));
                }
            } else {
                newPlausiErrors.add(new SspPlausiError(psistPlausiError));
            }
        }

        return newPlausiErrors;
    }

    public SspPlausiError(SspPlausi plausi) {
        _plausi = plausi;
    }

    public void addMultipleSchoolInfo(List<SchoolInfo> schoolInfo) {
        _school_Info = schoolInfo;
    }

    public void addActivityInfoWithLabel(String schoolIdType, String schoolId, String schoolLabel, String activityId) {
        _school_idType = schoolIdType;
        _school_id = schoolId;
        _school_label = schoolLabel;
        _activity_id = activityId;
    }

    public void addActivityInfo(String schoolIdType, String schoolId, String activityId) {
        _school_idType = schoolIdType;
        _school_id = schoolId;
        _activity_id = activityId;
    }

    public void addPersonInfo(String idType, String id, String origDeliveryData) {
        _person_idType = idType;
        _person_id = id;
        _person_origDeliveryData = origDeliveryData;
    }

    /**
     * @return the _personId
     */
    public Long getPersonId() {
        return _personId;
    }

    /**
     * @param personId the _personId to set
     */
    public void setPersonId(Long personId) {
        _personId = personId;
    }

    /**
     * @return the _activityId
     */
    public Long getActivityId() {
        return _activityId;
    }

    /**
     * @param activityId the _activityId to set
     */
    public void setActivityId(Long activityId) {
        _activityId = activityId;
    }

    /**
     * @return the _plausi
     */
    @ManyToOne
    @JoinColumn(name = "plausiId")
    @XmlTransient
    public SspPlausi getPlausi() {
        return _plausi;
    }

    /**
     * Warning!! Has to be private, because if it's public, the plausi data will be transferred
     * between server and client!!
     *
     * @param plausi the _plausi to set
     */
    private void setPlausi(SspPlausi plausi) {
        _plausi = plausi;
    }

    public void loadPlausiData() {
        setConfirmable(getPlausi().getIsConfirmable());
        setPlausiName_de(getPlausi().getName_de());
        setPlausiName_fr(getPlausi().getName_fr());
        setPlausiName_it(getPlausi().getName_it());
    }

    public void resetPlausi(SspPlausi plausi) {
        _plausi = plausi;
    }

    /**
     * @return the person orig data
     */
    @Transient
    public String getPersonOrigDeliveryData() {
        return _person_origDeliveryData;
    }

    /**
     * @return the person label
     */
    @Transient
    public String getPersonLabel() {
        return _person_idType != null ? _person_idType + ": " + _person_id : null;
    }

    @Transient
    public void setPersonLabel(String personLabel) {
        // only for wsdl to appear as property
    }

    @Transient
    public String getSchoolLabel() {
        if (_school_id != null) {
            String label = _school_label != null ? " - " + _school_label : "";
            return _school_idType != null ? _school_idType + ": " + _school_id + label : null;
        } else if (_school_Info != null) {
            StringBuilder sb = new StringBuilder();
            boolean firstEntry = true;
            for (SchoolInfo school : _school_Info) {
                if (!firstEntry) {
                    sb.append(", ");
                }
                String label = school.label != null ? " - " + school.label : "";
                sb.append(school.schoolIdType != null ? school.schoolIdType + ": " + school.schoolId + label : null);
                firstEntry = false;
            }
            return sb.toString();
        }
        return null;
    }

    /**
     * @return the activity label
     */
    @Transient
    public String getActivityLabel() {
        return _activity_id;
    }

    @Transient
    public void setActivityLabel(String activityLabel) {
        // only for wsdl to appear as property
    }

    /**
     * @return the person id type as delivered
     */
    @Transient
    public String getDeliveredPersonIdType() {
        return _person_idType;
    }

    /**
     * @return the person id as delivered
     */
    @Transient
    public String getDeliveredPersonId() {
        return _person_id;
    }

    /**
     * @return the school id type as delivered
     */
    @Transient
    public String getDeliveredSchoolIdType() {
        return _school_idType;
    }

    /**
     * @return the school id as delivered
     */
    @Transient
    public String getDeliveredSchoolId() {
        return _school_id;
    }

    /**
     * @return the activity id as delivered
     */
    @Transient
    public String getDeliveredActivityId() {
        return _activity_id;
    }

    @Transient
    public String getLogicalKey() {
        StringBuilder sb = new StringBuilder();

        if (_person_idType != null) {
            sb.append(_person_idType);
        }

        if (_person_id != null) {
            sb.append("_").append(_person_id);
        }

        // may change to BFS.UNB during plausi process! errorKey = errorKey + (_school_idType == null ? "" : "_" + _school_idType);
        if (_school_id != null) {
            sb.append("_").append(_school_id);
        }
        if (_activity_id != null) {
            sb.append("_").append(_activity_id);
        }
        if (getPlausi().getId() != null) {
            sb.append("_").append(getPlausi().getId());
        }
        if (getConfirmId() != null) {
            sb.append("#").append(getConfirmId());
        }

        return sb.toString();
    }
}
