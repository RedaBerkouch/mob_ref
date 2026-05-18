/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sdlserver

 */
package ch.bfs.meb.sdl.server.integration.repository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.HibernateException;
import org.hibernate.query.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.springframework.orm.hibernate5.HibernateCallback;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;
import org.springframework.stereotype.Repository;

import ch.bfs.meb.sdl.server.integration.dto.SdlConfigDelivery;
import ch.bfs.meb.server.commons.integration.dto.FilterContext;
import ch.bfs.meb.server.commons.integration.dto.SortContext;
import ch.bfs.meb.server.commons.util.IFilterUtility;
import ch.bfs.meb.util.StringUtils;

@Repository
public class ConfigDeliveryRepository extends HibernateDaoSupport implements IConfigDeliveryRepository {
    // property constants
    public static final String DELIVERYCODE = "deliveryCode";
    public static final String DL_USERS = "dl_users";
    public static final String RO_USERS = "ro_users";
    public static final String CREATION_USER = "creation_user";
    public static final String MODIFICATION_USER = "modification_user";
    public static final String USERTEXT = "userText";

    public static final String REFERENCEDATE = "referenceDate";
    public static final String DUEDATE = "dueDate";
    public static final String CREATION_DATE = "creation_date";
    public static final String MODIFICATION_DATE = "modification_date";

    private IFilterUtility _filterUtility;

    public void setFilterUtility(IFilterUtility filterUtility) {
        _filterUtility = filterUtility;
    }

    @Override
    public SdlConfigDelivery getConfigDeliveryById(Long configDeliveryId) {
        return (SdlConfigDelivery) getHibernateTemplate().get(SdlConfigDelivery.class, configDeliveryId);
    }

    @Override
    public SdlConfigDelivery getConfigDeliveryByCodeVersionAndCanton(final String deliveryCode, final Long version, final Long canton) {
        return (SdlConfigDelivery) getHibernateTemplate().execute(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
org.hibernate.query.Query<SdlConfigDelivery> query = session.createQuery(
        "from SdlConfigDelivery where canton=:canton and version=:version and deliveryCode=:deliveryCode", SdlConfigDelivery.class);
                query.setParameter("canton", canton);
                query.setParameter("version", version);
                query.setParameter("deliveryCode", deliveryCode);

                return query.uniqueResult();
            }
        });
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<SdlConfigDelivery> getConfigDeliveriesByCodeAndVersion(final String deliveryCode, final Long version) {
        return (List<SdlConfigDelivery>) getHibernateTemplate().execute(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
org.hibernate.query.Query<SdlConfigDelivery> query = session.createQuery(
        "from SdlConfigDelivery where version=:version and deliveryCode=:deliveryCode", SdlConfigDelivery.class);
                query.setParameter("version", version);
                query.setParameter("deliveryCode", deliveryCode);

                return query.list();
            }
        });
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<SdlConfigDelivery> getConfigDeliveriesByVersionAndCanton(final Long version, final Long canton) {
        return (List<SdlConfigDelivery>) getHibernateTemplate().execute(new HibernateCallback() {
            @Override
            public List<SdlConfigDelivery> doInHibernate(Session session) throws HibernateException {

        String hql ="from SdlConfigDelivery where canton=:canton and version=:version";
    Query<SdlConfigDelivery> query = session.createQuery(hql, SdlConfigDelivery.class);
                query.setParameter("canton", canton);
                query.setParameter("version", version);

                return query.list();
            }
        });
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<SdlConfigDelivery> getConfigDeliveriesByVersion(final Long version) {
        return (List<SdlConfigDelivery>) getHibernateTemplate().execute(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {

                Query<SdlConfigDelivery> query = session.createQuery(
                        "from SdlConfigDelivery where version=:version", SdlConfigDelivery.class);
                query.setParameter("version", version);

                return query.list();
            }
        });
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<SdlConfigDelivery> getConfigDeliveries(int start, int buffer, SortContext sortContext, FilterContext filterContext, Long version, Long canton) {
        String queryString;
        String sortColumn = _filterUtility.adaptColumnName(sortContext.getSortColumn(), "model", getUnderscoreColumns());
        String whereSelection = _filterUtility.getWhereFilterSelection(filterContext, "model", getStringColumns(), getDateColumns(), getUnderscoreColumns());
        String configDeliverySubquery = _filterUtility.getPredefinedFilterSubquery(filterContext, SecurityFilters.SDL_CONFIGDELIVERIES_TABLE, true);

        if (whereSelection.length() > 0) {
            whereSelection += " and ";
        }
        whereSelection += "model.version=" + version;

        if (canton > 0L) {
            whereSelection += " and model.canton=" + canton;
        }

        queryString = "select distinct model.deliveryId, " + sortColumn + " from " + configDeliverySubquery + " model " + " where " + whereSelection
                + " order by " + sortColumn + " " + (sortContext.getAscSortOrder() ? "asc" : "desc") + ", model.deliveryId asc";

        // SQL-Query: Because of HQL-Problems with from-subqueries..
        Query query = currentSession().createNativeQuery (queryString);
        if (start >= 0) {
            query.setFirstResult(start);
        }
        if (buffer > 0) {
            query.setMaxResults(buffer);
        }

        // get list of config delivery ids as long
        List<Long> configDeliveryIds = new ArrayList<Long>();
        List<Object[]> queryIdsList = query.list();
        for (Object[] row : queryIdsList) {
            configDeliveryIds.add(((BigDecimal) row[0]).longValue());
        }

        return reloadSortedConfigDelivery(configDeliveryIds);
    }

    @SuppressWarnings("unchecked")
    private List<SdlConfigDelivery> reloadSortedConfigDelivery(List<Long> configDeliveryIds) {
        if (configDeliveryIds == null || configDeliveryIds.isEmpty()) {
            return new ArrayList<SdlConfigDelivery>();
        }

        // query deliveries including the plausi errors
        Query queryResult = currentSession().createQuery("from SdlConfigDelivery where deliveryId in (:deliveryIds)");
        queryResult.setParameterList("deliveryIds", configDeliveryIds);
        // DistinctRootEntityResultTransformer not required => order by map below already does the same
        //		queryResult.setResultTransformer(DistinctRootEntityResultTransformer.INSTANCE);

        List<SdlConfigDelivery> tempList = queryResult.list();

        // reestablish old sort order
        Map<Long, SdlConfigDelivery> mapById = new HashMap<Long, SdlConfigDelivery>(tempList.size());
        for (SdlConfigDelivery entity : tempList) {
            mapById.put(entity.getDeliveryId(), entity);
        }
        List<SdlConfigDelivery> resultList = new ArrayList<SdlConfigDelivery>(mapById.size());
        for (Long id : configDeliveryIds) {
            SdlConfigDelivery entity = mapById.get(id);
            if (entity != null) {
                resultList.add(entity);
            }
        }

        return resultList;
    }

    @Override
    public Long getMaxNrOfConfigDeliveries(FilterContext filterContext, Long version, Long canton) {
        String queryString;
        String whereSelection = _filterUtility.getWhereFilterSelection(filterContext, "model", getStringColumns(), getDateColumns(), getUnderscoreColumns());
        String configDeliverySubquery = _filterUtility.getPredefinedFilterSubquery(filterContext, SecurityFilters.SDL_CONFIGDELIVERIES_TABLE, true);

        if (whereSelection.length() > 0) {
            whereSelection += " and ";
        }
        whereSelection += "model.version=" + version;

        if (canton > 0L) {
            whereSelection += " and model.canton=" + canton;
        }

        queryString = "select count (*) nrDeliveries from " + configDeliverySubquery + " model where " + whereSelection;

        // SQL-Query: Because of HQL-Problems with from-subqueries..
        Query query = currentSession().createNativeQuery (queryString).addScalar("nrDeliveries");

        return ((BigDecimal) query.uniqueResult()).longValue();
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<SdlConfigDelivery> getConfigDeliveriesOwnedBySchools(List<Long> schoolIds, SortContext sortContext, Long version) {
        String queryString;
        String sortColumn = _filterUtility.adaptColumnName(sortContext.getSortColumn(), "model", getUnderscoreColumns());
        String sidsSelection = schoolIds.isEmpty() ? "" : "meb_s.schoolId in (";
        boolean firstSid = true;
        for (Long sid : schoolIds) {
            if (!firstSid) {
                sidsSelection += ",";
            }
            sidsSelection += sid.toString();
            firstSid = false;
        }
        sidsSelection += (sidsSelection.length() == 0) ? "" : ")";

        queryString = "select distinct model.deliveryId, " + sortColumn
                + " from Sdl_ConfigDeliveries model, Sdl_Schools_ConfigDeliveries meb_s_cd, Schools meb_s"
                + " where model.deliveryId=meb_s_cd.deliveryId and meb_s_cd.schoolId=meb_s.schoolId and model.version=" + version
                + ((sidsSelection.length() == 0) ? "" : " and ") + sidsSelection + " order by " + sortColumn + " "
                + ((sortContext.getAscSortOrder()) ? "asc" : "desc");

        Query query = currentSession().createNativeQuery (queryString);

        // get list of config delivery ids as long
        List<Long> configDeliveryIds = new ArrayList<Long>();
        List<Object[]> queryIdsList = query.list();
        for (Object[] row : queryIdsList) {
            configDeliveryIds.add(((BigDecimal) row[0]).longValue());
        }

        return reloadSortedConfigDelivery(configDeliveryIds);
    }

    @Override
    public SdlConfigDelivery insertConfigDelivery(SdlConfigDelivery configDelivery) {
        configDelivery = (SdlConfigDelivery) getHibernateTemplate().merge(configDelivery);
        getHibernateTemplate().flush();
        return configDelivery;
    }

    @Override
    public SdlConfigDelivery updateConfigDelivery(SdlConfigDelivery configDelivery) {
        configDelivery = (SdlConfigDelivery) getHibernateTemplate().merge(configDelivery);
        getHibernateTemplate().flush();
        return configDelivery;
    }

    @Override
    public void deleteConfigDelivery(SdlConfigDelivery configDelivery) {
        getHibernateTemplate().delete(configDelivery);
        getHibernateTemplate().flush();
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sdl.server.integration.repository.IConfigDeliveryRepository#updateConfigDeliveryCodes(ch.bfs.meb.sdl.server.integration.dto.SdlConfigDelivery)
     */
    @Override
    public void updateConfigDeliveryCodes(SdlConfigDelivery configDelivery, String oldConfigDeliveryCode) {
        getHibernateTemplate().execute(session -> {
            String learnerUpdateQuery = "UPDATE SdlLearner SET configDeliveryCode = :newCode WHERE canton = :canton AND version = :version AND configDeliveryCode = :oldCode";
            Query<?> learnerQuery = session.createQuery(learnerUpdateQuery);
            learnerQuery.setParameter("newCode", configDelivery.getDeliveryCode());
            learnerQuery.setParameter("canton", configDelivery.getCanton());
            learnerQuery.setParameter("version", configDelivery.getVersion());
            learnerQuery.setParameter("oldCode", oldConfigDeliveryCode);
            learnerQuery.executeUpdate();

            String classUpdateQuery = "UPDATE SdlClass SET configDeliveryCode = :newCode WHERE canton = :canton AND version = :version AND configDeliveryCode = :oldCode";
            Query<?> classQuery = session.createQuery(classUpdateQuery);
            classQuery.setParameter("newCode", configDelivery.getDeliveryCode());
            classQuery.setParameter("canton", configDelivery.getCanton());
            classQuery.setParameter("version", configDelivery.getVersion());
            classQuery.setParameter("oldCode", oldConfigDeliveryCode);
            classQuery.executeUpdate();

            String schoolUpdateQuery = "UPDATE SdlSchool SET configDeliveryCode = :newCode WHERE canton = :canton AND version = :version AND configDeliveryCode = :oldCode";
            Query<?> schoolQuery = session.createQuery(schoolUpdateQuery);
            schoolQuery.setParameter("newCode", configDelivery.getDeliveryCode());
            schoolQuery.setParameter("canton", configDelivery.getCanton());
            schoolQuery.setParameter("version", configDelivery.getVersion());
            schoolQuery.setParameter("oldCode", oldConfigDeliveryCode);
            schoolQuery.executeUpdate();

            String deliveryUpdateQuery = "UPDATE SdlDelivery SET configDeliveryCode = :newCode WHERE canton = :canton AND version = :version AND configDeliveryCode = :oldCode";
            Query<?> deliveryQuery = session.createQuery(deliveryUpdateQuery);
            deliveryQuery.setParameter("newCode", configDelivery.getDeliveryCode());
            deliveryQuery.setParameter("canton", configDelivery.getCanton());
            deliveryQuery.setParameter("version", configDelivery.getVersion());
            deliveryQuery.setParameter("oldCode", oldConfigDeliveryCode);
            deliveryQuery.executeUpdate();

            return null;
        });

        getHibernateTemplate().flush();
    }

    /**
     * Returns all columns with underscores of according db table
     */
    protected List<String> getUnderscoreColumns() {
        ArrayList<String> underscoreColumns = new ArrayList<String>();
        underscoreColumns.add(DL_USERS);
        underscoreColumns.add(RO_USERS);
        underscoreColumns.add(CREATION_USER);
        underscoreColumns.add(CREATION_DATE);
        underscoreColumns.add(MODIFICATION_USER);
        underscoreColumns.add(MODIFICATION_DATE);
        return underscoreColumns;
    }

    /**
     * Returns all string columns of according db table
     */
    protected List<String> getStringColumns() {
        ArrayList<String> stringColumns = new ArrayList<String>();
        stringColumns.add(DELIVERYCODE);
        stringColumns.add(StringUtils.asCamelCase(DL_USERS));
        stringColumns.add(StringUtils.asCamelCase(RO_USERS));
        stringColumns.add(StringUtils.asCamelCase(CREATION_USER));
        stringColumns.add(StringUtils.asCamelCase(MODIFICATION_USER));
        stringColumns.add(USERTEXT);
        return stringColumns;
    }

    /**
     * Returns all date columns of according db table
     */
    protected List<String> getDateColumns() {
        ArrayList<String> dateColumns = new ArrayList<String>();
        dateColumns.add(REFERENCEDATE);
        dateColumns.add(DUEDATE);
        dateColumns.add(StringUtils.asCamelCase(CREATION_DATE));
        dateColumns.add(StringUtils.asCamelCase(MODIFICATION_DATE));
        return dateColumns;
    }
}
