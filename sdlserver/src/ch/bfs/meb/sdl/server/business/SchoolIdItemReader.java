/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sdlserver

  $Id$

 */
package ch.bfs.meb.sdl.server.business;

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
 * @author $Author$
 * @version $Revision$
 */
public class SchoolIdItemReader implements ItemStream, ItemReader<Long> {
    private SessionFactory _sessionFactory;
    private String _queryString = "";

    List<Long> _schoolList;
    Iterator<Long> _schoolIterator;

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
        _schoolList = new ArrayList<Long>(session.createQuery(_queryString).list());
        _schoolIterator = _schoolList.iterator();
        session.close();
    }

    @Override
    public Long read() {
        if (_schoolIterator.hasNext()) {
            return _schoolIterator.next();
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
        _queryString = "select schoolId from SdlSchool where deliveryId=" + deliveryId + " and isToDelete=0";
    }
}
