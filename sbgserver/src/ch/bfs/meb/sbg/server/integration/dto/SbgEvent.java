/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sbaserver

 */
package ch.bfs.meb.sbg.server.integration.dto;

import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlTransient;

import org.hibernate.annotations.GenericGenerator;

import ch.admin.bfs.sbg.transfer.Event;
import ch.admin.bfs.sbg.transfer.Plausierror;
import lombok.Getter;
import lombok.Setter;

/**
 * Persistent data transfer object for SBG events
 *
 * @author $Author: dzw $
 * @version $Revision: 104 $
 */
@Getter
@Setter
@Entity
@Table(name = "EVENT")
@GenericGenerator(name = "eventseqgen", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator", parameters = {
        @org.hibernate.annotations.Parameter(name = "sequence_name", value = "SBGSEQ"),
        @org.hibernate.annotations.Parameter(name = "increment_size", value = "100"),
        @org.hibernate.annotations.Parameter(name = "optimizer", value = "pooled-lo") })
public class SbgEvent {
    @Id
    @Column(name = "EVENTID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "eventseqgen")
    private Long eventid;
    @Column
    private Long pid;
    @Column
    private Long canton;
    @Column
    private Long version;
    @Column
    private Long type;

    /**
     * Code for SBFI ("Staatssekretariat fuer Bildung, Forschung und Innovation") State Secretariat for Education, Research and Innovation
     */
    @Column
    private Long sbfiCode;
    @Column
    private Long contractNr;
    @Column
    private Long professionCode;
    @Column
    private Long contractType;
    @Column
    private Date contractDate;
    @Column
    private Long educationYear;
    @Column
    private Long examType;
    @Column
    private Long examNr;
    @Column
    private Long examRepetition;
    @Column
    private Long examResult;
    @Column
    private Long cancelReason;
    @Column
    private Date cancelDate;
    @Column
    private Long burnr;
    @Column
    private String kantLbCode;
    @Column
    private Long keyAspect;
    @Column(length = 1024)
    private String userComment;
    @Column
    private String firmName;
    @Column
    private String firmStreet;
    @Column
    private String firmStreetNr;
    @Column
    private Long firmPlz;
    @Column
    private String firmMunicipality;
    @Column
    private boolean flagLbv;
    @Column
    private Long plausiStatus;
    @Column
    private boolean isValidated;
    @Column
    private String modUser;
    @Column
    private Date modDate;

    @XmlTransient
    @OneToMany(mappedBy = "eventId")
    @OrderBy("isConfirmed, plausiId, errorId")
    private Set<Plausierror> plausierrors = new LinkedHashSet<Plausierror>();

    /**
     * default constructor
     */
    public SbgEvent() {}

    public SbgEvent(Event transEvent) {
        setEventid(transEvent.getEventid());
        setPid(transEvent.getPid());
        setCanton(transEvent.getCanton());
        setVersion(transEvent.getVersion());
        setType(transEvent.getType());
        setSbfiCode(transEvent.getSbfiCode());
        setContractNr(transEvent.getContractNr());
        setProfessionCode(transEvent.getProfessionCode());
        setContractType(transEvent.getContractType());
        setContractDate(transEvent.getContractDate());
        setEducationYear(transEvent.getEducationYear());
        setExamType(transEvent.getExamType());
        setExamNr(transEvent.getExamNr());
        setExamRepetition(transEvent.getExamRepetition());
        setExamResult(transEvent.getExamResult());
        setCancelReason(transEvent.getCancelReason());
        setCancelDate(transEvent.getCancelDate());
        setBurnr(transEvent.getBurnr());
        setKantLbCode(transEvent.getKantLbCode());
        setKeyAspect(transEvent.getKeyAspect());
        setUserComment(transEvent.getUserComment());
        setFirmName(transEvent.getFirmName());
        setFirmStreet(transEvent.getFirmStreet());
        setFirmStreetNr(transEvent.getFirmStreetNr());
        setFirmPlz(transEvent.getFirmPlz());
        setFirmMunicipality(transEvent.getFirmMunicipality());
        setFlagLbv(transEvent.getFlagLbv());
        setPlausiStatus(transEvent.getPlausiStatus());
        setIsValidated(transEvent.getIsValidated());
        setModUser(transEvent.getModUser());
        setModDate(transEvent.getModDate());

        for (Plausierror plausiError : transEvent.getPlausiErrors()) {
            plausierrors.add(plausiError);
        }
    }

    public SbgEvent(SbgEvent anEvent, List<Plausierror> persistPlausiErrors) {
        setEventid(anEvent.getEventid());
        setPid(anEvent.getPid());
        setCanton(anEvent.getCanton());
        setVersion(anEvent.getVersion());
        setType(anEvent.getType());
        setContractNr(anEvent.getContractNr());
        setProfessionCode(anEvent.getProfessionCode());
        setContractType(anEvent.getContractType());
        setContractDate(anEvent.getContractDate());
        setEducationYear(anEvent.getEducationYear());
        setExamType(anEvent.getExamType());
        setExamNr(anEvent.getExamNr());
        setExamRepetition(anEvent.getExamRepetition());
        setExamResult(anEvent.getExamResult());
        setCancelReason(anEvent.getCancelReason());
        setCancelDate(anEvent.getCancelDate());
        setBurnr(anEvent.getBurnr());
        setKantLbCode(anEvent.getKantLbCode());
        setKeyAspect(anEvent.getKeyAspect());
        setUserComment(anEvent.getUserComment());
        setFirmName(anEvent.getFirmName());
        setFirmStreet(anEvent.getFirmStreet());
        setFirmStreetNr(anEvent.getFirmStreetNr());
        setFirmPlz(anEvent.getFirmPlz());
        setFirmMunicipality(anEvent.getFirmMunicipality());
        setFlagLbv(anEvent.getFlagLbv());
        setPlausiStatus(anEvent.getPlausiStatus());
        setIsValidated(anEvent.getIsValidated());
        setModUser(anEvent.getModUser());
        setModDate(anEvent.getModDate());

        if (plausierrors != null) {
            for (Plausierror plausiError : persistPlausiErrors) {
                plausierrors.add(new Plausierror(plausiError));
            }
        }
    }

    public boolean getIsValidated() {
        return this.isValidated;
    }

    // Specific accessors to keep the interface
    public void setIsValidated(boolean isValidated) {
        this.isValidated = isValidated;
    }

    public boolean getFlagLbv() {
        return this.flagLbv;
    }

    public void setFlagLbv(boolean flagLbv) {
        this.flagLbv = flagLbv;
    }
}
