/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sbaserver

 */
package ch.bfs.meb.sba.server.integration.repository;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.query.NativeQuery;
import org.hibernate.transform.DistinctRootEntityResultTransformer;
import org.hibernate.transform.Transformers;
import org.springframework.orm.hibernate5.HibernateCallback;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Repository;

import ch.bfs.meb.sba.server.integration.dto.SbaFilter;
import ch.bfs.meb.sba.server.integration.dto.SbaParameter;
import ch.bfs.meb.security.MebUser;
import ch.bfs.meb.util.CodegroupUtility;
import ch.bfs.meb.util.SecurityConstants;

@Repository
public class FilterRepository extends HibernateDaoSupport implements IFilterRepository {
    @Override
    public SbaFilter getFilterById(Long filterId) {
        return (SbaFilter) getHibernateTemplate().get(SbaFilter.class, filterId);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<SbaFilter> getFilters() {
        return (List<SbaFilter>) getHibernateTemplate().execute(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                Query query = session.createQuery("from SbaFilter as sf left join fetch sf.sbaParameters order by sf.filterOrder");
                query.setResultTransformer(DistinctRootEntityResultTransformer.INSTANCE);
                return query.list();
            }
        });
    }

    protected Long getMaxAuthorisationForActUser() {
        MebUser user = (MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (user.isInRole(SecurityConstants.ROLE_SBA_EA)) {
            return SecurityConstants.ROLE_EA;
        }
        if (user.isInRole(SecurityConstants.ROLE_SBA_EV)) {
            return SecurityConstants.ROLE_EV;
        }
        if (user.isInRole(SecurityConstants.ROLE_SBA_DV)) {
            return SecurityConstants.ROLE_DV;
        }
        if (user.isInRole(SecurityConstants.ROLE_SBA_DL)) {
            return SecurityConstants.ROLE_DL;
        }
        return SecurityConstants.ROLE_RO;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<SbaFilter> getActiveFiltersForRefObjectAndNameDe(final Long refObject, final String nameDe) {
        return (List<SbaFilter>) getHibernateTemplate().execute(session -> {
            String queryString = "select distinct sf from SbaFilter as sf left join fetch sf.sbaParameters " +
                    "where sf.isActive = true " +
                    "and sf.authorisationLevel <= :maxAuthForActUser " +
                    "and sf.refObject=:refObject " +
                    "and sf.name_de=:nameDe" +
                    " order by sf.filterOrder";

            org.hibernate.query.Query<SbaFilter> query = session.createQuery(queryString);
            query.setParameter("maxAuthForActUser", getMaxAuthorisationForActUser());
            query.setParameter("refObject", refObject);
            query.setParameter("nameDe", nameDe);


            return query.getResultList();
        });
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<SbaFilter> getActiveFiltersForRefObject(final Long refObject) {
        return (List<SbaFilter>) getHibernateTemplate().execute(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                String queryString = "select distinct sf from SbaFilter as sf left join fetch sf.sbaParameters " +
                        "where sf.isActive = true " +
                        "and sf.authorisationLevel <= :maxAuthForActUser " +
                        "and sf.refObject=:refObject " +
                        "order by sf.filterOrder";
                org.hibernate.query.Query<SbaFilter> query = session.createQuery(queryString, SbaFilter.class);
                query.setParameter("maxAuthForActUser", getMaxAuthorisationForActUser());
                query.setParameter("refObject", refObject);
                return query.getResultList();
            }
        });
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<SbaParameter> getParameters(final Long filterId) {
        return (List<SbaParameter>) getHibernateTemplate().execute(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                org.hibernate.query.Query<SbaParameter> query = session.createQuery("from SbaParameter where filterId=:filterId order by parameterOrder", SbaParameter.class);
                query.setParameter("filterId", filterId);
                return query.getResultList();
            }
        });
    }

    @Override
    public SbaFilter insertFilter(SbaFilter filter) {
        filter = (SbaFilter) getHibernateTemplate().merge(filter);
        getHibernateTemplate().flush();
        return filter;
    }

    @Override
    public SbaFilter updateFilter(SbaFilter filter) {
        filter = (SbaFilter) getHibernateTemplate().merge(filter);
        getHibernateTemplate().flush();
        return filter;
    }

    @Override
    public void deleteFilter(SbaFilter filter) {
        getHibernateTemplate().delete(filter);
        getHibernateTemplate().flush();
    }

    public Long getActVersion() {
        try {
            List<SbaFilter> filters = getActiveFiltersForRefObjectAndNameDe(CodegroupUtility.SBA_OBJECTTYPE_CONFIGURATION,
                    CodegroupUtility.MEB_FILTER_ACT_VERSION);
            if (filters.size() > 0) {
                return Long.parseLong(filters.get(0).getSource());
            }
        } catch (Exception e) {
            // nothing to do
        }

        return new Long(new GregorianCalendar().get(Calendar.YEAR));
    }

    public Long getInitVersion() {
        try {
            List<SbaFilter> filters = getActiveFiltersForRefObjectAndNameDe(CodegroupUtility.SBA_OBJECTTYPE_CONFIGURATION,
                    CodegroupUtility.MEB_FILTER_INIT_VERSION);
            if (filters.size() > 0) {
                return Long.parseLong(filters.get(0).getSource());
            }
        } catch (Exception e) {
            // nothing to do
        }

        return new Long(new GregorianCalendar().get(Calendar.YEAR));
    }
}
