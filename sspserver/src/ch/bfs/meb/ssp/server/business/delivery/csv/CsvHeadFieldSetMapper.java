/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sspserver

  $Id: CsvHeadFieldSetMapper.java  09.02.2010 09:30:23 jfu $

 */
package ch.bfs.meb.ssp.server.business.delivery.csv;

import org.springframework.batch.item.file.transform.FieldSet;

import ch.bfs.meb.ssp.server.business.DeliveryBO;

public class CsvHeadFieldSetMapper implements org.springframework.batch.item.file.mapping.FieldSetMapper<DeliveryBO> {
    @Override
    public DeliveryBO mapFieldSet(FieldSet fieldSet) {
        return new DeliveryBO(fieldSet);
    }
}