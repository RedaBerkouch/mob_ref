package ch.bfs.meb.sba.server.integration.dto;

import java.util.*;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlTransient;

import lombok.AllArgsConstructor;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.springframework.security.core.context.SecurityContextHolder;

import ch.bfs.meb.security.MebUser;
import ch.bfs.meb.server.commons.integration.dto.PlausiError;

/** Entity for {@link PlausiError} in SBA. */
@Entity
@Table(name = "SBA_PLAUSIERRORS")
@GenericGenerator(name = "plausierrorseqgen", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator", parameters = {
        @Parameter(name = "sequence_name", value = "SBASEQ"), @Parameter(name = "increment_size", value = "100"),
        @Parameter(name = "optimizer", value = "pooled-lo") })
public class SbaPlausiError extends PlausiError {

    @AllArgsConstructor
    public static class SchoolInfo {
        Long personId;
        String schoolIdType;
        String schoolId;
        String label;
    }

    // Fields
    private Long _personId;
    private Long _qualificationId;
    @XmlTransient
    private SbaPlausi _plausi;

    // Transient
    private String _person_idType;
    private String _person_id;
    private String _person_origDeliveryData;
    private String _school_idType;
    private String _school_id;
    private String _school_label;
    private String _examNr;
    private List<SchoolInfo> _school_Info;

    public SbaPlausiError() {}

    public SbaPlausiError(PlausiError plausierror) {
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

    public SbaPlausiError(SbaPlausiError plausierror) {
        this((PlausiError) plausierror);

        // Fields SbaPlausiError
        setPersonId(plausierror.getPersonId());
        setQualificationId(plausierror.getQualificationId());
        setPlausi(plausierror.getPlausi());
    }

    public static List<SbaPlausiError> updatePlausiErrorsData(Set<SbaPlausiError> psistPlausiErrors, List<PlausiError> dataPlausiErrors) {
        List<SbaPlausiError> newPlausiErrors = new ArrayList<>();
        HashMap<Long, PlausiError> dataPlausiErrorsMap = new HashMap<>();
        if (dataPlausiErrors != null) {
            for (PlausiError plausiError : dataPlausiErrors) {
                dataPlausiErrorsMap.put(plausiError.getErrorId(), plausiError);
            }
        }

        String userEmail = ((MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getEmail();
        for (SbaPlausiError psistPlausiError : psistPlausiErrors) {
            if (dataPlausiErrors != null) {
                PlausiError plausiError = dataPlausiErrorsMap.get(psistPlausiError.getErrorId());
                if (plausiError != null) {
                    SbaPlausiError newPlausiError = new SbaPlausiError(plausiError);
                    newPlausiError.setPersonId(psistPlausiError.getPersonId());
                    newPlausiError.setQualificationId(psistPlausiError.getQualificationId());
                    newPlausiError.setPlausi(psistPlausiError.getPlausi());
                    // confirmed state changed, update modification data
                    if (plausiError.getIsConfirmed() != psistPlausiError.getIsConfirmed()) {
                        newPlausiError.setModification_user(userEmail);
                        newPlausiError.setModification_date(new Date());
                    }
                    newPlausiErrors.add(newPlausiError);
                } else {
                    newPlausiErrors.add(new SbaPlausiError(psistPlausiError));
                }
            } else {
                newPlausiErrors.add(new SbaPlausiError(psistPlausiError));
            }
        }

        return newPlausiErrors;
    }

    public SbaPlausiError(SbaPlausi plausi) {
        _plausi = plausi;
    }

    public void addMultipleSchoolInfo(List<SchoolInfo> schoolInfo) {
        _school_Info = schoolInfo;
    }

    public void addQualificationInfoWithLabel(String schoolIdType, String schoolId, String schoolLabel, String examNr) {
        _school_idType = schoolIdType;
        _school_id = schoolId;
        _school_label = schoolLabel;
        _examNr = examNr;
    }

    public void addQualificationInfo(String schoolIdType, String schoolId, String examNr) {
        _school_idType = schoolIdType;
        _school_id = schoolId;
        _examNr = examNr;
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
     * @return the _qualificationId
     */
    public Long getQualificationId() {
        return _qualificationId;
    }

    /**
     * @param qualificationId the _qualificationId to set
     */
    public void setQualificationId(Long qualificationId) {
        _qualificationId = qualificationId;
    }

    /**
     * @return the _plausi
     */
    @ManyToOne
    @JoinColumn(name = "plausiId")
    @XmlTransient
    public SbaPlausi getPlausi() {
        return _plausi;
    }

    /**
     * Warning!! Has to be private, because if it's public, the plausi data will be transferred
     * between server and client!!
     *
     * @param plausi the _plausi to set
     */
    private void setPlausi(SbaPlausi plausi) {
        _plausi = plausi;
    }

    public void loadPlausiData() {
        setConfirmable(getPlausi().getIsConfirmable());
        setPlausiName_de(getPlausi().getName_de());
        setPlausiName_fr(getPlausi().getName_fr());
        setPlausiName_it(getPlausi().getName_it());
    }

    public void resetPlausi(SbaPlausi plausi) {
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

    @Transient
    public void setSchoolLabel(String schoolLabel) {
        // only for wsdl to appear as property
    }

    /**
     * @return the qualification label
     */
    @Transient
    public String getQualificationLabel() {
        return _examNr;
    }

    @Transient
    public void setQualificationLabel(String qualificationLabel) {
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
     * @return the qualification id (exam nr) as delivered
     */
    @Transient
    public String getDeliveredQualificationId() {
        return _examNr;
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
        if (_examNr != null) {
            sb.append("_").append(_examNr);
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
