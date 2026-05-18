/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sspserver

 */
package ch.bfs.meb.ssp.server.integration.repository;

import java.math.BigDecimal;
import java.util.*;

import org.hibernate.*;
import org.hibernate.query.NativeQuery;
import org.hibernate.type.StandardBasicTypes;
import org.springframework.orm.hibernate5.HibernateCallback;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;
import org.springframework.security.core.context.SecurityContextHolder;

import ch.bfs.meb.exception.MebUncheckedNotMonitoredException;
import ch.bfs.meb.integration.dto.ResultBase;
import ch.bfs.meb.security.MebUser;
import ch.bfs.meb.server.commons.integration.dto.FilterContext;
import ch.bfs.meb.server.commons.integration.dto.SortContext;
import ch.bfs.meb.server.commons.util.IFilterUtility;
import ch.bfs.meb.ssp.server.integration.dto.SspDelivery;
import ch.bfs.meb.ssp.server.integration.dto.SspPlausiError;
import ch.bfs.meb.util.CodegroupUtility;
import ch.bfs.meb.util.SecurityConstants;
import ch.bfs.meb.util.StringUtils;

import javax.persistence.Tuple;

/**
 * Repository for SspDeliveries.
 * 
 * @author $Author: msc $
 * @version $Revision: 957 $
 */
public class DeliveryRepository extends HibernateDaoSupport implements IDeliveryRepository {
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
     * @see ch.bfs.meb.ssp.server.integration.repository.IDeliveryRepository#getDeliveries()
     */
    @Override
    @SuppressWarnings("unchecked")
    public List<SspDelivery> getDeliveries(int start, int buffer, SortContext sortContext, FilterContext filterContext, Long version, Long canton) {
        String sortColumn = _filterUtility.adaptColumnName(
                sortContext.getSortColumn() == null ? MODIFICATION_DATE : sortContext.getSortColumn(),
                "model",
                getUnderscoreColumns()
        );

        String codegroupId = getCodegroupId(sortColumn);
        String whereSelection = _filterUtility.getWhereFilterSelection(
                filterContext,
                "model",
                getStringColumns(),
                getDateColumns(),
                getUnderscoreColumns()
        );

        String deliverySubquery = _filterUtility.getPredefinedFilterSubquery(
                filterContext,
                SecurityFilters.SSP_DELIVERIES_TABLE,
                true
        );

        Map<String, Object> parameters = new HashMap<>();

        if (canton != null && canton > 0L) {
            whereSelection += (whereSelection.isEmpty() ? "" : " and ") + "model.canton = :canton";
            parameters.put("canton", canton);
        }

        if (version != null) {
            whereSelection += (whereSelection.isEmpty() ? "" : " and ") + "model.version = :version";
            parameters.put("version", version);
        }

        String queryString;

        if (sortContext.getLocale() != null && codegroupId != null) {
            String locale = sortContext.getLocale();

            queryString = "select distinct model.deliveryId as deliveryId, " +
                    "(case when " + sortColumn + " is null then '' " +
                    "      when meb_cg.code is null then to_char(" + sortColumn + ") " +
                    "      else meb_cg.codetext end) as sorttext " +
                    "from " + deliverySubquery + " model " +
                    "left outer join ( " +
                    "  select cg1.* from Codegroups cg1, ( " +
                    "    select codegroupid, code, language, max(validFromYear) as validFromYear " +
                    "    from Codegroups " +
                    "    where codegroupid = :codegroupId and language = :locale " +
                    "    group by codegroupid, code, language " +
                    "  ) cg2 " +
                    "  where cg1.codegroupId = cg2.codegroupId and cg1.code = cg2.code " +
                    "    and cg1.language = cg2.language and cg1.validFromYear = cg2.validFromYear " +
                    ") meb_cg on meb_cg.code = " + sortColumn +
                    (whereSelection.isEmpty() ? "" : " where " + whereSelection) +
                    " order by sorttext " + (sortContext.getAscSortOrder() ? "asc" : "desc") + ", model.deliveryId asc";

            parameters.put("codegroupId", codegroupId);
            parameters.put("locale", locale);
        } else {
            queryString = "select distinct model.deliveryId as deliveryId, " + sortColumn + " as sorttext " +
                    "from " + deliverySubquery + " model " +
                    (whereSelection.isEmpty() ? "" : " where " + whereSelection) +
                    " order by " + sortColumn + " " + (sortContext.getAscSortOrder() ? "asc" : "desc") + ", model.deliveryId asc";
        }

        // Création de la requête native typée
        NativeQuery<Object[]> query = currentSession()
                .createNativeQuery(queryString)
                .addScalar("deliveryId", StandardBasicTypes.BIG_DECIMAL)
                .addScalar("sorttext", StandardBasicTypes.STRING);

        // Appliquer les paramètres dynamiques
        parameters.forEach(query::setParameter);

        // Pagination
        if (start >= 0) query.setFirstResult(start);
        if (buffer > 0) query.setMaxResults(buffer);

        // Exécution
        List<Object[]> queryResult = query.getResultList();
        List<Long> deliveryIds = new ArrayList<>();
        for (Object[] row : queryResult) {
            deliveryIds.add(((BigDecimal) row[0]).longValue());
        }

        return getDeliveriesByIds(deliveryIds);
    }


    @Override
    public Long getMaxNrOfDeliveries(FilterContext filterContext, Long version, Long canton) {
        String queryString;
        String whereSelection = _filterUtility.getWhereFilterSelection(filterContext, "model", getStringColumns(), getDateColumns(), getUnderscoreColumns());
        String deliverySubquery = _filterUtility.getPredefinedFilterSubquery(filterContext, SecurityFilters.SSP_DELIVERIES_TABLE, true);

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
        NativeQuery query =currentSession().createNativeQuery (queryString).addScalar("nrDeliveries");

        return ((BigDecimal) query.uniqueResult()).longValue();
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.ssp.server.integration.repository.IDeliveryRepository#getDeliveriesForCanton(java.lang.Long, java.lang.Long)
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<SspDelivery> getDeliveriesForCanton(final Long canton, final Long version) {
        return (List<SspDelivery>) getHibernateTemplate().execute(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                Query<SspDelivery> query = session.createQuery("from SspDelivery where canton= :canton and version= :version", SspDelivery.class);
                query.setParameter("canton", canton);
                query.setParameter("version", version);
                return query.list();
            }
        });
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.ssp.server.integration.repository.IDeliveryRepository#getDeliveryById(java.lang.Long)
     */
    @Override
    public SspDelivery getDeliveryById(Long deliveryId) {
        String hql = "select distinct d from SspDelivery d " +
                "left join fetch d.plausierrors pe " +
                "where d.deliveryId = :deliveryId";

        SspDelivery delivery = currentSession()
                .createQuery(hql, SspDelivery.class)
                .setParameter("deliveryId", deliveryId)
                .uniqueResult();

        // Initialiser pe.plausi manuellement si nécessaire
        if (delivery != null && delivery.getPlausierrors() != null) {
            delivery.getPlausierrors().forEach(pe -> {
                if (pe.getPlausi() != null) {
                    pe.getPlausi().getId(); // force l'initialisation de pe.plausi
                }
            });
        }

        return delivery;
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.ssp.server.integration.repository.IDeliveryRepository#getDeliveryById(java.lang.Long, org.hibernate.LockMode)
     */
    @Override
    public SspDelivery getDeliveryById(Long deliveryId, LockMode lockMode) {
        Query<SspDelivery> query = currentSession().createQuery(
                "from SspDelivery d left join fetch d.plausierrors pe left join fetch pe.plausi where d.deliveryId= :deliveryId",
                SspDelivery.class
        );
        query.setParameter("deliveryId", deliveryId);
        query.setLockMode("d", lockMode);
        return query.uniqueResult();
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<SspPlausiError> getTopPlausiErrorsForDelivery(final Long deliveryId) {
        return (List<SspPlausiError>) getHibernateTemplate().execute(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                Query q = session.createQuery("from SspDelivery d where d.deliveryId=:deliveryId");
                q.setLong("deliveryId", deliveryId);
                if ((SspDelivery) q.uniqueResult() == null) {
                    return null;
                }

                q = session.createQuery(
                        "from SspPlausiError as pe left join fetch pe.plausi where pe.personId is null and pe.isToDelete=0 and pe.deliveryId=:deliveryId order by pe.isConfirmed, pe.plausi, pe.errorId");
                q.setLong("deliveryId", deliveryId);
                q.setMaxResults(ResultBase.MAX_NUMBER_ERRORS + 1);
                return q.list();
            }
        });
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.ssp.server.integration.repository.IDeliveryRepository#getDeliveryByIdentification(java.lang.Long, java.lang.Long, java.lang.String)
     */
    @Override
    public SspDelivery getDeliveryByIdentification(final Long canton, final Long version, final String id) {
        return (SspDelivery) getHibernateTemplate().execute(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                Query<SspDelivery> query = session.createQuery(
                        "from SspDelivery where canton= :canton and version= :version and deliveryCode= :deliveryCode",
                        SspDelivery.class
                );
                query.setParameter("canton", canton);
                query.setParameter("version", version);
                query.setParameter("deliveryCode", id);
                return query.uniqueResult();
            }
        });
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.ssp.server.integration.repository.IDeliveryRepository#updateDelivery(ch.bfs.meb.ssp.server.integration.dto.SspDelivery)
     */
    @Override
    public SspDelivery updateDelivery(SspDelivery delivery) {
        delivery = (SspDelivery) getHibernateTemplate().merge(delivery);
        getHibernateTemplate().flush();
        return delivery;
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.ssp.server.integration.repository.IDeliveryRepository#refreshDeliveryNumbers(ch.bfs.meb.ssp.server.integration.dto.SspDelivery)
     */
    @SuppressWarnings("unchecked")
    @Override
    public SspDelivery refreshDeliveryNumbers(final SspDelivery delivery) {
        delivery.resetDeliveryNumbers();

        List<Object[]> resultList = (List<Object[]>) getHibernateTemplate().execute((HibernateCallback<Object>) session -> {
            NativeQuery<Object[]> query = session.createNativeQuery(
                    "select plausiStatus as plausiStatus, deliveryStatus as deliveryStatus, count(personId) as count " +
                            "from SSP_PERSONS where deliveryId = :deliveryId and isToDelete = 0 group by plausiStatus, deliveryStatus"
            );
            query.setParameter("deliveryId", delivery.getDeliveryId());
            query.addScalar("plausiStatus", org.hibernate.type.StandardBasicTypes.LONG);
            query.addScalar("deliveryStatus", org.hibernate.type.StandardBasicTypes.LONG);
            query.addScalar("count", org.hibernate.type.StandardBasicTypes.LONG);
            return query.list();
        });

        for (Object[] row : resultList) {
            Long personPlausiStatus = ((Number) row[0]).longValue();
            Long personStatus = ((Number) row[1]).longValue();
            Long nrPersons = ((Number) row[2]).longValue();
            delivery.addPlausiPersons(personPlausiStatus, personStatus, nrPersons);
        }

        // Deuxième requête
        resultList = (List<Object[]>) getHibernateTemplate().execute((HibernateCallback<Object>) session -> {
            String sql =
                    "select " + CodegroupUtility.MEB_PLAUSISTATUS_VALID + " as plausistatus, count(a.ACTIVITYID) as nr " +
                            "from SSP_PERSONS p, SSP_ACTIVITIES a " +
                            "where a.PERSONID = p.PERSONID and p.deliveryId = :deliveryId1 and p.isToDelete = 0 and a.plausiStatus > " + CodegroupUtility.MEB_PLAUSISTATUS_NOTVALID + " " +
                            "union " +
                            "select " + CodegroupUtility.MEB_PLAUSISTATUS_UNDEFINED + " as plausistatus, count(a.ACTIVITYID) as nr " +
                            "from SSP_PERSONS p, SSP_ACTIVITIES a " +
                            "where a.PERSONID = p.PERSONID and p.deliveryId = :deliveryId2 and p.isToDelete = 0";

            NativeQuery<Object[]> query = session.createNativeQuery(sql);
            query.setParameter("deliveryId1", delivery.getDeliveryId());
            query.setParameter("deliveryId2", delivery.getDeliveryId());
            return query.list();
        });

        for (Object[] row : resultList) {
            Long activityPlausiStatus = ((Number) row[0]).longValue();
            Long nrActivities = ((Number) row[1]).longValue();
            delivery.addPlausiActivities(activityPlausiStatus, nrActivities);
        }

        for (SspPlausiError error : delivery.getPlausierrors()) {
            error.loadPlausiData();
        }
        return delivery;
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.ssp.server.integration.repository.IDeliveryRepository#setAllPersonsToDelete(java.lang.Long)
     */
    @Override
    public void setAllPersonsToDelete(Long deliveryId) {
        getHibernateTemplate().execute(session -> {
            Query query = session.createQuery("update SspPerson set isToDelete = :isToDelete where deliveryId = :deliveryId");
            query.setParameter("isToDelete", true);
            query.setParameter("deliveryId", deliveryId);
            int result = query.executeUpdate();
            session.flush();
            return result;
        });
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.ssp.server.integration.repository.IDeliveryRepository#setDeliveryErrorsToDelete(java.lang.Long)
     */
    @Override
    public void setDeliveryErrorsToDelete(Long deliveryId, boolean deliveryOnly) {
        if (deliveryOnly) {
            // TODO lsc: instead of name_de take business id from plausi
            String queryString = "update SspPlausiError set isToDelete=1 where errorId in "
                    + "(select e.errorId from SspPlausiError e where e.deliveryId=:deliveryId and e.plausi in "
                    + "(select distinct(p1.plausiId) from SspPlausi p1, SspPlausi p2 "
                    + "where p1.objectLevel=:objectLevel "
                    + "or (p1.name_de = p2.name_de and p2.objectLevel=:objectLevel)))";

            getHibernateTemplate().execute((Session session) -> {
                org.hibernate.query.Query<?> query = session.createQuery(queryString);
                query.setParameter("deliveryId", deliveryId);
                query.setParameter("objectLevel", CodegroupUtility.SSP_OBJECTTYPE_DELIVERY);
                return query.executeUpdate();
            });

        } else {
            String queryString = "update SspPlausiError set isToDelete=1 where errorId in "
                    + "(select e.errorId from SspPlausiError e where e.deliveryId=:deliveryId and e.plausi in "
                    + "(select distinct(p1.plausiId) from SspPlausi p1, SspPlausi p2 "
                    + "where (p1.objectLevel>=:objectLevelMin and p1.objectLevel<=:objectLevelMax) "
                    + "or (p1.name_de = p2.name_de and p2.objectLevel>=:objectLevelMin and p2.objectLevel<=:objectLevelMax)))";

            getHibernateTemplate().execute((Session session) -> {
                org.hibernate.query.Query<?> query = session.createQuery(queryString);
                query.setParameter("deliveryId", deliveryId);
                query.setParameter("objectLevelMin", CodegroupUtility.SSP_OBJECTTYPE_DELIVERY);
                query.setParameter("objectLevelMax", CodegroupUtility.SSP_OBJECTTYPE_ACTIVITY);
                return query.executeUpdate();
            });
        }

        getHibernateTemplate().flush();
    }



    @Override
    public void markReplacedPersonsToDelete(Long deliveryId) {
        getHibernateTemplate().execute(session -> {
            String queryString = "update SspPerson p set isToDelete = :isToDelete where p.deliveryId = :deliveryId and p.deliveryStatus > :deliveryStatus and "
                    + "exists (select p2 from SspPerson p2 where p2.deliveryId = p.deliveryId and p2.idType = p.idType and p2.id = p.id and p2.deliveryStatus <= :maxDeliveryStatus)";

            Query query = session.createQuery(queryString);
            query.setParameter("isToDelete", true);
            query.setParameter("deliveryId", deliveryId);
            query.setParameter("deliveryStatus", CodegroupUtility.MEB_DATASTATUS_IMPORTED);
            query.setParameter("maxDeliveryStatus", CodegroupUtility.MEB_DATASTATUS_IMPORTED);

            int result = query.executeUpdate();
            session.flush();
            return result;
        });
    }

    @Override
    public void deleteMarkedObjects(Long deliveryId) {
        getHibernateTemplate().execute(new HibernateCallback<Void>() {
            @Override
            public Void doInHibernate(Session session) throws HibernateException {
                // Suppression des erreurs de plausi marquées à supprimer
                org.hibernate.query.Query query1 = session.createQuery(
                        "delete from SspPlausiError where isToDelete=1 and deliveryId=:deliveryId");
                query1.setParameter("deliveryId", deliveryId);
                query1.executeUpdate();

                // Suppression des erreurs de plausi liées à des personnes marquées à supprimer
                org.hibernate.query.Query query2 = session.createQuery(
                        "delete from SspPlausiError where errorId in " +
                                "(select e.errorId from SspPlausiError e, SspPerson p " +
                                " where p.personId=e.personId and p.isToDelete=1 and p.deliveryId=:deliveryId)");
                query2.setParameter("deliveryId", deliveryId);
                query2.executeUpdate();

                // Suppression des activités liées à des personnes marquées à supprimer
                org.hibernate.query.Query query3 = session.createQuery(
                        "delete from SspActivity where activityId in " +
                                "(select a.activityId from SspActivity a, SspPerson p " +
                                " where p.personId=a.personId and p.isToDelete=1 and p.deliveryId=:deliveryId)");
                query3.setParameter("deliveryId", deliveryId);
                query3.executeUpdate();

                // Suppression des personnes marquées à supprimer
                org.hibernate.query.Query query4 = session.createQuery(
                        "delete from SspPerson where isToDelete=1 and deliveryId=:deliveryId");
                query4.setParameter("deliveryId", deliveryId);
                query4.executeUpdate();

                session.flush();
                return null;
            }
        });
    }



    /* (non-Javadoc)
     * @see ch.bfs.meb.ssp.server.integration.repository.IDeliveryRepository#restoreMarkedObjects(java.lang.Long)
     */
    @Override
    public void restoreMarkedObjects(Long deliveryId) {
        getHibernateTemplate().execute(session -> {
            // Delete new plausi errors on delivery
            String queryString1 = "delete from SspPlausiError where errorId in (select e.errorId from SspPlausiError e, SspPlausi p where e.isToDelete = :isToDelete and e.deliveryId = :deliveryId and e.plausi = p.plausiId and p.objectLevel = :objectLevel)";
            Query query1 = session.createQuery(queryString1);
            query1.setParameter("isToDelete", false);
            query1.setParameter("deliveryId", deliveryId);
            query1.setParameter("objectLevel", CodegroupUtility.SSP_OBJECTTYPE_DELIVERY);
            query1.executeUpdate();

            // Delete new plausi errors on persons (and activities...)
            String queryString2 = "delete from SspPlausiError where errorId in (select e.errorId from SspPlausiError e, SspPerson p where e.personId = p.personId and p.isToDelete = :isToDelete and p.deliveryId = :deliveryId)";
            Query query2 = session.createQuery(queryString2);
            query2.setParameter("isToDelete", false);
            query2.setParameter("deliveryId", deliveryId);
            query2.executeUpdate();

            // Delete new activities
            String queryString3 = "delete from SspActivity where activityId in (select a.activityId from SspActivity a, SspPerson p where a.personId = p.personId and p.isToDelete = :isToDelete and p.deliveryId = :deliveryId and a.deliveryStatus = :deliveryStatus)";
            Query query3 = session.createQuery(queryString3);
            query3.setParameter("isToDelete", false);
            query3.setParameter("deliveryId", deliveryId);
            query3.setParameter("deliveryStatus", CodegroupUtility.MEB_DATASTATUS_IMPORTED);
            query3.executeUpdate();

            // Delete new persons
            String queryString4 = "delete from SspPerson where isToDelete = :isToDelete and deliveryId = :deliveryId and deliveryStatus = :deliveryStatus";
            Query query4 = session.createQuery(queryString4);
            query4.setParameter("isToDelete", false);
            query4.setParameter("deliveryId", deliveryId);
            query4.setParameter("deliveryStatus", CodegroupUtility.MEB_DATASTATUS_IMPORTED);
            query4.executeUpdate();

            // Restore old data
            String queryString5 = "update SspPlausiError set isToDelete = :newIsToDelete where isToDelete = :oldIsToDelete and deliveryId = :deliveryId";
            Query query5 = session.createQuery(queryString5);
            query5.setParameter("newIsToDelete", false);
            query5.setParameter("oldIsToDelete", true);
            query5.setParameter("deliveryId", deliveryId);
            query5.executeUpdate();

            String queryString6 = "update SspPerson set isToDelete = :newIsToDelete where isToDelete = :oldIsToDelete and deliveryId = :deliveryId";
            Query query6 = session.createQuery(queryString6);
            query6.setParameter("newIsToDelete", false);
            query6.setParameter("oldIsToDelete", true);
            query6.setParameter("deliveryId", deliveryId);
            query6.executeUpdate();

            session.flush();
            return null;
        });
    }


    /* (non-Javadoc)
     * @see ch.bfs.meb.ssp.server.integration.repository.IDeliveryRepository#updateDeliveredObjects(java.lang.Long)
     */
    @Override
    public void updateDeliveredObjects(final Long deliveryId) {
        getHibernateTemplate().execute(session -> {
            String queryString1 = "update SspActivity set deliveryStatus = :newStatus where activityId in (select a.activityId from SspActivity a, SspPerson p where a.personId = p.personId and p.isToDelete = :isToDelete and p.deliveryId = :deliveryId and a.deliveryStatus = :currentStatus)";
            String queryString2 = "update SspPerson set deliveryStatus = :newStatus where isToDelete = :isToDelete and deliveryId = :deliveryId and deliveryStatus = :currentStatus";

            org.hibernate.query.Query query1 = session.createQuery(queryString1);
            query1.setParameter("newStatus", CodegroupUtility.MEB_DATASTATUS_DELIVERED);
            query1.setParameter("isToDelete", false);
            query1.setParameter("deliveryId", deliveryId);
            query1.setParameter("currentStatus", CodegroupUtility.MEB_DATASTATUS_IMPORTED);
            query1.executeUpdate();

            org.hibernate.query.Query query2 = session.createQuery(queryString2);
            query2.setParameter("newStatus", CodegroupUtility.MEB_DATASTATUS_DELIVERED);
            query2.setParameter("isToDelete", false);
            query2.setParameter("deliveryId", deliveryId);
            query2.setParameter("currentStatus", CodegroupUtility.MEB_DATASTATUS_IMPORTED);
            query2.executeUpdate();

            session.flush();
            return null;
        });
    }


    /* (non-Javadoc)
     * @see ch.bfs.meb.ssp.server.integration.repository.IDeliveryRepository#updatePlausistatus(java.lang.Long)
     */
    @Override
    public void updatePlausistatus(Long deliveryId) {
        getHibernateTemplate().execute(session -> {
            String queryString = "update SspDelivery set plausiStatus = case " +
                    "when (select count(e) from SspPlausiError e where e.deliveryId = :deliveryId and e.personId is null) = 0 then 2 " +
                    "when (select count(e) from SspPlausiError e where e.deliveryId = :deliveryId and e.personId is null and e.isConfirmed = 0) > 0 then 1 " +
                    "else 3 end " +
                    "where deliveryId = :deliveryId";

            org.hibernate.query.Query query = session.createQuery(queryString);
            query.setParameter("deliveryId", deliveryId);
            query.executeUpdate();
            session.flush();
            return null;
        });
    }


    /* (non-Javadoc)
     * @see ch.bfs.meb.ssp.server.integration.repository.IDeliveryRepository#updateAllPlausistatus(java.lang.Long)
     */
    @Override
    public void updateAllPlausistatus(Long deliveryId) {
        Session session = currentSession();

        // Update SspActivity
        Query<?> updateActivity = session.createQuery(
                "update SspActivity a " +
                        "set plausiStatus = case " +
                        "  when (select count(e) from SspPlausiError e where e.activityId = a.activityId) = 0 then 2 " +
                        "  when (select count(e) from SspPlausiError e where e.activityId = a.activityId and e.isConfirmed = false) > 0 then 1 " +
                        "  else 3 " +
                        "end " +
                        "where a.activityId in (" +
                        "  select a2.activityId from SspActivity a2 join SspPerson p " +
                        "  on a2.personId = p.personId " +
                        "  where p.isToDelete = false and p.deliveryId = :deliveryId" +
                        ")"
        );
        updateActivity.setParameter("deliveryId", deliveryId);
        updateActivity.executeUpdate();

        // Update SspPerson
        Query<?> updatePerson = session.createQuery(
                "update SspPerson p " +
                        "set plausiStatus = case " +
                        "  when (select count(e) from SspPlausiError e where e.personId = p.personId and e.activityId is null) = 0 then 2 " +
                        "  when (select count(e) from SspPlausiError e where e.personId = p.personId and e.activityId is null and e.isConfirmed = false) > 0 then 1 " +
                        "  else 3 " +
                        "end " +
                        "where p.isToDelete = false and p.deliveryId = :deliveryId"
        );
        updatePerson.setParameter("deliveryId", deliveryId);
        updatePerson.executeUpdate();

        // Update SspDelivery
        Query<?> updateDelivery = session.createQuery(
                "update SspDelivery d " +
                        "set plausiStatus = case " +
                        "  when (select count(e) from SspPlausiError e where e.isToDelete = false and e.deliveryId = :deliveryId and e.personId is null) = 0 then 2 " +
                        "  when (select count(e) from SspPlausiError e where e.isToDelete = false and e.deliveryId = :deliveryId and e.personId is null and e.isConfirmed = false) > 0 then 1 " +
                        "  else 3 " +
                        "end " +
                        "where d.deliveryId = :deliveryId"
        );
        updateDelivery.setParameter("deliveryId", deliveryId);
        updateDelivery.executeUpdate();

        session.flush();
    }



    /* (non-Javadoc)
     * @see ch.bfs.meb.ssp.server.integration.repository.IDeliveryRepository#updateConfigDeliveryCode(ch.bfs.meb.ssp.server.integration.dto.SspDelivery, java.lang.String)
     */
    @Override
    public void updateConfigDeliveryCode(SspDelivery delivery, String configDeliveryCode) {
        getHibernateTemplate().execute(session -> {
            String queryString1 = "update SspActivity set configDeliveryCode = :configDeliveryCode where activityId in (select a.activityId from SspActivity a, SspPerson p where a.personId = p.personId and p.deliveryId = :deliveryId)";
            String queryString2 = "update SspPerson set configDeliveryCode = :configDeliveryCode where deliveryId = :deliveryId";

            Query query1 = session.createQuery(queryString1);
            query1.setParameter("configDeliveryCode", configDeliveryCode);
            query1.setParameter("deliveryId", delivery.getDeliveryId());
            query1.executeUpdate();

            Query query2 = session.createQuery(queryString2);
            query2.setParameter("configDeliveryCode", configDeliveryCode);
            query2.setParameter("deliveryId", delivery.getDeliveryId());
            query2.executeUpdate();

            delivery.setConfigDeliveryCode(configDeliveryCode);
            session.flush();
            return null;
        });
    }


    /* (non-Javadoc)
     * @see ch.bfs.meb.ssp.server.integration.repository.IDeliveryRepository#existsPerson(java.lang.Long)
     */
    @Override
    public boolean existsPerson(final Long deliveryId) {
        return (Long) getHibernateTemplate().execute(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                Query<Long> query = session.createQuery(
                        "select count(p) from SspPerson p where p.isToDelete=0 and p.deliveryId= :deliveryId and rownum = 1",
                        Long.class);
                query.setParameter("deliveryId", deliveryId);
                return query.uniqueResult();
            }
        }) > 0L;
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.ssp.server.integration.repository.IDeliveryRepository#getNumberOfObjects(java.lang.Long)
     */
    @Override
    public Long getNumberOfPersons(final Long deliveryId) {
        return (Long) getHibernateTemplate().execute(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                Query<Long> query = session.createQuery(
                        "select count(p) from SspPerson p where p.isToDelete=0 and p.deliveryId= :deliveryId", Long.class);
                query.setParameter("deliveryId", deliveryId);
                return query.uniqueResult();
            }
        });
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.ssp.server.integration.repository.IDeliveryRepository#getNumberOfActivities(java.lang.Long)
     */
    @Override
    public Long getNumberOfActivities(final Long deliveryId) {
        return (Long) getHibernateTemplate().execute(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                Query<Long> query = session.createQuery(
                        "select count(distinct a) from SspPerson p, SspActivity a where a.personId=p.personId and p.isToDelete=0 and p.deliveryId= :deliveryId",
                        Long.class);
                query.setParameter("deliveryId", deliveryId);
                return query.uniqueResult();
            }
        });
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.ssp.server.integration.repository.IDeliveryRepository#modifiedAfter(java.lang.Long, java.util.Date)
     */
    @Override
    public boolean modifiedAfter(final Long deliveryId, final Date modificationDate) {
        // check activities
        Long newObjects = (Long) getHibernateTemplate().execute(session -> {
            Query<Long> query = session.createQuery(
                    "select count(a) from SspPerson p, SspActivity a where a.personId = p.personId and p.deliveryId = :deliveryId and a.modification_date > :modificationDate", Long.class);
            query.setParameter("deliveryId", deliveryId);
            query.setParameter("modificationDate", modificationDate);
            return query.uniqueResult();
        });
        if (newObjects > 0L) {
            return true;
        }
        // check persons
        newObjects = (Long) getHibernateTemplate().execute(session -> {
            Query<Long> query = session.createQuery(
                    "select count(p) from SspPerson p where p.deliveryId = :deliveryId and p.modification_date > :modificationDate", Long.class);
            query.setParameter("deliveryId", deliveryId);
            query.setParameter("modificationDate", modificationDate);
            return query.uniqueResult();
        });
        return newObjects > 0L;
    }


    /* (non-Javadoc)
     * @see ch.bfs.meb.ssp.server.integration.repository.IDeliveryRepository#allPlausibel(java.lang.Long)
     */
    @Override
    public boolean allPlausibel(SspDelivery delivery) {
        final Long deliveryId = delivery.getDeliveryId();
        // check activity
        Long notPlausibel = (Long) getHibernateTemplate().execute(session -> {
            Query<Long> query = session.createQuery(
                    "select count(a) from SspPerson p, SspActivity a where a.personId = p.personId and p.deliveryId = :deliveryId and a.plausiStatus = :plausiStatus", Long.class);
            query.setParameter("deliveryId", deliveryId);
            query.setParameter("plausiStatus", CodegroupUtility.MEB_PLAUSISTATUS_NOTVALID);
            return query.uniqueResult();
        });
        if (notPlausibel > 0L) {
            return false;
        }
        // check person
        notPlausibel = (Long) getHibernateTemplate().execute(session -> {
            Query<Long> query = session.createQuery(
                    "select count(p) from SspPerson p where p.deliveryId = :deliveryId and p.plausiStatus = :plausiStatus", Long.class);
            query.setParameter("deliveryId", deliveryId);
            query.setParameter("plausiStatus", CodegroupUtility.MEB_PLAUSISTATUS_NOTVALID);
            return query.uniqueResult();
        });
        if (notPlausibel > 0L) {
            return false;
        }
        return !delivery.getPlausiStatus().equals(CodegroupUtility.MEB_PLAUSISTATUS_NOTVALID);
    }


    /* (non-Javadoc)
     * @see ch.bfs.meb.ssp.server.integration.repository.IDeliveryRepository#prevalidate(java.lang.Long, java.lang.String)
     */
    @Override
    public void prevalidate(SspDelivery delivery, String username) {
        Date now = new Date();
        Long deliveryId = delivery.getDeliveryId();

        getHibernateTemplate().execute(session -> {
            String queryString1 = "update SspActivity set deliveryStatus = :newStatus, prevalidation_user = :username, prevalidation_date = :now " +
                    "where activityId in (select a.activityId from SspActivity a, SspPerson p where a.personId = p.personId and p.deliveryId = :deliveryId and a.deliveryStatus = :currentStatus)";
            Query query1 = session.createQuery(queryString1);
            query1.setParameter("newStatus", CodegroupUtility.MEB_DATASTATUS_PREVALIDATED);
            query1.setParameter("username", username);
            query1.setParameter("now", now);
            query1.setParameter("deliveryId", deliveryId);
            query1.setParameter("currentStatus", CodegroupUtility.MEB_DATASTATUS_DELIVERED);
            query1.executeUpdate();

            String queryString2 = "update SspPerson set deliveryStatus = :newStatus, prevalidation_user = :username, prevalidation_date = :now " +
                    "where deliveryId = :deliveryId and deliveryStatus = :currentStatus";
            Query query2 = session.createQuery(queryString2);
            query2.setParameter("newStatus", CodegroupUtility.MEB_DATASTATUS_PREVALIDATED);
            query2.setParameter("username", username);
            query2.setParameter("now", now);
            query2.setParameter("deliveryId", deliveryId);
            query2.setParameter("currentStatus", CodegroupUtility.MEB_DATASTATUS_DELIVERED);
            query2.executeUpdate();

            delivery.setDeliveryStatus(CodegroupUtility.MEB_DELIVERYSTATUS_PREVALIDATED);
            delivery.setPrevalidation_user(username);
            delivery.setPrevalidation_date(now);
            session.merge(delivery);

            session.flush();
            return null;
        });
    }


    /* (non-Javadoc)
     * @see ch.bfs.meb.ssp.server.integration.repository.IDeliveryRepository#validate(java.lang.Long, java.lang.Long)
     */
    @Override
    public void validate(SspDelivery delivery, String username) {
        Date now = new Date();
        Long deliveryId = delivery.getDeliveryId();

        getHibernateTemplate().execute(session -> {
            // Update deliveryStatus, validation_user, and validation_date for SspActivity
            String queryString1 = "update SspActivity set deliveryStatus = :newStatus, validation_user = :username, validation_date = :now " +
                    "where activityId in (select a.activityId from SspActivity a, SspPerson p where a.personId = p.personId and p.deliveryId = :deliveryId and a.deliveryStatus in (:statusDelivered, :statusPrevalidated))";
            Query query1 = session.createQuery(queryString1);
            query1.setParameter("newStatus", CodegroupUtility.MEB_DATASTATUS_VALIDATED);
            query1.setParameter("username", username);
            query1.setParameter("now", now);
            query1.setParameter("deliveryId", deliveryId);
            query1.setParameter("statusDelivered", CodegroupUtility.MEB_DATASTATUS_DELIVERED);
            query1.setParameter("statusPrevalidated", CodegroupUtility.MEB_DATASTATUS_PREVALIDATED);
            query1.executeUpdate();

            // Update prevalidation_user and prevalidation_date for SspActivity
            String queryString2 = "update SspActivity set prevalidation_user = case when prevalidation_user is null then validation_user else prevalidation_user end, " +
                    "prevalidation_date = case when prevalidation_date is null then validation_date else prevalidation_date end " +
                    "where activityId in (select a.activityId from SspActivity a, SspPerson p where a.personId = p.personId and p.deliveryId = :deliveryId and a.deliveryStatus = :newStatus)";
            Query query2 = session.createQuery(queryString2);
            query2.setParameter("deliveryId", deliveryId);
            query2.setParameter("newStatus", CodegroupUtility.MEB_DATASTATUS_VALIDATED);
            query2.executeUpdate();

            // Update deliveryStatus, validation_user, and validation_date for SspPerson
            String queryString3 = "update SspPerson set deliveryStatus = :newStatus, validation_user = :username, validation_date = :now " +
                    "where deliveryId = :deliveryId and deliveryStatus in (:statusDelivered, :statusPrevalidated)";
            Query query3 = session.createQuery(queryString3);
            query3.setParameter("newStatus", CodegroupUtility.MEB_DATASTATUS_VALIDATED);
            query3.setParameter("username", username);
            query3.setParameter("now", now);
            query3.setParameter("deliveryId", deliveryId);
            query3.setParameter("statusDelivered", CodegroupUtility.MEB_DATASTATUS_DELIVERED);
            query3.setParameter("statusPrevalidated", CodegroupUtility.MEB_DATASTATUS_PREVALIDATED);
            query3.executeUpdate();

            // Update prevalidation_user and prevalidation_date for SspPerson
            String queryString4 = "update SspPerson set prevalidation_user = case when prevalidation_user is null then validation_user else prevalidation_user end, " +
                    "prevalidation_date = case when prevalidation_date is null then validation_date else prevalidation_date end " +
                    "where deliveryId = :deliveryId and deliveryStatus = :newStatus";
            Query query4 = session.createQuery(queryString4);
            query4.setParameter("deliveryId", deliveryId);
            query4.setParameter("newStatus", CodegroupUtility.MEB_DATASTATUS_VALIDATED);
            query4.executeUpdate();

            // Update delivery object
            delivery.setDeliveryStatus(CodegroupUtility.MEB_DELIVERYSTATUS_VALIDATED);
            delivery.setValidation_user(username);
            delivery.setValidation_date(now);
            if (delivery.getPrevalidation_user() == null) {
                delivery.setPrevalidation_user(username);
                delivery.setPrevalidation_date(now);
            }
            session.merge(delivery);

            session.flush();
            return null;
        });
    }


    /* (non-Javadoc)
     * @see ch.bfs.meb.ssp.server.integration.repository.IDeliveryRepository#undoPrevalidate(java.lang.Long)
     */
    @Override
    public void undoPrevalidate(SspDelivery delivery) {
        Long deliveryId = delivery.getDeliveryId();

        getHibernateTemplate().execute(session -> {
            // Update SspActivity
            String queryString1 = "update SspActivity set deliveryStatus = :newStatus, prevalidation_user = null, prevalidation_date = null " +
                    "where activityId in (select a.activityId from SspActivity a, SspPerson p where a.personId = p.personId and p.deliveryId = :deliveryId and a.deliveryStatus = :currentStatus)";
            Query query1 = session.createQuery(queryString1);
            query1.setParameter("newStatus", CodegroupUtility.MEB_DATASTATUS_DELIVERED);
            query1.setParameter("deliveryId", deliveryId);
            query1.setParameter("currentStatus", CodegroupUtility.MEB_DATASTATUS_PREVALIDATED);
            query1.executeUpdate();

            // Update SspPerson
            String queryString2 = "update SspPerson set deliveryStatus = :newStatus, prevalidation_user = null, prevalidation_date = null " +
                    "where deliveryId = :deliveryId and deliveryStatus = :currentStatus";
            Query query2 = session.createQuery(queryString2);
            query2.setParameter("newStatus", CodegroupUtility.MEB_DATASTATUS_DELIVERED);
            query2.setParameter("deliveryId", deliveryId);
            query2.setParameter("currentStatus", CodegroupUtility.MEB_DATASTATUS_PREVALIDATED);
            query2.executeUpdate();

            // Update delivery object
            delivery.setDeliveryStatus(CodegroupUtility.MEB_DELIVERYSTATUS_DELIVERED);
            delivery.setPrevalidation_user(null);
            delivery.setPrevalidation_date(null);
            session.merge(delivery);

            session.flush();
            return null;
        });
    }


    /* (non-Javadoc)
     * @see ch.bfs.meb.ssp.server.integration.repository.IDeliveryRepository#undoValidate(java.lang.Long)
     */
    @Override
    public void undoValidate(SspDelivery delivery) {
        Long deliveryId = delivery.getDeliveryId();

        getHibernateTemplate().execute(session -> {
            // Update SspActivity
            String queryString1 = "update SspActivity set deliveryStatus = :newStatus, validation_user = null, validation_date = null " +
                    "where activityId in (select a.activityId from SspActivity a, SspPerson p where a.personId = p.personId and p.deliveryId = :deliveryId and a.deliveryStatus = :currentStatus)";
            Query query1 = session.createQuery(queryString1);
            query1.setParameter("newStatus", CodegroupUtility.MEB_DATASTATUS_PREVALIDATED);
            query1.setParameter("deliveryId", deliveryId);
            query1.setParameter("currentStatus", CodegroupUtility.MEB_DATASTATUS_VALIDATED);
            query1.executeUpdate();

            // Update SspPerson
            String queryString2 = "update SspPerson set deliveryStatus = :newStatus, validation_user = null, validation_date = null " +
                    "where deliveryId = :deliveryId and deliveryStatus = :currentStatus";
            Query query2 = session.createQuery(queryString2);
            query2.setParameter("newStatus", CodegroupUtility.MEB_DATASTATUS_PREVALIDATED);
            query2.setParameter("deliveryId", deliveryId);
            query2.setParameter("currentStatus", CodegroupUtility.MEB_DATASTATUS_VALIDATED);
            query2.executeUpdate();

            // Update delivery object
            delivery.setDeliveryStatus(CodegroupUtility.MEB_DELIVERYSTATUS_PREVALIDATED);
            delivery.setValidation_user(null);
            delivery.setValidation_date(null);
            session.merge(delivery);

            session.flush();
            return null;
        });
    }


    /* (non-Javadoc)
     * @see ch.bfs.meb.ssp.server.integration.repository.IDeliveryRepository#deleteAll(java.lang.Long, java.lang.Long)
     */
    @Override
    public void deleteAll(Long deliveryId, Long checkStatus) {
        // Check persons
        getHibernateTemplate().execute(session -> {
            Query query = session.createQuery("from SspPerson where deliveryId = :deliveryId and deliveryStatus >= :checkStatus");
            query.setParameter("deliveryId", deliveryId);
            query.setParameter("checkStatus", checkStatus);
            query.setMaxResults(1);
            if (!query.list().isEmpty()) {
                throw new MebUncheckedNotMonitoredException("maintain.delete.childrenValidatedError.messages");
            }
            return null;
        });

        // Check activities
        getHibernateTemplate().execute(session -> {
            Query query = session.createQuery(
                    "from SspActivity where exists (from SspActivity a, SspPerson p where a.personId = p.personId and p.deliveryId = :deliveryId and a.deliveryStatus >= :checkStatus)"
            );
            query.setParameter("deliveryId", deliveryId);
            query.setParameter("checkStatus", checkStatus);
            query.setMaxResults(1);
            if (!query.list().isEmpty()) {
                throw new MebUncheckedNotMonitoredException("maintain.delete.childrenValidatedError.messages");
            }
            return null;
        });

        // Delete plausierrors on all objects of delivery
        getHibernateTemplate().execute(session -> {
            Query query = session.createQuery("delete from SspPlausiError where deliveryId = :deliveryId");
            query.setParameter("deliveryId", deliveryId);
            query.executeUpdate();
            return null;
        });

        // Delete activities and persons
        getHibernateTemplate().execute(session -> {
            Query query = session.createQuery("delete from SspActivity where personId in (select p.personId from SspPerson p where p.deliveryId = :deliveryId)");
            query.setParameter("deliveryId", deliveryId);
            query.executeUpdate();
            return null;
        });

        getHibernateTemplate().execute(session -> {
            Query query = session.createQuery("delete from SspPerson where deliveryId = :deliveryId");
            query.setParameter("deliveryId", deliveryId);
            query.executeUpdate();
            return null;
        });

        getHibernateTemplate().flush();
    }


    /* (non-Javadoc)
     * @see ch.bfs.meb.ssp.server.integration.repository.IDeliveryRepository#deleteAll(java.lang.Long)
     */
    @Override
    public void deleteAll(Long deliveryId) {
        // check if the delivery can be deleted
        MebUser user = (MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        long status = CodegroupUtility.MEB_DATASTATUS_PREVALIDATED;
        if (user.isInRole(SecurityConstants.ROLE_SSP_DV)) {
            status = CodegroupUtility.MEB_DATASTATUS_VALIDATED;
        }

        deleteAll(deliveryId, status);
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.ssp.server.integration.repository.IDeliveryRepository#deleteDelivery(ch.bfs.meb.ssp.server.integration.dto.SspDelivery)
     */
    @Override
    public void deleteDelivery(SspDelivery delivery) {
        getHibernateTemplate().delete(delivery);
        getHibernateTemplate().flush();
    }

    @SuppressWarnings("unchecked")
    private List<SspDelivery> getDeliveriesByIds(List<Long> deliveryIds) {
        if (deliveryIds == null || deliveryIds.isEmpty()) {
            return new ArrayList<SspDelivery>();
        }

        // query deliveries
        Query queryResult = currentSession().createQuery("from SspDelivery d where d.deliveryId in (:deliveryIds)");
        queryResult.setParameterList("deliveryIds", deliveryIds);
        // DistinctRootEntityResultTransformer not required => order by map below already does the same
        //		queryResult.setResultTransformer(DistinctRootEntityResultTransformer.INSTANCE);

        List<SspDelivery> tempList = queryResult.list();

        // reestablish old sort order
        Map<Long, SspDelivery> mapById = new HashMap<Long, SspDelivery>(tempList.size());
        for (SspDelivery entity : tempList) {
            mapById.put(entity.getDeliveryId(), entity);
        }
        List<SspDelivery> resultList = new ArrayList<SspDelivery>(mapById.size());
        for (Long id : deliveryIds) {
            SspDelivery entity = mapById.get(id);
            if (entity != null) {
                resultList.add(entity);
            }
        }

        return resultList;
    }

    @Override
    @SuppressWarnings("unchecked")
    public HashMap<Long, String> getPersonConfirmRules(final Long deliveryId) {
        HashMap<Long, String> personConfirmRules = new HashMap<>();
        List<Object[]> resultList = (List<Object[]>) getHibernateTemplate().execute(session -> {
            Query<Object[]> query = session.createQuery(
                    "select p.personId, p.confirmRules from SspPerson p where p.deliveryId = :deliveryId and p.confirmRules is not null",
                    Object[].class
            );
            query.setParameter("deliveryId", deliveryId);
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
    public HashMap<Long, String> getActivityConfirmRules( Long deliveryId) {
        HashMap<Long, String> activityConfirmRules = new HashMap<Long, String>();
        List<Object[]> resultList = (List<Object[]>) getHibernateTemplate().execute((Session session) -> {

            String hql = "select a.activityId, a.confirmRules from SspActivity a, SspPerson p where a.personId = p.personId and p.deliveryId=:delId and a.confirmRules is not null";
            org.hibernate.query.Query query = session.createQuery(hql);
            query.setParameter("delId", deliveryId);
                return query.list();

        });
        for (Object[] row : resultList) {
            Long activityId = ((Number) row[0]).longValue();
            String confirmRules = (String) row[1];
            activityConfirmRules.put(activityId, confirmRules);
        }
        return activityConfirmRules;
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
