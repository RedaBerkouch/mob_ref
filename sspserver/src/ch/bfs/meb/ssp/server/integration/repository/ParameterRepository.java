/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sspserver

 */
package ch.bfs.meb.ssp.server.integration.repository;

import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.query.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate5.HibernateCallback;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;

import ch.bfs.meb.ssp.server.integration.dto.SspParameter;

/**
 * Repository for SspParameters.
 * 
 * @author $Author: msc $
 * @version $Revision: 305 $
 */
public class ParameterRepository extends HibernateDaoSupport implements IParameterRepository {
    /*
     * (non-Javadoc)
     * 
     * @seech.bfs.meb.ssp.server.integration.repository.IParameterRepository#
     * getParameterById(java.lang.Long)
     */
    @Override
    public SspParameter getParameterById(Long parameterId) {
        return (SspParameter) getHibernateTemplate().get(SspParameter.class, parameterId);
    }

    /*
     * (non-Javadoc)
     * 
     * @seech.bfs.meb.ssp.server.integration.repository.IParameterRepository#
     * getParametersForExport(java.lang.Long)
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<SspParameter> getParametersForExport(final Long exportId) {
        return getHibernateTemplate().execute(session -> {
            Query<SspParameter> query = session.createQuery(
                    "from SspParameter where exportId = :exportId order by parameterOrder", SspParameter.class);
            query.setParameter("exportId", exportId);
            return query.list();
        });
    }

    /*
     * (non-Javadoc)
     * 
     * @seech.bfs.meb.ssp.server.integration.repository.IParameterRepository#
     * getParametersForFilter(java.lang.Long)
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<SspParameter> getParametersForFilter(final Long filterId) {
        return getHibernateTemplate().execute(session -> {
            Query<SspParameter> query = session.createQuery(
                    "from SspParameter where filterId = :filterId order by parameterOrder", SspParameter.class);
            query.setParameter("filterId", filterId);
            return query.list();
        });
    }

    /*
     * (non-Javadoc)
     * 
     * @seech.bfs.meb.ssp.server.integration.repository.IParameterRepository#
     * getParametersForPlausi(java.lang.Long)
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<SspParameter> getParametersForPlausi(final Long plausiId) {
        return getHibernateTemplate().execute(session -> {
            Query<SspParameter> query = session.createQuery(
                    "from SspParameter where plausiId = :plausiId order by parameterOrder", SspParameter.class);
            query.setParameter("plausiId", plausiId);
            return query.list();
        });
    }

    /*
     * (non-Javadoc)
     * 
     * @seech.bfs.meb.ssp.server.integration.repository.IParameterRepository#
     * insertParameter(ch.bfs.meb.ssp.server.integration.dto.SspParameter)
     */
    @Override
    public SspParameter insertParameter(SspParameter parameter) {
        parameter = (SspParameter) getHibernateTemplate().merge(parameter);
        getHibernateTemplate().flush();
        return parameter;
    }

    /*
     * (non-Javadoc)
     * 
     * @seech.bfs.meb.ssp.server.integration.repository.IParameterRepository#
     * updateParameter(ch.bfs.meb.ssp.server.integration.dto.SspParameter)
     */
    @Override
    public SspParameter updateParameter(SspParameter parameter) {
        parameter = (SspParameter) getHibernateTemplate().merge(parameter);
        getHibernateTemplate().flush();
        return parameter;
    }

    /*
     * (non-Javadoc)
     * 
     * @seech.bfs.meb.ssp.server.integration.repository.IParameterRepository#
     * deleteParameter(ch.bfs.meb.ssp.server.integration.dto.SspParameter)
     */
    @Override
    public void deleteParameter(SspParameter parameter) {
        getHibernateTemplate().delete(parameter);
        getHibernateTemplate().flush();
    }
}
