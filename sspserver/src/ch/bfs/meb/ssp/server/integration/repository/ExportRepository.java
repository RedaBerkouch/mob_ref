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
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.transform.DistinctRootEntityResultTransformer;
import org.springframework.orm.hibernate5.HibernateCallback;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Repository;

import ch.bfs.meb.security.MebUser;
import ch.bfs.meb.ssp.server.integration.dto.SspExport;
import ch.bfs.meb.ssp.server.integration.dto.SspParameter;
import ch.bfs.meb.util.SecurityConstants;

@Repository
public class ExportRepository extends HibernateDaoSupport implements IExportRepository {
    protected Long getMaxAuthorisationForActUser() {
        MebUser user = (MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (user.isInRole(SecurityConstants.ROLE_SSP_EA)) {
            return 4L;
        }
        if (user.isInRole(SecurityConstants.ROLE_SSP_EV)) {
            return 3L;
        }
        if (user.isInRole(SecurityConstants.ROLE_SSP_DV)) {
            return 2L;
        }
        if (user.isInRole(SecurityConstants.ROLE_SSP_DL)) {
            return 1L;
        }
        return 0L;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<SspExport> getExports() {
        return getHibernateTemplate().execute(new HibernateCallback<List<SspExport>>() {
            @Override
            public List<SspExport> doInHibernate(Session session) throws HibernateException {
                String hql = "from SspExport se left join fetch se.sspParameters where se.authorisationLevel <= :maxAuthorisationLevel order by se.exportOrder";

                Query<SspExport> query = session.createQuery(hql, SspExport.class);
                query.setParameter("maxAuthorisationLevel", getMaxAuthorisationForActUser());
                query.setResultTransformer(DistinctRootEntityResultTransformer.INSTANCE); // If needed, adjust if using Hibernate 5.2 or later

                return query.getResultList();
            }
        });
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<SspExport> getActiveExports() {
        Session session = currentSession();

        @SuppressWarnings("unchecked")
        List<SspExport> results = session.createQuery(
                        "select distinct se " +
                                "from SspExport as se " +
                                "left join fetch se.sspParameters " +
                                "where se.isActive = true " +
                                "and se.authorisationLevel <= :auth " +
                                "order by se.exportOrder"
                )
                .setParameter("auth", getMaxAuthorisationForActUser())
                .list();

        return results;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<? extends Object> executeGenericQuery(String sqlSource) {
        Query query =currentSession().createNativeQuery (sqlSource);
        return query.list();
    }

    @Override
    public SspExport getExportById(Long exportId) {
        return (SspExport) getHibernateTemplate().get(SspExport.class, exportId);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<SspParameter> getParameters(final Long exportId) {
        return getHibernateTemplate().execute(new HibernateCallback<List<SspParameter>>() {
            @Override
            public List<SspParameter> doInHibernate(Session session) throws HibernateException {
                String hql = "from SspParameter where exportId = :exportId order by parameterOrder";

                Query<SspParameter> query = session.createQuery(hql, SspParameter.class);
                query.setParameter("exportId", exportId);

                return query.getResultList();
            }
        });
    }

    @Override
    public SspExport insertExport(SspExport export) {
        export = (SspExport) getHibernateTemplate().merge(export);
        getHibernateTemplate().flush();
        return export;
    }

    @Override
    public SspExport updateExport(SspExport export) {
        export = (SspExport) getHibernateTemplate().merge(export);
        getHibernateTemplate().flush();
        return export;
    }

    @Override
    public void deleteExport(SspExport export) {
        getHibernateTemplate().delete(export);
        getHibernateTemplate().flush();
    }
}