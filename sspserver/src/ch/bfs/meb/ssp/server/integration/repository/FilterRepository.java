/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sspserver

 */
package ch.bfs.meb.ssp.server.integration.repository;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.query.Query;
import org.hibernate.Session;
import org.hibernate.transform.DistinctRootEntityResultTransformer;
import org.springframework.orm.hibernate5.HibernateCallback;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Repository;

import ch.bfs.meb.security.MebUser;
import ch.bfs.meb.ssp.server.integration.dto.SspFilter;
import ch.bfs.meb.ssp.server.integration.dto.SspParameter;
import ch.bfs.meb.util.CodegroupUtility;
import ch.bfs.meb.util.SecurityConstants;

@Repository
public class FilterRepository extends HibernateDaoSupport implements IFilterRepository {
    @Override
    public SspFilter getFilterById(Long filterId) {
        return (SspFilter) getHibernateTemplate().get(SspFilter.class, filterId);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<SspFilter> getFilters() {
        return (List<SspFilter>) getHibernateTemplate().execute(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                Query query = session.createQuery("from SspFilter as sf left join fetch sf.sspParameters order by sf.filterOrder");
                query.setResultTransformer(DistinctRootEntityResultTransformer.INSTANCE);
                return query.list();
            }
        });
    }

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
    public List<SspFilter> getActiveFiltersForRefObjectAndNameDe(final Long refObject, final String nameDe) {
        return getHibernateTemplate().execute(new HibernateCallback<List<SspFilter>>() {
            @Override
            public List<SspFilter> doInHibernate(Session session) throws HibernateException {
                String hql = "select distinct sf from SspFilter sf left join fetch sf.sspParameters " +
                        "where sf.isActive = true " +
                        "and sf.authorisationLevel <= :maxAuthorisationLevel " +
                        "and sf.refObject = :refObject " +
                        "and sf.name_de = :nameDe " +
                        "order by sf.filterOrder";

                Query<SspFilter> query = session.createQuery(hql, SspFilter.class);
                query.setParameter("maxAuthorisationLevel", getMaxAuthorisationForActUser());
                query.setParameter("refObject", refObject);
                query.setParameter("nameDe", nameDe);

                return query.getResultList();
            }
        });
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<SspFilter> getActiveFiltersForRefObject(final Long refObject) {
        return getHibernateTemplate().execute(new HibernateCallback<List<SspFilter>>() {
            @Override
            public List<SspFilter> doInHibernate(Session session) throws HibernateException {
                String hql = "select distinct sf from SspFilter sf left join fetch sf.sspParameters " +
                        "where sf.isActive = true " +
                        "and sf.authorisationLevel <= :maxAuthorisationLevel " +
                        "and sf.refObject = :refObject " +
                        "order by sf.filterOrder";

                Query<SspFilter> query = session.createQuery(hql, SspFilter.class);
                query.setParameter("maxAuthorisationLevel", getMaxAuthorisationForActUser());
                query.setParameter("refObject", refObject);

                return query.getResultList();
            }
        });
    }


    @SuppressWarnings("unchecked")
    @Override
    public List<SspParameter> getParameters(final Long filterId) {
        return getHibernateTemplate().execute(new HibernateCallback<List<SspParameter>>() {
            @Override
            public List<SspParameter> doInHibernate(Session session) throws HibernateException {
                String hql = "from SspParameter where filterId = :filterId order by parameterOrder";

                Query<SspParameter> query = session.createQuery(hql, SspParameter.class);
                query.setParameter("filterId", filterId);

                return query.getResultList();
            }
        });
    }

    @Override
    public SspFilter insertFilter(SspFilter filter) {
        filter = (SspFilter) getHibernateTemplate().merge(filter);
        getHibernateTemplate().flush();
        return filter;
    }

    @Override
    public SspFilter updateFilter(SspFilter filter) {
        filter = (SspFilter) getHibernateTemplate().merge(filter);
        getHibernateTemplate().flush();
        return filter;
    }

    @Override
    public void deleteFilter(SspFilter filter) {
        getHibernateTemplate().delete(filter);
        getHibernateTemplate().flush();
    }

    public Long getActVersion() {
        try {
            List<SspFilter> filters = getActiveFiltersForRefObjectAndNameDe(CodegroupUtility.SSP_OBJECTTYPE_CONFIGURATION,
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
            List<SspFilter> filters = getActiveFiltersForRefObjectAndNameDe(CodegroupUtility.SSP_OBJECTTYPE_CONFIGURATION,
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
