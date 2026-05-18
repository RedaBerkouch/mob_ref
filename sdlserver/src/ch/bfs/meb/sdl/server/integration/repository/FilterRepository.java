/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sdlserver

 */
package ch.bfs.meb.sdl.server.integration.repository;

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

import ch.bfs.meb.sdl.server.integration.dto.SdlFilter;
import ch.bfs.meb.sdl.server.integration.dto.SdlParameter;
import ch.bfs.meb.security.MebUser;
import ch.bfs.meb.util.CodegroupUtility;
import ch.bfs.meb.util.SecurityConstants;

@Repository
public class FilterRepository extends HibernateDaoSupport implements IFilterRepository {
    @Override
    public SdlFilter getFilterById(Long filterId) {
        return (SdlFilter) getHibernateTemplate().get(SdlFilter.class, filterId);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<SdlFilter> getFilters() {
        return (List<SdlFilter>) getHibernateTemplate().execute(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                Query query = session.createQuery("from SdlFilter as sf left join fetch sf.sdlParameters order by sf.filterOrder");
                query.setResultTransformer(DistinctRootEntityResultTransformer.INSTANCE);
                return query.list();
            }
        });
    }

    protected Long getMaxAuthorisationForActUser() {
        MebUser user = (MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (user.isInRole(SecurityConstants.ROLE_SDL_EA)) {
            return 4L;
        }
        if (user.isInRole(SecurityConstants.ROLE_SDL_EV)) {
            return 3L;
        }
        if (user.isInRole(SecurityConstants.ROLE_SDL_DV)) {
            return 2L;
        }
        if (user.isInRole(SecurityConstants.ROLE_SDL_DL)) {
            return 1L;
        }
        return 0L;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<SdlFilter> getActiveFiltersForRefObjectAndNameDe(final Long refObject, final String nameDe) {
        return (List<SdlFilter>) getHibernateTemplate().execute(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                org.hibernate.query.Query<SdlFilter> query = session.createQuery(
                        "from SdlFilter as sf left join fetch sf.sdlParameters where sf.isActive = true and sf.authorisationLevel <= :maxAuthForActUser and sf.refObject= :refObject and sf.name_de= :nameDe order by sf.filterOrder",
                        SdlFilter.class);
                query.setParameter("maxAuthForActUser", getMaxAuthorisationForActUser());
                query.setParameter("refObject", refObject);
                query.setParameter("nameDe", nameDe);
                query.setResultTransformer(DistinctRootEntityResultTransformer.INSTANCE);
                return query.list();
            }
        });
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<SdlFilter> getActiveFiltersForRefObject(final Long refObject) {
        return (List<SdlFilter>) getHibernateTemplate().execute(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                org.hibernate.query.Query<SdlFilter> query = session.createQuery(
                        "from SdlFilter as sf left join fetch sf.sdlParameters where sf.isActive = 1 and sf.authorisationLevel <= :maxAuthForActUser and sf.refObject= :refObject order by sf.filterOrder",
                        SdlFilter.class);
                query.setParameter("maxAuthForActUser", getMaxAuthorisationForActUser());
                query.setParameter("refObject", refObject);
                query.setResultTransformer(DistinctRootEntityResultTransformer.INSTANCE);
                return query.list();
            }
        });
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<SdlParameter> getParameters(final Long filterId) {
        return (List<SdlParameter>) getHibernateTemplate().execute(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                org.hibernate.query.Query<SdlParameter> query = session.createQuery(
                    "from SdlParameter where filterId= :filterId order by parameterOrder",
                    SdlParameter.class);
                query.setParameter("filterId", filterId);
                return query.list();
            }
        });
    }

    @Override
    public SdlFilter insertFilter(SdlFilter filter) {
        filter = (SdlFilter) getHibernateTemplate().merge(filter);
        getHibernateTemplate().flush();
        return filter;
    }

    @Override
    public SdlFilter updateFilter(SdlFilter filter) {
        filter = (SdlFilter) getHibernateTemplate().merge(filter);
        getHibernateTemplate().flush();
        return filter;
    }

    @Override
    public void deleteFilter(SdlFilter filter) {
        getHibernateTemplate().delete(filter);
        getHibernateTemplate().flush();
    }

    public Long getActVersion() {
        try {
            List<SdlFilter> filters = getActiveFiltersForRefObjectAndNameDe(CodegroupUtility.SDL_OBJECTTYPE_CONFIGURATION,
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
            List<SdlFilter> filters = getActiveFiltersForRefObjectAndNameDe(CodegroupUtility.SDL_OBJECTTYPE_CONFIGURATION,
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
