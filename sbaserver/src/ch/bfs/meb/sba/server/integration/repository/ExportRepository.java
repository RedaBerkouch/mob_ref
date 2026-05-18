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
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.transform.DistinctRootEntityResultTransformer;
import org.springframework.orm.hibernate5.HibernateCallback;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Repository;

import ch.bfs.meb.sba.server.integration.dto.SbaExport;
import ch.bfs.meb.sba.server.integration.dto.SbaParameter;
import ch.bfs.meb.security.MebUser;
import ch.bfs.meb.util.SecurityConstants;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Root;

@Repository
public class ExportRepository extends HibernateDaoSupport implements IExportRepository {
    protected Long getMaxAuthorisationForActUser() {
        MebUser user = (MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (user.isInRole(SecurityConstants.ROLE_SBA_EA)) {
            return 4L;
        }
        if (user.isInRole(SecurityConstants.ROLE_SBA_EV)) {
            return 3L;
        }
        if (user.isInRole(SecurityConstants.ROLE_SBA_DV)) {
            return 2L;
        }
        if (user.isInRole(SecurityConstants.ROLE_SBA_DL)) {
            return 1L;
        }
        return 0L;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<SbaExport> getExports() {
        return (List<SbaExport>) getHibernateTemplate().execute(new HibernateCallback() {

            @Override
            public Object doInHibernate(Session session) throws HibernateException {

                CriteriaBuilder builder = session.getCriteriaBuilder();
                CriteriaQuery<SbaExport> cq = builder.createQuery(SbaExport.class);
                Root<SbaExport> root = cq.from(SbaExport.class);

                root.fetch("sbaParameters", JoinType.LEFT);

                cq.select(root).distinct(true); //  IMPORTANT

                cq.where(builder.lessThanOrEqualTo(
                        root.get("authorisationLevel"),
                        getMaxAuthorisationForActUser()
                ));

                cq.orderBy(builder.asc(root.get("exportOrder")));

                List<SbaExport> sbaExports = session.createQuery(cq).getResultList();
                return sbaExports;

            }
        });
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<SbaExport> getActiveExports() {
        return (List<SbaExport>) getHibernateTemplate().execute(new HibernateCallback() {

            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                Query<SbaExport> query = session.createQuery(
                        "from SbaExport as se left join fetch se.sbaParameters where se.isActive = 1 and se.authorisationLevel <= :authLevel order by se.exportOrder", SbaExport.class);
                query.setParameter("authLevel", getMaxAuthorisationForActUser());
                query.setResultTransformer(DistinctRootEntityResultTransformer.INSTANCE);
                return query.list();
            }
        });
    }


    @SuppressWarnings("unchecked")
    @Override
    public List<? extends Object> executeGenericQuery(String sqlSource) {
        Query query = currentSession().createNativeQuery (sqlSource);
        return query.list();
    }

    @Override
    public SbaExport getExportById(Long exportId) {
        return (SbaExport) getHibernateTemplate().get(SbaExport.class, exportId);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<SbaParameter> getParameters(final Long exportId) {
        return (List<SbaParameter>) getHibernateTemplate().execute(new HibernateCallback() {

            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                org.hibernate.query.Query<SbaParameter> query = session.createQuery("from SbaParameter where exportId=:exportId order by parameterOrder", SbaParameter.class);
                query.setParameter("exportId", exportId);
                return query.list();
            }
        });
    }

    @Override
    public SbaExport insertExport(SbaExport export) {
        export = (SbaExport) getHibernateTemplate().merge(export);
        getHibernateTemplate().flush();
        return export;
    }

    @Override
    public SbaExport updateExport(SbaExport export) {
        export = (SbaExport) getHibernateTemplate().merge(export);
        getHibernateTemplate().flush();
        return export;
    }

    @Override
    public void deleteExport(SbaExport export) {
        getHibernateTemplate().delete(export);
        getHibernateTemplate().flush();
    }
}