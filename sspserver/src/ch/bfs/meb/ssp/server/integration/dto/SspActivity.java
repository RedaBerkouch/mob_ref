/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sspserver

 */
package ch.bfs.meb.ssp.server.integration.dto;

import java.math.BigDecimal;
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
 * Persistence Object for the activity data table
 *
 * @author $Author: jfu $
 * @version $Revision: 948 $
 */
@Data
@EqualsAndHashCode(exclude = { "plausierrors" })
@Entity
@Table(name = "SSP_ACTIVITIES")
@GenericGenerator(name = "activityseqgen", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator", parameters = {
        @Parameter(name = "sequence_name", value = "SSPSEQ"), @Parameter(name = "increment_size", value = "100"),
        @Parameter(name = "optimizer", value = "pooled-lo") })
public class SspActivity {
    public static Long PERSCATEGORY_OFFICE = 20L;

    @Id
    @Column(name = "ACTIVITYID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "activityseqgen")
    private Long activityId;
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
    private Long id;
    @Column
    private String schoolIdType;
    @Column
    private String schoolId;
    @Column
    private Long persCategory;
    @Column
    private Long contractType;
    @Column
    private Long qualification;
    @Column
    private BigDecimal pensum;
    @Column
    private BigDecimal fullTimeRef;
    @Column
    private Long schoolType;
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
    @OneToMany(mappedBy = "activityId", fetch = FetchType.EAGER)
    @OrderBy("isConfirmed, plausi, errorId")
    private Set<SspPlausiError> plausierrors = new LinkedHashSet<SspPlausiError>();

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

    public SspActivity() {
        super();
    }

    public SspActivity(SspActivity activity, List<SspPlausiError> plausiErrors) {
        setActivityId(activity.getActivityId());
        setPersonId(activity.getPersonId());
        setCanton(activity.getCanton());
        setVersion(activity.getVersion());
        setDeliveryCode(activity.getDeliveryCode());
        setConfigDeliveryCode(activity.getConfigDeliveryCode());
        setId(activity.getId());
        setSchoolIdType(activity.getSchoolIdType());
        setSchoolId(activity.getSchoolId());
        setPersCategory(activity.getPersCategory());
        setContractType(activity.getContractType());
        setQualification(activity.getQualification());
        setPensum(activity.getPensum());
        setFullTimeRef(activity.getFullTimeRef());
        setSchoolType(activity.getSchoolType());
        setDeliveryStatus(activity.getDeliveryStatus());
        setPlausiStatus(activity.getPlausiStatus());
        setCreation_user(activity.getCreation_user());
        setCreation_date(activity.getCreation_date());
        setModification_user(activity.getModification_user());
        setModification_date(activity.getModification_date());
        setPrevalidation_user(activity.getPrevalidation_user());
        setPrevalidation_date(activity.getPrevalidation_date());
        setValidation_user(activity.getValidation_user());
        setValidation_date(activity.getValidation_date());
        setUserText(activity.getUserText());
        setConfirmRules(activity.getConfirmRules());
        setNameBurSchool(activity.getNameBurSchool());
        setCharPublFlg(activity.getCharPublFlg());
        setCharPrivSubFlg(activity.getCharPrivSubFlg());
        setCharPrivNoSubFlg(activity.getCharPrivNoSubFlg());
        setIsSpecialSchool(activity.getIsSpecialSchool());

        if (plausiErrors != null) {
            for (SspPlausiError plausiError : plausiErrors) {
                plausierrors.add(new SspPlausiError(plausiError));
            }
        }
    }
}
