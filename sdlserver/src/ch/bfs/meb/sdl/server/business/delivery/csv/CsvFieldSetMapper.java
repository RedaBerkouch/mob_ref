/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sdlserver

  $Id: CsvFieldSetMapper.java  09.02.2010 09:30:23 jfu $

 */
package ch.bfs.meb.sdl.server.business.delivery.csv;

import org.springframework.batch.item.file.transform.FieldSet;

public class CsvFieldSetMapper implements org.springframework.batch.item.file.mapping.FieldSetMapper<ItemBO> {
    @Override
    public ItemBO mapFieldSet(FieldSet fieldSet) {
        return new ItemBO(fieldSet);
    }
}