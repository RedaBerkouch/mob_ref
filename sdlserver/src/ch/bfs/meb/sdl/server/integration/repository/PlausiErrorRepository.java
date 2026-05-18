package ch.bfs.meb.sdl.server.integration.repository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.bfs.meb.exception.MebUncheckedException;
import org.hibernate.HibernateException;
import org.hibernate.query.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.query.NativeQuery;
import org.springframework.orm.hibernate5.HibernateCallback;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;

import ch.bfs.meb.sdl.server.business.plausi.PlausierrorBO;
import ch.bfs.meb.sdl.server.business.plausi.PlausireportFactory;
import ch.bfs.meb.sdl.server.integration.dto.SdlPlausiError;
import ch.bfs.meb.util.CodegroupUtility;

/** Repository for {@link ch.bfs.meb.server.commons.integration.dto.PlausiError}. */
public class PlausiErrorRepository extends HibernateDaoSupport implements IPlausiErrorRepository {
    private final static long IN_CLAUSE_LIMIT = 500;

    private List<SdlPlausiError> buildErrorListFromSQLResults(List<SdlPlausiError> errorList, List<Object[]> additionalDataList) {
        List<SdlPlausiError> plausierrors = new ArrayList<>();
        // Build complete error information and remove the to be deleted errors
        for (int i = 0; i < errorList.size(); i++) {
            SdlPlausiError error = errorList.get(i);
            Object[] objs = additionalDataList.get(i);
            error.addSchoolInfoWithLabel((String) objs[0], (String) objs[1], (String) objs[7]);
            if (objs[2] != null) {
                error.addClassInfo((String) objs[2]);
                if (objs[3] != null) {
                    error.addLearnerInfo((String) objs[3], (String) objs[4], (String) objs[5]);
                }
            }
            //  School.isToDelete != null?
            boolean isSchoolToDelete = (objs[6] != null && objs[6] instanceof BigDecimal && ((BigDecimal) objs[6]).intValue() == 1);
            if (!isSchoolToDelete && !error.getIsToDelete()) {
                plausierrors.add(error);
            }
        }
        return plausierrors;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<SdlPlausiError> getPlausiErrorsForCanton(final Long cantonId) {
        Session session = currentSession();

        // HQL principal
        String errorQueryString =
                "SELECT e FROM SdlPlausiError e " +
                        "WHERE e.cantonId = :cantonId " +
                        "AND e.isConfirmed = false " +
                        "AND e.plausi.objectLevel = :objectLevel " +
                        "ORDER BY e.schoolId DESC, e.errorId ASC";

        org.hibernate.query.Query<SdlPlausiError> errorQuery =
                session.createQuery(errorQueryString, SdlPlausiError.class);
        errorQuery.setParameter("cantonId", cantonId);
        errorQuery.setParameter("objectLevel", CodegroupUtility.SDL_OBJECTTYPE_CANTON);
        errorQuery.setMaxResults(PlausireportFactory.MAX_ERRORS);

        // SQL natif additionnel
        String additionalQueryString = getAdditionalQueryString(
                false,
                "WHERE e.cantonId = :cantonId AND e.isConfirmed = 0 AND p.objectLevel = :objectLevel"
        );

        @SuppressWarnings("unchecked")
        List<Object[]> additionalDataList = session
                .createNativeQuery(additionalQueryString)
                .setParameter("cantonId", cantonId)
                .setParameter("objectLevel", CodegroupUtility.SDL_OBJECTTYPE_CANTON)
                .setMaxResults(PlausireportFactory.MAX_ERRORS)
                .list();

        List<SdlPlausiError> errorList = errorQuery.list();

        return buildErrorListFromSQLResults(errorList, additionalDataList);
    }



    @Override
    public List<SdlPlausiError> getPlausiErrorsForDelivery(final Long deliveryId) {
        Session session = currentSession();

        // HQL principal
        String errorQueryString =
                "SELECT e FROM SdlPlausiError e " +
                        "WHERE e.deliveryId = :deliveryId " +
                        "AND e.isConfirmed = 0 " +
                        "AND e.plausi.objectLevel >= :objectLevel " +
                        "ORDER BY e.schoolId DESC, e.errorId ASC";

        org.hibernate.query.Query<SdlPlausiError> errorQuery =
                session.createQuery(errorQueryString, SdlPlausiError.class);
        errorQuery.setParameter("deliveryId", deliveryId);
        errorQuery.setParameter("objectLevel", CodegroupUtility.SDL_OBJECTTYPE_DELIVERY);
        errorQuery.setMaxResults(PlausireportFactory.MAX_ERRORS);

        // SQL natif additionnel
        String additionalQueryString = getAdditionalQueryString(
                true,
                "WHERE e.deliveryId = :deliveryId " +
                        "AND e.isConfirmed = 0 " +
                        "AND p.objectLevel >= :objectLevel"
        );

        @SuppressWarnings("unchecked")
        List<Object[]> additionalDataList = session
                .createNativeQuery(additionalQueryString)
                .setParameter("deliveryId", deliveryId)
                .setParameter("objectLevel", CodegroupUtility.SDL_OBJECTTYPE_DELIVERY)
                .setMaxResults(PlausireportFactory.MAX_ERRORS)
                .list();

        List<SdlPlausiError> errorList = errorQuery.list();

        if (errorList.size() != additionalDataList.size()) {
            throw new MebUncheckedException(
                    "inconsistent BUR data (deliveryId=" + deliveryId +
                            ", errorList.size=" + errorList.size() +
                            ", additionalDataList.size=" + additionalDataList.size() + ")"
            );
        }

        return buildErrorListFromSQLResults(errorList, additionalDataList);
    }



    /* @see ch.bfs.meb.sdl.server.integration.repository.IPlausiErrorRepository#getPlausiErrorsForDelivery(java.lang.Long) */
    @SuppressWarnings("unchecked")
    @Override
    public List<SdlPlausiError> getAllPlausiErrorsForDelivery(final Long deliveryId) {
        return (List<SdlPlausiError>) getHibernateTemplate().execute(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
// Select all errors of level delivery and below - Mantis 1646: Order by newest schools first!
                String errorQueryString = "SELECT e FROM SdlPlausiError e "//
                        + "WHERE e.deliveryId=:deliveryId and e.plausi.objectLevel >= :objectLevel "//
                        + "ORDER BY e.schoolId desc, e.errorId asc ";
                org.hibernate.query.Query<SdlPlausiError> errorQuery = session.createQuery(errorQueryString, SdlPlausiError.class);
                errorQuery.setParameter("deliveryId", deliveryId);
                errorQuery.setParameter("objectLevel", CodegroupUtility.SDL_OBJECTTYPE_DELIVERY);
                errorQuery.setMaxResults(PlausireportFactory.MAX_ERRORS);

                String additionalQueryString = getAdditionalQueryString(
                        true,"WHERE e.deliveryId=:deliveryId and e.isConfirmed=0 and p.objectLevel >= :objectLevel");
                org.hibernate.query.NativeQuery sqlQuery = session.createNativeQuery(additionalQueryString);
                sqlQuery.setParameter("deliveryId", deliveryId);
                sqlQuery.setParameter("objectLevel", CodegroupUtility.SDL_OBJECTTYPE_DELIVERY);
                sqlQuery.setMaxResults(PlausireportFactory.MAX_ERRORS);

                List<SdlPlausiError> errorList = errorQuery.list();
                List<Object[]> additionalDataList = sqlQuery.list();

                if (errorList.size() != additionalDataList.size()) {
                    throw new MebUncheckedException("inconsistent BUR data (deliveryId=" + deliveryId + ", "
                            + "errorList.size=" + errorList.size() + ", additionalDataList.size=" + additionalDataList.size() + ")");
                }

                return buildErrorListFromSQLResults(errorList, additionalDataList);
            }
        });
    }

    /* @see ch.bfs.meb.sdl.server.integration.repository.IPlausiErrorRepository#findForCanton(java.lang.Long, java.lang.Long) */
    @SuppressWarnings("unchecked")
    @Override
    public List<SdlPlausiError> findForCanton(final Long cantonId, final Long plausiId, final Boolean toDelete) {
        return (List<SdlPlausiError>) getHibernateTemplate().execute(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                org.hibernate.query.Query<SdlPlausiError> query = session.createQuery("FROM SdlPlausiError WHERE cantonId=:cantonId and plausiId=:plausiId and isToDelete=:toDelete", SdlPlausiError.class);
                query.setParameter("cantonId", cantonId);
                query.setParameter("plausiId", plausiId);
                query.setParameter("toDelete", toDelete);
                return query.list();
            }
        });
    }

    /* @see ch.bfs.meb.sdl.server.integration.repository.IPlausiErrorRepository#findForDelivery(java.lang.Long, java.lang.Long) */
    @SuppressWarnings("unchecked")
    @Override
    public List<SdlPlausiError> findExternalErrorsForDelivery(final Long deliveryId, final Boolean toDelete) {
        return (List<SdlPlausiError>) getHibernateTemplate().execute(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
// Load all errors of external plausis for deliveryId
                String queryString = "FROM SdlPlausiError e "
                        + "WHERE e.deliveryId=:deliveryId "
                        + "AND e.isToDelete=:toDelete "
                        + "AND e.plausi in (SELECT p.plausiId FROM SdlPlausi p WHERE p.type=:type) ";
                org.hibernate.query.Query<SdlPlausiError> query = session.createQuery(queryString, SdlPlausiError.class);
                query.setParameter("deliveryId", deliveryId);
                query.setParameter("toDelete", toDelete);
                query.setParameter("type", CodegroupUtility.MEB_PLAUSITYPE_EXTERNAL);
                return query.list();
            }
        });
    }

    /* @see ch.bfs.meb.sdl.server.integration.repository.IPlausiErrorRepository#findForSchool(java.lang.Long, java.lang.Long) */
    @SuppressWarnings("unchecked")
    @Override
    public List<SdlPlausiError> findForSchool(final Long schoolId, final Long plausiId) {
        return (List<SdlPlausiError>) getHibernateTemplate().execute(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                org.hibernate.query.Query<SdlPlausiError> query = session.createQuery("FROM SdlPlausiError WHERE schoolId=:schoolId AND plausiId=:plausiId", SdlPlausiError.class);
                query.setParameter("schoolId", schoolId);
                query.setParameter("plausiId", plausiId);
                return query.list();
            }
        });
    }

    /*  @see ch.bfs.meb.sdl.server.integration.repository.IPlausiErrorRepository#findForClass(java.lang.Long, java.lang.Long) */
    @SuppressWarnings("unchecked")
    @Override
    public List<SdlPlausiError> findForClass(final Long classId, final Long plausiId) {
        return (List<SdlPlausiError>) getHibernateTemplate().execute(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                org.hibernate.query.Query<SdlPlausiError> query = session.createQuery("FROM SdlPlausiError WHERE classId=:classId and plausiId=:plausiId ", SdlPlausiError.class);
                query.setParameter("classId", classId);
                query.setParameter("plausiId", plausiId);
                return query.list();
            }
        });
    }

    /* @see ch.bfs.meb.sdl.server.integration.repository.IPlausiErrorRepository#findForLearner(java.lang.Long, java.lang.Long) */
    @SuppressWarnings("unchecked")
    @Override
    public List<SdlPlausiError> findForLearner(final Long learnerId, final Long plausiId) {
        return (List<SdlPlausiError>) getHibernateTemplate().execute(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                org.hibernate.query.Query<SdlPlausiError> query = session.createQuery("FROM SdlPlausiError WHERE learnerId=:learnerId and plausiId=:plausiId", SdlPlausiError.class);
                query.setParameter("learnerId", learnerId);
                query.setParameter("plausiId", plausiId);
                return query.list();
            }
        });
    }

    /* @see ch.bfs.meb.sdl.server.integration.repository.IPlausiErrorRepository#getPlausierrorById(java.lang.Long) */
    @Override
    public SdlPlausiError getPlausierrorById(Long plausierrorId) {
        return getHibernateTemplate().get(SdlPlausiError.class, plausierrorId);
    }

    /* @see ch.bfs.meb.sdl.server.integration.repository.IPlausiErrorRepository#getPlausiErrorsByPlausiId(java.lang.Long) */
    @Override
    public Long getNofPlausiErrorsByPlausiId(final Long plausiId) {
        return (Long) getHibernateTemplate().execute(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                org.hibernate.query.Query<Long> query = session.createQuery("SELECT count(*) FROM SdlPlausiError e WHERE e.plausi.plausiId=:plausiId", Long.class);
                query.setParameter("plausiId", plausiId);
                return query.uniqueResult();
            }
        });
    }

    /* @see ch.bfs.meb.sdl.server.integration.repository.IPlausiErrorRepository#isDeliveryWithUnconfirmedErrors(java.lang.Long) */
    @Override
    public boolean isDeliveryWithUnconfirmedErrors(final Long deliveryId) {
        return (Boolean) getHibernateTemplate().execute(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                org.hibernate.query.NativeQuery query = session.createNativeQuery(
                        "SELECT * FROM dual WHERE exists " +
                                "(" +
                                "SELECT * FROM sdl_plausierrors e, sdl_schools s " +
                                "WHERE e.deliveryId=:deliveryId AND s.deliveryId=:deliveryId AND e.isConfirmed = 0 " +
                                "AND ((e.schoolId is null AND e.isToDelete=0) OR (e.schoolId=s.schoolId AND s.isToDelete=0)) " +
                                ")");
                query.setParameter("deliveryId", deliveryId);
                return query.list().size() > 0;
            }
        });
    }

    /* @see ch.bfs.meb.sdl.server.integration.repository.IPlausiErrorRepository#insertPlausiError(ch.bfs.meb.sdl.server.integration.dto.SdlPlausiError) */
    @Override
    public Long insertPlausiError(SdlPlausiError plausiError) {
        Long plausiErrorId = (Long) getHibernateTemplate().save(plausiError);
        getHibernateTemplate().flush();
        return plausiErrorId;
    }

    /* @see ch.bfs.meb.sdl.server.integration.repository.IPlausiErrorRepository#insertPlausiError(ch.bfs.meb.sdl.server.integration.dto.SdlPlausiError) */
    @Override
    public void insertPlausiError(List<SdlPlausiError> plausiErrorList) {
        for (SdlPlausiError plausiError : plausiErrorList) {
            getHibernateTemplate().save(plausiError);
        }
        getHibernateTemplate().flush();
    }

    /* @see ch.bfs.meb.sdl.server.integration.repository.IPlausiErrorRepository#updatePlausiError(ch.bfs.meb.sdl.server.integration.dto.SdlPlausiError) */
    @Override
    public SdlPlausiError updatePlausiError(SdlPlausiError plausiError) {
        if (plausiError.getPlausi() == null) {
            // update from client
            SdlPlausiError save = getHibernateTemplate().load(SdlPlausiError.class, plausiError.getErrorId());
            plausiError.resetPlausi(save.getPlausi());
        }
        plausiError = getHibernateTemplate().merge(plausiError);
        getHibernateTemplate().flush();
        return plausiError;
    }

    /* @see ch.bfs.meb.sdl.server.integration.repository.IPlausiErrorRepository#updatePlausiError(ch.bfs.meb.sdl.server.integration.dto.SdlPlausiError) */
    @Override
    public List<SdlPlausiError> updatePlausiError(List<SdlPlausiError> plausiErrorList) {
        List<SdlPlausiError> mergedPlausiErrorList = new ArrayList<>();
        for (SdlPlausiError plausiError : plausiErrorList) {
            if (plausiError.getPlausi() == null) {
                // update from client
                SdlPlausiError save = getHibernateTemplate().load(SdlPlausiError.class, plausiError.getErrorId());
                plausiError.resetPlausi(save.getPlausi());
            }
            plausiError = getHibernateTemplate().merge(plausiError);
            mergedPlausiErrorList.add(plausiError);
        }
        getHibernateTemplate().flush();
        return mergedPlausiErrorList;
    }

    /* @see ch.bfs.meb.sdl.server.integration.repository.IPlausiErrorRepository#deletePlausiError(ch.bfs.meb.sdl.server.integration.dto.SdlPlausiError) */
    @Override
    public void deletePlausiError(final SdlPlausiError plausiError) {
        getHibernateTemplate().execute(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                org.hibernate.query.Query query = session.createQuery("DELETE FROM SdlPlausiError e WHERE e.errorId=:errorId");
                query.setParameter("errorId", plausiError.getErrorId());
                return query.executeUpdate();
            }
        });
        // getHibernateTemplate().delete (plausiError); doesn't work -> Mantis 1405: a different object with the same identifier value was already associated with the session
        getHibernateTemplate().flush();
    }

    /* @see ch.bfs.meb.sdl.server.integration.repository.IPlausiErrorRepository#deletePlausiErrorBOs(java.util.List) */
    @Override
    public void deletePlausiErrorBOs(final List<PlausierrorBO> plausiErrorBos) {
        if (plausiErrorBos.isEmpty()) {
            return;
        }
        List<Long> idList = new ArrayList<>();
        long counter = 0;
        for (PlausierrorBO plausierrorBO : plausiErrorBos) {
            counter++;
            idList.add(plausierrorBO.getThisPlausierror().getErrorId());
            if (counter % IN_CLAUSE_LIMIT == 0) {
                getHibernateTemplate().bulkUpdate("DELETE FROM SdlPlausiError WHERE errorId in (" + idList + ")");
                idList.clear();
            }
        }
        getHibernateTemplate().flush();
    }

    /* @see ch.bfs.meb.sdl.server.integration.repository.IPlausiErrorRepository#deletePlausiErrorBOs(java.util.List) */
    @Override
    public void deletePlausiErrors(final List<SdlPlausiError> plausiErrors) {
        StringBuilder idList = new StringBuilder();
        long counter = 0;
        for (SdlPlausiError plausierror : plausiErrors) {
            counter++;
            if (counter > 1) {
                idList.append(",");
            }
            idList.append(plausierror.getErrorId());

            if (counter % IN_CLAUSE_LIMIT == 0) {
                getHibernateTemplate().bulkUpdate("DELETE FROM SdlPlausiError WHERE errorId in (" + idList + ")");
                idList = new StringBuilder();
                counter = 0;
            }
        }
        getHibernateTemplate().flush();
    }

    private Map<Long, Long> buildErrorNumbersMap(List<Object[]> errorNumbersList) {
        Map<Long, Long> plausiErrorNumbers = new HashMap<>();
        for (Object[] objs : errorNumbersList) {
            if (objs[0] instanceof BigDecimal && objs[1] instanceof BigDecimal) {
                plausiErrorNumbers.put(((BigDecimal) objs[0]).longValue(), ((BigDecimal) objs[1]).longValue());
            }
        }
        return plausiErrorNumbers;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<Long, Long> getNumberOfPlausiErrorsForCanton(final Long cantonId) {
        return (Map<Long, Long>) getHibernateTemplate().execute(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                // Select number of all errors per plausi
                String errorNumbersQueryString = "SELECT p.plausiId, count(*) " +
                        "FROM Sdl_PlausiErrors e " +
                        "LEFT OUTER JOIN Sdl_Plausis p on e.plausiId = p.plausiId " +
                        "LEFT OUTER JOIN Sdl_Schools s on e.schoolId = s.schoolId " +
                        "WHERE e.cantonId=:cantonId AND e.isConfirmed=0 AND p.objectLevel = :objectLevel " +
                        "AND (s.isToDelete = 0 OR s.isToDelete is null) AND (e.isToDelete = 0 OR e.isToDelete is null) " +
                        "GROUP BY p.plausiId ";

                org.hibernate.query.NativeQuery<Object[]> errorNumbersQuery = session.createNativeQuery(errorNumbersQueryString);
                errorNumbersQuery.setParameter("cantonId", cantonId);
                errorNumbersQuery.setParameter("objectLevel", CodegroupUtility.SDL_OBJECTTYPE_CANTON);

                List<Object[]> errorNumbersList = errorNumbersQuery.list();

                return buildErrorNumbersMap(errorNumbersList);
            }
        });
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<Long, Long> getNumberOfPlausiErrorsForDelivery(final Long deliveryId) {
        return (Map<Long, Long>) getHibernateTemplate().execute(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                String errorNumbersQueryString =
                        "SELECT p.plausiId, count(*) "
                                + "FROM Sdl_PlausiErrors e "
                                + "LEFT OUTER JOIN Sdl_Plausis p on e.plausiId = p.plausiId "
                                + "LEFT OUTER JOIN Sdl_Schools s on e.schoolId = s.schoolId "
                                + "WHERE e.deliveryId=:deliveryId AND e.isConfirmed=0 AND p.objectLevel >= :objectLevel"
                                + " AND (s.isToDelete = 0 OR s.isToDelete is null) AND (e.isToDelete = 0 OR e.isToDelete is null)"
                                + " GROUP BY p.plausiId";

                org.hibernate.query.NativeQuery errorNumbersQuery = session.createNativeQuery(errorNumbersQueryString);
                errorNumbersQuery.setParameter("deliveryId", deliveryId);
                errorNumbersQuery.setParameter("objectLevel", CodegroupUtility.SDL_OBJECTTYPE_DELIVERY);

                List<Object[]> errorNumbersList = errorNumbersQuery.list();

                return buildErrorNumbersMap(errorNumbersList);
            }
        });
    }

    /*
     * Mantis 1783: load confirmed errors to enable taking over confirmation info in amend/replace use case
     *
     * @see ch.bfs.meb.sdl.server.integration.repository.IPlausiErrorRepository#findConfirmedInternalErrors(java.lang.Long)
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<SdlPlausiError> findConfirmedInternalErrors(final Long deliveryId) {
        return (List<SdlPlausiError>) getHibernateTemplate().execute(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                // Load all confirmed internal errors for deliveryId
                String queryString = "from SdlPlausiError e where e.deliveryId=:deliveryId and e.isConfirmed = 1 and e.plausi.type=:type order by e.errorId asc";
                org.hibernate.query.Query<SdlPlausiError> query = session.createQuery(queryString);
                query.setParameter("deliveryId", deliveryId);
                query.setParameter("type", CodegroupUtility.MEB_PLAUSITYPE_INTERNAL);

                String additionalQueryString = "select s.idType as schoolIdType, s.id as schoolId, c.id as classId, l.idType as learnerIdType, l.id as learnerId "
                        + "FROM Sdl_PlausiErrors e "
                        + "left outer join Sdl_Plausis plausi on e.plausiId = plausi.plausiId "
                        + "left outer join Sdl_Schools s on e.schoolid = s.schoolid "
                        + "left outer join Sdl_Classes c on e.classid = c.classid "
                        + "left outer join Sdl_Learners l on e.learnerid = l.learnerid "
                        + "WHERE e.deliveryId=:deliveryId and e.isConfirmed = 1 and plausi.type=:type "
                        + "ORDER BY e.errorId asc ";
                org.hibernate.query.NativeQuery<Object[]> sqlQuery = session.createNativeQuery(additionalQueryString);
                sqlQuery.setParameter("deliveryId", deliveryId);
                sqlQuery.setParameter("type", CodegroupUtility.MEB_PLAUSITYPE_INTERNAL);

                List<SdlPlausiError> errorList = query.list();
                List<Object[]> additionalDataList = sqlQuery.list();

// Build complete error information for enabling logicalKey()-function on plausierror
                for (int i = 0; i < errorList.size(); i++) {
                    SdlPlausiError error = errorList.get(i);
                    Object[] objs = additionalDataList.get(i);
                    error.addSchoolInfo((String) objs[0], (String) objs[1]);
                    if (objs[2] != null) {
                        error.addClassInfo((String) objs[2]);
                        if (objs[3] != null) {
                            error.addLearnerInfo((String) objs[3], (String) objs[4], null);
                        }
                    }
                }
                return errorList;
            }
        });
    }

    private String getAdditionalQueryString(boolean includeBurSchoolLabel, String whereClause) {
        return "SELECT s.idType as sIdType, s.id as sId, c.id as cId, l.idType as lIdType, "//
                +   "l.id as lId, l.origDeliveryData, s.isToDelete, "//
                + (includeBurSchoolLabel ? "b.label" : "null as ignore" ) + " "//
                + "FROM Sdl_PlausiErrors e "//
                + "LEFT OUTER JOIN Sdl_Plausis p on e.plausiId = p.plausiId "//
                + "LEFT OUTER JOIN Sdl_Schools s on e.schoolid = s.schoolid "//
                + "LEFT OUTER JOIN Sdl_Classes c on e.classid = c.classid "//
                + "LEFT OUTER JOIN Sdl_Learners l on e.learnerid = l.learnerid "//
                + (includeBurSchoolLabel ?
                  "LEFT OUTER JOIN SCHOOLS b on  TO_CHAR(b.burnr) = s.id and s.idtype = 'CH.BUR' " : "") + " "//
                + whereClause + " "//
                + "ORDER BY e.schoolId desc, e.errorId asc ";
    }
}
