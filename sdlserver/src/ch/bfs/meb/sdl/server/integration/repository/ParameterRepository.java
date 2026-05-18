/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sdlserver

 */
package ch.bfs.meb.sdl.server.integration.repository;

import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.query.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate5.HibernateCallback;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;

import ch.bfs.meb.sdl.server.integration.dto.SdlParameter;

/**
 * Repository for SdlParameters.
 * 
 * @author $Author$
 * @version $Revision$
 */
public class ParameterRepository extends HibernateDaoSupport implements IParameterRepository {
    /*
     * (non-Javadoc)
     * 
     * @seech.bfs.meb.sdl.server.integration.repository.IParameterRepository#
     * getParameterById(java.lang.Long)
     */
    @Override
    public SdlParameter getParameterById(Long parameterId) {
        return (SdlParameter) getHibernateTemplate().get(SdlParameter.class, parameterId);
    }

    /*
     * (non-Javadoc)
     * 
     * @seech.bfs.meb.sdl.server.integration.repository.IParameterRepository#
     * getParametersForExport(java.lang.Long)
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<SdlParameter> getParametersForExport(final Long exportId) {
        return (List<SdlParameter>) getHibernateTemplate().execute(new HibernateCallback() {

            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                org.hibernate.query.Query query = session.createQuery("from SdlParameter where exportId = :exportId order by parameterOrder");
                query.setParameter("exportId", exportId);
                return query.list();
            }
        });
    }

    /*
     * (non-Javadoc)
     * 
     * @seech.bfs.meb.sdl.server.integration.repository.IParameterRepository#
     * getParametersForFilter(java.lang.Long)
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<SdlParameter> getParametersForFilter(final Long filterId) {
        return (List<SdlParameter>) getHibernateTemplate().execute(new HibernateCallback() {

            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                org.hibernate.query.Query query = session.createQuery("from SdlParameter where filterId=:filterId order by parameterOrder");
                query.setParameter("filterId", filterId);
                return query.list();
            }
        });
    }

    /*
     * (non-Javadoc)
     * 
     * @seech.bfs.meb.sdl.server.integration.repository.IParameterRepository#
     * getParametersForPlausi(java.lang.Long)
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<SdlParameter> getParametersForPlausi(final Long plausiId) {
        return (List<SdlParameter>) getHibernateTemplate().execute(new HibernateCallback() {

            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                org.hibernate.query.Query query = session.createQuery("from SdlParameter where plausiId = :plausiId order by parameterOrder");
                query.setParameter("plausiId", plausiId);
                return query.list();
            }
        });
    }

    /*
     * (non-Javadoc)
     * 
     * @seech.bfs.meb.sdl.server.integration.repository.IParameterRepository#
     * insertParameter(ch.bfs.meb.sdl.server.integration.dto.SdlParameter)
     */
    @Override
    public SdlParameter insertParameter(SdlParameter parameter) {
        parameter = (SdlParameter) getHibernateTemplate().merge(parameter);
        getHibernateTemplate().flush();
        return parameter;
    }

    /*
     * (non-Javadoc)
     * 
     * @seech.bfs.meb.sdl.server.integration.repository.IParameterRepository#
     * updateParameter(ch.bfs.meb.sdl.server.integration.dto.SdlParameter)
     */
    @Override
    public SdlParameter updateParameter(SdlParameter parameter) {
        parameter = (SdlParameter) getHibernateTemplate().merge(parameter);
        getHibernateTemplate().flush();
        return parameter;
    }

    /*
     * (non-Javadoc)
     * 
     * @seech.bfs.meb.sdl.server.integration.repository.IParameterRepository#
     * deleteParameter(ch.bfs.meb.sdl.server.integration.dto.SdlParameter)
     */
    @Override
    public void deleteParameter(SdlParameter parameter) {
        getHibernateTemplate().delete(parameter);
        getHibernateTemplate().flush();
    }
}
