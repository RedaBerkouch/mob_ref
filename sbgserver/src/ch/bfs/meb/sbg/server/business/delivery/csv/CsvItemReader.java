/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sbgserver

  $Id: CsvItemReader.java  14.04.2010 16:11:04 jfu $

 */
package ch.bfs.meb.sbg.server.business.delivery.csv;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import ch.admin.bfs.sbg.business.*;
import ch.bfs.meb.util.StringUtils;

public class CsvItemReader extends org.springframework.batch.item.file.FlatFileItemReader<Object> {
    private static final char PERSON = 'P';
    private static final char CONTRACT = 'C';
    private static final char ONGOING_EDUCATION = 'L';
    private static final char EXAM = 'E';
    private static final char CANCELLATION = 'A';

    private boolean loaded = false;
    private List<PersonBO> persons = null;
    private Long _deliveryId;
    private Long _canton;
    private Long _year;

    public void setDeliveryId(Long deliveryId) {
        _deliveryId = deliveryId;
    }

    public void setCanton(Long canton) {
        _canton = canton;
    }

    public void setYear(Long year) {
        _year = year;
    }

    @Override
    protected Object doRead() throws Exception {
        if (!loaded) {
            LinkedHashMap<String, PersonBO> cache = new LinkedHashMap<String, PersonBO>();

            Object line = super.doRead();
            while (line != null) {
                if (line instanceof ItemBO) {
                    ItemBO item = (ItemBO) line;
                    if (!item.isEmpty()) {
                        String key = StringUtils.emptyForNull(item.getPersonId());

                        if (!key.equals("")) {
                            PersonBO person = cache.get(key);
                            if (person == null) {
                                person = new PersonBO(key, _deliveryId, _year, _canton);
                                cache.put(key, person);
                            }

                            if (item.getType() != null && item.getType().length() > 0) {
                                switch (item.getType().charAt(0)) {
                                case PERSON:
                                    person.setCsvData(item);
                                    break;
                                case CONTRACT:
                                    ContractBO contract = new ContractBO(item, person);
                                    person.addEvent(contract);
                                    break;
                                case ONGOING_EDUCATION:
                                    OngoingEducationBO education = new OngoingEducationBO(item, person);
                                    person.addEvent(education);
                                    break;
                                case EXAM:
                                    ExamBO exam = new ExamBO(item, person);
                                    person.addEvent(exam);
                                    break;
                                case CANCELLATION:
                                    CancellationBO cancellation = new CancellationBO(item, person);
                                    person.addEvent(cancellation);
                                    break;
                                }
                            } else {
                                //TODO: Exception?
                            }
                        }
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
            // remove reference to save memory (the personBO gets blown up in the following steps 
            // and doesn't have to exist further after it is written)
            persons.set(getCurrentItemCount(), null);
            return person;
        }
    }
}
