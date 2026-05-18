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

/**
 * Persistence Object for the learner data table
 * 
 * @author $Author$
 * @version $Revision$
 */
@Entity
@Table(name = "SDL_LEARNERS")
@GenericGenerator(name = "learnerseqgen", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator", parameters = {
        @Parameter(name = "sequence_name", value = "SDLSEQ"), @Parameter(name = "increment_size", value = "100"),
        @Parameter(name = "optimizer", value = "pooled-lo") })
public class SdlLearner {
    // Fields
    private Long _learnerId;
    private Long _classId;
    private Long _canton;
    private Long _version;
    private String _idType;
    private String _id;
    private Long _sex;
    private Date _birthdate;
    private Long _nationality;
    private Long _language;
    private Long _residence;
    private Long _historic_residence;
    private Long _country;
    private Long _schoolType;
    private Long _cantonalYear;
    private Long _educationType;
    private Long _planStatus;
    private Long _profMatura;
    private Long _prev_schoolType;
    private Long _prev_cantonalYear;
    private String _addition1;
    private String _addition2;
    private String _addition3;
    private String _addition4;
    private String _addition5;
    private String _origDeliveryData;
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

    private Set<SdlPlausiError> _plausierrors = new LinkedHashSet<SdlPlausiError>();

    public SdlLearner() {
        super();
    }

    public SdlLearner(SdlLearner learner, List<SdlPlausiError> plausiErrors) {
        setLearnerId(learner.getLearnerId());
        setClassId(learner.getClassId());
        setCanton(learner.getCanton());
        setVersion(learner.getVersion());
        setIdType(learner.getIdType());
        setId(learner.getId());
        setSex(learner.getSex());
        setBirthdate(learner.getBirthdate());
        setNationality(learner.getNationality());
        setLanguage(learner.getLanguage());
        setResidence(learner.getResidence());
        setHistoric_residence(learner.getHistoric_residence());
        setCountry(learner.getCountry());
        setSchoolType(learner.getSchoolType());
        setCantonalYear(learner.getCantonalYear());
        setEducationType(learner.getEducationType());
        setPlanStatus(learner.getPlanStatus());
        setProfMatura(learner.getProfMatura());
        setPrev_schoolType(learner.getPrev_schoolType());
        setPrev_cantonalYear(learner.getPrev_cantonalYear());
        setAddition1(learner.getAddition1());
        setAddition2(learner.getAddition2());
        setAddition3(learner.getAddition3());
        setAddition4(learner.getAddition4());
        setAddition5(learner.getAddition5());
        setOrigDeliveryData(learner.getOrigDeliveryData());
        setDeliveryStatus(learner.getDeliveryStatus());
        setPlausiStatus(learner.getPlausiStatus());
        setCreation_user(learner.getCreation_user());
        setCreation_date(learner.getCreation_date());
        setModification_user(learner.getModification_user());
        setModification_date(learner.getModification_date());
        setPrevalidation_user(learner.getPrevalidation_user());
        setPrevalidation_date(learner.getPrevalidation_date());
        setValidation_user(learner.getValidation_user());
        setValidation_date(learner.getValidation_date());
        setUserText(learner.getUserText());
        setConfirmRules(learner.getConfirmRules());
        setDeliveryCode(learner.getDeliveryCode());
        setConfigDeliveryCode(learner.getConfigDeliveryCode());

        if (plausiErrors != null) {
            for (SdlPlausiError plausiError : plausiErrors) {
                _plausierrors.add(new SdlPlausiError(plausiError));
            }
        }
    }

    // Property accessors
    @Id
    @Column(name = "LEARNERID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "learnerseqgen")
    /*
      @return the _learnerId
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

    @Column
    public Long getClassId() {
        return _classId;
    }

    public void setClassId(Long classId) {
        _classId = classId;
    }

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
     * @return the _idType
     */
    public String getIdType() {
        return _idType;
    }

    /**
     * @param idType the _idType to set
     */
    public void setIdType(String idType) {
        _idType = idType;
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
     * @return the _sex
     */
    public Long getSex() {
        return _sex;
    }

    /**
     * @param sex the _sex to set
     */
    public void setSex(Long sex) {
        _sex = sex;
    }

    /**
     * @return the _birthdate
     */
    public Date getBirthdate() {
        return _birthdate;
    }

    /**
     * @param birthdate the _birthdate to set
     */
    public void setBirthdate(Date birthdate) {
        _birthdate = birthdate;
    }

    /**
     * @return the _nationality
     */
    public Long getNationality() {
        return _nationality;
    }

    /**
     * @param nationality the _nationality to set
     */
    public void setNationality(Long nationality) {
        _nationality = nationality;
    }

    /**
     * @return the _language
     */
    public Long getLanguage() {
        return _language;
    }

    /**
     * @param language the _language to set
     */
    public void setLanguage(Long language) {
        _language = language;
    }

    /**
     * @return the _residence
     */
    public Long getResidence() {
        return _residence;
    }

    /**
     * @param residence the _residence to set
     */
    public void setResidence(Long residence) {
        _residence = residence;
    }

    /**
     * @return the _historic_residence
     */
    public Long getHistoric_residence() {
        return _historic_residence;
    }

    /**
     * @param historicResidence the _historic_residence to set
     */
    public void setHistoric_residence(Long historicResidence) {
        _historic_residence = historicResidence;
    }

    /**
     * @return the _country
     */
    public Long getCountry() {
        return _country;
    }

    /**
     * @param country the _country to set
     */
    public void setCountry(Long country) {
        _country = country;
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

    /**
     * @return the _cantonalYear
     */
    public Long getCantonalYear() {
        return _cantonalYear;
    }

    /**
     * @param cantonalYear the _cantonalYear to set
     */
    public void setCantonalYear(Long cantonalYear) {
        _cantonalYear = cantonalYear;
    }

    /**
     * @return the _educationType
     */
    public Long getEducationType() {
        return _educationType;
    }

    /**
     * @param educationType the _educationType to set
     */
    public void setEducationType(Long educationType) {
        _educationType = educationType;
    }

    /**
     * @return the _planStatus
     */
    public Long getPlanStatus() {
        return _planStatus;
    }

    /**
     * @param planStatus the _planStatus to set
     */
    public void setPlanStatus(Long planStatus) {
        _planStatus = planStatus;
    }

    /**
     * @return the _profMatura
     */
    public Long getProfMatura() {
        return _profMatura;
    }

    /**
     * @param profMatura the _profMatura to set
     */
    public void setProfMatura(Long profMatura) {
        _profMatura = profMatura;
    }

    /**
     * @return the _prev_schoolType
     */
    public Long getPrev_schoolType() {
        return _prev_schoolType;
    }

    /**
     * @param prevSchoolType the _prev_schoolType to set
     */
    public void setPrev_schoolType(Long prevSchoolType) {
        _prev_schoolType = prevSchoolType;
    }

    /**
     * @return the _prev_cantonalYear
     */
    public Long getPrev_cantonalYear() {
        return _prev_cantonalYear;
    }

    /**
     * @param prevCantonalYear the _prev_cantonalYear to set
     */
    public void setPrev_cantonalYear(Long prevCantonalYear) {
        _prev_cantonalYear = prevCantonalYear;
    }

    /**
     * @return the _addition1
     */
    public String getAddition1() {
        return _addition1;
    }

    /**
     * @param addition1 the _addition1 to set
     */
    public void setAddition1(String addition1) {
        _addition1 = addition1;
    }

    /**
     * @return the _addition2
     */
    public String getAddition2() {
        return _addition2;
    }

    /**
     * @param addition2 the _addition2 to set
     */
    public void setAddition2(String addition2) {
        _addition2 = addition2;
    }

    /**
     * @return the _addition3
     */
    public String getAddition3() {
        return _addition3;
    }

    /**
     * @param addition3 the _addition3 to set
     */
    public void setAddition3(String addition3) {
        _addition3 = addition3;
    }

    /**
     * @return the _addition4
     */
    public String getAddition4() {
        return _addition4;
    }

    /**
     * @param addition4 the _addition4 to set
     */
    public void setAddition4(String addition4) {
        _addition4 = addition4;
    }

    /**
     * @return the _addition5
     */
    public String getAddition5() {
        return _addition5;
    }

    /**
     * @param addition5 the _addition5 to set
     */
    public void setAddition5(String addition5) {
        _addition5 = addition5;
    }

    /**
     * @return the _origDeliveryData
     */
    public String getOrigDeliveryData() {
        return _origDeliveryData;
    }

    /**
     * @param origDeliveryData the _origDeliveryData to set
     */
    public void setOrigDeliveryData(String origDeliveryData) {
        _origDeliveryData = origDeliveryData;
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

    @XmlTransient
    @OneToMany(mappedBy = "learnerId")
    //	@Sort (type=SortType.COMPARATOR, comparator=SdlPlausierrorComparator.class)
    @OrderBy("isConfirmed, plausi, errorId")
    public Set<SdlPlausiError> getPlausierrors() {
        return _plausierrors;
    }

    public void setPlausierrors(Set<SdlPlausiError> plausierrors) {
        _plausierrors = plausierrors;
    }
}
