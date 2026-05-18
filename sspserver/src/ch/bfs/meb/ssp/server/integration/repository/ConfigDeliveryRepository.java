/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sspserver

 */
package ch.bfs.meb.ssp.server.integration.repository;

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

import ch.bfs.meb.server.commons.integration.dto.FilterContext;
import ch.bfs.meb.server.commons.integration.dto.SortContext;
import ch.bfs.meb.server.commons.util.IFilterUtility;
import ch.bfs.meb.ssp.server.integration.dto.SspConfigDelivery;
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
    public SspConfigDelivery getConfigDeliveryById(Long configDeliveryId) {
        return (SspConfigDelivery) getHibernateTemplate().get(SspConfigDelivery.class, configDeliveryId);
    }

    public SspConfigDelivery getConfigDeliveryByCodeVersionAndCanton(final String deliveryCode, final Long version, final Long canton) {
        return getHibernateTemplate().execute(new HibernateCallback<SspConfigDelivery>() {
            @Override
            public SspConfigDelivery doInHibernate(Session session) {
                String hql = "from SspConfigDelivery where canton = :canton and version = :version and deliveryCode = :deliveryCode";
                Query<SspConfigDelivery> query = session.createQuery(hql, SspConfigDelivery.class);
                query.setParameter("canton", canton);
                query.setParameter("version", version);
                query.setParameter("deliveryCode", deliveryCode);
                return query.uniqueResult();
            }
        });
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<SspConfigDelivery> getConfigDeliveriesByCodeAndVersion(final String deliveryCode, final Long version) {
        return getHibernateTemplate().execute(new HibernateCallback<List<SspConfigDelivery>>() {
            @Override
            public List<SspConfigDelivery> doInHibernate(Session session) throws HibernateException {
                String hql = "from SspConfigDelivery where version = :version and deliveryCode = :deliveryCode";
                Query<SspConfigDelivery> query = session.createQuery(hql, SspConfigDelivery.class);
                query.setParameter("version", version);
                query.setParameter("deliveryCode", deliveryCode);
                return query.list();
            }
        });
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<SspConfigDelivery> getConfigDeliveriesByVersionAndCanton(final Long version, final Long canton) {
        return (List<SspConfigDelivery>) getHibernateTemplate().execute(new HibernateCallback() {
            @Override
            public List<SspConfigDelivery> doInHibernate(Session session) throws HibernateException {

                String hql ="from SspConfigDelivery where canton=:canton and version=:version";
                Query<SspConfigDelivery> query = session.createQuery(hql, SspConfigDelivery.class);
                query.setParameter("canton", canton);
                query.setParameter("version", version);

                return query.list();
            }
        });
    }



    @SuppressWarnings("unchecked")
    @Override
    public List<SspConfigDelivery> getConfigDeliveriesByVersion(final Long version) {
        return getHibernateTemplate().execute(new HibernateCallback<List<SspConfigDelivery>>() {
            @Override
            public List<SspConfigDelivery> doInHibernate(Session session) throws HibernateException {
                String hql = "from SspConfigDelivery where version = :version";
                Query<SspConfigDelivery> query = session.createQuery(hql, SspConfigDelivery.class);
                query.setParameter("version", version);
                return query.list();
            }
        });
    }


    @SuppressWarnings("unchecked")
    @Override
    public List<SspConfigDelivery> getConfigDeliveries(int start, int buffer, SortContext sortContext, FilterContext filterContext, Long version, Long canton) {
        String queryString;
        String sortColumn = _filterUtility.adaptColumnName(sortContext.getSortColumn(), "model", getUnderscoreColumns());
        String whereSelection = _filterUtility.getWhereFilterSelection(filterContext, "model", getStringColumns(), getDateColumns(), getUnderscoreColumns());
        String configDeliverySubquery = _filterUtility.getPredefinedFilterSubquery(filterContext, SecurityFilters.SSP_CONFIGDELIVERIES_TABLE, true);

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
        Query query =currentSession().createNativeQuery (queryString);
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
    private List<SspConfigDelivery> reloadSortedConfigDelivery(List<Long> configDeliveryIds) {
        if (configDeliveryIds == null || configDeliveryIds.isEmpty()) {
            return new ArrayList<SspConfigDelivery>();
        }

        // query deliveries including the plausi errors
        Query queryResult = currentSession().createQuery("from SspConfigDelivery where deliveryId in (:deliveryIds)");
        queryResult.setParameterList("deliveryIds", configDeliveryIds);
        // DistinctRootEntityResultTransformer not required => order by map below already does the same
        //		queryResult.setResultTransformer(DistinctRootEntityResultTransformer.INSTANCE);

        List<SspConfigDelivery> tempList = queryResult.list();

        // reestablish old sort order
        Map<Long, SspConfigDelivery> mapById = new HashMap<Long, SspConfigDelivery>(tempList.size());
        for (SspConfigDelivery entity : tempList) {
            mapById.put(entity.getDeliveryId(), entity);
        }
        List<SspConfigDelivery> resultList = new ArrayList<SspConfigDelivery>(mapById.size());
        for (Long id : configDeliveryIds) {
            SspConfigDelivery entity = mapById.get(id);
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
        String configDeliverySubquery = _filterUtility.getPredefinedFilterSubquery(filterContext, SecurityFilters.SSP_CONFIGDELIVERIES_TABLE, true);

        if (whereSelection.length() > 0) {
            whereSelection += " and ";
        }
        whereSelection += "model.version=" + version;

        if (canton > 0L) {
            whereSelection += " and model.canton=" + canton;
        }

        queryString = "select count (*) nrDeliveries from " + configDeliverySubquery + " model where " + whereSelection;

        // SQL-Query: Because of HQL-Problems with from-subqueries..
        Query query =currentSession().createNativeQuery (queryString).addScalar("nrDeliveries");

        return ((BigDecimal) query.uniqueResult()).longValue();
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<SspConfigDelivery> getConfigDeliveriesOwnedBySchools(List<Long> schoolIds, SortContext sortContext, Long version) {
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
                + " from Ssp_ConfigDeliveries model, Ssp_Schools_ConfigDeliveries meb_s_cd, Schools meb_s"
                + " where model.deliveryId=meb_s_cd.deliveryId and meb_s_cd.schoolId=meb_s.schoolId and model.version=" + version
                + ((sidsSelection.length() == 0) ? "" : " and ") + sidsSelection + " order by " + sortColumn + " "
                + ((sortContext.getAscSortOrder()) ? "asc" : "desc");

        Query query =currentSession().createNativeQuery (queryString);

        // get list of config delivery ids as long
        List<Long> configDeliveryIds = new ArrayList<Long>();
        List<Object[]> queryIdsList = query.list();
        for (Object[] row : queryIdsList) {
            configDeliveryIds.add(((BigDecimal) row[0]).longValue());
        }

        return reloadSortedConfigDelivery(configDeliveryIds);
    }

    @Override
    public SspConfigDelivery insertConfigDelivery(SspConfigDelivery configDelivery) {
        configDelivery = (SspConfigDelivery) getHibernateTemplate().merge(configDelivery);
        getHibernateTemplate().flush();
        return configDelivery;
    }

    @Override
    public SspConfigDelivery updateConfigDelivery(SspConfigDelivery configDelivery) {
        configDelivery = (SspConfigDelivery) getHibernateTemplate().merge(configDelivery);
        getHibernateTemplate().flush();
        return configDelivery;
    }

    @Override
    public void deleteConfigDelivery(SspConfigDelivery configDelivery) {
        getHibernateTemplate().delete(configDelivery);
        getHibernateTemplate().flush();
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.ssp.server.integration.repository.IConfigDeliveryRepository#updateConfigDeliveryCodes(ch.bfs.meb.ssp.server.integration.dto.SspConfigDelivery, java.lang.String)
     */
    @Override
    public void updateConfigDeliveryCodes(SspConfigDelivery configDelivery, String oldConfigDeliveryCode) {
        HibernateCallback<Integer> updateSspActivity = session -> {
            String hql = "update SspActivity set configDeliveryCode = :newCode " +
                    "where canton = :canton and version = :version and configDeliveryCode = :oldCode";
            Query<?> query = session.createQuery(hql);
            query.setParameter("newCode", configDelivery.getDeliveryCode());
            query.setParameter("canton", configDelivery.getCanton());
            query.setParameter("version", configDelivery.getVersion());
            query.setParameter("oldCode", oldConfigDeliveryCode);
            return query.executeUpdate();
        };

        HibernateCallback<Integer> updateSspPerson = session -> {
            String hql = "update SspPerson set configDeliveryCode = :newCode " +
                    "where canton = :canton and version = :version and configDeliveryCode = :oldCode";
            Query<?> query = session.createQuery(hql);
            query.setParameter("newCode", configDelivery.getDeliveryCode());
            query.setParameter("canton", configDelivery.getCanton());
            query.setParameter("version", configDelivery.getVersion());
            query.setParameter("oldCode", oldConfigDeliveryCode);
            return query.executeUpdate();
        };

        HibernateCallback<Integer> updateSspDelivery = session -> {
            String hql = "update SspDelivery set configDeliveryCode = :newCode " +
                    "where canton = :canton and version = :version and configDeliveryCode = :oldCode";
            Query<?> query = session.createQuery(hql);
            query.setParameter("newCode", configDelivery.getDeliveryCode());
            query.setParameter("canton", configDelivery.getCanton());
            query.setParameter("version", configDelivery.getVersion());
            query.setParameter("oldCode", oldConfigDeliveryCode);
            return query.executeUpdate();
        };

        getHibernateTemplate().execute(updateSspActivity);
        getHibernateTemplate().execute(updateSspPerson);
        getHibernateTemplate().execute(updateSspDelivery);
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
