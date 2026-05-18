/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sbgserver

  $Id: PersonData.java  30.03.2012 21:05:41 Administrator $

 */
package ch.admin.bfs.sbg.util;

import java.util.ArrayList;
import java.util.List;

public class PersonData {
    private Long persId = null;

    private final List<String[]> persons = new ArrayList<String[]>();
    private final List<String[]> persContracts = new ArrayList<String[]>();
    private final List<String[]> persEducations = new ArrayList<String[]>();
    private final List<String[]> persExams = new ArrayList<String[]>();
    private final List<String[]> persCancellations = new ArrayList<String[]>();

    public static final Long DUMMY_PERS_ID = new Long(-1);

    /* Accessors */
    public Long getPersId() {
        return persId;
    }

    public void setPersId(Long persId) {
        this.persId = persId;
    }

    public List<String[]> getPersons() {
        return persons;
    }

    public void addPerson(String[] person) {
        this.persons.add(person);
    }

    public List<String[]> getPersContracts() {
        return persContracts;
    }

    public void addPersContract(String[] persContract) {
        this.persContracts.add(persContract);
    }

    public List<String[]> getPersEducations() {
        return persEducations;
    }

    public void addPersEducation(String[] persEducation) {
        this.persEducations.add(persEducation);
    }

    public List<String[]> getPersExams() {
        return persExams;
    }

    public void addPersExam(String[] persExam) {
        this.persExams.add(persExam);
    }

    public List<String[]> getPersCancellations() {
        return persCancellations;
    }

    public void addPersCancellation(String[] persCancellation) {
        this.persCancellations.add(persCancellation);
    }

}
