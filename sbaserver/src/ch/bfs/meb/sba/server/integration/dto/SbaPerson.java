/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sbaserver

 */
package ch.bfs.meb.sba.server.integration.dto;

import java.util.*;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlTransient;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Where;

/**
 * Persistence Object for the person data table
 * 
 * @author $Author: dzw $
 * @version $Revision: 948 $
 */
@Entity
@Table(name = "SBA_PERSONS")
@GenericGenerator(name = "personseqgen", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator", parameters = {
        @Parameter(name = "sequence_name", value = "SBASEQ"), @Parameter(name = "increment_size", value = "100"),
        @Parameter(name = "optimizer", value = "pooled-lo") })
public class SbaPerson {
    // Fields
    private Long _personId;
    private Long _deliveryId;
    private String _deliveryCode;
    private String _configDeliveryCode;
    private Long _canton;
    private Long _version;
    private String _idType;
    private String _id;
    private Long _sex;
    private Date _birthdate;
    private Long _residence;
    private Long _historic_residence;
    private Long _country;
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
    private boolean _isToDelete;

    private Set<SbaQualification> _qualification = new HashSet<SbaQualification>();
    private Set<SbaPlausiError> _plausierrors = new LinkedHashSet<SbaPlausiError>();

    public SbaPerson() {
        super();
    }

    public SbaPerson(SbaPerson person, List<SbaPlausiError> plausiErrors) {
        setPersonId(person.getPersonId());
        setDeliveryId(person.getDeliveryId());
        setCanton(person.getCanton());
        setVersion(person.getVersion());
        setDeliveryCode(person.getDeliveryCode());
        setConfigDeliveryCode(person.getConfigDeliveryCode());
        setIdType(person.getIdType());
        setId(person.getId());
        setSex(person.getSex());
        setBirthdate(person.getBirthdate());
        setResidence(person.getResidence());
        setHistoric_residence(person.getHistoric_residence());
        setCountry(person.getCountry());
        setOrigDeliveryData(person.getOrigDeliveryData());
        setDeliveryStatus(person.getDeliveryStatus());
        setPlausiStatus(person.getPlausiStatus());
        setCreation_user(person.getCreation_user());
        setCreation_date(person.getCreation_date());
        setModification_user(person.getModification_user());
        setModification_date(person.getModification_date());
        setPrevalidation_user(person.getPrevalidation_user());
        setPrevalidation_date(person.getPrevalidation_date());
        setValidation_user(person.getValidation_user());
        setValidation_date(person.getValidation_date());
        setUserText(person.getUserText());
        setConfirmRules(person.getConfirmRules());
        setIsToDelete(person.getIsToDelete());
        getQualifications().addAll(person.getQualifications());

        if (plausiErrors != null) {
            for (SbaPlausiError plausiError : plausiErrors) {
                _plausierrors.add(new SbaPlausiError(plausiError));
            }
        }
    }

    // Property accessors
    @Id
    @Column(name = "PERSONID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "personseqgen")
    /*
      @return the _personId
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

    @Column
    public Long getDeliveryId() {
        return _deliveryId;
    }

    public void setDeliveryId(Long deliveryId) {
        _deliveryId = deliveryId;
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

    /**
     * @return the isToDelete
     */
    @Column
    public boolean getIsToDelete() {
        return _isToDelete;
    }

    /**
     * @param isToDelete the isToDelete to set
     */
    public void setIsToDelete(boolean isToDelete) {
        _isToDelete = isToDelete;
    }

    /**
     * @return the qualifications
     */
    @XmlTransient
    @OneToMany(mappedBy = "personId")
    public Set<SbaQualification> getQualifications() {
        return _qualification;
    }

    /**
     * @param qualifications the qualificationes to set
     */
    public void setQualifications(Set<SbaQualification> qualifications) {
        _qualification = qualifications;
    }

    /**
     * @return the plausierrors
     */
    @XmlTransient
    @OneToMany(mappedBy = "personId")
    @Where(clause = "qualificationId is null")
    @OrderBy("isConfirmed, plausi, errorId")
    public Set<SbaPlausiError> getPlausierrors() {
        return _plausierrors;
    }

    /**
     * @param plausierrors the plausierrors to set
     */
    public void setPlausierrors(Set<SbaPlausiError> plausierrors) {
        _plausierrors = plausierrors;
    }

}
