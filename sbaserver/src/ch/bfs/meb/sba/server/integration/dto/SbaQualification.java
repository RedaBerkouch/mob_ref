/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sbaserver

 */
package ch.bfs.meb.sba.server.integration.dto;

import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlTransient;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Persistence Object for the qualification data table.
 *
 * @author $Author: jfu $
 * @version $Revision: 948 $
 */
@Data
@EqualsAndHashCode(exclude = { "plausierrors" })
@Entity
@Table(name = "SBA_QUALIFICATIONS")
@GenericGenerator(name = "qualificationseqgen", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator", parameters = {
        @Parameter(name = "sequence_name", value = "SBASEQ"), @Parameter(name = "increment_size", value = "100"),
        @Parameter(name = "optimizer", value = "pooled-lo") })
public class SbaQualification {
    public static Long PERSCATEGORY_OFFICE = 20L;

    @Id
    @Column(name = "QUALIFICATIONID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "qualificationseqgen")
    private Long qualificationId;
    @Column
    private Long personId;
    @Column
    private Long canton;
    @Column
    private Long version;
    @Column
    private String deliveryCode;
    @Column
    private String configDeliveryCode;
    @Column
    private String schoolIdType;
    @Column
    private String schoolId;
    @Column
    private Long educationType;
    @Column
    private Long examType;
    @Column
    private Date examDate;
    @Column
    private Long examNr;
    @Column
    private Long result;
    @Column(name="MATURITY_LANGUAGES")
    private Long maturityLanguages;
    @Column
    private Long deliveryStatus;
    @Column
    private Long plausiStatus;
    @Column
    private String creation_user;
    @Column
    private Date creation_date;
    @Column
    private String modification_user;
    @Column
    private Date modification_date;
    @Column
    private String prevalidation_user;
    @Column
    private Date prevalidation_date;
    @Column
    private String validation_user;
    @Column
    private Date validation_date;
    @Column
    private String userText;
    @Column
    private String confirmRules;

    @XmlTransient
    @OneToMany(mappedBy = "qualificationId", fetch = FetchType.EAGER)
    @OrderBy("isConfirmed, plausi, errorId")
    private Set<SbaPlausiError> plausierrors = new LinkedHashSet<SbaPlausiError>();

    @Transient
    private String nameBurSchool;
    @Transient
    private Long charPublFlg;
    @Transient
    private Long charPrivSubFlg;
    @Transient
    private Long charPrivNoSubFlg;
    @Transient
    private Boolean isSpecialSchool;

    public SbaQualification() {
        super();
    }

    public SbaQualification(SbaQualification qualification, List<SbaPlausiError> plausiErrors) {
        setQualificationId(qualification.getQualificationId());
        setPersonId(qualification.getPersonId());
        setCanton(qualification.getCanton());
        setVersion(qualification.getVersion());
        setDeliveryCode(qualification.getDeliveryCode());
        setConfigDeliveryCode(qualification.getConfigDeliveryCode());
        setSchoolIdType(qualification.getSchoolIdType());
        setSchoolId(qualification.getSchoolId());
        setEducationType(qualification.getEducationType());
        setExamType(qualification.getExamType());
        setExamDate(qualification.getExamDate());
        setExamNr(qualification.getExamNr());
        setResult(qualification.getResult());
        setMaturityLanguages(qualification.getMaturityLanguages());
        setDeliveryStatus(qualification.getDeliveryStatus());
        setPlausiStatus(qualification.getPlausiStatus());
        setCreation_user(qualification.getCreation_user());
        setCreation_date(qualification.getCreation_date());
        setModification_user(qualification.getModification_user());
        setModification_date(qualification.getModification_date());
        setPrevalidation_user(qualification.getPrevalidation_user());
        setPrevalidation_date(qualification.getPrevalidation_date());
        setValidation_user(qualification.getValidation_user());
        setValidation_date(qualification.getValidation_date());
        setUserText(qualification.getUserText());
        setConfirmRules(qualification.getConfirmRules());
        setNameBurSchool(qualification.getNameBurSchool());
        setCharPublFlg(qualification.getCharPublFlg());
        setCharPrivSubFlg(qualification.getCharPrivSubFlg());
        setCharPrivNoSubFlg(qualification.getCharPrivNoSubFlg());
        setIsSpecialSchool(qualification.getIsSpecialSchool());

        if (plausiErrors != null) {
            for (SbaPlausiError plausiError : plausiErrors) {
                plausierrors.add(new SbaPlausiError(plausiError));
            }
        }
    }
}