/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sbaserver

 */
package ch.bfs.meb.sba.server.integration.repository;

import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.springframework.orm.hibernate5.HibernateCallback;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;

import ch.bfs.meb.sba.server.integration.dto.SbaParameter;

/**
 * Repository for SbaParameters.
 * 
 * @author $Author: msc $
 * @version $Revision: 305 $
 */
public class ParameterRepository extends HibernateDaoSupport implements IParameterRepository {
    /*
     * (non-Javadoc)
     * 
     * @seech.bfs.meb.sba.server.integration.repository.IParameterRepository#
     * getParameterById(java.lang.Long)
     */
    @Override
    public SbaParameter getParameterById(Long parameterId) {
        return (SbaParameter) getHibernateTemplate().get(SbaParameter.class, parameterId);
    }

    /*
     * (non-Javadoc)
     * 
     * @seech.bfs.meb.sba.server.integration.repository.IParameterRepository#
     * getParametersForExport(java.lang.Long)
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<SbaParameter> getParametersForExport(final Long exportId) {
        return (List<SbaParameter>) getHibernateTemplate().execute(new HibernateCallback() {

            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                org.hibernate.query.Query<SbaParameter> query = session.createQuery("from SbaParameter where exportId=:exportId order by parameterOrder", SbaParameter.class);
                query.setParameter("exportId", exportId);
                return query.getResultList();
            }
        });
    }

    /*
     * (non-Javadoc)
     * 
     * @seech.bfs.meb.sba.server.integration.repository.IParameterRepository#
     * getParametersForFilter(java.lang.Long)
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<SbaParameter> getParametersForFilter(final Long filterId) {
        return (List<SbaParameter>) getHibernateTemplate().execute(new HibernateCallback() {

            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                org.hibernate.query.Query<SbaParameter> query = session.createQuery("from SbaParameter where filterId=:filterId order by parameterOrder", SbaParameter.class);
                query.setParameter("filterId", filterId);
                return query.getResultList();
            }
        });
    }

    /*
     * (non-Javadoc)
     * 
     * @seech.bfs.meb.sba.server.integration.repository.IParameterRepository#
     * getParametersForPlausi(java.lang.Long)
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<SbaParameter> getParametersForPlausi(final Long plausiId) {
        return (List<SbaParameter>) getHibernateTemplate().execute(new HibernateCallback() {

            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                Query<SbaParameter> query = session.createQuery("from SbaParameter where plausiId=:plausiId order by parameterOrder", SbaParameter.class);
                query.setParameter("plausiId", plausiId);
                return query.getResultList();
            }
        });
    }

    /*
     * (non-Javadoc)
     * 
     * @seech.bfs.meb.sba.server.integration.repository.IParameterRepository#
     * insertParameter(ch.bfs.meb.sba.server.integration.dto.SbaParameter)
     */
    @Override
    public SbaParameter insertParameter(SbaParameter parameter) {
        parameter = (SbaParameter) getHibernateTemplate().merge(parameter);
        getHibernateTemplate().flush();
        return parameter;
    }

    /*
     * (non-Javadoc)
     * 
     * @seech.bfs.meb.sba.server.integration.repository.IParameterRepository#
     * updateParameter(ch.bfs.meb.sba.server.integration.dto.SbaParameter)
     */
    @Override
    public SbaParameter updateParameter(SbaParameter parameter) {
        parameter = (SbaParameter) getHibernateTemplate().merge(parameter);
        getHibernateTemplate().flush();
        return parameter;
    }

    /*
     * (non-Javadoc)
     * 
     * @seech.bfs.meb.sba.server.integration.repository.IParameterRepository#
     * deleteParameter(ch.bfs.meb.sba.server.integration.dto.SbaParameter)
     */
    @Override
    public void deleteParameter(SbaParameter parameter) {
        getHibernateTemplate().delete(parameter);
        getHibernateTemplate().flush();
    }
}
