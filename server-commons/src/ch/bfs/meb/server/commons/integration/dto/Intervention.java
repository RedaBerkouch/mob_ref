/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: server-commons

 */
package ch.bfs.meb.server.commons.integration.dto;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.persistence.*;

/**
 * Data Transfer Object for the intervention data table
 */
@MappedSuperclass
public class Intervention {
    // Fields
    private Long _interventionId;
    private Long _deliveryId;
    private Long _type;
    private String _intervention_user;
    private Date _intervention_date;
    private String _report_de;
    private String _report_fr;
    private String _report_it;
    private String _text;

    // transient fields
    private Long _canton;
    private Long _version;

    // Property accessors
    @Id
    @Column(name = "INTERVENTIONID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "interventionseqgen")
    public Long getInterventionId() {
        return _interventionId;
    }

    public void setInterventionId(Long interventionId) {
        _interventionId = interventionId;
    }

    /**
     * @return the _deliveryId
     */
    @Column
    public Long getDeliveryId() {
        return _deliveryId;
    }

    /**
     * @param deliveryId the _deliveryId to set
     */
    public void setDeliveryId(Long deliveryId) {
        _deliveryId = deliveryId;
    }

    /**
     * @return the _type
     */
    public Long getType() {
        return _type;
    }

    /**
     * @param type the _type to set
     */
    public void setType(Long type) {
        _type = type;
    }

    /**
     * @return the _intervention_user
     */
    public String getIntervention_user() {
        return _intervention_user;
    }

    /**
     * @param interventionUser the _intervention_user to set
     */
    public void setIntervention_user(String interventionUser) {
        _intervention_user = interventionUser;
    }

    /**
     * @return the _intervention_date
     */
    public Date getIntervention_date() {
        return _intervention_date;
    }

    /**
     * @param interventionDate the _intervention_date to set
     */
    public void setIntervention_date(Date interventionDate) {
        _intervention_date = interventionDate;
    }

    /**
     * @return the _report_de
     */
    public String getReport_de() {
        return _report_de;
    }

    /**
     * @param reportDe the _report_de to set
     */
    public void setReport_de(String reportDe) {
        _report_de = reportDe;
    }

    /**
     * @return the _report_fr
     */
    public String getReport_fr() {
        return _report_fr;
    }

    /**
     * @param reportFr the _report_fr to set
     */
    public void setReport_fr(String reportFr) {
        _report_fr = reportFr;
    }

    /**
     * @return the _report_it
     */
    public String getReport_it() {
        return _report_it;
    }

    /**
     * @param reportIt the _report_it to set
     */
    public void setReport_it(String reportIt) {
        _report_it = reportIt;
    }

    /**
     * @return the _text
     */
    public String getText() {
        return _text;
    }

    /**
     * @param text the _text to set
     */
    public void setText(String text) {
        _text = text;
    }

    @Transient
    public Long getCanton() {
        return this._canton;
    }

    public void setCanton(Long canton) {
        this._canton = canton;
    }

    @Transient
    public Long getVersion() {
        return this._version;
    }

    public void setVersion(Long version) {
        this._version = version;
    }

    @Transient
    protected byte[] zip(byte[] content, String filename) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ZipOutputStream zipOut = new ZipOutputStream(os);
        zipOut.putNextEntry(new ZipEntry(filename));
        zipOut.write(content);
        zipOut.closeEntry();
        zipOut.close();
        return os.toByteArray();
    }
}