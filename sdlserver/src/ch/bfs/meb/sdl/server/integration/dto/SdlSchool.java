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

import lombok.Data;

/**
 * Persistence Object for the school data table
 *
 * @author $Author$
 * @version $Revision$
 */
@Data
@Entity
@Table(name = "SDL_SCHOOLS")
@GenericGenerator(name = "schoolseqgen", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator", parameters = {
        @Parameter(name = "sequence_name", value = "SDLSEQ"), @Parameter(name = "increment_size", value = "100"),
        @Parameter(name = "optimizer", value = "pooled-lo") })
public class SdlSchool {
    /**
     * Technical key.
     */
    @Id
    @Column(name = "SCHOOLID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "schoolseqgen")
    private Long schoolId;

    private Long deliveryId;
    @Column
    private Long canton;
    @Column
    private Long version;
    @Column
    private String deliveryCode;
    @Column
    private String configDeliveryCode;

    private String idType;

    /** Business key: BUR key when idType="CH.BUR" or CantonalCode otherwise. */
    private String id;

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
    @Column
    private boolean isToDelete;

    @XmlTransient
    @OneToMany(mappedBy = "schoolId", fetch = FetchType.EAGER)
    private Set<SdlClass> classes = new HashSet<SdlClass>();

    @XmlTransient
    @OneToMany(mappedBy = "schoolId", fetch = FetchType.EAGER)
    @Where(clause = "classId is null")
    @OrderBy("isConfirmed, plausi, errorId")
    private Set<SdlPlausiError> plausierrors = new LinkedHashSet<SdlPlausiError>();

    // transient
    @Transient
    private String burSchoolLabel;
    @Transient
    private Long charPublFlg;
    @Transient
    private Long charPrivSubFlg;
    @Transient
    private Long charPrivNoSubFlg;
    @Transient
    private Boolean isSpecialSchool;

    public SdlSchool() {
        super();
    }

    public SdlSchool(SdlSchool school, List<SdlPlausiError> plausiErrors) {
        setSchoolId(school.getSchoolId());
        setDeliveryId(school.getDeliveryId());
        setCanton(school.getCanton());
        setVersion(school.getVersion());
        setDeliveryCode(school.getDeliveryCode());
        setConfigDeliveryCode(school.getConfigDeliveryCode());
        setIdType(school.getIdType());
        setId(school.getId());
        setDeliveryStatus(school.getDeliveryStatus());
        setPlausiStatus(school.getPlausiStatus());
        setCreation_user(school.getCreation_user());
        setCreation_date(school.getCreation_date());
        setModification_user(school.getModification_user());
        setModification_date(school.getModification_date());
        setPrevalidation_user(school.getPrevalidation_user());
        setPrevalidation_date(school.getPrevalidation_date());
        setValidation_user(school.getValidation_user());
        setValidation_date(school.getValidation_date());
        setUserText(school.getUserText());
        setConfirmRules(school.getConfirmRules());
        setToDelete(school.isToDelete());
        getClasses().addAll(school.getClasses());
        setBurSchoolLabel(school.getBurSchoolLabel());
        setCharPublFlg(school.getCharPublFlg());
        setCharPrivSubFlg(school.getCharPrivSubFlg());
        setCharPrivNoSubFlg(school.getCharPrivNoSubFlg());
        setIsSpecialSchool(school.getIsSpecialSchool());

        if (plausiErrors != null) {
            for (SdlPlausiError plausiError : plausiErrors) {
                plausierrors.add(new SdlPlausiError(plausiError));
            }
        }
    }
}