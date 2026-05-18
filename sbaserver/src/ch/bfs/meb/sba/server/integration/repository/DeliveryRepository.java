/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sbaserver

 */
package ch.bfs.meb.sba.server.integration.repository;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.*;

import ch.bfs.meb.sba.server.integration.dto.SbaQualification;
import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.hibernate.query.NativeQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.hibernate5.HibernateCallback;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;
import org.springframework.security.core.context.SecurityContextHolder;

import ch.bfs.meb.exception.MebUncheckedNotMonitoredException;
import ch.bfs.meb.integration.dto.ResultBase;
import ch.bfs.meb.sba.server.integration.dto.SbaDelivery;
import ch.bfs.meb.sba.server.integration.dto.SbaPlausiError;
import ch.bfs.meb.security.MebUser;
import ch.bfs.meb.server.commons.integration.dto.FilterContext;
import ch.bfs.meb.server.commons.integration.dto.SortContext;
import ch.bfs.meb.server.commons.util.IFilterUtility;
import ch.bfs.meb.util.CodegroupUtility;
import ch.bfs.meb.util.SecurityConstants;
import ch.bfs.meb.util.StringUtils;

/**
 * Repository for SbaDeliveries.
 *
 * @author $Author: msc $
 * @version $Revision: 957 $
 */
public class DeliveryRepository extends HibernateDaoSupport implements IDeliveryRepository {
    private static final Logger LOGGER = LoggerFactory.getLogger(DeliveryRepository.class);

    // property constants
    public static final String DELIVERYCODE = "deliveryCode";
    public static final String CREATION_USER = "creation_user";
    public static final String MODIFICATION_USER = "modification_user";
    public static final String PREVALIDATION_USER = "prevalidation_user";
    public static final String VALIDATION_USER = "validation_user";
    public static final String USERTEXT = "userText";

    public static final String DELIVERYDATE = "deliveryDate";
    public static final String CREATION_DATE = "creation_date";
    public static final String MODIFICATION_DATE = "modification_date";
    public static final String PREVALIDATION_DATE = "prevalidation_date";
    public static final String VALIDATION_DATE = "validation_date";

    public static final String DELIVERYSTATUS = "deliveryStatus";
    public static final String PLAUSISTATUS = "plausiStatus";
    public static final String VERSION = "version";

    private IFilterUtility _filterUtility;

    public void setFilterUtility(IFilterUtility filterUtility) {
        _filterUtility = filterUtility;
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sba.server.integration.repository.IDeliveryRepository#getDeliveries()
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<SbaDelivery> getDeliveries(int start, int buffer, SortContext sortContext, FilterContext filterContext, Long version, Long canton) {
        String queryString;
        String sortColumn = _filterUtility.adaptColumnName(sortContext.getSortColumn() == null ? MODIFICATION_DATE : sortContext.getSortColumn(), "model",
                getUnderscoreColumns());
        String codegroupId = getCodegroupId(sortColumn);
        String whereSelection = _filterUtility.getWhereFilterSelection(filterContext, "model", getStringColumns(), getDateColumns(), getUnderscoreColumns());
        String deliverySubquery = _filterUtility.getPredefinedFilterSubquery(filterContext, SecurityFilters.SBA_DELIVERIES_TABLE, true);

        if (canton > 0L) {
            if (whereSelection.length() > 0) {
                whereSelection += " and ";
            }
            whereSelection += "model.canton=" + canton;
        }

        if (version != null) {
            if (whereSelection.length() > 0) {
                whereSelection += " and ";
            }
            whereSelection += "model.version=" + version;
        }

        if (sortContext.getLocale() != null && codegroupId != null) {
            // sort according to locale dependent code texts
            String mainLocale = sortContext.getLocale();
            queryString = "select distinct model.deliveryId, " + "(case when " + sortColumn + " is null then '' "
                    + "      when meb_cg.code is null then to_char(" + sortColumn + ") " + "      else  meb_cg.codetext end) sorttext " + " from "
                    + deliverySubquery + " model " + " left outer join  " + " (select cg1.* from Codegroups cg1, "
                    + "   (select codegroupid, code, language, max(validFromYear) validFromYear from Codegroups where codegroupId = '" + codegroupId
                    + "' and language = '" + mainLocale + "' group by codegroupid, code, language" + "   ) cg2"
                    + "  where cg1.codegroupId = cg2.codegroupId and cg1.code = cg2.code and cg1.language = cg2.language and cg1.validFromYear = cg2.validFromYear   "
                    + " ) meb_cg on meb_cg.code = " + sortColumn + (whereSelection.length() > 0 ? " where " + whereSelection : "") + " order by sorttext "
                    + ((sortContext.getAscSortOrder()) ? "asc" : "desc") + ", model.deliveryId asc";
        } else {
            queryString = "select distinct model.deliveryId, " + sortColumn + " from " + deliverySubquery + " model"
                    + (whereSelection.length() > 0 ? " where " + whereSelection : "") + " order by " + sortColumn + " "
                    + ((sortContext.getAscSortOrder()) ? "asc" : "desc") + ", model.deliveryId asc";
        }

        // SQL-Query: Because of HQL-Problems with from-subqueries..
        Query queryIds = currentSession().createNativeQuery (queryString);
        if (start >= 0) {
            queryIds.setFirstResult(start);
        }
        if (buffer > 0) {
            queryIds.setMaxResults(buffer);
        }

        // get list of delivery ids as long
        List<Long> deliveryIds = new ArrayList<Long>();
        List<Object[]> queryIdsList = queryIds.list();
        for (Object[] row : queryIdsList) {
            deliveryIds.add(((BigDecimal) row[0]).longValue());
        }

        return getDeliveriesByIds(deliveryIds);
    }

    @Override
    public Long getMaxNrOfDeliveries(FilterContext filterContext, Long version, Long canton) {
        String queryString;
        String whereSelection = _filterUtility.getWhereFilterSelection(filterContext, "model", getStringColumns(), getDateColumns(), getUnderscoreColumns());
        String deliverySubquery = _filterUtility.getPredefinedFilterSubquery(filterContext, SecurityFilters.SBA_DELIVERIES_TABLE, true);

        if (canton > 0L) {
            if (whereSelection.length() > 0) {
                whereSelection += " and ";
            }
            whereSelection += "model.canton=" + canton;
        }

        if (version != null) {
            if (whereSelection.length() > 0) {
                whereSelection += " and ";
            }
            whereSelection += "model.version=" + version;
        }

        queryString = "select count (*) nrDeliveries from " + deliverySubquery + " model" + (whereSelection.length() > 0 ? " where " + whereSelection : "");

        // SQL-Query: Because of HQL-Problems with from-subqueries..
        Query query = currentSession().createNativeQuery (queryString).addScalar("nrDeliveries");

        return ((BigDecimal) query.uniqueResult()).longValue();
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sba.server.integration.repository.IDeliveryRepository#getDeliveriesForCanton(java.lang.Long, java.lang.Long)
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<SbaDelivery> getDeliveriesForCanton(final Long canton, final Long version) {
        return (List<SbaDelivery>) getHibernateTemplate().execute(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                org.hibernate.query.Query<SbaDelivery> query = session.createQuery("from SbaDelivery where canton=:canton and version=:version", SbaDelivery.class);
                query.setParameter("canton", canton);
                query.setParameter("version", version);
                return query.getResultList();
            }
        });
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sba.server.integration.repository.IDeliveryRepository#getDeliveryById(java.lang.Long)
     */
    @Override
    public SbaDelivery getDeliveryById(Long deliveryId) {
        org.hibernate.query.Query<SbaDelivery> query = currentSession().createQuery(
                "from SbaDelivery d left join fetch d.plausierrors pe left join fetch pe.plausi where d.deliveryId=:deliveryId",
                SbaDelivery.class);
        query.setParameter("deliveryId", deliveryId);
        return query.uniqueResult();
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sba.server.integration.repository.IDeliveryRepository#getDeliveryById(java.lang.Long, org.hibernate.LockMode)
     */
    @Override
    public SbaDelivery getDeliveryById(Long deliveryId, LockMode lockMode) {
        org.hibernate.query.Query<SbaDelivery> query = currentSession().createQuery(
                "from SbaDelivery d left join fetch d.plausierrors pe left join fetch pe.plausi where d.deliveryId=:deliveryId",
                SbaDelivery.class);
        query.setParameter("deliveryId", deliveryId);
        query.setLockMode("d", lockMode);
        return query.uniqueResult();
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<SbaPlausiError> getTopPlausiErrorsForDelivery(final Long deliveryId) {
        return (List<SbaPlausiError>) getHibernateTemplate().execute(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                Query q = session.createQuery("from SbaDelivery d where d.deliveryId=:deliveryId");
                q.setLong("deliveryId", deliveryId);
                if ((SbaDelivery) q.uniqueResult() == null) {
                    return null;
                }

                q = session.createQuery(
                        "from SbaPlausiError as pe left join fetch pe.plausi where pe.personId is null and pe.isToDelete=0 and pe.deliveryId=:deliveryId order by pe.isConfirmed, pe.plausi, pe.errorId");
                q.setLong("deliveryId", deliveryId);
                q.setMaxResults(ResultBase.MAX_NUMBER_ERRORS + 1);
                return q.list();
            }
        });
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sba.server.integration.repository.IDeliveryRepository#getDeliveryByIdentification(java.lang.Long, java.lang.Long, java.lang.String)
     */
    @Override
    public SbaDelivery getDeliveryByIdentification(final Long canton, final Long version, final String id) {
        return (SbaDelivery) getHibernateTemplate().execute(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                org.hibernate.query.Query<SbaDelivery> query = session.createQuery("from SbaDelivery where canton=:canton and version=:version and deliveryCode=:id", SbaDelivery.class);
                query.setParameter("canton", canton);
                query.setParameter("version", version);
                query.setParameter("id", id);
                return query.uniqueResult();
            }
        });
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sba.server.integration.repository.IDeliveryRepository#updateDelivery(ch.bfs.meb.sba.server.integration.dto.SbaDelivery)
     */
    @Override
    public SbaDelivery updateDelivery(SbaDelivery delivery) {
        delivery = (SbaDelivery) getHibernateTemplate().merge(delivery);
        getHibernateTemplate().flush();
        return delivery;
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sba.server.integration.repository.IDeliveryRepository#refreshDeliveryNumbers(ch.bfs.meb.sba.server.integration.dto.SbaDelivery)
     */
    @SuppressWarnings("unchecked")
    @Override
    public SbaDelivery refreshDeliveryNumbers(final SbaDelivery delivery) {
        delivery.resetDeliveryNumbers();

        List<Object[]> resultList = (List<Object[]>) getHibernateTemplate().execute(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                org.hibernate.query.Query<Object[]> query = session.createQuery(
                        "select plausiStatus, deliveryStatus, count (personId) from SbaPerson where deliveryId=:deliveryId and isToDelete=0 group by plausiStatus, deliveryStatus", Object[].class);
                query.setParameter("deliveryId", delivery.getDeliveryId());
                return query.list();
            }
        });
        for (Object[] row : resultList) {
            Long personPlausiStatus = (Long) row[0];
            Long personStatus = (Long) row[1];
            Long nrPersons = (Long) row[2];

            delivery.addPlausiPersons(personPlausiStatus, personStatus, nrPersons);
        }

        resultList = (List<Object[]>) getHibernateTemplate().execute(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                NativeQuery<Object[]> query = session.createNativeQuery(" select " + CodegroupUtility.MEB_PLAUSISTATUS_VALID
                        + " as plausistatus, count(q.QUALIFICATIONID) as nr from SBA_PERSONS p, SBA_QUALIFICATIONS q where q.PERSONID=p.PERSONID and p.deliveryId=:deliveryId and p.isToDelete=0 and q.plausiStatus > "
                        + CodegroupUtility.MEB_PLAUSISTATUS_NOTVALID + " union" + " select " + CodegroupUtility.MEB_PLAUSISTATUS_UNDEFINED
                        + " as plausistatus, count(q.QUALIFICATIONID) as nr from SBA_PERSONS p, SBA_QUALIFICATIONS q where q.PERSONID=p.PERSONID and p.deliveryId=:deliveryId and p.isToDelete=0");
                query.setParameter("deliveryId", delivery.getDeliveryId());
                return query.getResultList();
            }
        });
        for (Object[] row : resultList) {
            Long qualificationPlausiStatus = ((BigDecimal) row[0]).longValue();
            Long nrQualifications = ((BigDecimal) row[1]).longValue();

            delivery.addPlausiQualifications(qualificationPlausiStatus, nrQualifications);
        }
        for (SbaPlausiError error : delivery.getPlausierrors()) {
            error.loadPlausiData();
        }
        return delivery;
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sba.server.integration.repository.IDeliveryRepository#setAllPersonsToDelete(java.lang.Long)
     */
    @Override
    public void setAllPersonsToDelete(Long deliveryId) {
        getHibernateTemplate().execute(new HibernateCallback<Void>() {
            @Override
            public Void doInHibernate(Session session) throws HibernateException {
                org.hibernate.query.Query query = session.createQuery("update SbaPerson set isToDelete=1 where deliveryId=:deliveryId");
                query.setParameter("deliveryId", deliveryId);
                query.executeUpdate();
                session.flush();
                return null;
            }
        });
    }

    @Override
    public void markReplacedPersonsToDelete(Long deliveryId) {
        final String queryString = "update SbaPerson p set p.isToDelete=1 where p.deliveryId = :deliveryId "
                + "and p.deliveryStatus > :deliveryStatusA and "
                + "exists (select p2 from SbaPerson p2 where p2.deliveryId = p.deliveryId "
                + "and p2.idType = p.idType and p2.id = p.id and p2.deliveryStatus <= :deliveryStatusB)";

        getHibernateTemplate().execute(new HibernateCallback<Void>() {
            public Void doInHibernate(Session session) throws HibernateException {
                org.hibernate.query.Query query = session.createQuery(queryString);
                query.setParameter("deliveryId", deliveryId);
                query.setParameter("deliveryStatusA", CodegroupUtility.MEB_DATASTATUS_IMPORTED);
                query.setParameter("deliveryStatusB", CodegroupUtility.MEB_DATASTATUS_IMPORTED);
                query.executeUpdate();
                return null;
            }
        });

        getHibernateTemplate().flush();
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sba.server.integration.repository.IDeliveryRepository#setDeliveryErrorsToDelete(java.lang.Long)
     */
    @Override
    public void setDeliveryErrorsToDelete(Long deliveryId, boolean deliveryOnly) {
        if (deliveryOnly) {
            String queryString = "update SbaPlausiError set isToDelete=1 where errorId in "
                    + "(select e.errorId from SbaPlausiError e where e.deliveryId=:deliveryId and e.plausi in "
                    + "(select distinct(p1.plausiId) from SbaPlausi p1, SbaPlausi p2 where p1.objectLevel=:objectLevel or (p1.name_de = p2.name_de and p2.objectLevel=:objectLevel)))";

            getHibernateTemplate().execute((Session session) -> {
                org.hibernate.query.Query query = session.createQuery(queryString);
                query.setParameter("deliveryId", deliveryId);
                query.setParameter("objectLevel", CodegroupUtility.SBA_OBJECTTYPE_DELIVERY);
                return query.executeUpdate();
            });

        } else {
            String queryString = "update SbaPlausiError set isToDelete=1 where errorId in "
                    + "(select e.errorId from SbaPlausiError e where e.deliveryId=:deliveryId and e.plausi in "
                    + "(select distinct(p1.plausiId) from SbaPlausi p1, SbaPlausi p2 where (p1.objectLevel>=:objectLevelMin and p1.objectLevel<=:objectLevelMax) or (p1.name_de = p2.name_de and p2.objectLevel>=:objectLevelMin and p2.objectLevel<=:objectLevelMax)))";

            getHibernateTemplate().execute((Session session) -> {
                org.hibernate.query.Query query = session.createQuery(queryString);
                query.setParameter("deliveryId", deliveryId);
                query.setParameter("objectLevelMin", CodegroupUtility.SBA_OBJECTTYPE_DELIVERY);
                query.setParameter("objectLevelMax", CodegroupUtility.SBA_OBJECTTYPE_QUALIFICATION);
                return query.executeUpdate();
            });
        }

        getHibernateTemplate().flush();
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sba.server.integration.repository.IDeliveryRepository#deleteMarkedObjects(java.lang.Long)
     */
    @Override
    public void deleteMarkedObjects(Long deliveryId) {
        getHibernateTemplate().execute(new HibernateCallback<Void>() {
            @Override
            public Void doInHibernate(Session session) throws HibernateException {
                org.hibernate.query.Query query1 = session.createQuery("delete from SbaPlausiError where isToDelete=1 and deliveryId=:deliveryId");
                query1.setParameter("deliveryId", deliveryId);
                query1.executeUpdate();

                org.hibernate.query.Query query2 = session.createQuery(
                        "delete from SbaPlausiError where errorId in (select e.errorId from SbaPlausiError e, SbaPerson p where p.personId=e.personId and p.isToDelete=1 and p.deliveryId=:deliveryId)");
                query2.setParameter("deliveryId", deliveryId);
                query2.executeUpdate();

                org.hibernate.query.Query query3 = session.createQuery(
                        "delete from SbaQualification where qualificationId in (select q.qualificationId from SbaQualification q, SbaPerson p where p.personId=q.personId and p.isToDelete=1 and p.deliveryId=:deliveryId)");
                query3.setParameter("deliveryId", deliveryId);
                query3.executeUpdate();

                org.hibernate.query.Query query4 = session.createQuery("delete from SbaPerson where isToDelete=1 and deliveryId=:deliveryId");
                query4.setParameter("deliveryId", deliveryId);
                query4.executeUpdate();

                session.flush();
                return null;
            }
        });
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sba.server.integration.repository.IDeliveryRepository#restoreMarkedObjects(java.lang.Long)
     */
    @Override
    public void restoreMarkedObjects(Long deliveryId) {
        // delete new data
        // delete new plausierrors on delivery
        getHibernateTemplate().execute(session -> {
            String hql = "delete from SbaPlausiError " +
                    "where errorId in (" +
                    " select e.errorId " +
                    " from SbaPlausiError e, SbaPlausi p " +
                    " where e.isToDelete = false " +
                    " and e.deliveryId = :deliveryId " +
                    " and e.plausi = p.plausiId " +
                    " and p.objectLevel = :objectLevel)";
            Query query = session.createQuery(hql);
            query.setParameter("deliveryId", deliveryId);
            query.setParameter("objectLevel", CodegroupUtility.SBA_OBJECTTYPE_DELIVERY);
            return query.executeUpdate();
        });

        // delete new plausierrors on persons (and qualifications...)
        getHibernateTemplate().execute(session -> {
            String hql = "delete from SbaPlausiError " +
                    "where errorId in (" +
                    " select e.errorId " +
                    " from SbaPlausiError e, SbaPerson p " +
                    " where e.personId = p.personId " +
                    " and p.isToDelete = false " +
                    " and p.deliveryId = :deliveryId)";
            Query query = session.createQuery(hql);
            query.setParameter("deliveryId", deliveryId);
            return query.executeUpdate();
        });

        // delete new qualifications
        getHibernateTemplate().execute(session -> {
            String hql = "delete from SbaQualification " +
                    "where qualificationId in (" +
                    " select q.qualificationId " +
                    " from SbaQualification q, SbaPerson p " +
                    " where q.personId = p.personId " +
                    " and p.isToDelete = false " +
                    " and p.deliveryId = :deliveryId " +
                    " and q.deliveryStatus = :deliveryStatus)";
            Query query = session.createQuery(hql);
            query.setParameter("deliveryId", deliveryId);
            query.setParameter("deliveryStatus", CodegroupUtility.MEB_DATASTATUS_IMPORTED);
            return query.executeUpdate();
        });

        // delete new persons
        getHibernateTemplate().execute(session -> {
            String hql = "delete from SbaPerson " +
                    "where isToDelete = false " +
                    "and deliveryId = :deliveryId " +
                    "and deliveryStatus = :deliveryStatus";
            Query query = session.createQuery(hql);
            query.setParameter("deliveryId", deliveryId);
            query.setParameter("deliveryStatus", CodegroupUtility.MEB_DATASTATUS_IMPORTED);
            return query.executeUpdate();
        });

        // restore old data
        getHibernateTemplate().execute(session -> {
            String hql = "update SbaPlausiError " +
                    "set isToDelete = false " +
                    "where isToDelete = true " +
                    "and deliveryId = :deliveryId";
            Query query = session.createQuery(hql);
            query.setParameter("deliveryId", deliveryId);
            return query.executeUpdate();
        });

        getHibernateTemplate().execute(session -> {
            String hql = "update SbaPerson " +
                    "set isToDelete = false " +
                    "where isToDelete = true " +
                    "and deliveryId = :deliveryId";
            Query query = session.createQuery(hql);
            query.setParameter("deliveryId", deliveryId);
            return query.executeUpdate();
        });

        getHibernateTemplate().flush();
    }


    /* (non-Javadoc)
     * @see ch.bfs.meb.sba.server.integration.repository.IDeliveryRepository#updateDeliveredObjects(java.lang.Long)
     */
    @Override
    public void updateDeliveredObjects(final Long deliveryId) {
        getHibernateTemplate().execute(new HibernateCallback<Void>() {
            @Override
            public Void doInHibernate(Session session) throws HibernateException {
                org.hibernate.query.Query query1 = session.createQuery(
                        "update SbaQualification set deliveryStatus=:deliveryStatus where qualificationId in " +
                                " (select q.qualificationId from SbaQualification q, SbaPerson p where " +
                                "q.personId=p.personId and p.isToDelete=0 and p.deliveryId=:deliveryId and q.deliveryStatus=:deliveryStatusImported)");
                query1.setParameter("deliveryStatus", CodegroupUtility.MEB_DATASTATUS_DELIVERED);
                query1.setParameter("deliveryId", deliveryId);
                query1.setParameter("deliveryStatusImported", CodegroupUtility.MEB_DATASTATUS_IMPORTED);
                query1.executeUpdate();

                org.hibernate.query.Query query2 = session.createQuery(
                        "update SbaPerson set deliveryStatus=:deliveryStatus where isToDelete=0 and deliveryId=:deliveryId and deliveryStatus=:deliveryStatusImported");
                query2.setParameter("deliveryStatus", CodegroupUtility.MEB_DATASTATUS_DELIVERED);
                query2.setParameter("deliveryId", deliveryId);
                query2.setParameter("deliveryStatusImported", CodegroupUtility.MEB_DATASTATUS_IMPORTED);
                query2.executeUpdate();

                session.flush();
                return null;
            }
        });
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sba.server.integration.repository.IDeliveryRepository#updateConfigDeliveryCode(ch.bfs.meb.sba.server.integration.dto.SbaDelivery, java.lang.String)
     */
    @Override
    public void updateConfigDeliveryCode(SbaDelivery delivery, String configDeliveryCode) {
        getHibernateTemplate().execute(new HibernateCallback<Void>() {
            @Override
            public Void doInHibernate(Session session) throws HibernateException {
                org.hibernate.query.Query query1 = session.createQuery(
                        "update SbaQualification set configDeliveryCode=:configDeliveryCode where qualificationId in " +
                                "(select q.qualificationId from SbaQualification q, SbaPerson p where q.personId=p.personId and p.deliveryId=:deliveryId)");
                query1.setParameter("configDeliveryCode", configDeliveryCode);
                query1.setParameter("deliveryId", delivery.getDeliveryId());
                query1.executeUpdate();

                org.hibernate.query.Query query2 = session.createQuery("update SbaPerson set configDeliveryCode=:configDeliveryCode where deliveryId=:deliveryId");
                query2.setParameter("configDeliveryCode", configDeliveryCode);
                query2.setParameter("deliveryId", delivery.getDeliveryId());
                query2.executeUpdate();

                session.flush();
                return null;
            }
        });
        delivery.setConfigDeliveryCode(configDeliveryCode);
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sba.server.integration.repository.IDeliveryRepository#updatePlausistatus(java.lang.Long)
     */
    @Override
    public void updatePlausistatus(Long deliveryId) {
        getHibernateTemplate().execute((Session session) -> {
            org.hibernate.query.Query query = session.createQuery(
                    "update SbaDelivery set plausiStatus=case " +
                            "when (select count(e) from SbaPlausiError e where e.deliveryId=:deliveryId and e.personId is null)=0 then 2 " +
                            "when (select count(e) from SbaPlausiError e where e.deliveryId=:deliveryId and e.personId is null and e.isConfirmed=0)>0 then 1 " +
                            "else 3 end " +
                            "where deliveryId=:deliveryId");
            query.setParameter("deliveryId", deliveryId);
            query.executeUpdate();
            session.flush();
            return null;
        });
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sba.server.integration.repository.IDeliveryRepository#updateAllPlausistatus(java.lang.Long)
     */
    @Override
    public void updateAllPlausistatus(Long deliveryId) {
        getHibernateTemplate().execute(new HibernateCallback<Void>() {
            @Override
            public Void doInHibernate(Session session) throws HibernateException {
                org.hibernate.query.Query query1 = session.createQuery(
                        "update SbaQualification q set plausiStatus=case when (select count(e) from SbaPlausiError e where e.qualificationId=q.qualificationId)=0 then 2 when (select count(e) from SbaPlausiError e where e.qualificationId=q.qualificationId and e.isConfirmed=0)>0 then 1 else 3 end " +
                                "where qualificationId in (select q.qualificationId from SbaQualification q, SbaPerson p where q.personId=p.personId and p.isToDelete=0 and p.deliveryId=:deliveryId)");
                query1.setParameter("deliveryId", deliveryId);
                query1.executeUpdate();

                org.hibernate.query.Query query2 = session.createQuery(
                        "update SbaPerson p set plausiStatus=case when (select count(e) from SbaPlausiError e where e.personId=p.personId and e.qualificationId is null)=0 then 2 when (select count(e) from SbaPlausiError e where e.personId=p.personId and e.qualificationId is null and e.isConfirmed=0)>0 then 1 else 3 end where isToDelete=0 and deliveryId=:deliveryId");
                query2.setParameter("deliveryId", deliveryId);
                query2.executeUpdate();

                org.hibernate.query.Query query3 = session.createQuery(
                        "update SbaDelivery set plausiStatus=case when (select count(e) from SbaPlausiError e where e.isToDelete=0 and e.deliveryId=:deliveryId and e.personId is null)=0 then 2 when (select count(e) from SbaPlausiError e where e.isToDelete=0 and e.deliveryId=:deliveryId and e.personId is null and e.isConfirmed=0)>0 then 1 else 3 end where deliveryId=:deliveryId");
                query3.setParameter("deliveryId", deliveryId);
                query3.executeUpdate();

                session.flush();
                return null;
            }
        });
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sba.server.integration.repository.IDeliveryRepository#existsPerson(java.lang.Long)
     */
    @Override
    public boolean existsPerson(final Long deliveryId) {
        return (Long) getHibernateTemplate().execute((Session session) -> {
            org.hibernate.query.Query query = session.createQuery(
                    "select count(p) from SbaPerson p where p.isToDelete=0 and p.deliveryId=:deliveryId and rownum = 1");
            query.setParameter("deliveryId", deliveryId);
            return query.uniqueResult();
        }) > 0L;
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sba.server.integration.repository.IDeliveryRepository#getNumberOfObjects(java.lang.Long)
     */
    @Override
    public Long getNumberOfPersons(final Long deliveryId) {
        return (Long) getHibernateTemplate().execute((Session session) -> {
            org.hibernate.query.Query query = session.createQuery("select count(p) from SbaPerson p where p.isToDelete=0 and p.deliveryId=:deliveryId");
            query.setParameter("deliveryId", deliveryId);
            return query.uniqueResult();
        });
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sba.server.integration.repository.IDeliveryRepository#getNumberOfQualifications(java.lang.Long)
     */
    @Override
    public Long getNumberOfQualifications(final Long deliveryId) {
        return (Long) getHibernateTemplate().execute((Session session) -> {
            org.hibernate.query.Query query = session.createQuery(
                    "select count(distinct a) from SbaPerson p, SbaQualification a where a.personId=p.personId and p.isToDelete=0 and p.deliveryId=:deliveryId");
            query.setParameter("deliveryId", deliveryId);
            return query.uniqueResult();
        });
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sba.server.integration.repository.IDeliveryRepository#modifiedAfter(java.lang.Long, java.util.Date)
     */
    @Override
    public boolean modifiedAfter(final Long deliveryId, final Date modificationDate) {
        // check qualifications
        Long newObjects = (Long) getHibernateTemplate().execute((Session session) -> {
            org.hibernate.query.Query query = session.createQuery(
                    "select count(a) from SbaPerson p, SbaQualification a where a.personId=p.personId and p.deliveryId=:deliveryId and a.modification_date>:modificationDate");
            query.setParameter("deliveryId", deliveryId);
            query.setParameter("modificationDate", modificationDate);
            return query.uniqueResult();
        });
        if (newObjects > 0L) {
            return true;
        }
        // check persons
        newObjects = (Long) getHibernateTemplate().execute((Session session) -> {
            org.hibernate.query.Query query = session.createQuery("select count(p) from SbaPerson p where p.deliveryId=:deliveryId and p.modification_date>:modificationDate");
            query.setParameter("deliveryId", deliveryId);
            query.setParameter("modificationDate", modificationDate);
            return query.uniqueResult();
        });
        return newObjects > 0L;
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sba.server.integration.repository.IDeliveryRepository#allPlausibel(java.lang.Long)
     */
    @Override
    public boolean allPlausibel(SbaDelivery delivery) {
        final Long deliveryId = delivery.getDeliveryId();
        // check qualification
        Long notPlausibel = (Long) getHibernateTemplate().execute((Session session) -> {
            org.hibernate.query.Query query = session.createQuery(
                    "select count(a) from SbaPerson p, SbaQualification a where a.personId=p.personId and p.deliveryId=:deliveryId and a.plausiStatus=:status");
            query.setParameter("deliveryId", deliveryId);
            query.setParameter("status", CodegroupUtility.MEB_PLAUSISTATUS_NOTVALID);
            return query.uniqueResult();
        });

        if (notPlausibel > 0L) {
            return false;
        }

        // check person
        notPlausibel = (Long) getHibernateTemplate().execute((Session session) -> {
            org.hibernate.query.Query query = session.createQuery(
                    "select count(p) from SbaPerson p where p.deliveryId=:deliveryId and p.plausiStatus=:status");
            query.setParameter("deliveryId", deliveryId);
            query.setParameter("status", CodegroupUtility.MEB_PLAUSISTATUS_NOTVALID);
            return query.uniqueResult();
        });

        if (notPlausibel > 0L) {
            return false;
        }

        return !delivery.getPlausiStatus().equals(CodegroupUtility.MEB_PLAUSISTATUS_NOTVALID);
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sba.server.integration.repository.IDeliveryRepository#prevalidate(java.lang.Long, java.lang.String)
     */
    @Override
    public void prevalidate(SbaDelivery delivery, String userEmail) {
        LOGGER.debug("prevalidate called: deliveryId={}, userEmail={}", delivery.getDeliveryId(), userEmail);
        try {
            Date now = new Date();
            Long deliveryId = delivery.getDeliveryId();

            getHibernateTemplate().execute((Session session) -> {
                String hql = "update SbaQualification set deliveryStatus=:status, prevalidation_user=:user, prevalidation_date=:now " +
                        "where qualificationId in (select q.qualificationId from SbaQualification q, SbaPerson p where q.personId=p.personId " +
                        "and p.deliveryId=:deliveryId and q.deliveryStatus=:qStatus)";
                org.hibernate.query.Query query = session.createQuery(hql);
                query.setParameter("status", CodegroupUtility.MEB_DATASTATUS_PREVALIDATED);
                query.setParameter("user", userEmail);
                query.setParameter("now", now);
                query.setParameter("deliveryId", deliveryId);
                query.setParameter("qStatus", CodegroupUtility.MEB_DATASTATUS_DELIVERED);
                int result = query.executeUpdate();
                return result;
            });

            getHibernateTemplate().execute((Session session) -> {
                String hql = "update SbaPerson set deliveryStatus=:status, prevalidation_user=:user, prevalidation_date=:now " +
                        "where deliveryId=:deliveryId and deliveryStatus=:pStatus";
                org.hibernate.query.Query query = session.createQuery(hql);
                query.setParameter("status", CodegroupUtility.MEB_DATASTATUS_PREVALIDATED);
                query.setParameter("user", userEmail);
                query.setParameter("now", now);
                query.setParameter("deliveryId", deliveryId);
                query.setParameter("pStatus", CodegroupUtility.MEB_DATASTATUS_DELIVERED);
                int result = query.executeUpdate();
                return result;
            });

            delivery.setDeliveryStatus(CodegroupUtility.MEB_DELIVERYSTATUS_PREVALIDATED);
            delivery.setPrevalidation_user(userEmail);
            delivery.setPrevalidation_date(now);
            getHibernateTemplate().merge(delivery);
            getHibernateTemplate().flush();
        } catch (Exception e) {
            LOGGER.error("Unexpected error in prevalidate: deliveryId={}, userEmail={}", delivery.getDeliveryId(), userEmail, e);
            throw e;
        }
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sba.server.integration.repository.IDeliveryRepository#validate(java.lang.Long, java.lang.Long)
     */
    @Override
    public void validate(SbaDelivery delivery, String userEmail) {
        Date now = new Date();
        Long deliveryId = delivery.getDeliveryId();
        // prevalidation user and date have to be set in a second update statement because Hql cannot
        // handle parameter in then branch of case statement (Hibernate bug HHH-4700)
        getHibernateTemplate().execute((Session session) -> {
            String hql = "update SbaQualification set deliveryStatus=:status, validation_user=:user, validation_date=:now " +
                    "where qualificationId in (select q.qualificationId from SbaQualification q, SbaPerson p where q.personId=p.personId " +
                    "and p.deliveryId=:deliveryId and q.deliveryStatus in (:delivered, :prevalidated))";
            org.hibernate.query.Query query = session.createQuery(hql);
            query.setParameter("status", CodegroupUtility.MEB_DATASTATUS_VALIDATED);
            query.setParameter("user", userEmail);
            query.setParameter("now", now);
            query.setParameter("deliveryId", deliveryId);
            query.setParameter("delivered", CodegroupUtility.MEB_DATASTATUS_DELIVERED);
            query.setParameter("prevalidated", CodegroupUtility.MEB_DATASTATUS_PREVALIDATED);
            return query.executeUpdate();
        });
        getHibernateTemplate().execute((Session session) -> {
            String hql = "update SbaQualification set prevalidation_user = case when prevalidation_user is null then validation_user else prevalidation_user end, prevalidation_date = case when prevalidation_date is null then validation_date else prevalidation_date end where qualificationId in (select q.qualificationId from SbaQualification q, SbaPerson p where q.personId=p.personId and p.deliveryId=:deliveryId and q.deliveryStatus=:status)";
            org.hibernate.query.Query query = session.createQuery(hql);
            query.setParameter("deliveryId", deliveryId);
            query.setParameter("status", CodegroupUtility.MEB_DATASTATUS_VALIDATED);
            return query.executeUpdate();
        });
        getHibernateTemplate().execute(session -> {
            String hql = "update SbaPerson set deliveryStatus=:deliveryStatus, validation_user=:user, validation_date=:now where deliveryId=:delId and deliveryStatus in (:status1, :status2)";
            org.hibernate.query.Query query = session.createQuery(hql);
            query.setParameter("deliveryStatus", CodegroupUtility.MEB_DATASTATUS_VALIDATED);
            query.setParameter("user", userEmail);
            query.setParameter("now", now);
            query.setParameter("delId", deliveryId);
            query.setParameter("status1", CodegroupUtility.MEB_DATASTATUS_DELIVERED);
            query.setParameter("status2", CodegroupUtility.MEB_DATASTATUS_PREVALIDATED);
            return query.executeUpdate();
        });
        getHibernateTemplate().execute((Session session) -> {
            String hql = "update SbaPerson set prevalidation_user = case when prevalidation_user is null then validation_user else prevalidation_user end, prevalidation_date = case when prevalidation_date is null then validation_date else prevalidation_date end where deliveryId =:delivery_Id and deliveryStatus =:delivery_status";
            org.hibernate.query.Query query = session.createQuery(hql);
            query.setParameter("delivery_Id", deliveryId);
            query.setParameter("delivery_status", CodegroupUtility.MEB_DATASTATUS_VALIDATED);
            return query.executeUpdate();
        });
        delivery.setDeliveryStatus(CodegroupUtility.MEB_DELIVERYSTATUS_VALIDATED);
        delivery.setValidation_user(userEmail);
        delivery.setValidation_date(now);
        if (delivery.getPrevalidation_user() == null) {
            delivery.setPrevalidation_user(userEmail);
            delivery.setPrevalidation_date(now);
        }
        getHibernateTemplate().merge(delivery);
        getHibernateTemplate().flush();
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sba.server.integration.repository.IDeliveryRepository#undoPrevalidate(java.lang.Long)
     */
    @Override
    public void undoPrevalidate(SbaDelivery delivery) {
        Long deliveryId = delivery.getDeliveryId();
        getHibernateTemplate().execute((Session session) -> {
            String hql = "update SbaQualification set deliveryStatus=:status, prevalidation_user=null, prevalidation_date=null where qualificationId in (select q.qualificationId from SbaQualification q, SbaPerson p where q.personId=p.personId and p.deliveryId=:deliveryId and q.deliveryStatus=:qStatus)";
            org.hibernate.query.Query query = session.createQuery(hql);
            query.setParameter("status", CodegroupUtility.MEB_DATASTATUS_DELIVERED);
            query.setParameter("deliveryId", deliveryId);
            query.setParameter("qStatus", CodegroupUtility.MEB_DATASTATUS_PREVALIDATED);
            return query.executeUpdate();
        });

        getHibernateTemplate().execute((Session session) -> {
            String hql = "update SbaPerson set deliveryStatus=:status, prevalidation_user=null, prevalidation_date=null where deliveryId=:deliveryId and deliveryStatus=:pStatus";
            org.hibernate.query.Query query = session.createQuery(hql);
            query.setParameter("status", CodegroupUtility.MEB_DATASTATUS_DELIVERED);
            query.setParameter("deliveryId", deliveryId);
            query.setParameter("pStatus", CodegroupUtility.MEB_DATASTATUS_PREVALIDATED);
            return query.executeUpdate();
        });

        delivery.setDeliveryStatus(CodegroupUtility.MEB_DELIVERYSTATUS_DELIVERED);
        delivery.setPrevalidation_user(null);
        delivery.setPrevalidation_date(null);
        getHibernateTemplate().merge(delivery);
        getHibernateTemplate().flush();
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sba.server.integration.repository.IDeliveryRepository#undoValidate(java.lang.Long)
     */
    @Override
    public void undoValidate(SbaDelivery delivery) {
        Long deliveryId = delivery.getDeliveryId();
        getHibernateTemplate().execute((Session session) -> {
            String hql = "update SbaQualification set deliveryStatus=:deliveryStatus, validation_user=null, validation_date=null where qualificationId in (select q.qualificationId from SbaQualification q, SbaPerson p where q.personId=p.personId and p.deliveryId=:delId and q.deliveryStatus=:qStatus)";
            org.hibernate.query.Query query = session.createQuery(hql);
            query.setParameter("deliveryStatus", CodegroupUtility.MEB_DATASTATUS_PREVALIDATED);
            query.setParameter("delId", deliveryId);
            query.setParameter("qStatus", CodegroupUtility.MEB_DATASTATUS_VALIDATED);
            return query.executeUpdate();
        });

        getHibernateTemplate().execute((Session session) -> {
            String hql = "update SbaPerson set deliveryStatus=:deliveryStatus, validation_user=null, validation_date=null where deliveryId=:delId and deliveryStatus=:pStatus";
            org.hibernate.query.Query query = session.createQuery(hql);
            query.setParameter("deliveryStatus", CodegroupUtility.MEB_DATASTATUS_PREVALIDATED);
            query.setParameter("delId", deliveryId);
            query.setParameter("pStatus", CodegroupUtility.MEB_DATASTATUS_VALIDATED);
            return query.executeUpdate();
        });

        delivery.setDeliveryStatus(CodegroupUtility.MEB_DELIVERYSTATUS_PREVALIDATED);
        delivery.setValidation_user(null);
        delivery.setValidation_date(null);
        getHibernateTemplate().merge(delivery);
        getHibernateTemplate().flush();
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sba.server.integration.repository.IDeliveryRepository#deleteAll(java.lang.Long, java.lang.Long)
     */
    @Override
    public void deleteAll(Long deliveryId, Long checkStatus) {

        // check persons
        org.hibernate.query.Query query = currentSession().createQuery("from SbaPerson where deliveryId=:delId and deliveryStatus>=:status");
        query.setParameter("delId", deliveryId);
        query.setParameter("status", checkStatus);
        query.setMaxResults(1);
        if (query.list().size() > 0) {
            throw new MebUncheckedNotMonitoredException("maintain.delete.childrenValidatedError.messages");
        }

        // check qualifications
        query = currentSession().createQuery(
                "from SbaQualification where exists (from SbaQualification q, SbaPerson p where q.personId = p.personId and p.deliveryId=:delId and q.deliveryStatus>=:status)");
        query.setParameter("delId", deliveryId);
        query.setParameter("status", checkStatus);
        query.setMaxResults(1);
        if (query.list().size() > 0) {
            throw new MebUncheckedNotMonitoredException("maintain.delete.childrenValidatedError.messages");
        }

        // delete plausierrors on all objects of delivery
        getHibernateTemplate().execute((Session session) -> {
            String hql = "delete from SbaPlausiError where deliveryId=:delId";
            org.hibernate.query.Query deleteQuery = session.createQuery(hql);
            deleteQuery.setParameter("delId", deliveryId);
            return deleteQuery.executeUpdate();
        });

        // delete qualifications and persons
        getHibernateTemplate().execute((Session session) -> {
            String hql = "delete from SbaQualification where personId in (select p.personId from SbaPerson p where p.deliveryId=:delId)";
            org.hibernate.query.Query deleteQuery = session.createQuery(hql);
            deleteQuery.setParameter("delId", deliveryId);
            return deleteQuery.executeUpdate();
        });

        getHibernateTemplate().execute((Session session) -> {
            String hql = "delete from SbaPerson where deliveryId=:delId";
            org.hibernate.query.Query deleteQuery = session.createQuery(hql);
            deleteQuery.setParameter("delId", deliveryId);
            return deleteQuery.executeUpdate();
        });

        getHibernateTemplate().flush();
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sba.server.integration.repository.IDeliveryRepository#deleteAll(java.lang.Long)
     */
    @Override
    public void deleteAll(Long deliveryId) {
        // check if the delivery can be deleted
        MebUser user = (MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        long status = CodegroupUtility.MEB_DATASTATUS_PREVALIDATED;
        if (user.isInRole(SecurityConstants.ROLE_SBA_DV)) {
            status = CodegroupUtility.MEB_DATASTATUS_VALIDATED;
        }

        deleteAll(deliveryId, status);
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sba.server.integration.repository.IDeliveryRepository#deleteDelivery(ch.bfs.meb.sba.server.integration.dto.SbaDelivery)
     */
    @Override
    public void deleteDelivery(SbaDelivery delivery) {
        getHibernateTemplate().delete(delivery);
        getHibernateTemplate().flush();
    }

    @SuppressWarnings("unchecked")
    private List<SbaDelivery> getDeliveriesByIds(List<Long> deliveryIds) {
        if (deliveryIds == null || deliveryIds.isEmpty()) {
            return new ArrayList<SbaDelivery>();
        }

        // query deliveries
        Query queryResult = currentSession().createQuery("from SbaDelivery d where d.deliveryId in (:deliveryIds)");
        queryResult.setParameterList("deliveryIds", deliveryIds);
        // DistinctRootEntityResultTransformer not required => order by map below already does the same
        //		queryResult.setResultTransformer(DistinctRootEntityResultTransformer.INSTANCE);

        List<SbaDelivery> tempList = queryResult.list();

        // reestablish old sort order
        Map<Long, SbaDelivery> mapById = new HashMap<Long, SbaDelivery>(tempList.size());
        for (SbaDelivery entity : tempList) {
            mapById.put(entity.getDeliveryId(), entity);
        }
        List<SbaDelivery> resultList = new ArrayList<SbaDelivery>(mapById.size());
        for (Long id : deliveryIds) {
            SbaDelivery entity = mapById.get(id);
            if (entity != null) {
                resultList.add(entity);
            }
        }

        return resultList;
    }

    @Override
    @SuppressWarnings("unchecked")
    public HashMap<Long, String> getPersonConfirmRules(Long deliveryId) {
        HashMap<Long, String> personConfirmRules = new HashMap<Long, String>();
        List<Object[]> resultList = (List<Object[]>) getHibernateTemplate().execute((Session session) -> {
            String hql = "select p.personId, p.confirmRules from SbaPerson p where p.deliveryId=:delId and p.confirmRules is not null";
            org.hibernate.query.Query query = session.createQuery(hql);
            query.setParameter("delId", deliveryId);
            return query.list();
        });
        for (Object[] row : resultList) {
            Long personId = ((Number) row[0]).longValue();
            String confirmRules = (String) row[1];
            personConfirmRules.put(personId, confirmRules);
        }
        return personConfirmRules;
    }

    @Override
    @SuppressWarnings("unchecked")
    public HashMap<Long, String> getQualificationConfirmRules(Long deliveryId) {
        HashMap<Long, String> qualificationConfirmRules = new HashMap<Long, String>();

        List<Object[]> resultList = (List<Object[]>) getHibernateTemplate().execute((Session session) -> {
            String sql = "select q.qualificationId, q.confirmRules from Sba_Qualifications q, Sba_Persons p where q.personId = p.personId and p.deliveryId=:delId and q.confirmRules is not null";
            org.hibernate.query.NativeQuery<?> query = session.createNativeQuery(sql);
            query.setParameter("delId", deliveryId);
            return query.list();
        });
        for (Object[] row : resultList) {
            Long qualificationId = ((Number) row[0]).longValue();
            String confirmRules = (String) row[1];
            qualificationConfirmRules.put(qualificationId, confirmRules);
        }
        return qualificationConfirmRules;
    }

    /**
     * Gets the physical code group id as stored in database for a given column
     * name.
     *
     * @param colName
     *            column id of database table
     * @return physical code group id as stored in database
     */
    protected String getCodegroupId(String colName) {
        if (colName.equals("model." + DELIVERYSTATUS)) {
            return CodegroupUtility.MEB_DELIVERYSTATUS;
        } else if (colName.equals("model." + PLAUSISTATUS)) {
            return CodegroupUtility.MEB_PLAUSISTATUS;
        }

        return null;
    }

    /**
     * Returns all columns with underscores of according db table
     */
    protected List<String> getUnderscoreColumns() {
        ArrayList<String> underscoreColumns = new ArrayList<String>();
        underscoreColumns.add(CREATION_USER);
        underscoreColumns.add(CREATION_DATE);
        underscoreColumns.add(MODIFICATION_USER);
        underscoreColumns.add(MODIFICATION_DATE);
        underscoreColumns.add(PREVALIDATION_USER);
        underscoreColumns.add(PREVALIDATION_DATE);
        underscoreColumns.add(VALIDATION_USER);
        underscoreColumns.add(VALIDATION_DATE);
        return underscoreColumns;
    }

    /**
     * Returns all string columns of according db table
     */
    protected List<String> getStringColumns() {
        ArrayList<String> stringColumns = new ArrayList<String>();
        stringColumns.add(DELIVERYCODE);
        stringColumns.add(StringUtils.asCamelCase(CREATION_USER));
        stringColumns.add(StringUtils.asCamelCase(MODIFICATION_USER));
        stringColumns.add(StringUtils.asCamelCase(PREVALIDATION_USER));
        stringColumns.add(StringUtils.asCamelCase(VALIDATION_USER));
        stringColumns.add(USERTEXT);
        return stringColumns;
    }

    /**
     * Returns all date columns of according db table
     */
    protected List<String> getDateColumns() {
        ArrayList<String> dateColumns = new ArrayList<String>();
        dateColumns.add(DELIVERYDATE);
        dateColumns.add(StringUtils.asCamelCase(CREATION_DATE));
        dateColumns.add(StringUtils.asCamelCase(MODIFICATION_DATE));
        dateColumns.add(StringUtils.asCamelCase(PREVALIDATION_DATE));
        dateColumns.add(StringUtils.asCamelCase(VALIDATION_DATE));
        return dateColumns;
    }
}