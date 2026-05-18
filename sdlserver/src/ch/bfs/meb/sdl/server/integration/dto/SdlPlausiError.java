/*
 * MEB Portal
 * Bundesamt für Statistik
 *
 * adesso Schweiz AG
 * Copyright (c) 2009, 2010
 *
 * Projekt: sdlserver
 *
 */
package ch.bfs.meb.sdl.server.integration.dto;

import java.util.*;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlTransient;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.springframework.security.core.context.SecurityContextHolder;

import ch.bfs.meb.security.MebUser;
import ch.bfs.meb.server.commons.integration.dto.PlausiError;

/**
 * Persistence Object for the plausi error table
 *
 * @author $Author$
 * @version $Revision$
 */
@Entity
@Table(name = "SDL_PLAUSIERRORS")
@GenericGenerator(name = "plausierrorseqgen", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator", parameters = {
        @Parameter(name = "sequence_name", value = "SDLSEQ"), @Parameter(name = "increment_size", value = "100"),
        @Parameter(name = "optimizer", value = "pooled-lo") })
public class SdlPlausiError extends PlausiError {
    // Fields
    private Long _schoolId;
    private Long _classId;
    private Long _learnerId;
    @XmlTransient
    private SdlPlausi _plausi;

    // Transient
    private String _school_idType;
    private String _school_id;
    private String _school_label;
    private String _class_id;
    private String _learner_idType;
    private String _learner_id;
    private String _learner_origDeliveryData;

    public SdlPlausiError() {}

    public SdlPlausiError(PlausiError plausierror) {
        // Fields PlausiError
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

    public SdlPlausiError(SdlPlausiError plausierror) {
        this((PlausiError) plausierror);

        // Fields SdlPlausiError
        setSchoolId(plausierror.getSchoolId());
        setClassId(plausierror.getClassId());
        setLearnerId(plausierror.getLearnerId());
        setPlausi(plausierror.getPlausi());
    }

    public static List<SdlPlausiError> updatePlausiErrorsData(Set<SdlPlausiError> psistPlausiErrors, List<PlausiError> dataPlausiErrors) {
        List<SdlPlausiError> newPlausiErrors = new ArrayList<>();
        HashMap<Long, PlausiError> dataPlausiErrorsMap = new HashMap<>();
        if (dataPlausiErrors != null) {
            for (PlausiError plausiError : dataPlausiErrors) {
                dataPlausiErrorsMap.put(plausiError.getErrorId(), plausiError);
            }
        }

        String userEmail = ((MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getEmail();
        for (SdlPlausiError psistPlausiError : psistPlausiErrors) {
            if (dataPlausiErrors != null) {
                PlausiError plausiError = dataPlausiErrorsMap.get(psistPlausiError.getErrorId());
                if (plausiError != null) {
                    SdlPlausiError newPlausiError = new SdlPlausiError(plausiError);
                    newPlausiError.setSchoolId(psistPlausiError.getSchoolId());
                    newPlausiError.setClassId(psistPlausiError.getClassId());
                    newPlausiError.setLearnerId(psistPlausiError.getLearnerId());
                    newPlausiError.setPlausi(psistPlausiError.getPlausi());
                    // confirmed state changed, update modification data
                    if (plausiError.getIsConfirmed() != psistPlausiError.getIsConfirmed()) {
                        newPlausiError.setModification_user(userEmail);
                        newPlausiError.setModification_date(new Date());
                    }
                    newPlausiErrors.add(newPlausiError);
                } else {
                    newPlausiErrors.add(new SdlPlausiError(psistPlausiError));
                }
            } else {
                newPlausiErrors.add(new SdlPlausiError(psistPlausiError));
            }
        }

        return newPlausiErrors;
    }

    public SdlPlausiError(SdlPlausi plausi) {
        _plausi = plausi;
    }

    public void addSchoolInfoWithLabel(String idType, String id, String label) {
        _school_idType = idType;
        _school_id = id;
        _school_label = label;
    }

    public void addSchoolInfo(String idType, String id) {
        _school_idType = idType;
        _school_id = id;
        _school_label = null;
    }

    public void addClassInfo(String id) {
        _class_id = id;
    }

    public void addLearnerInfo(String idType, String id, String origDeliveryData) {
        _learner_idType = idType;
        _learner_id = id;
        _learner_origDeliveryData = origDeliveryData;
    }

    /**
     * @return the _schoolId
     */
    public Long getSchoolId() {
        return _schoolId;
    }

    /**
     * @param schoolId the _schoolId to set
     */
    public void setSchoolId(Long schoolId) {
        _schoolId = schoolId;
    }

    /**
     * @return the _classId
     */
    public Long getClassId() {
        return _classId;
    }

    /**
     * @param classId the _classId to set
     */
    public void setClassId(Long classId) {
        _classId = classId;
    }

    /**
     * @return the _learnerId
     */
    public Long getLearnerId() {
        return _learnerId;
    }

    /**
     * @param learnerId the _learnerId to set
     */
    public void setLearnerId(Long learnerId) {
        _learnerId = learnerId;
    }

    /**
     * @return the _plausi
     */
    @ManyToOne
    @JoinColumn(name = "plausiId")
    @XmlTransient
    public SdlPlausi getPlausi() {
        return _plausi;
    }

    /**
     * Warning!! Has to be private, because if it's public, the plausi data will be transferred
     * between server and client!!
     *
     * @param plausi the _plausi to set
     */
    private void setPlausi(SdlPlausi plausi) {
        _plausi = plausi;
    }

    public void loadPlausiData() {
        setConfirmable(getPlausi().getIsConfirmable());
        setPlausiName_de(getPlausi().getName_de());
        setPlausiName_fr(getPlausi().getName_fr());
        setPlausiName_it(getPlausi().getName_it());
    }

    public void resetPlausi(SdlPlausi plausi) {
        _plausi = plausi;
    }

    /**
     * @return the learner label
     */
    @Transient
    public String getLearnerLabel() {
        return _learner_idType != null ? _learner_idType + ": " + _learner_id : null;
    }

    @Transient
    public void setLearnerLabel(String learnerLabel) {
        // only for wsdl to appear as property
    }

    /**
     * @return the learner orig data
     */
    @Transient
    public String getLearnerOrigDeliveryData() {
        return _learner_origDeliveryData;
    }

    /**
     * @return the school label
     */
    @Transient
    public String getSchoolLabel() {
        String label = _school_label != null ? " - " + _school_label : "";
        return _school_idType != null ? _school_idType + ": " + _school_id + label : null;
    }

    @Transient
    public void setSchoolLabel(String schoolLabel) {
        // only for wsdl to appear as property
    }

    /**
     * @return the class label
     */
    @Transient
    public String getClassLabel() {
        return _class_id;
    }

    @Transient
    public void setClassLabel(String classLabel) {
        // only for wsdl to appear as property
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
     * @return the class id as delivered
     */
    @Transient
    public String getDeliveredClassId() {
        return _class_id;
    }

    /**
     * @return the learner id type as delivered
     */
    @Transient
    public String getDeliveredLearnerIdType() {
        return _learner_idType;
    }

    /**
     * @return the learner id as delivered
     */
    @Transient
    public String getDeliveredLearnerId() {
        return _learner_id;
    }

    @Transient
    public String getLogicalKey() {
        StringBuilder sb = new StringBuilder();
        // may change to BFS.UNB during plausi process! errorKey = errorKey + (_school_idType == null ? "" : _school_idType);

        if (_school_id != null) {
            sb.append("_").append(_school_id);
        }

        if (_class_id != null) {
            sb.append("_").append(_class_id);
        }

        if (_learner_idType != null) {
            sb.append(_learner_idType);
        }

        if (_learner_id != null) {
            sb.append("_").append(_learner_id);
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
