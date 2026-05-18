/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sbaserver

  $Id: CsvItemReader.java  14.04.2010 16:11:04 jfu $

 */
package ch.bfs.meb.sba.server.business.delivery.csv;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import ch.bfs.meb.sba.server.business.PersonBO;
import ch.bfs.meb.sba.server.business.QualificationBO;
import ch.bfs.meb.util.StringUtils;

public class CsvItemReader extends org.springframework.batch.item.file.FlatFileItemReader<Object> {
    private boolean loaded = false;
    private List<PersonBO> persons = null;

    @Override
    protected Object doRead() throws Exception {
        if (!loaded) {
            LinkedHashMap<String, PersonBO> cache = new LinkedHashMap<String, PersonBO>();

            Object line = super.doRead();
            while (line != null) {
                if (line instanceof ItemBO) {
                    ItemBO item = (ItemBO) line;
                    if (!item.isEmpty()) {
                        String key = StringUtils.emptyForNull(item.getPersonIdCategory()) + StringUtils.emptyForNull(item.getPersonId())
                                + StringUtils.emptyForNull(item.getSex()) + StringUtils.emptyForNull(item.getDateOfBirth());

                        PersonBO personBO = cache.get(key);
                        if (personBO == null) {
                            personBO = new PersonBO(item);
                            cache.put(key, personBO);
                        }

                        QualificationBO qualificationBO = new QualificationBO(item, personBO);
                        personBO.addQualificationBO(qualificationBO);
                    }
                }
                line = super.doRead();
            }

            persons = new ArrayList<PersonBO>(cache.values());
            setCurrentItemCount(0);
            setMaxItemCount(persons.size());
            loaded = true;
        }

        if (getCurrentItemCount() >= persons.size()) {
            return null;
        } else {
            PersonBO person = persons.get(getCurrentItemCount());
            // remove reference to save memory (the schoolBO gets blown up in the following steps 
            // and doesn't have to exist further after it is written)
            persons.set(getCurrentItemCount(), null);
            return person;
        }
    }
}
