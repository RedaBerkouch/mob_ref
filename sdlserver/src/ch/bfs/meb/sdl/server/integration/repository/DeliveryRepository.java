/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sdlserver

 */
package ch.bfs.meb.sdl.server.integration.repository;

import ch.bfs.meb.exception.MebUncheckedNotMonitoredException;
import ch.bfs.meb.integration.dto.ResultBase;
import ch.bfs.meb.sdl.server.integration.dto.SdlClass;
import ch.bfs.meb.sdl.server.integration.dto.SdlDelivery;
import ch.bfs.meb.sdl.server.integration.dto.SdlLearner;
import ch.bfs.meb.sdl.server.integration.dto.SdlPlausiError;
import ch.bfs.meb.sdl.server.integration.dto.SdlSchool;
import ch.bfs.meb.security.MebUser;
import ch.bfs.meb.server.commons.integration.dto.FilterContext;
import ch.bfs.meb.server.commons.integration.dto.SortContext;
import ch.bfs.meb.server.commons.util.IFilterUtility;
import ch.bfs.meb.util.CodegroupUtility;
import ch.bfs.meb.util.SecurityConstants;
import ch.bfs.meb.util.StringUtils;
import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.hibernate.query.NativeQuery;
import org.springframework.orm.hibernate5.HibernateCallback;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.*;

/**
 * Repository for SdlDeliveries.
 * 
 * @author $Author$
 * @version $Revision$
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
     * @see ch.bfs.meb.sdl.server.integration.repository.IDeliveryRepository#getDeliveries()
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<SdlDelivery> getDeliveries(int start, int buffer, SortContext sortContext, FilterContext filterContext, Long version, Long canton) {
        String queryString;
        String sortColumn = _filterUtility.adaptColumnName(
                sortContext.getSortColumn() == null ? StringUtils.asCamelCase(MODIFICATION_DATE) : sortContext.getSortColumn(), "model",
                getUnderscoreColumns());
        String codegroupId = getCodegroupId(sortColumn);
        String whereSelection = _filterUtility.getWhereFilterSelection(filterContext, "model", getStringColumns(), getDateColumns(), getUnderscoreColumns());
        String deliverySubquery = _filterUtility.getPredefinedFilterSubquery(filterContext, SecurityFilters.SDL_DELIVERIES_TABLE, true);

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
        String deliverySubquery = _filterUtility.getPredefinedFilterSubquery(filterContext, SecurityFilters.SDL_DELIVERIES_TABLE, true);

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
     * @see ch.bfs.meb.sdl.server.integration.repository.IDeliveryRepository#getDeliveriesForCanton(java.lang.Long, java.lang.Long)
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<SdlDelivery> getDeliveriesForCanton(final Long canton, final Long version) {
        return (List<SdlDelivery>) getHibernateTemplate().execute(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                org.hibernate.query.Query<SdlDelivery> query = session.createQuery("from SdlDelivery where canton=:canton and version=:version", SdlDelivery.class);
                query.setParameter("canton", canton);
                query.setParameter("version", version);
                return query.list();
            }
        });
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sdl.server.integration.repository.IDeliveryRepository#getDeliveryById(java.lang.Long)
     */
    @Override
    public SdlDelivery getDeliveryById(Long deliveryId) {
        Query<SdlDelivery> query = currentSession().createQuery(
                "from SdlDelivery d " +
                        "left join fetch d.plausierrors pe " +
                        "left join fetch pe.plausi " +
                        "where d.deliveryId = :deliveryId", SdlDelivery.class);
        query.setParameter("deliveryId", deliveryId);
        return query.uniqueResult();
    }



    /* (non-Javadoc)
     * @see ch.bfs.meb.sdl.server.integration.repository.IDeliveryRepository#getDeliveryById(java.lang.Long, org.hibernate.LockMode)
     */
    @Override
    public SdlDelivery getDeliveryById(Long deliveryId, LockMode lockMode) {
        return getHibernateTemplate().execute(session -> {
            org.hibernate.query.Query<SdlDelivery> query = session.createQuery(
                    "from SdlDelivery d left join fetch d.plausierrors pe left join fetch pe.plausi where d.deliveryId=:deliveryId",
                    SdlDelivery.class);
            query.setParameter("deliveryId", deliveryId);
            query.setLockMode("d", lockMode);
            return query.uniqueResult();
        });
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<SdlPlausiError> getTopPlausiErrorsForDelivery(final Long deliveryId) {
        return (List<SdlPlausiError>) getHibernateTemplate().execute(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                Query q = session.createQuery("from SdlDelivery d where d.deliveryId=:deliveryId");
                q.setLong("deliveryId", deliveryId);
                if ((SdlDelivery) q.uniqueResult() == null) {
                    return null;
                }

                q = session.createQuery(
                        "from SdlPlausiError as pe left join fetch pe.plausi where pe.schoolId is null and pe.isToDelete=0 and pe.deliveryId=:deliveryId order by pe.isConfirmed, pe.plausi, pe.errorId");
                q.setLong("deliveryId", deliveryId);
                q.setMaxResults(ResultBase.MAX_NUMBER_ERRORS + 1);
                return q.list();
            }
        });
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sdl.server.integration.repository.IDeliveryRepository#getDeliveryByIdentification(java.lang.Long, java.lang.Long, java.lang.String)
     */
    @Override
    public SdlDelivery getDeliveryByIdentification(final Long canton, final Long version, final String id) {
        return (SdlDelivery) getHibernateTemplate().execute(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                org.hibernate.query.Query<SdlDelivery> query = session.createQuery("from SdlDelivery where canton=:canton and version=:version and deliveryCode=:id", SdlDelivery.class);
                query.setParameter("canton", canton);
                query.setParameter("version", version);
                query.setParameter("id", id);
                return query.uniqueResult();
            }
        });
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sdl.server.integration.repository.IDeliveryRepository#updateDelivery(ch.bfs.meb.sdl.server.integration.dto.SdlDelivery)
     */
    @Override
    public SdlDelivery updateDelivery(SdlDelivery delivery) {
        delivery = (SdlDelivery) getHibernateTemplate().merge(delivery);
        getHibernateTemplate().flush();
        return delivery;
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sdl.server.integration.repository.IDeliveryRepository#refreshDeliveryNumbers(ch.bfs.meb.sdl.server.integration.dto.SdlDelivery)
     */
    @SuppressWarnings("unchecked")
    @Override
    public SdlDelivery refreshDeliveryNumbers(final SdlDelivery delivery) {
        delivery.resetDeliveryNumbers();

        // 1. Calcul des écoles groupées par plausiStatus et deliveryStatus
        @SuppressWarnings("unchecked")
        List<Object[]> schoolStats = (List<Object[]>) getHibernateTemplate().execute(new HibernateCallback<List<Object[]>>() {
            @Override
            public List<Object[]> doInHibernate(Session session) throws HibernateException {
                javax.persistence.Query query = session.createQuery(
                        "select plausiStatus, deliveryStatus, count(schoolId) " +
                                "from SdlSchool " +
                                "where deliveryId = :deliveryId and isToDelete = 0 " +
                                "group by plausiStatus, deliveryStatus"
                );
                query.setParameter("deliveryId", delivery.getDeliveryId());
                return query.getResultList();
            }
        });

        for (Object[] row : schoolStats) {
            Long plausiStatus = ((Number) row[0]).longValue();
            Long deliveryStatus = ((Number) row[1]).longValue();
            Long count = ((Number) row[2]).longValue();

            delivery.addPlausiSchools(plausiStatus, deliveryStatus, count);
        }

        // 2. Calcul des classes avec plausiStatus
        @SuppressWarnings("unchecked")
        List<Object[]> classStats = (List<Object[]>) getHibernateTemplate().execute(new HibernateCallback<List<Object[]>>() {
            @Override
            public List<Object[]> doInHibernate(Session session) throws HibernateException {
                String sql =
                        "select " + CodegroupUtility.MEB_PLAUSISTATUS_VALID + " as plausiStatus, count(c.CLASSID) as nr " +
                                "from SDL_SCHOOLS s, SDL_CLASSES c " +
                                "where c.schoolId = s.SCHOOLID and s.deliveryId = :deliveryId and s.isToDelete = 0 and c.plausiStatus > " + CodegroupUtility.MEB_PLAUSISTATUS_NOTVALID +
                                " union all " +
                                "select " + CodegroupUtility.MEB_PLAUSISTATUS_UNDEFINED + " as plausiStatus, count(c.CLASSID) as nr " +
                                "from SDL_SCHOOLS s, SDL_CLASSES c " +
                                "where c.schoolId = s.SCHOOLID and s.deliveryId = :deliveryId and s.isToDelete = 0";

                NativeQuery<Object[]> query = session.createNativeQuery(sql);
                query.setParameter("deliveryId", delivery.getDeliveryId());

                return query.list();
            }
        });

        for (Object[] row : classStats) {
            Long plausiStatus = ((Number) row[0]).longValue();
            Long count = ((Number) row[1]).longValue();

            delivery.addPlausiClasses(plausiStatus, count);
        }

        // 3. Calcul des apprenants avec plausiStatus
        @SuppressWarnings("unchecked")
        List<Object[]> learnerStats = (List<Object[]>) getHibernateTemplate().execute(new HibernateCallback<List<Object[]>>() {
            @Override
            public List<Object[]> doInHibernate(Session session) throws HibernateException {
                String sql =
                        "select " + CodegroupUtility.MEB_PLAUSISTATUS_VALID + " as plausiStatus, count(l.LEARNERID) as nr " +
                                "from SDL_SCHOOLS s, SDL_CLASSES c, SDL_LEARNERS l " +
                                "where l.classId = c.CLASSID and c.schoolId = s.SCHOOLID and s.deliveryId = :deliveryId and s.isToDelete = 0 and l.plausiStatus > " + CodegroupUtility.MEB_PLAUSISTATUS_NOTVALID +
                                " union all " +
                                "select " + CodegroupUtility.MEB_PLAUSISTATUS_UNDEFINED + " as plausiStatus, count(l.LEARNERID) as nr " +
                                "from SDL_SCHOOLS s, SDL_CLASSES c, SDL_LEARNERS l " +
                                "where l.classId = c.CLASSID and c.schoolId = s.SCHOOLID and s.deliveryId = :deliveryId and s.isToDelete = 0";

                NativeQuery<Object[]> query = session.createNativeQuery(sql);
                query.setParameter("deliveryId", delivery.getDeliveryId());

                return query.list();
            }
        });

        for (Object[] row : learnerStats) {
            Long plausiStatus = ((Number) row[0]).longValue();
            Long count = ((Number) row[1]).longValue();

            delivery.addPlausiLearners(plausiStatus, count);
        }

        // 4. Chargement des plausiErrors associés à la livraison
        for (SdlPlausiError error : delivery.getPlausierrors()) {
            error.loadPlausiData();
        }

        return delivery;
    }


    /* (non-Javadoc)
     * @see ch.bfs.meb.sdl.server.integration.repository.IDeliveryRepository#setAllSchoolsToDelete(java.lang.Long)
     */
    @Override
    public void setAllSchoolsToDelete(Long deliveryId) {
        getHibernateTemplate().execute(new HibernateCallback<Object>() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                Query query = session.createQuery("update SdlSchool set isToDelete=1 where deliveryId= :deliveryId");
                query.setParameter("deliveryId", deliveryId);
                int result = query.executeUpdate();
                return null;
            }
        });
        getHibernateTemplate().flush();
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sdl.server.integration.repository.IDeliveryRepository#setDeliveryErrorsToDelete(java.lang.Long)
     */
    @Override
    public void setDeliveryErrorsToDelete(Long deliveryId, boolean deliveryOnly) {
        if (deliveryOnly) {
            // TODO lsc: instead of name_de take business id from plausi
            getHibernateTemplate().execute(new HibernateCallback<Integer>() {
                @Override
                public Integer doInHibernate(Session session) throws HibernateException {
                    org.hibernate.query.Query query = session.createQuery(
                            "update SdlPlausiError set isToDelete=1 where errorId in "
                                    + "(select e.errorId from SdlPlausiError e where e.deliveryId=:dId and e.plausi in "
                                    + "(select distinct(p1.plausiId) from SdlPlausi p1, SdlPlausi p2 where p1.objectLevel=:objLevel1 or (p1.name_de = p2.name_de and p2.objectLevel=:objLevel2)))");
                    query.setParameter("dId", deliveryId);
                    query.setParameter("objLevel1", CodegroupUtility.SDL_OBJECTTYPE_DELIVERY);
                    query.setParameter("objLevel2", CodegroupUtility.SDL_OBJECTTYPE_DELIVERY);
                    return query.executeUpdate();
                }
            });
        } else {
            String queryString = "update SdlPlausiError set isToDelete=1 where errorId in "
                    + "(select e.errorId from SdlPlausiError e where e.deliveryId=:deliveryId and e.plausi in "
                    + "(select distinct(p1.plausiId) from SdlPlausi p1, SdlPlausi p2 where (p1.objectLevel>=:sdlObjTypeDelivery1 and p1.objectLevel<=:sdlObjTypeLearner1) or (p1.name_de = p2.name_de and p2.objectLevel>=:sdlObjTypeDelivery2 and p2.objectLevel<=:sdlObjTypeLearner2)))";

            getHibernateTemplate().execute(session -> {
                org.hibernate.query.Query query = session.createQuery(queryString);
                query.setParameter("deliveryId", deliveryId);
                query.setParameter("sdlObjTypeDelivery1", CodegroupUtility.SDL_OBJECTTYPE_DELIVERY);
                query.setParameter("sdlObjTypeLearner1", CodegroupUtility.SDL_OBJECTTYPE_LEARNER);
                query.setParameter("sdlObjTypeDelivery2", CodegroupUtility.SDL_OBJECTTYPE_DELIVERY);
                query.setParameter("sdlObjTypeLearner2", CodegroupUtility.SDL_OBJECTTYPE_LEARNER);
                return query.executeUpdate();
            });
        }
        getHibernateTemplate().flush();
    }

    @Override
    public void markReplacedSchoolsToDelete(Long deliveryId) {
        String hql = "update SdlSchool s set isToDelete=1 where s.deliveryId = :deliveryId and s.deliveryStatus > :status and "
                + "exists (select s2 from SdlSchool s2 where s2.deliveryId = s.deliveryId and s2.idType = s.idType and s2.id = s.id and s2.deliveryStatus <= :status)";

        getHibernateTemplate().execute(new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException {
                Query query = session.createQuery(hql);
                query.setParameter("deliveryId", deliveryId);
                query.setParameter("status", CodegroupUtility.MEB_DATASTATUS_IMPORTED);
                return query.executeUpdate();
            }
        });
        getHibernateTemplate().flush();
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sdl.server.integration.repository.IDeliveryRepository#deleteMarkedObjects(java.lang.Long)
     */
    @Override
    public void deleteMarkedObjects(Long deliveryId) {
        getHibernateTemplate().execute(new HibernateCallback<Object>() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                String hql = "delete from SdlPlausiError where isToDelete=1 and deliveryId= :deliveryId";
                Query query = session.createQuery(hql);
                query.setParameter("deliveryId", deliveryId);
                int rowsAffected = query.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("Deleted " + rowsAffected + " rows.");
                }
                return null;
            }
        });
        getHibernateTemplate().execute(new HibernateCallback<Integer>() {
            public Integer doInHibernate(Session session) throws HibernateException {
                String hql = "delete from SdlPlausiError where errorId in (select e.errorId from SdlPlausiError e, SdlSchool s where e.schoolId=s.schoolId and s.isToDelete=1 and s.deliveryId=:deliveryId)";
                Query query = session.createQuery(hql);
                query.setParameter("deliveryId", deliveryId);
                return query.executeUpdate();
            }
        });
        getHibernateTemplate().execute(session -> {
            Query query = session.createQuery("delete from SdlLearner where learnerId in (select l.learnerId from SdlLearner l, SdlClass c, SdlSchool s where l.classId=c.classId and c.schoolId=s.schoolId and s.isToDelete=1 and s.deliveryId=:deliveryId)");
            query.setParameter("deliveryId", deliveryId);
            return query.executeUpdate();
        });
        getHibernateTemplate().execute(session -> {
            Query query = session.createQuery("delete from SdlClass where classId in (select c.classId from SdlClass c, SdlSchool s where c.schoolId=s.schoolId and s.isToDelete=1 and s.deliveryId=:deliveryId)");
            query.setParameter("deliveryId", deliveryId);
            return query.executeUpdate();
        });
        getHibernateTemplate().execute(new HibernateCallback<Void>() {
            public Void doInHibernate(Session session) throws HibernateException {
                Query q = session.createQuery("delete from SdlSchool where isToDelete=1 and deliveryId= :deliveryId");
                q.setParameter("deliveryId", deliveryId);
                q.executeUpdate();
                return null;
            }
        });
        getHibernateTemplate().flush();
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sdl.server.integration.repository.IDeliveryRepository#restoreMarkedObjects(java.lang.Long)
     */
    @Override
    public void restoreMarkedObjects(Long deliveryId) {
        // delete new data
        // delete new plausierrors on delivery
        getHibernateTemplate().execute(session -> {
            String hql = "delete from SdlPlausiError where errorId in (select e.errorId from SdlPlausiError e, SdlPlausi p where e.isToDelete=false and e.deliveryId= :deliveryId and e.plausi=p.plausiId and p.objectLevel= :objectLevel)";
            Query query = session.createQuery(hql);
            query.setParameter("deliveryId", deliveryId);
            query.setParameter("objectLevel", CodegroupUtility.SDL_OBJECTTYPE_DELIVERY);
            return query.executeUpdate();
        });
        // delete new plausierrors on schools (and classes and learners...)
        getHibernateTemplate().execute(new HibernateCallback<Void>() {
            public Void doInHibernate(Session session) throws HibernateException {
                Query q = session.createQuery("delete from SdlPlausiError where errorId in (select e.errorId from SdlPlausiError e, SdlSchool s where e.schoolId=s.schoolId and s.isToDelete=false and s.deliveryId=:deliveryId)");
                q.setParameter("deliveryId", deliveryId);
                q.executeUpdate();
                return null;
            }
        });
        // delete new learners
        getHibernateTemplate().execute(new HibernateCallback<Void>() {
            public Void doInHibernate(Session session) throws HibernateException {
                Query q = session.createQuery("delete from SdlLearner where learnerId in (select l.learnerId from SdlLearner l, SdlClass c, SdlSchool s where l.classId=c.classId and c.schoolId=s.schoolId and s.isToDelete=false and s.deliveryId=:deliveryId and l.deliveryStatus=:status)");
                q.setParameter("deliveryId", deliveryId);
                q.setParameter("status", CodegroupUtility.MEB_DATASTATUS_IMPORTED);
                q.executeUpdate();
                return null;
            }
        });
        // delete new classes
        getHibernateTemplate().execute(new HibernateCallback<Integer>() {
            @Override
            public Integer doInHibernate(Session session) throws HibernateException {
                String hql = "delete from SdlClass where classId in (select c.classId from SdlClass c, SdlSchool s where c.schoolId=s.schoolId and s.isToDelete=false and s.deliveryId= :deliveryId and c.deliveryStatus= :deliveryStatus)";
                Query query = session.createQuery(hql);
                query.setParameter("deliveryId", deliveryId);
                query.setParameter("deliveryStatus", CodegroupUtility.MEB_DATASTATUS_IMPORTED);
                return query.executeUpdate();
            }
        });
        // delete new schools
        getHibernateTemplate().execute(new HibernateCallback<Integer>() {
            @Override
            public Integer doInHibernate(Session session) throws HibernateException {
                Query query = session.createQuery("delete from SdlSchool where isToDelete = false and deliveryId = :deliveryId and deliveryStatus = :deliveryStatus");
                query.setParameter("deliveryId", deliveryId);
                query.setParameter("deliveryStatus", CodegroupUtility.MEB_DATASTATUS_IMPORTED);
                return query.executeUpdate();
            }
        });

        // restore old data
        getHibernateTemplate().execute(new HibernateCallback<Object>() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                Query query = session.createQuery("update SdlPlausiError set isToDelete=:newValue where isToDelete=:oldValue and deliveryId=:deliveryId");
                query.setParameter("newValue", false);
                query.setParameter("oldValue", true);
                query.setParameter("deliveryId", deliveryId);
                return query.executeUpdate();
            }
        });
        getHibernateTemplate().execute(new HibernateCallback<Integer>() {
            @Override
            public Integer doInHibernate(Session session) throws HibernateException {
                Query query = session.createQuery("update SdlSchool set isToDelete=false where isToDelete=true and deliveryId = :deliveryId");
                query.setParameter("deliveryId", deliveryId);
                return query.executeUpdate();
            }
        });
        getHibernateTemplate().flush();
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sdl.server.integration.repository.IDeliveryRepository#updateDeliveredObjects(java.lang.Long)
     */
    @Override
    public void updateDeliveredObjects(final Long deliveryId) {
        //		Long newObjects = (Long) getHibernateTemplate().execute(new HibernateCallback()
        //		{
        //			@Override
        //			public Object doInHibernate(Session session) throws HibernateException, SQLException
        //			{
        //				Query query = session.createQuery ("select count(distinct l) from SdlSchool s, SdlClass c, SdlLearner l where l.classId=c.classId and c.schoolId=s.schoolId and s.isToDelete=0 and s.deliveryId=? and s.deliveryStatus=?");
        //				query.setLong (0, deliveryId);
        //				query.setParameter (1, CodegroupUtility.MEB_DATASTATUS_IMPORTED);
        //				return query.uniqueResult ();
        //			}
        //		});
        getHibernateTemplate().execute(new HibernateCallback<Integer>() {
            @Override
            public Integer doInHibernate(Session session) throws HibernateException {
                String hql = "update SdlLearner set deliveryStatus=:deliveryStatus where learnerId in (select l.learnerId from SdlLearner l, SdlClass c, SdlSchool s where l.classId=c.classId and c.schoolId=s.schoolId and s.isToDelete=0 and s.deliveryId=:deliveryId and l.deliveryStatus=:oldDeliveryStatus)";
                Query query = session.createQuery(hql);
                query.setParameter("deliveryStatus", CodegroupUtility.MEB_DATASTATUS_DELIVERED);
                query.setParameter("deliveryId", deliveryId);
                query.setParameter("oldDeliveryStatus", CodegroupUtility.MEB_DATASTATUS_IMPORTED);
                return query.executeUpdate();
            }
        });
        getHibernateTemplate().execute(new HibernateCallback<Integer>() {
            @Override
            public Integer doInHibernate(Session session) throws HibernateException {
                String hql = "update SdlClass set deliveryStatus=:deliveryStatus where classId in (select c.classId from SdlClass c, SdlSchool s where c.schoolId=s.schoolId and s.isToDelete=0 and s.deliveryId=:deliveryId and c.deliveryStatus=:oldDeliveryStatus)";
                Query query = session.createQuery(hql);
                query.setParameter("deliveryStatus", CodegroupUtility.MEB_DATASTATUS_DELIVERED);
                query.setParameter("deliveryId", deliveryId);
                query.setParameter("oldDeliveryStatus", CodegroupUtility.MEB_DATASTATUS_IMPORTED);
                return query.executeUpdate();
            }
        });
        getHibernateTemplate().execute(new HibernateCallback<Integer>() {
            @Override
            public Integer doInHibernate(Session session) throws HibernateException {
                Query query = session.createQuery("update SdlSchool set deliveryStatus=:deliveryStatus where isToDelete=0 and deliveryId=:deliveryId and deliveryStatus=:oldDeliveryStatus");
                query.setParameter("deliveryStatus", CodegroupUtility.MEB_DATASTATUS_DELIVERED);
                query.setParameter("deliveryId", deliveryId);
                query.setParameter("oldDeliveryStatus", CodegroupUtility.MEB_DATASTATUS_IMPORTED);
                return query.executeUpdate();
            }
        });
        getHibernateTemplate().flush();

        //		return newObjects;
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sdl.server.integration.repository.IDeliveryRepository#updatePlausistatus(java.lang.Long)
     */
    @Override
    public void updatePlausistatus(Long deliveryId) {
        getHibernateTemplate().execute(new HibernateCallback<Void>() {
            @Override
            public Void doInHibernate(Session session) throws HibernateException {
                org.hibernate.query.Query query = session.createQuery(
                        "update SdlDelivery set plausiStatus = " +
                                "case " +
                                "  when (select count(e) from SdlPlausiError e where e.deliveryId = :deliveryId and e.schoolId is null) = 0 then 2 " +
                                "  when (select count(e) from SdlPlausiError e where e.deliveryId = :deliveryId and e.schoolId is null and e.isConfirmed = 0) > 0 then 1 " +
                                "  else 3 " +
                                "end " +
                                "where deliveryId = :deliveryId"
                );
                query.setParameter("deliveryId", deliveryId);
                query.executeUpdate();
                session.flush();
                return null;
            }
        });
    }


    /* (non-Javadoc)
     * @see ch.bfs.meb.sdl.server.integration.repository.IDeliveryRepository#updateAllPlausistatus(java.lang.Long)
     */
    @Override
    public void updateAllPlausistatus(Long deliveryId) {
        getHibernateTemplate().execute(new HibernateCallback<Void>() {
            @Override
            public Void doInHibernate(Session session) throws HibernateException {
                // Mise à jour des learners
                org.hibernate.query.Query query1 = session.createQuery(
                        "update SdlLearner l set plausiStatus = " +
                                "case " +
                                "  when (select count(e) from SdlPlausiError e where e.learnerId = l.learnerId) = 0 then 2 " +
                                "  when (select count(e) from SdlPlausiError e where e.learnerId = l.learnerId and e.isConfirmed = 0) > 0 then 1 " +
                                "  else 3 " +
                                "end " +
                                "where learnerId in (" +
                                "  select l.learnerId from SdlLearner l, SdlClass c, SdlSchool s " +
                                "  where l.classId = c.classId and c.schoolId = s.schoolId and s.isToDelete = 0 and s.deliveryId = :deliveryId)"
                );
                query1.setParameter("deliveryId", deliveryId);
                query1.executeUpdate();

                // 2Mise à jour des classes
                org.hibernate.query.Query query2 = session.createQuery(
                        "update SdlClass c set plausiStatus = " +
                                "case " +
                                "  when (select count(e) from SdlPlausiError e where e.classId = c.classId and e.learnerId is null) = 0 then 2 " +
                                "  when (select count(e) from SdlPlausiError e where e.classId = c.classId and e.learnerId is null and e.isConfirmed = 0) > 0 then 1 " +
                                "  else 3 " +
                                "end " +
                                "where classId in (" +
                                "  select c.classId from SdlClass c, SdlSchool s " +
                                "  where c.schoolId = s.schoolId and s.isToDelete = 0 and s.deliveryId = :deliveryId)"
                );
                query2.setParameter("deliveryId", deliveryId);
                query2.executeUpdate();

                // Mise à jour des écoles
                org.hibernate.query.Query query3 = session.createQuery(
                        "update SdlSchool s set plausiStatus = " +
                                "case " +
                                "  when (select count(e) from SdlPlausiError e where e.schoolId = s.schoolId and e.classId is null) = 0 then 2 " +
                                "  when (select count(e) from SdlPlausiError e where e.schoolId = s.schoolId and e.classId is null and e.isConfirmed = 0) > 0 then 1 " +
                                "  else 3 " +
                                "end " +
                                "where isToDelete = 0 and deliveryId = :deliveryId"
                );
                query3.setParameter("deliveryId", deliveryId);
                query3.executeUpdate();

                //  Mise à jour de la livraison
                org.hibernate.query.Query query4 = session.createQuery(
                        "update SdlDelivery set plausiStatus = " +
                                "case " +
                                "  when (select count(e) from SdlPlausiError e where e.isToDelete = 0 and e.deliveryId = :deliveryId and e.schoolId is null) = 0 then 2 " +
                                "  when (select count(e) from SdlPlausiError e where e.isToDelete = 0 and e.deliveryId = :deliveryId and e.schoolId is null and e.isConfirmed = 0) > 0 then 1 " +
                                "  else 3 " +
                                "end " +
                                "where deliveryId = :deliveryId"
                );
                query4.setParameter("deliveryId", deliveryId);
                query4.executeUpdate();

                // Synchronisation
                session.flush();
                return null;
            }
        });
    }


    /* (non-Javadoc)
     * @see ch.bfs.meb.sdl.server.integration.repository.IDeliveryRepository#existsSchool(java.lang.Long)
     */
    @Override
    public boolean existsSchool(final Long deliveryId) {
        return (Long) getHibernateTemplate().execute(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                Query<Long> query = session.createQuery("select count(s) from SdlSchool s where s.deliveryId=:deliveryId and rownum = 1", Long.class);
                query.setParameter("deliveryId", deliveryId);

                return query.uniqueResult();
            }
        }) > 0L;
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sdl.server.integration.repository.IDeliveryRepository#getNumberOfObjects(java.lang.Long)
     */
    @Override
    public Long getNumberOfObjects(final Long deliveryId) {
        return (Long) getHibernateTemplate().execute(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                Query<Long> query = session.createQuery(
                        "select count(distinct s)+count(distinct c)+count(distinct l) from SdlSchool s left join s.classes c left join c.learners l where s.deliveryId= :deliveryId", Long.class);
                query.setParameter("deliveryId", deliveryId);
                return query.uniqueResult();
            }
        });
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sdl.server.integration.repository.IDeliveryRepository#getNumberOfLearners(java.lang.Long)
     */
    @Override
    public Long getNumberOfLearners(final Long deliveryId) {
        return (Long) getHibernateTemplate().execute(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                Query<Long> query = session.createQuery(
                        "select count(distinct l) from SdlSchool s, SdlClass c, SdlLearner l where l.classId=c.classId and c.schoolId=s.schoolId and s.deliveryId=:deliveryId",
                        Long.class);
                query.setParameter("deliveryId", deliveryId);
                return query.uniqueResult();
            }
        });
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sdl.server.integration.repository.IDeliveryRepository#modifiedAfter(java.lang.Long, java.util.Date)
     */
    @Override
    public boolean modifiedAfter(final Long deliveryId, final Date modificationDate) {
        // check learner
        Long newObjects = (Long) getHibernateTemplate().execute(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                Query<Long> query = session.createQuery(
                        "select count(l) from SdlSchool s, SdlClass c, SdlLearner l " +
                                "where l.classId=c.classId and c.schoolId=s.schoolId and " +
                                "s.deliveryId=:deliveryId and l.modification_date > :modificationDate", Long.class);
                query.setParameter("deliveryId", deliveryId);
                query.setParameter("modificationDate", modificationDate);
                return query.uniqueResult();
            }
        });
        if (newObjects > 0L) {
            return true;
        }
        // check classes
        newObjects = (Long) getHibernateTemplate().execute(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                org.hibernate.query.Query query = session
                        .createQuery("select count(c) from SdlSchool s, SdlClass c where c.schoolId=s.schoolId and s.deliveryId=:deliveryId and c.modification_date>:modDate");
                query.setParameter("deliveryId", deliveryId);
                query.setParameter("modDate", modificationDate);
                return query.uniqueResult();
            }
        });
        if (newObjects > 0L) {
            return true;
        }
        // check schools
        newObjects = (Long) getHibernateTemplate().execute(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                Query<Long> query = session.createQuery("select count(s) from SdlSchool s where s.deliveryId= :deliveryId and s.modification_date> :modificationDate", Long.class);
                query.setParameter("deliveryId", deliveryId);
                query.setParameter("modificationDate", modificationDate);
                return query.uniqueResult();
            }
        });
        return newObjects > 0L;
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sdl.server.integration.repository.IDeliveryRepository#allPlausibel(java.lang.Long)
     */
    @Override
    public boolean allPlausibel(SdlDelivery delivery) {
        final Long deliveryId = delivery.getDeliveryId();
        // check learner
        Long notPlausibel = (Long) getHibernateTemplate().execute(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                Query<Long> query = session.createQuery(
                        "select count(l) from SdlSchool s, SdlClass c, SdlLearner l " +
                                "where l.classId=c.classId and c.schoolId=s.schoolId and " +
                                "s.deliveryId=:deliveryId and l.plausiStatus= :plausiStatus", Long.class);
                query.setParameter("deliveryId", deliveryId);
                query.setParameter("plausiStatus", CodegroupUtility.MEB_PLAUSISTATUS_NOTVALID);
                return query.uniqueResult();
            }
        });
        if (notPlausibel > 0L) {
            return false;
        }
        // check classes
        notPlausibel = (Long) getHibernateTemplate().execute(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                Query<Long> query = session.createQuery(
                        "select count(c) from SdlSchool s, SdlClass c " +
                                "where c.schoolId=s.schoolId and s.deliveryId=:deliveryId and c.plausiStatus= :plausiStatus", Long.class);
                query.setParameter("deliveryId", deliveryId);
                query.setParameter("plausiStatus", CodegroupUtility.MEB_PLAUSISTATUS_NOTVALID);
                return query.uniqueResult();
            }
        });
        if (notPlausibel > 0L) {
            return false;
        }
        // check schools
        notPlausibel = (Long) getHibernateTemplate().execute(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                Query<Long> query = session.createQuery(
                        "select count(s) from SdlSchool s " +
                                "where s.deliveryId=:deliveryId and s.plausiStatus= :plausiStatus", Long.class);
                query.setParameter("deliveryId", deliveryId);
                query.setParameter("plausiStatus", CodegroupUtility.MEB_PLAUSISTATUS_NOTVALID);
                return query.uniqueResult();
            }
        });
        if (notPlausibel > 0L) {
            return false;
        }
        return !delivery.getPlausiStatus().equals(CodegroupUtility.MEB_PLAUSISTATUS_NOTVALID);
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sdl.server.integration.repository.IDeliveryRepository#prevalidatePossible(java.lang.Long, java.lang.String)
     */
    @Override
           public void prevalidatePossible(SdlDelivery delivery, String username) {
            Date now = new Date();
            Long deliveryId = delivery.getDeliveryId();
            Session session = currentSession();

            // --- 1. SdlLearner ---
            Query<?> q1 = session.createQuery(
                    "update SdlLearner set deliveryStatus=:status, prevalidation_user=:user, prevalidation_date=:date " +
                            "where learnerId in (select l.learnerId from SdlLearner l, SdlClass c, SdlSchool s " +
                            "where l.classId=c.classId and c.schoolId=s.schoolId and s.deliveryId=:deliveryId " +
                            "and l.deliveryStatus=:delivered and l.plausiStatus<>:notValid)"
            );
            q1.setParameter("status", CodegroupUtility.MEB_DATASTATUS_PREVALIDATED);
            q1.setParameter("user", username);
            q1.setParameter("date", now);
            q1.setParameter("deliveryId", deliveryId);
            q1.setParameter("delivered", CodegroupUtility.MEB_DATASTATUS_DELIVERED);
            q1.setParameter("notValid", CodegroupUtility.MEB_PLAUSISTATUS_NOTVALID);
            q1.executeUpdate();

            // --- 2. SdlClass ---
            Query<?> q2 = session.createQuery(
                    "update SdlClass set deliveryStatus=:status, prevalidation_user=:user, prevalidation_date=:date " +
                            "where classId in (select c.classId from SdlClass c, SdlSchool s " +
                            "where c.schoolId=s.schoolId and s.deliveryId=:deliveryId " +
                            "and c.deliveryStatus=:delivered and c.plausiStatus<>:notValid " +
                            "and not exists (select l from SdlLearner l where l.classId=c.classId and l.deliveryStatus<>:status))"
            );
            q2.setParameter("status", CodegroupUtility.MEB_DATASTATUS_PREVALIDATED);
            q2.setParameter("user", username);
            q2.setParameter("date", now);
            q2.setParameter("deliveryId", deliveryId);
            q2.setParameter("delivered", CodegroupUtility.MEB_DATASTATUS_DELIVERED);
            q2.setParameter("notValid", CodegroupUtility.MEB_PLAUSISTATUS_NOTVALID);
            q2.executeUpdate();

            // --- 3. SdlSchool ---
            Query<?> q3 = session.createQuery(
                    "update SdlSchool set deliveryStatus=:status, prevalidation_user=:user, prevalidation_date=:date " +
                            "where schoolId in (select s.schoolId from SdlSchool s " +
                            "where s.deliveryId=:deliveryId and s.deliveryStatus=:delivered " +
                            "and s.plausiStatus<>:notValid and not exists " +
                            "(select c from SdlClass c where c.schoolId=s.schoolId and c.deliveryStatus<>:status))"
            );
            q3.setParameter("status", CodegroupUtility.MEB_DATASTATUS_PREVALIDATED);
            q3.setParameter("user", username);
            q3.setParameter("date", now);
            q3.setParameter("deliveryId", deliveryId);
            q3.setParameter("delivered", CodegroupUtility.MEB_DATASTATUS_DELIVERED);
            q3.setParameter("notValid", CodegroupUtility.MEB_PLAUSISTATUS_NOTVALID);
            q3.executeUpdate();

            // --- 4. Mise à jour de SdlDelivery ---
            delivery.setDeliveryStatus(CodegroupUtility.MEB_DELIVERYSTATUS_PREVALIDATED);
            delivery.setPrevalidation_user(username);
            delivery.setPrevalidation_date(now);
            session.merge(delivery);

            // --- 5. Synchronisation avec la DB ---
            session.flush();
        }



    /* (non-Javadoc)
     * @see ch.bfs.meb.sdl.server.integration.repository.IDeliveryRepository#validatePossible(java.lang.Long, java.lang.Long)
     */
    @Override
    public void validatePossible(SdlDelivery delivery, String username) {
        Date now = new Date();
        Long deliveryId = delivery.getDeliveryId();
        Session session = currentSession();

        // --- 1. SdlLearner : mise à jour statut + validation info ---
        Query<?> q1 = session.createQuery(
                "update SdlLearner set deliveryStatus=:status, validation_user=:user, validation_date=:date " +
                        "where learnerId in (select l.learnerId from SdlLearner l, SdlClass c, SdlSchool s " +
                        "where l.classId=c.classId and c.schoolId=s.schoolId and s.deliveryId=:deliveryId " +
                        "and l.deliveryStatus in (:delivered, :prevalidated) and l.plausiStatus<>:notValid)"
        );
        q1.setParameter("status", CodegroupUtility.MEB_DATASTATUS_VALIDATED);
        q1.setParameter("user", username);
        q1.setParameter("date", now);
        q1.setParameter("deliveryId", deliveryId);
        q1.setParameter("delivered", CodegroupUtility.MEB_DATASTATUS_DELIVERED);
        q1.setParameter("prevalidated", CodegroupUtility.MEB_DATASTATUS_PREVALIDATED);
        q1.setParameter("notValid", CodegroupUtility.MEB_PLAUSISTATUS_NOTVALID);
        q1.executeUpdate();

        // --- 2. SdlLearner : mise à jour prevalidation si null ---
        Query<?> q2 = session.createQuery(
                "update SdlLearner set " +
                        "prevalidation_user = case when prevalidation_user is null then validation_user else prevalidation_user end, " +
                        "prevalidation_date = case when prevalidation_date is null then validation_date else prevalidation_date end " +
                        "where learnerId in (select l.learnerId from SdlLearner l, SdlClass c, SdlSchool s " +
                        "where l.classId=c.classId and c.schoolId=s.schoolId and s.deliveryId=:deliveryId " +
                        "and l.deliveryStatus=:status)"
        );
        q2.setParameter("deliveryId", deliveryId);
        q2.setParameter("status", CodegroupUtility.MEB_DATASTATUS_VALIDATED);
        q2.executeUpdate();

        // --- 3. SdlClass : validation ---
        Query<?> q3 = session.createQuery(
                "update SdlClass set deliveryStatus=:status, validation_user=:user, validation_date=:date " +
                        "where classId in (select c.classId from SdlClass c, SdlSchool s " +
                        "where c.schoolId=s.schoolId and s.deliveryId=:deliveryId " +
                        "and c.deliveryStatus in (:delivered, :prevalidated) and c.plausiStatus<>:notValid " +
                        "and not exists (select l from SdlLearner l where l.classId=c.classId and l.deliveryStatus<>:status))"
        );
        q3.setParameter("status", CodegroupUtility.MEB_DATASTATUS_VALIDATED);
        q3.setParameter("user", username);
        q3.setParameter("date", now);
        q3.setParameter("deliveryId", deliveryId);
        q3.setParameter("delivered", CodegroupUtility.MEB_DATASTATUS_DELIVERED);
        q3.setParameter("prevalidated", CodegroupUtility.MEB_DATASTATUS_PREVALIDATED);
        q3.setParameter("notValid", CodegroupUtility.MEB_PLAUSISTATUS_NOTVALID);
        q3.executeUpdate();

        // --- 4. SdlClass : prevalidation si null ---
        Query<?> q4 = session.createQuery(
                "update SdlClass set " +
                        "prevalidation_user = case when prevalidation_user is null then validation_user else prevalidation_user end, " +
                        "prevalidation_date = case when prevalidation_date is null then validation_date else prevalidation_date end " +
                        "where classId in (select c.classId from SdlClass c, SdlSchool s " +
                        "where c.schoolId=s.schoolId and s.deliveryId=:deliveryId " +
                        "and c.deliveryStatus=:status)"
        );
        q4.setParameter("deliveryId", deliveryId);
        q4.setParameter("status", CodegroupUtility.MEB_DATASTATUS_VALIDATED);
        q4.executeUpdate();

        // --- 5. SdlSchool : validation ---
        Query<?> q5 = session.createQuery(
                "update SdlSchool set deliveryStatus=:status, validation_user=:user, validation_date=:date " +
                        "where schoolId in (select s.schoolId from SdlSchool s " +
                        "where s.deliveryId=:deliveryId and s.deliveryStatus in (:delivered, :prevalidated) " +
                        "and s.plausiStatus<>:notValid " +
                        "and not exists (select c from SdlClass c where c.schoolId=s.schoolId and c.deliveryStatus<>:status))"
        );
        q5.setParameter("status", CodegroupUtility.MEB_DATASTATUS_VALIDATED);
        q5.setParameter("user", username);
        q5.setParameter("date", now);
        q5.setParameter("deliveryId", deliveryId);
        q5.setParameter("delivered", CodegroupUtility.MEB_DATASTATUS_DELIVERED);
        q5.setParameter("prevalidated", CodegroupUtility.MEB_DATASTATUS_PREVALIDATED);
        q5.setParameter("notValid", CodegroupUtility.MEB_PLAUSISTATUS_NOTVALID);
        q5.executeUpdate();

        // --- 6. SdlSchool : prevalidation si null ---
        Query<?> q6 = session.createQuery(
                "update SdlSchool set " +
                        "prevalidation_user = case when prevalidation_user is null then validation_user else prevalidation_user end, " +
                        "prevalidation_date = case when prevalidation_date is null then validation_date else prevalidation_date end " +
                        "where schoolId in (select s.schoolId from SdlSchool s where s.deliveryId=:deliveryId and s.deliveryStatus=:status)"
        );
        q6.setParameter("deliveryId", deliveryId);
        q6.setParameter("status", CodegroupUtility.MEB_DATASTATUS_VALIDATED);
        q6.executeUpdate();

        // --- 7. Mise à jour de la livraison ---
        delivery.setDeliveryStatus(CodegroupUtility.MEB_DELIVERYSTATUS_VALIDATED);
        delivery.setValidation_user(username);
        delivery.setValidation_date(now);
        if (delivery.getPrevalidation_user() == null) {
            delivery.setPrevalidation_user(username);
            delivery.setPrevalidation_date(now);
        }
        session.merge(delivery);
        session.flush();
    }


    /* (non-Javadoc)
     * @see ch.bfs.meb.sdl.server.integration.repository.IDeliveryRepository#undoPrevalidate(java.lang.Long)
     */
    @Override
    public void undoPrevalidate(SdlDelivery delivery) {
        Long deliveryId = delivery.getDeliveryId();
        getHibernateTemplate().execute(new HibernateCallback<Integer>() {
            public Integer doInHibernate(Session session) throws HibernateException {
                String hql = "update SdlLearner set deliveryStatus=:deliveryStatus, prevalidation_user=null, " +
                        "prevalidation_date=null where learnerId in (select l.learnerId from SdlLearner l, SdlClass c, " +
                        "SdlSchool s where l.classId=c.classId and c.schoolId=s.schoolId and s.deliveryId=:deliveryId and l.deliveryStatus=:dataStatus)";

                Query query = session.createQuery(hql);
                query.setParameter("deliveryStatus", CodegroupUtility.MEB_DATASTATUS_DELIVERED);
                query.setParameter("deliveryId", deliveryId);
                query.setParameter("dataStatus", CodegroupUtility.MEB_DATASTATUS_PREVALIDATED);

                return query.executeUpdate();
            }
        });
        getHibernateTemplate().execute(new HibernateCallback<Integer>() {
            public Integer doInHibernate(Session session) throws HibernateException {
                String hql = "update SdlClass set deliveryStatus=:deliveryStatus, prevalidation_user=null, prevalidation_date=null " +
                        "where classId in (select c.classId from SdlClass c, SdlSchool s where c.schoolId=s.schoolId and s.deliveryId=:deliveryId and c.deliveryStatus=:dataStatus)";

                Query query = session.createQuery(hql);
                query.setParameter("deliveryStatus", CodegroupUtility.MEB_DATASTATUS_DELIVERED);
                query.setParameter("deliveryId", deliveryId);
                query.setParameter("dataStatus", CodegroupUtility.MEB_DATASTATUS_PREVALIDATED);

                return query.executeUpdate();
            }
        });
        getHibernateTemplate().execute(new HibernateCallback<Integer>() {
            public Integer doInHibernate(Session session) throws HibernateException {
                String hql = "update SdlSchool set deliveryStatus=:deliveryStatus, prevalidation_user=null, prevalidation_date=null " +
                        "where schoolId in (select s.schoolId from SdlSchool s where s.deliveryId=:deliveryId and s.deliveryStatus=:dataStatus)";

                Query query = session.createQuery(hql);
                query.setParameter("deliveryStatus", CodegroupUtility.MEB_DATASTATUS_DELIVERED);
                query.setParameter("deliveryId", deliveryId);
                query.setParameter("dataStatus", CodegroupUtility.MEB_DATASTATUS_PREVALIDATED);

                return query.executeUpdate();
            }
        });
        delivery.setDeliveryStatus(CodegroupUtility.MEB_DELIVERYSTATUS_DELIVERED);
        delivery.setPrevalidation_user(null);
        delivery.setPrevalidation_date(null);
        getHibernateTemplate().merge(delivery);
        getHibernateTemplate().flush();
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sdl.server.integration.repository.IDeliveryRepository#undoValidate(java.lang.Long)
     */
    @Override
    public void undoValidate(SdlDelivery delivery, Long newDataStatus) {
        Long deliveryId = delivery.getDeliveryId();
        getHibernateTemplate().execute(new HibernateCallback<Integer>() {
            public Integer doInHibernate(Session session) throws HibernateException {
                String hql = "update SdlLearner set deliveryStatus=:newDataStatus, validation_user=null, validation_date=null " +
                        "where learnerId in (select l.learnerId from SdlLearner l, SdlClass c, SdlSchool s where l.classId=c.classId and c.schoolId=s.schoolId and s.deliveryId=:deliveryId and l.deliveryStatus=:oldDataStatus)";

                Query query = session.createQuery(hql);
                query.setParameter("newDataStatus", newDataStatus);
                query.setParameter("deliveryId", deliveryId);
                query.setParameter("oldDataStatus", CodegroupUtility.MEB_DATASTATUS_VALIDATED);

                return query.executeUpdate();
            }
        });
        getHibernateTemplate().execute(new HibernateCallback<Object>() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                Query query = session.createQuery(
                        "update SdlClass set deliveryStatus=:deliveryStatus, validation_user=null, validation_date=null " +
                                "where classId in (select c.classId from SdlClass c, SdlSchool s where c.schoolId=s.schoolId" +
                                " and s.deliveryId=:deliveryId and c.deliveryStatus=:dataStatus)");

                query.setParameter("deliveryStatus", newDataStatus);
                query.setParameter("deliveryId", deliveryId);
                query.setParameter("dataStatus", CodegroupUtility.MEB_DATASTATUS_VALIDATED);
                return query.executeUpdate();
            }
        });
        getHibernateTemplate().execute(new HibernateCallback<Object>() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                Query query = session.createQuery(
                        "update SdlSchool set deliveryStatus=:newDataStatus, validation_user=null, validation_date=null " +
                                "where schoolId in (select s.schoolId from SdlSchool s where s.deliveryId=:deliveryId and s.deliveryStatus=:currentStatus)");

                query.setParameter("newDataStatus", newDataStatus);
                query.setParameter("deliveryId", deliveryId);
                query.setParameter("currentStatus", CodegroupUtility.MEB_DATASTATUS_VALIDATED);

                return query.executeUpdate();
            }
        });
        delivery.setDeliveryStatus(CodegroupUtility.MEB_DELIVERYSTATUS_PREVALIDATED);
        delivery.setValidation_user(null);
        delivery.setValidation_date(null);
        getHibernateTemplate().merge(delivery);
        getHibernateTemplate().flush();
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sdl.server.integration.repository.IDeliveryRepository#deleteAll(java.lang.Long, java.lang.Long)
     */
    @Override
    public void deleteAll(Long deliveryId, Long checkStatus) {
        // check schools
        Query<SdlSchool> query = currentSession().createQuery("from SdlSchool where deliveryId=:deliveryId and deliveryStatus>=:checkStatus", SdlSchool.class);
        query.setParameter("deliveryId", deliveryId);
        query.setParameter("checkStatus", checkStatus);
        query.setMaxResults(1);
        if (query.getResultList().size() > 0) {
            throw new MebUncheckedNotMonitoredException("maintain.delete.childrenValidatedError.messages");
        }




// check classes
        Query<SdlClass> classQuery = currentSession().createQuery(
                "from SdlClass where exists (from SdlClass c, SdlSchool s where c.schoolId=s.schoolId and s.deliveryId=:deliveryId and c.deliveryStatus>=:checkStatus)",
                SdlClass.class
        );
        classQuery.setParameter("deliveryId", deliveryId);
        classQuery.setParameter("checkStatus", checkStatus);
        classQuery.setMaxResults(1);
        if (!classQuery.getResultList().isEmpty()) {
            throw new MebUncheckedNotMonitoredException("maintain.delete.childrenValidatedError.messages");
        }

// check learners
        Query<SdlLearner> learnerQuery = currentSession().createQuery(
                "from SdlLearner where exists (from SdlLearner l, SdlClass c, SdlSchool s where l.classId=c.classId and c.schoolId=s.schoolId and s.deliveryId=:deliveryId and l.deliveryStatus>=:checkStatus)",
                SdlLearner.class
        );
        learnerQuery.setParameter("deliveryId", deliveryId);
        learnerQuery.setParameter("checkStatus", checkStatus);
        learnerQuery.setMaxResults(1);
        if (!learnerQuery.getResultList().isEmpty()) {
            throw new MebUncheckedNotMonitoredException("maintain.delete.childrenValidatedError.messages");
        }

        // delete plausierrors on all objects of delivery
        getHibernateTemplate().execute(new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException {
                String hql = "delete from SdlPlausiError where deliveryId= :deliveryId";
               return session.createQuery( hql )
                        .setParameter( "deliveryId", deliveryId)
                        .executeUpdate();

            }
        });
        // delete learners, classes and schools
        getHibernateTemplate().execute(session -> {
            Query deleteQuery = session.createQuery(
                    "delete from SdlLearner where classId in (select c.classId from SdlClass c, SdlSchool s where c.schoolId=s.schoolId and s.deliveryId=:deliveryId)"
            );
            deleteQuery.setParameter("deliveryId", deliveryId);
            return deleteQuery.executeUpdate();
        });
        getHibernateTemplate().execute(session -> {
            String queryString = "delete from SdlClass where schoolId in (select s.schoolId from SdlSchool s where s.deliveryId=:deliveryId)";
            Query deleteQuery = session.createQuery(queryString);
            deleteQuery.setParameter("deliveryId", deliveryId);
            return deleteQuery.executeUpdate();
        });
        String hql = "delete from SdlSchool where deliveryId= :deliveryId";
        getHibernateTemplate().execute(new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException {
                Query query = session.createQuery(hql);
                query.setParameter("deliveryId", deliveryId);
                return query.executeUpdate();
            }
        });
        getHibernateTemplate().flush();
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sdl.server.integration.repository.IDeliveryRepository#deleteAll(java.lang.Long)
     */
    @Override
    public void deleteAll(Long deliveryId) {
        // check if the delivery can be deleted
        MebUser user = (MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        long status = CodegroupUtility.MEB_DATASTATUS_PREVALIDATED;
        if (user.isInRole(SecurityConstants.ROLE_SDL_DV)) {
            status = CodegroupUtility.MEB_DATASTATUS_VALIDATED;
        }

        deleteAll(deliveryId, status);
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sdl.server.integration.repository.IDeliveryRepository#deleteDelivery(ch.bfs.meb.sdl.server.integration.dto.SdlDelivery)
     */
    @Override
    public void deleteDelivery(SdlDelivery delivery) {
        getHibernateTemplate().delete(delivery);
        getHibernateTemplate().flush();
    }

    @SuppressWarnings("unchecked")
    private List<SdlDelivery> getDeliveriesByIds(List<Long> deliveryIds) {
        if (deliveryIds == null || deliveryIds.isEmpty()) {
            return new ArrayList<SdlDelivery>();
        }

        // query deliveries including the plausi errors
        Query queryResult = currentSession().createQuery("from SdlDelivery d where d.deliveryId in (:deliveryIds)");
        queryResult.setParameterList("deliveryIds", deliveryIds);
        // DistinctRootEntityResultTransformer not required => order by map below already does the same
        //		queryResult.setResultTransformer(DistinctRootEntityResultTransformer.INSTANCE);

        List<SdlDelivery> tempList = queryResult.list();

        // reestablish old sort order
        Map<Long, SdlDelivery> mapById = new HashMap<Long, SdlDelivery>(tempList.size());
        for (SdlDelivery entity : tempList) {
            mapById.put(entity.getDeliveryId(), entity);
        }
        List<SdlDelivery> resultList = new ArrayList<SdlDelivery>(mapById.size());
        for (Long id : deliveryIds) {
            SdlDelivery entity = mapById.get(id);
            if (entity != null) {
                resultList.add(entity);
            }
        }

        return resultList;
    }

    @Override
    @SuppressWarnings("unchecked")
    public HashMap<Long, String> getSchoolConfirmRules(final Long deliveryId) {
        HashMap<Long, String> schoolConfirmRules = new HashMap<Long, String>();
        List<Object[]> resultList = (List<Object[]>) getHibernateTemplate().execute(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                String hql = "select s.schoolId, s.confirmRules from SdlSchool s where s.deliveryId= :deliveryId and s.confirmRules is not null";
                org.hibernate.query.Query query = session.createQuery(hql);
                query.setParameter("deliveryId", deliveryId);
                return query.list();
            }
        });
        for (Object[] row : resultList) {
            Long schoolId = ((Number) row[0]).longValue();
            String confirmRules = (String) row[1];
            schoolConfirmRules.put(schoolId, confirmRules);
        }
        return schoolConfirmRules;
    }

    @Override
    @SuppressWarnings("unchecked")
    public HashMap<Long, String> getClassConfirmRules(final Long deliveryId) {
        HashMap<Long, String> classConfirmRules = new HashMap<Long, String>();
        List<Object[]> resultList = (List<Object[]>) getHibernateTemplate().execute(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                String sql = "select c.classId, c.confirmRules from Sdl_Classes c, Sdl_Schools s where c.schoolId = s.schoolId and s.deliveryId= :deliveryId and c.confirmRules is not null";

                org.hibernate.query.NativeQuery<?> query = session.createNativeQuery(sql);
                query.setParameter("deliveryId", deliveryId);

                return query.list();
            }
        });
        for (Object[] row : resultList) {
            Long classId = ((Number) row[0]).longValue();
            String confirmRules = (String) row[1];
            classConfirmRules.put(classId, confirmRules);
        }
        return classConfirmRules;
    }

    @Override
    @SuppressWarnings("unchecked")
    public HashMap<Long, String> getLearnerConfirmRules(final Long deliveryId) {
        HashMap<Long, String> learnerConfirmRules = new HashMap<Long, String>();
        List<Object[]> resultList = (List<Object[]>) getHibernateTemplate().execute(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                String sql = "select l.learnerId, l.confirmRules from Sdl_Learners l, Sdl_Classes c, Sdl_Schools s where l.classId = c.classId and c.schoolId = s.schoolId and s.deliveryId= :deliveryId and l.confirmRules is not null";

                org.hibernate.query.NativeQuery<?> query = session.createNativeQuery(sql);
                query.setParameter("deliveryId", deliveryId);

                return query.list();
            }
        });
        for (Object[] row : resultList) {
            Long learnerId = ((Number) row[0]).longValue();
            String confirmRules = (String) row[1];
            learnerConfirmRules.put(learnerId, confirmRules);
        }
        return learnerConfirmRules;
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
