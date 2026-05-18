package ch.bfs.meb.sbg.server.integration.repository;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.LockOptions;
import org.hibernate.query.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Example;
import org.hibernate.transform.DistinctRootEntityResultTransformer;
import org.springframework.orm.hibernate5.HibernateCallback;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Repository;

import ch.bfs.meb.sbg.server.integration.dto.SbgFilter;
import ch.bfs.meb.sbg.server.integration.dto.SbgParameter;
import ch.bfs.meb.security.MebUser;
import ch.bfs.meb.util.CodegroupUtility;
import ch.bfs.meb.util.SecurityConstants;

/**
 * Data access object (DAO) for domain model class {@link ch.bfs.meb.sbg.server.integration.dto.SbgFilter}.
 *
 * @see ch.bfs.meb.sbg.server.integration.repository.IFilterRepository
 */
@Repository
public class FilterRepository extends HibernateDaoSupport implements IFilterRepository {
    // property constants
    public static final String REFOBJECT = "refObject";
    public static final String NAME_DE = "name_de";
    public static final String NAME_FR = "name_fr";
    public static final String DESCRIPTION_DE = "description_de";
    public static final String DESCRIPTION_FR = "description_fr";
    public static final String SOURCE = "source";
    public static final String AUTHORISATIONLEVEL = "authorisationLevel";
    public static final String MODUSER = "modUser";
    public static final String ISDEFAULT = "isDefault";
    public static final String ISACTIVE = "isActive";

    @Override
    public SbgFilter getFilterById(Long filterId) {
        return (SbgFilter) getHibernateTemplate().get(SbgFilter.class, filterId);
    }

    @Override
    public List<SbgFilter> getFilters() {
        return (List<SbgFilter>) getHibernateTemplate().execute(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                Query query = session.createQuery("from SbgFilter as filter left join fetch filter. order by filter.filterOrder");
                query.setResultTransformer(DistinctRootEntityResultTransformer.INSTANCE);
                return query.list();
            }
        });
    }

    public void save(SbgFilter filter) {
        currentSession().save(filter);
    }

    @SuppressWarnings("unchecked")
    public List<SbgFilter> findByExample(SbgFilter example) {
        return currentSession().createCriteria(SbgFilter.class).add(Example.create(example)).list();
    }

    @SuppressWarnings("unchecked")
    public List<SbgFilter> findByProperty(String propertyName, Object value) {
        String queryString = "from SbgFilter as filter where filter." + propertyName + "= :value";
        org.hibernate.query.Query<SbgFilter> queryObject = currentSession().createQuery(queryString, SbgFilter.class);
        queryObject.setParameter("value", value);
        return queryObject.list();
    }

    @Override
    public List<SbgFilter> getActiveFilters() {
        String queryString = "from SbgFilter as filter where filter.isActive = true";
        Query queryObject = currentSession().createQuery(queryString);
        return queryObject.list();
    }

    @Override
    public List<SbgFilter> getDefaultPersonFilters() {
        String queryString = "from SbgFilter where isDefault = true and refObject = 1";
        Query queryObject = currentSession().createQuery(queryString);
        return queryObject.list();
    }

    public void attachDirty(SbgFilter instance) {
        currentSession().saveOrUpdate(instance);
    }

    public void attachClean(SbgFilter instance) {
        currentSession().buildLockRequest(LockOptions.NONE).lock(instance);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<SbgFilter> getActiveFiltersForRefObjectAndNameDe(final Long refObject, final String nameDe) {
        return (List<SbgFilter>) getHibernateTemplate().execute(session -> {

            Query<SbgFilter> query = session.createQuery(
                    "select distinct filter " +
                            "from SbgFilter filter " +
                            "left join fetch filter.sbgParameters " +
                            "where filter.isActive = true " +
                            "and filter.authorisationLevel <= :auth " +
                            "and filter.refObject = :refObj " +
                            "and filter.name_de = :nameDe " +
                            "order by filter.filterOrder",
                    SbgFilter.class
            );

            query.setParameter("auth", getMaxAuthorisationForActUser());
            query.setParameter("refObj", refObject);
            query.setParameter("nameDe", nameDe);

            // Optionnel: dédoublonnage
            query.setResultTransformer(DistinctRootEntityResultTransformer.INSTANCE);

            return query.list();
        });
    }


    @Override
    @SuppressWarnings("unchecked")
    public List<SbgFilter> getActiveFiltersForRefObject(final Long refObject) {
        return (List<SbgFilter>) getHibernateTemplate().execute(session -> {

            Query<SbgFilter> query = session.createQuery(
                    "select distinct filter " +
                            "from SbgFilter filter " +
                            "left join fetch filter.sbgParameters " +
                            "where filter.isActive = true " +
                            "and filter.authorisationLevel <= :maxAuthForActUser " +
                            "and filter.refObject = :refObject " +
                            "order by filter.filterOrder",
                    SbgFilter.class
            );

            query.setParameter("maxAuthForActUser", getMaxAuthorisationForActUser());
            query.setParameter("refObject", refObject);

            return query.list();
        });
    }


    @SuppressWarnings("unchecked")
    @Override
    public List<SbgParameter> getParameters(final Long filterId) {
        return (List<SbgParameter>) getHibernateTemplate().execute(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
org.hibernate.query.Query<SbgParameter> query = session.createQuery("from SbgParameter where filterId=:filterId order by parameterOrder, parameterId", SbgParameter.class);
                query.setParameter("filterId", filterId);
                return query.list();
            }
        });
    }

    @Override
    public SbgFilter updateFilter(SbgFilter filter) {
        filter = (SbgFilter) getHibernateTemplate().merge(filter);
        getHibernateTemplate().flush();
        return filter;
    }

    @Override
    public SbgFilter insertFilter(SbgFilter filter) {
        filter = (SbgFilter) getHibernateTemplate().merge(filter);
        getHibernateTemplate().flush();
        return filter;
    }

    @Override
    public void deleteFilter(SbgFilter filter) {
        getHibernateTemplate().delete(filter);
        getHibernateTemplate().flush();
    }

    @Override
    public Long getActVersion() {
        try {
            List<SbgFilter> filters = getActiveFiltersForRefObjectAndNameDe(CodegroupUtility.SBG_OBJECTTYPE_CONFIGURATION,
                    CodegroupUtility.MEB_FILTER_ACT_VERSION);
            if (filters.size() > 0) {
                return Long.parseLong(filters.get(0).getSource());
            }
        } catch (Exception e) {
            // nothing to do
        }

        return new Long(new GregorianCalendar().get(Calendar.YEAR));
    }

    @Override
    public Long getInitVersion() {
        try {
            List<SbgFilter> filters = getActiveFiltersForRefObjectAndNameDe(CodegroupUtility.SBG_OBJECTTYPE_CONFIGURATION,
                    CodegroupUtility.MEB_FILTER_INIT_VERSION);
            if (filters.size() > 0) {
                return Long.parseLong(filters.get(0).getSource());
            }
        } catch (Exception e) {
            // nothing to do
        }

        return new Long(new GregorianCalendar().get(Calendar.YEAR));
    }

    protected Long getMaxAuthorisationForActUser() {
        MebUser user = (MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (user.isInRole(SecurityConstants.ROLE_SBG_EA)) {
            return SecurityConstants.ROLE_EA;
        }
        if (user.isInRole(SecurityConstants.ROLE_SBG_EV)) {
            return SecurityConstants.ROLE_EV;
        }
        if (user.isInRole(SecurityConstants.ROLE_SBG_DV)) {
            return SecurityConstants.ROLE_DV;
        }
        if (user.isInRole(SecurityConstants.ROLE_SBG_DL)) {
            return SecurityConstants.ROLE_DL;
        }
        return SecurityConstants.ROLE_RO;
    }
}
