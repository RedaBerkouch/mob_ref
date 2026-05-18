/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sspserver

 */
package ch.bfs.meb.ssp.server.integration.dto;

import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

import ch.bfs.meb.server.commons.integration.dto.Parameter;

/**
 * Persistence Object for the parameter data table
 * 
 * @author $Author: dzw $
 * @version $Revision: 355 $
 */
@Entity
@Table(name = "SSP_PARAMETERS")
@GenericGenerator(name = "parameterseqgen", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator", parameters = {
        @org.hibernate.annotations.Parameter(name = "sequence_name", value = "SSPSEQ"),
        @org.hibernate.annotations.Parameter(name = "increment_size", value = "100"),
        @org.hibernate.annotations.Parameter(name = "optimizer", value = "pooled-lo") })
public class SspParameter extends Parameter {
    /** Default constructor */
    public SspParameter() {}

    /** Copy from dto */
    public SspParameter(Parameter dtoParameter) {
        setParameterId(dtoParameter.getParameterId());
        setFilterId(dtoParameter.getFilterId());
        setPlausiId(dtoParameter.getPlausiId());
        setExportId(dtoParameter.getExportId());
        setUniqueName(dtoParameter.getUniqueName());
        setName_de(dtoParameter.getName_de());
        setName_fr(dtoParameter.getName_fr());
        setName_it(dtoParameter.getName_it());
        setDefaultValue(dtoParameter.getDefaultValue());
        setParameterOrder(dtoParameter.getParameterOrder());
    }
}
