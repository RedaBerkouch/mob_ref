/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sspserver

  $Id: SchoolIdItemReader.java 690 2010-02-11 08:42:58Z dzw $

 */
package ch.admin.bfs.sbg.business;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemStream;
import org.springframework.transaction.annotation.Transactional;

import ch.admin.bfs.sbg.db.dao.PersonDAO;
import ch.admin.bfs.sbg.psist.PersistPerson;
import lombok.Setter;

/**
 * Reader for the school id's of a delivery
 * 
 * @author $Author: dzw $
 * @version $Revision: 690 $
 */
@Transactional
public class PersonIdItemReader implements ItemStream, ItemReader<Long> {

    private long deliveryId;
    private Iterator<Long> personIdIterator;

    @Setter
    private PersonDAO personDAO;

    @Override
    public void open(ExecutionContext executionContext) {

        Set<PersistPerson> persons = personDAO.loadWholeDelivery(deliveryId);
        List<Long> personIdList = persons.stream().filter(person -> !person.getIsToDelete()).map(persistPerson -> persistPerson.getPid())
                .collect(Collectors.toList());
        personIdIterator = personIdList.iterator();
    }

    @Override
    public Long read() {

        if (personIdIterator.hasNext()) {
            return personIdIterator.next();
        }

        return null;
    }

    @Override
    public void update(ExecutionContext executionContext) {}

    @Override
    public void close() {}

    @BeforeStep
    public void setQuery(StepExecution stepExecution) {
        deliveryId = stepExecution.getJobParameters().getLong("deliveryId");
    }
}
