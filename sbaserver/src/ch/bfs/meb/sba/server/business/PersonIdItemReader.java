/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sbaserver

  $Id: SchoolIdItemReader.java 690 2010-02-11 08:42:58Z dzw $

 */
package ch.bfs.meb.sba.server.business;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.hibernate.SessionFactory;
import org.hibernate.StatelessSession;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemStream;

/**
 * Reader for the school id's of a delivery
 * 
 * @author $Author: dzw $
 * @version $Revision: 690 $
 */
public class PersonIdItemReader implements ItemStream, ItemReader<Long> {
    private SessionFactory _sessionFactory;
    private String _queryString = "";

    List<Long> _personList;
    Iterator<Long> _personIterator;

    /**
     * @param sessionFactory hibernate session factory
     */
    public void setSessionFactory(SessionFactory sessionFactory) {
        _sessionFactory = sessionFactory;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void open(ExecutionContext executionContext) {
        StatelessSession session = _sessionFactory.openStatelessSession();
        _personList = new ArrayList<Long>(session.createQuery(_queryString).list());
        _personIterator = _personList.iterator();
        session.close();
    }

    @Override
    public Long read() {
        if (_personIterator.hasNext()) {
            return _personIterator.next();
        } else {
            return null;
        }
    }

    @Override
    public void update(ExecutionContext executionContext) {}

    @Override
    public void close() {}

    @BeforeStep
    public void setQuery(StepExecution stepExecution) {
        JobExecution jobExecution = stepExecution.getJobExecution();
        Long deliveryId = jobExecution.getJobParameters().getLong("deliveryId");
        _queryString = "select personId from SbaPerson where deliveryId=" + deliveryId + " and isToDelete=0";
    }
}
