/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: server-commons

 */
package ch.bfs.meb.server.commons.integration.dto;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.*;

/**
 * Data Transfer Object for the plausi data table
 */
@MappedSuperclass
public class Plausi {
    // Fields
    private Long _plausiId;
    private String _id;
    private Long _type;
    private String _name_de;
    private String _name_fr;
    private String _name_it;
    private String _description_de;
    private String _description_fr;
    private String _description_it;
    private String _source;
    private Long _objectLevel;
    private boolean _isActive;
    private boolean _isConfirmable;
    private Long _validFrom;
    private Long _validTo;
    private Long _plausiOrder;

    // Property accessors
    @Id
    @Column(name = "PLAUSIID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "plausiseqgen")
    public Long getPlausiId() {
        return _plausiId;
    }

    public void setPlausiId(Long plausiId) {
        _plausiId = plausiId;
    }

    @Column
    public String getId() {
        return _id;
    }

    public void setId(String id) {
        _id = id;
    }

    @Column
    public Long getType() {
        return _type;
    }

    public void setType(Long type) {
        _type = type;
    }

    @Column
    public String getDescription_de() {
        return _description_de;
    }

    public void setDescription_de(String description_de) {
        _description_de = description_de;
    }

    @Column
    public String getDescription_fr() {
        return _description_fr;
    }

    public void setDescription_fr(String description_fr) {
        _description_fr = description_fr;
    }

    @Column
    public String getDescription_it() {
        return _description_it;
    }

    public void setDescription_it(String description_it) {
        _description_it = description_it;
    }

    @Column
    public String getName_de() {
        return _name_de;
    }

    public void setName_de(String name_de) {
        _name_de = name_de;
    }

    @Column
    public String getName_fr() {
        return _name_fr;
    }

    public void setName_fr(String name_fr) {
        _name_fr = name_fr;
    }

    @Column
    public String getName_it() {
        return _name_it;
    }

    public void setName_it(String name_it) {
        _name_it = name_it;
    }

    @Column
    public String getSource() {
        return _source;
    }

    public void setSource(String source) {
        _source = source;
    }

    @Column
    public Long getObjectLevel() {
        return _objectLevel;
    }

    public void setObjectLevel(Long objectLevel) {
        _objectLevel = objectLevel;
    }

    @Transient
    public List<Parameter> getParameters() {
        return new ArrayList<Parameter>();
    }

    public void setParameters(List<Parameter> parameters) {}

    @Column
    public boolean getIsActive() {
        return _isActive;
    }

    public void setIsActive(boolean isActive) {
        _isActive = isActive;
    }

    @Column
    public boolean getIsConfirmable() {
        return _isConfirmable;
    }

    public void setIsConfirmable(boolean isConfirmable) {
        _isConfirmable = isConfirmable;
    }

    @Column
    public Long getValidFrom() {
        return _validFrom;
    }

    public void setValidFrom(Long validFrom) {
        _validFrom = validFrom;
    }

    @Column
    public Long getValidTo() {
        return _validTo;
    }

    public void setValidTo(Long validTo) {
        _validTo = validTo;
    }

    @Column
    public Long getPlausiOrder() {
        return _plausiOrder;
    }

    public void setPlausiOrder(Long plausiOrder) {
        _plausiOrder = plausiOrder;
    }
}
