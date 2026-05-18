/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sdlserver

  $Id$
 */
package ch.bfs.meb.sdl.server.integration.dto;

import java.util.*;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlTransient;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Where;

/**
 * Persistence Object for the class data table
 * 
 * @author $Author$
 * @version $Revision$
 */
@Entity
@Table(name = "SDL_CLASSES")
@GenericGenerator(name = "classseqgen", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator", parameters = {
        @Parameter(name = "sequence_name", value = "SDLSEQ"), @Parameter(name = "increment_size", value = "100"),
        @Parameter(name = "optimizer", value = "pooled-lo") })
public class SdlClass {
    // Fields
    private Long _classId;
    private Long _schoolId;
    private Long _canton;
    private Long _version;
    private String _id;
    private Long _schoolType;
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
    private String _deliveryCode;
    private String _configDeliveryCode;

    private Set<SdlLearner> _learners = new HashSet<SdlLearner>();
    private Set<SdlPlausiError> _plausierrors = new LinkedHashSet<SdlPlausiError>();

    public SdlClass() {
        super();
    }

    public SdlClass(SdlClass sdlClass, List<SdlPlausiError> plausiErrors) {
        setClassId(sdlClass.getClassId());
        setSchoolId(sdlClass.getSchoolId());
        setCanton(sdlClass.getCanton());
        setVersion(sdlClass.getVersion());
        setId(sdlClass.getId());
        setSchoolType(sdlClass.getSchoolType());
        setDeliveryStatus(sdlClass.getDeliveryStatus());
        setPlausiStatus(sdlClass.getPlausiStatus());
        setCreation_user(sdlClass.getCreation_user());
        setCreation_date(sdlClass.getCreation_date());
        setModification_user(sdlClass.getModification_user());
        setModification_date(sdlClass.getModification_date());
        setPrevalidation_user(sdlClass.getPrevalidation_user());
        setPrevalidation_date(sdlClass.getPrevalidation_date());
        setValidation_user(sdlClass.getValidation_user());
        setValidation_date(sdlClass.getValidation_date());
        setUserText(sdlClass.getUserText());
        setConfirmRules(sdlClass.getConfirmRules());
        setDeliveryCode(sdlClass.getDeliveryCode());
        setConfigDeliveryCode(sdlClass.getConfigDeliveryCode());
        getLearners().addAll(sdlClass.getLearners());

        if (plausiErrors != null) {
            for (SdlPlausiError plausiError : plausiErrors) {
                _plausierrors.add(new SdlPlausiError(plausiError));
            }
        }
    }

    // Property accessors
    @Id
    @Column(name = "CLASSID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "classseqgen")
    public Long getClassId() {
        return _classId;
    }

    public void setClassId(Long classId) {
        _classId = classId;
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

    /**
     * @return the _id
     */
    public String getId() {
        return _id;
    }

    /**
     * @param id the _id to set
     */
    public void setId(String id) {
        _id = id;
    }

    /**
     * @return the _schoolType
     */
    public Long getSchoolType() {
        return _schoolType;
    }

    /**
     * @param schoolType the _schoolType to set
     */
    public void setSchoolType(Long schoolType) {
        _schoolType = schoolType;
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

    /**
     * @return the learners
     */
    @XmlTransient
    @OneToMany(mappedBy = "classId")
    public Set<SdlLearner> getLearners() {
        return _learners;
    }

    /**
     * @param learners the learners to set
     */
    public void setLearners(Set<SdlLearner> learners) {
        _learners = learners;
    }

    /**
     * @return the plausierrors
     */
    @XmlTransient
    @OneToMany(mappedBy = "classId")
    @Where(clause = "learnerId is null")
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
}
