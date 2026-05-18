/*
 * MEB Portal
 * Bundesamt für Statistik
 *
 * adesso Schweiz AG
 * Copyright (c) 2009, 2010
 *
 * Projekt: sspserver
 *
 */
package ch.bfs.meb.ssp.server.integration.repository;

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

import ch.bfs.meb.ssp.server.business.plausi.PlausierrorBO;
import ch.bfs.meb.ssp.server.business.plausi.PlausireportFactory;
import ch.bfs.meb.ssp.server.integration.dto.SspPlausiError;
import ch.bfs.meb.util.CodegroupUtility;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PlausiErrorRepository extends HibernateDaoSupport implements IPlausiErrorRepository {
    private final static long IN_CLAUSE_LIMIT = 500;

    private List<SspPlausiError> buildErrorListFromSQLResults(
            List<SspPlausiError> errorList, List<Object[]> additionalDataList, List<Object[]> additionalSchoolDataList) {
        List<SspPlausiError> plausierrors = new ArrayList<>();
        // Build complete error information and remove the to be deleted errors
        for (int i = 0; i < errorList.size(); i++) {
            SspPlausiError error = errorList.get(i);
            Object[] additionalData = additionalDataList.get(i);
            addPersonInfo(error, additionalData);
            addActivityAndSchoolInfo(error, additionalData, additionalSchoolDataList);
            boolean isPersonToDelete = (additionalData[6] != null && additionalData[6] instanceof BigDecimal && ((BigDecimal) additionalData[6]).intValue() == 1);
            if (!isPersonToDelete && !error.getIsToDelete()) {
                plausierrors.add(error);
            }
        }
        return plausierrors;
    }

    private void addPersonInfo(SspPlausiError error, Object[] additionalData) {
        error.addPersonInfo((String) additionalData[0], (String) additionalData[1], (String) additionalData[2]);
    }

    private void addActivityAndSchoolInfo(SspPlausiError error, Object[] additionalData, List<Object[]> additionalSchoolDataList) {
        if (error.getActivityId() != null) {
            error.addActivityInfoWithLabel((String) additionalData[3], (String) additionalData[4],
                    ((additionalData[7] != null) ? additionalData[7].toString() : null),
                    ((additionalData[5] != null) ? additionalData[5].toString() : null));
        } else {
            addMultipleSchoolInfo(error, additionalSchoolDataList);
        }
    }

    private void addMultipleSchoolInfo(SspPlausiError error, List<Object[]> additionalSchoolDataList) {
        if (additionalSchoolDataList != null) {
            List<SspPlausiError.SchoolInfo> relevantSchoolDataList = new ArrayList<>();
            for (Object[] schoolData : additionalSchoolDataList) {
                Long personId;
                try {
                    personId = new Long(schoolData[0].toString());
                } catch (Exception e) {
                    throw new MebUncheckedException(String.format("invalid schoolData[0]=%s, errorId=%d, errormessage=%s",
                            schoolData[0] != null ? schoolData[0].toString() : "null", error.getErrorId(), e.getMessage()));
                }
                if (personId.equals(error.getPersonId())) {
                    relevantSchoolDataList.add(
                            new SspPlausiError.SchoolInfo(personId,
                                schoolData[1] != null ? schoolData[1].toString() : null,
                                schoolData[2] != null ? schoolData[2].toString() : null,
                                schoolData[3] != null ? schoolData[3].toString() : null));
                }
            }
            error.addMultipleSchoolInfo(relevantSchoolDataList);
        }
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.ssp.server.integration.repository.IPlausiErrorRepository#getPlausiErrorsForCanton(java.lang.Long)
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<SspPlausiError> getPlausiErrorsForCanton(final Long cantonId) {
        return (List<SspPlausiError>) getHibernateTemplate().execute(session -> {
            // Select all errors of level canton - Mantis 1646: Order by newest persons first!
            String errorQueryString = "SELECT e " +
                    "FROM SspPlausiError e " +
                    "WHERE e.cantonId = :cantonId and e.isConfirmed = 0 and e.plausi.objectLevel = :objectLevel " +
                    "ORDER BY e.personId desc, e.errorId asc";
            org.hibernate.query.Query errorQuery = session.createQuery(errorQueryString, SspPlausiError.class);
            errorQuery.setParameter("cantonId", cantonId);
            errorQuery.setParameter("objectLevel", CodegroupUtility.SSP_OBJECTTYPE_CANTON);
            errorQuery.setMaxResults(PlausireportFactory.MAX_ERRORS);

            String additionalSqlQueryString = getAdditionalQueryString("WHERE e.cantonId = :cantonId and e.isConfirmed = 0 and plausi.objectLevel = :objectLevel");
            org.hibernate.query.NativeQuery sqlQuery = session.createNativeQuery(additionalSqlQueryString);
            sqlQuery.setParameter("cantonId", cantonId);
            sqlQuery.setParameter("objectLevel", CodegroupUtility.SSP_OBJECTTYPE_CANTON);
            sqlQuery.setMaxResults(PlausireportFactory.MAX_ERRORS);

            List<SspPlausiError> errorList = errorQuery.list();
            List<Object[]> additionalDataList = sqlQuery.list();

            return buildErrorListFromSQLResults(errorList, additionalDataList, null);
        });
    }


    /* (non-Javadoc)
     * @see ch.bfs.meb.ssp.server.integration.repository.IPlausiErrorRepository#getPlausiErrorsForDelivery(java.lang.Long)
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<SspPlausiError> getPlausiErrorsForDelivery(final Long deliveryId) {
        return (List<SspPlausiError>) getHibernateTemplate().execute(session -> {
            // Select all errors of level delivery and below - Mantis 1646: Order by newest persons first!
            String errorQueryString = "SELECT e " +
                    "FROM SspPlausiError e " +
                    "WHERE e.deliveryId = :deliveryId AND e.isConfirmed = 0 AND e.plausi.objectLevel >= :objectLevel " +
                    "ORDER BY e.personId desc, e.errorId asc";
            Query<SspPlausiError> errorQuery = session.createQuery(errorQueryString, SspPlausiError.class);
            errorQuery.setParameter("deliveryId", deliveryId);
            errorQuery.setParameter("objectLevel", CodegroupUtility.SSP_OBJECTTYPE_DELIVERY);
            errorQuery.setMaxResults(PlausireportFactory.MAX_ERRORS);

            String additionalQueryString = getAdditionalQueryString(
                    "WHERE e.deliveryId = :deliveryId AND e.isConfirmed = 0 AND plausi.objectLevel >= :objectLevel");
            NativeQuery sqlQuery = session.createNativeQuery(additionalQueryString);
            sqlQuery.setParameter("deliveryId", deliveryId);
            sqlQuery.setParameter("objectLevel", CodegroupUtility.SSP_OBJECTTYPE_DELIVERY);
            sqlQuery.setMaxResults(PlausireportFactory.MAX_ERRORS);

            String additionalSchoolDataSqlQueryString = getAdditionalSchoolDataSqlQueryString();
            NativeQuery additionalSchoolDataSqlQuery = session.createNativeQuery(additionalSchoolDataSqlQueryString);
            additionalSchoolDataSqlQuery.setParameter("deliveryId", deliveryId);
            additionalSchoolDataSqlQuery.setMaxResults(PlausireportFactory.MAX_ERRORS);

            List<SspPlausiError> errorList = errorQuery.list();
            List<Object[]> additionalDataList = sqlQuery.list();
            List<Object[]> additionalSchoolDataList = additionalSchoolDataSqlQuery.list();

            if (errorList.size() != additionalDataList.size()) {
                throw new MebUncheckedException("inconsistent BUR data (deliveryId=" + deliveryId + ")");
            }

            return buildErrorListFromSQLResults(errorList, additionalDataList, additionalSchoolDataList);
        });
    }


    /* (non-Javadoc)
     * @see ch.bfs.meb.ssp.server.integration.repository.IPlausiErrorRepository#getPlausiErrorsForDelivery(java.lang.Long)
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<SspPlausiError> getAllPlausiErrorsForDelivery(final Long deliveryId) {
        return (List<SspPlausiError>) getHibernateTemplate().execute(session -> {
            // Select all errors of level delivery and below - Mantis 1646: Order by newest persons first!
            String errorQueryString = "SELECT e FROM SspPlausiError e " +
                    "WHERE e.deliveryId = :deliveryId AND e.plausi.objectLevel >= :objectLevel " +
                    "ORDER BY e.personId desc, e.errorId asc";
            Query<SspPlausiError> errorQuery = session.createQuery(errorQueryString, SspPlausiError.class);
            errorQuery.setParameter("deliveryId", deliveryId);
            errorQuery.setParameter("objectLevel", CodegroupUtility.SSP_OBJECTTYPE_DELIVERY);
            errorQuery.setMaxResults(PlausireportFactory.MAX_ERRORS);

            String additionalQueryString = getAdditionalQueryString("WHERE e.deliveryId = :deliveryId AND plausi.objectLevel >= :objectLevel");
            NativeQuery sqlQuery = session.createNativeQuery(additionalQueryString);
            sqlQuery.setParameter("deliveryId", deliveryId);
            sqlQuery.setParameter("objectLevel", CodegroupUtility.SSP_OBJECTTYPE_DELIVERY);
            sqlQuery.setMaxResults(PlausireportFactory.MAX_ERRORS);

            String additionalSchoolDataSqlQueryString = getAdditionalSchoolDataSqlQueryString();
            NativeQuery additionalSchoolDataSqlQuery = session.createNativeQuery(additionalSchoolDataSqlQueryString);
            additionalSchoolDataSqlQuery.setParameter("deliveryId", deliveryId);
            additionalSchoolDataSqlQuery.setMaxResults(PlausireportFactory.MAX_ERRORS);

            List<SspPlausiError> errorList = errorQuery.list();
            List<Object[]> additionalDataList = sqlQuery.list();
            List<Object[]> additionalSchoolDataList = additionalSchoolDataSqlQuery.list();

            if (errorList.size() != additionalDataList.size()) {
                throw new MebUncheckedException("inconsistent BUR data (deliveryId=" + deliveryId + ")");
            }

            return buildErrorListFromSQLResults(errorList, additionalDataList, additionalSchoolDataList);
        });
    }


    /* (non-Javadoc)
     * @see ch.bfs.meb.ssp.server.integration.repository.IPlausiErrorRepository#findForCanton(java.lang.Long, java.lang.Long)
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<SspPlausiError> findForCanton(final Long cantonId, final Long plausiId, final Boolean toDelete) {
        return (List<SspPlausiError>) getHibernateTemplate().execute(session -> {
            Query<SspPlausiError> query = session.createQuery(
                    "FROM SspPlausiError WHERE cantonId = :cantonId AND plausiId = :plausiId AND isToDelete = :toDelete",
                    SspPlausiError.class
            );
            query.setParameter("cantonId", cantonId);
            query.setParameter("plausiId", plausiId);
            query.setParameter("toDelete", toDelete);
            return query.list();
        });
    }


    /* (non-Javadoc)
     * @see ch.bfs.meb.ssp.server.integration.repository.IPlausiErrorRepository#findForDelivery(java.lang.Long, java.lang.Long)
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<SspPlausiError> findExternalErrorsForDelivery(final Long deliveryId, final Boolean toDelete) {
        return (List<SspPlausiError>) getHibernateTemplate().execute(session -> {
            // Load all errors of external plausis for deliveryId
            String queryString = "FROM SspPlausiError e WHERE e.deliveryId = :deliveryId AND e.isToDelete = :toDelete AND e.plausi.type = :plausiType";
            Query<SspPlausiError> query = session.createQuery(queryString, SspPlausiError.class);
            query.setParameter("deliveryId", deliveryId);
            query.setParameter("toDelete", toDelete);
            query.setParameter("plausiType", CodegroupUtility.MEB_PLAUSITYPE_EXTERNAL);
            return query.list();
        });
    }


    /* (non-Javadoc)
     * @see ch.bfs.meb.ssp.server.integration.repository.IPlausiErrorRepository#findForDelivery(java.lang.Long, java.lang.Long)
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<SspPlausiError> findForDelivery(final Long deliveryId, final Long plausiId, final Boolean toDelete) {
        return (List<SspPlausiError>) getHibernateTemplate().execute(session -> {
            // TODO lsc: load all external errors of plausis with the same business id (replace name_de by business id)
            String queryString = "FROM SspPlausiError e " +
                    "WHERE e.deliveryId = :deliveryId " +
                    "AND e.isToDelete = :toDelete " +
                    "AND e.plausi in (select p.plausiId from SspPlausi p where p.type = :plausiType)";
            Query<SspPlausiError> query = session.createQuery(queryString, SspPlausiError.class);
            query.setParameter("deliveryId", deliveryId);
            query.setParameter("toDelete", toDelete);
            query.setParameter("plausiType", CodegroupUtility.MEB_PLAUSITYPE_EXTERNAL);
            return query.list();
        });
    }


    @SuppressWarnings("unchecked")
    @Override
    public List<SspPlausiError> findErrorsForDelivery(final Long deliveryId, final Long plausiType, final Boolean toDelete) {
        return (List<SspPlausiError>) getHibernateTemplate().execute(session -> {
            // Load all errors of external plausis for deliveryId
            String queryString = "FROM SspPlausiError e WHERE e.deliveryId = :deliveryId AND e.isToDelete = :toDelete AND e.plausi.type = :plausiType";
            Query<SspPlausiError> query = session.createQuery(queryString, SspPlausiError.class);
            query.setParameter("deliveryId", deliveryId);
            query.setParameter("toDelete", toDelete);
            query.setParameter("plausiType", plausiType);
            return query.list();
        });
    }


    /* (non-Javadoc)
     * @see ch.bfs.meb.ssp.server.integration.repository.IPlausiErrorRepository#findForPerson(java.lang.Long, java.lang.Long)
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<SspPlausiError> findForPerson(final Long personId, final Long plausiId) {
        return (List<SspPlausiError>) getHibernateTemplate().execute(session -> {
            Query<SspPlausiError> query = session.createQuery(
                    "FROM SspPlausiError WHERE personId = :personId AND plausiId = :plausiId",
                    SspPlausiError.class
            );
            query.setParameter("personId", personId);
            query.setParameter("plausiId", plausiId);
            return query.list();
        });
    }


    /* (non-Javadoc)
     * @see ch.bfs.meb.ssp.server.integration.repository.IPlausiErrorRepository#findForAcitivity(java.lang.Long, java.lang.Long)
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<SspPlausiError> findForActivity(final Long activityId, final Long plausiId) {
        return (List<SspPlausiError>) getHibernateTemplate().execute(session -> {
            Query<SspPlausiError> query = session.createQuery(
                    "FROM SspPlausiError WHERE activityId = :activityId AND plausiId = :plausiId",
                    SspPlausiError.class
            );
            query.setParameter("activityId", activityId);
            query.setParameter("plausiId", plausiId);
            return query.list();
        });
    }


    /* (non-Javadoc)
     * @see ch.bfs.meb.ssp.server.integration.repository.IPlausiErrorRepository#getPlausierrorById(java.lang.Long)
     */
    @Override
    public SspPlausiError getPlausierrorById(Long plausierrorId) {
        return getHibernateTemplate().get(SspPlausiError.class, plausierrorId);
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.ssp.server.integration.repository.IPlausiErrorRepository#getPlausiErrorsByPlausiId(java.lang.Long)
     */
    @Override
    public Long getNofPlausiErrorsByPlausiId(final Long plausiId) {
        return (Long) getHibernateTemplate().execute(session -> {
            Query<Long> query = session.createQuery(
                    "SELECT count(*) FROM SspPlausiError e WHERE e.plausi.plausiId = :plausiId",
                    Long.class
            );
            query.setParameter("plausiId", plausiId);
            return query.uniqueResult();
        });
    }


    /* (non-Javadoc)
     * @see ch.bfs.meb.ssp.server.integration.repository.IPlausiErrorRepository#isDeliveryWithUnconfirmedErrors(java.lang.Long)
     */
    @Override
    public boolean isDeliveryWithUnconfirmedErrors(final Long deliveryId) {
        return (Boolean) getHibernateTemplate().execute(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                NativeQuery query = session.createNativeQuery(
                        "SELECT * FROM dual WHERE exists (SELECT * FROM ssp_plausierrors e, ssp_persons p WHERE e.deliveryId=:deliveryId " +
                                "AND p.deliveryId=:deliveryId AND e.ISCONFIRMED = 0 AND ((e.personId is null AND e.isToDelete=0) " +
                                "OR (e.personId=p.personId AND p.isToDelete=0)))");
                query.setParameter("deliveryId", deliveryId);
                return query.list().size() > 0;
            }
        });
    }



    /* (non-Javadoc)
     * @see ch.bfs.meb.ssp.server.integration.repository.IPlausiErrorRepository#insertPlausiError(ch.bfs.meb.ssp.server.integration.dto.SspPlausiError)
     */
    @Override
    public Long insertPlausiError(SspPlausiError plausiError) {
        Long plausiErrorId = (Long) getHibernateTemplate().save(plausiError);
        getHibernateTemplate().flush();
        return plausiErrorId;
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.ssp.server.integration.repository.IPlausiErrorRepository#updatePlausiError(ch.bfs.meb.ssp.server.integration.dto.SspPlausiError)
     */
    @Override
    public SspPlausiError updatePlausiError(SspPlausiError plausiError) {
        if (plausiError.getPlausi() == null) {
            // update from client
            SspPlausiError save = (SspPlausiError) getHibernateTemplate().load(SspPlausiError.class, plausiError.getErrorId());
            plausiError.resetPlausi(save.getPlausi());
        }
        plausiError = (SspPlausiError) getHibernateTemplate().merge(plausiError);
        getHibernateTemplate().flush();
        return plausiError;
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sspl.server.integration.repository.IPlausiErrorRepository#deletePlausiError(ch.bfs.meb.ssp.server.integration.dto.SspPlausiError)
     */
    @Override
    public void deletePlausiError(final SspPlausiError plausiError) {
        getHibernateTemplate().execute(session -> {
            // Delete plausi error
            Query<?> query = session.createQuery("DELETE FROM SspPlausiError e WHERE e.errorId = :errorId");
            query.setParameter("errorId", plausiError.getErrorId());
            return query.executeUpdate();
        });
        // getHibernateTemplate().delete(plausiError); doesn't work -> Mantis 1405: a different object with the same identifier value was already associated with the session
        getHibernateTemplate().flush();
    }


    /* (non-Javadoc)
     * @see ch.bfs.meb.ssp.server.integration.repository.IPlausiErrorRepository#deletePlausiErrors(java.util.List)
     */
    @Override
    public void deletePlausiErrors(final List<PlausierrorBO> plausiErrorBos) {
        if (plausiErrorBos.isEmpty()) {
            return;
        }
        List<Long> idList = new ArrayList<>();
        long counter = 0;
        for (PlausierrorBO plausierrorBO : plausiErrorBos) {
            counter++;
            idList.add(plausierrorBO.getThisPlausierror().getErrorId());
            if (counter % IN_CLAUSE_LIMIT == 0) {
                getHibernateTemplate().bulkUpdate("DELETE FROM SspPlausiError WHERE errorId in (" + idList + ")");
                idList.clear();
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
        return (Map<Long, Long>) getHibernateTemplate().execute(session -> {
            // Select number of all errors per plausi
            String errorNumbersQueryString = "SELECT p.plausiId, count(*) " +
                    "FROM Ssp_PlausiErrors e " +
                    "LEFT OUTER JOIN Ssp_Plausis p on e.plausiId = p.plausiId " +
                    "LEFT OUTER JOIN Ssp_Persons pers on e.personId = pers.personId " +
                    "WHERE e.cantonId = :cantonId AND e.isConfirmed = 0 AND p.objectLevel = :objectLevel " +
                    "AND (pers.isToDelete = 0 OR pers.isToDelete is null) " +
                    "AND (e.isToDelete = 0 OR e.isToDelete is null) " +
                    "GROUP BY p.plausiId";
            NativeQuery errorNumbersQuery = session.createNativeQuery(errorNumbersQueryString);
            errorNumbersQuery.setParameter("cantonId", cantonId);
            errorNumbersQuery.setParameter("objectLevel", CodegroupUtility.SSP_OBJECTTYPE_CANTON);

            List<Object[]> errorNumbersList = errorNumbersQuery.list();
            return buildErrorNumbersMap(errorNumbersList);
        });
    }


    @SuppressWarnings("unchecked")
    @Override
    public Map<Long, Long> getNumberOfPlausiErrorsForDelivery(final Long deliveryId) {
        return (Map<Long, Long>) getHibernateTemplate().execute(session -> {
            // Select number of all errors per plausi
            String errorNumbersQueryString = "SELECT p.plausiId, count(*) " +
                    "FROM Ssp_PlausiErrors e " +
                    "LEFT OUTER JOIN Ssp_Plausis p on e.plausiId = p.plausiId " +
                    "LEFT OUTER JOIN Ssp_Persons pers on e.personId = pers.personId " +
                    "WHERE e.deliveryId = :deliveryId AND e.isConfirmed = 0 AND p.objectLevel >= :objectLevel " +
                    "AND (pers.isToDelete = 0 OR pers.isToDelete is null) " +
                    "AND (e.isToDelete = 0 OR e.isToDelete is null) " +
                    "GROUP BY p.plausiId";
            NativeQuery errorNumbersQuery = session.createNativeQuery(errorNumbersQueryString);
            errorNumbersQuery.setParameter("deliveryId", deliveryId);
            errorNumbersQuery.setParameter("objectLevel", CodegroupUtility.SSP_OBJECTTYPE_DELIVERY);

            List<Object[]> errorNumbersList = errorNumbersQuery.list();
            return buildErrorNumbersMap(errorNumbersList);
        });
    }


    /*
     * Mantis 1783: load confirmed errors to enable taking over confirmation info in amend/replace use case
     * @see ch.bfs.meb.ssp.server.integration.repository.IPlausiErrorRepository#findConfirmedInternalErrors(java.lang.Long)
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<SspPlausiError> findConfirmedInternalErrors(final Long deliveryId) {
        Session session = getHibernateTemplate().getSessionFactory().getCurrentSession();
        // Load all confirmed internal errors  for deliveryId
        //        String queryString = "FROM SspPlausiError e "//
        //                + "WHERE e.deliveryId = :deliveryId AND e.isConfirmed = 1 AND e.plausi.type = " + CodegroupUtility.MEB_PLAUSITYPE_INTERNAL + " "//
        //                + "ORDER BY e.errorId asc ";
        String queryString = "SELECT e.* "//
                + "FROM Ssp_PlausiErrors e "//
                + "INNER JOIN Ssp_Plausis plausi ON e.PlausiId = plausi.plausiId "//
                + "WHERE e.deliveryId = :deliveryId AND e.isConfirmed = 1 AND plausi.type = " + CodegroupUtility.MEB_PLAUSITYPE_INTERNAL + " "//
                + "ORDER BY e.errorId asc ";
        SQLQuery sqlQuery = session.createSQLQuery(queryString);
        sqlQuery.setLong("deliveryId", deliveryId);
        sqlQuery.addEntity(SspPlausiError.class); // sqlQuery.setResultTransformer(Transformers.aliasToBean(SspPlausiError.class));
        log.debug("Execute SQL-Query with deliveryId=" + deliveryId + queryString);

        List plausiErrorRecords = sqlQuery.list();
        log.debug(plausiErrorRecords.size() + " PlausiError records found.");
        List<SspPlausiError> plausiErrors = (List<SspPlausiError>) plausiErrorRecords;

        String additionalSqlQueryString = "SELECT p.idType, p.id as pid, a.schoolIdType, a.schoolId, a.id as aid "//
                + "FROM Ssp_PlausiErrors e "//
                + "LEFT OUTER JOIN Ssp_Plausis plausi ON e.plausiId = plausi.plausiId "//
                + "LEFT OUTER JOIN Ssp_Persons p ON e.personId = p.personId "//
                + "LEFT OUTER JOIN Ssp_Activities a ON e.activityId = a.activityId "//
                + "WHERE e.deliveryId = :deliveryId AND e.isConfirmed = 1 AND plausi.type = " + CodegroupUtility.MEB_PLAUSITYPE_INTERNAL + " "//
                + "ORDER BY e.errorId ASC ";
        SQLQuery additionalSqlQuery = session.createSQLQuery(additionalSqlQueryString);
        additionalSqlQuery.setLong("deliveryId", deliveryId);

        List<Object[]> additionalDataList = additionalSqlQuery.list();

        // Build complete error information for enabling logicalKey()-function on plausierror
        for (int i = 0; i < plausiErrors.size(); i++) {
            SspPlausiError error = plausiErrors.get(i);
            Object[] additionalData = additionalDataList.get(i);
            error.addPersonInfo((String) additionalData[0], (String) additionalData[1], null);
            error.addActivityInfo((String) additionalData[2], (String) additionalData[3], ((additionalData[4] != null) ? additionalData[4].toString() : null));
        }
        return plausiErrors;
    }

    private String getAdditionalQueryString(String whereClause) {
        return "SELECT p.idType, p.id as pid, p.origDeliveryData, "//
                + "a.schoolIdType, a.schoolId, a.id as aid, p.isToDelete, b.label "//
                + "FROM Ssp_PlausiErrors e "//
                + "LEFT OUTER JOIN Ssp_Plausis plausi on e.plausiId = plausi.plausiId "//
                + "LEFT OUTER JOIN Ssp_Persons p on e.personId = p.personId "//
                + "LEFT OUTER JOIN Ssp_Activities a on e.activityId = a.activityId "//
                + "LEFT OUTER JOIN SCHOOLS b ON TO_CHAR(b.burnr) = a.schoolid AND a.schoolIdType = 'CH.BUR' "//
                + whereClause + " "//
                + "ORDER BY e.personId desc, e.errorId asc ";
    }

    private String getAdditionalSchoolDataSqlQueryString() {
        return "SELECT DISTINCT e.personId, schoolIdType, a.schoolId, label "//
                + "FROM Ssp_Activities a "//
                + "JOIN Ssp_PlausiErrors e ON e.personId = a.personId "//
                + "LEFT OUTER JOIN SCHOOLS b ON TO_CHAR(b.burnr) = a.schoolid "//
                + "WHERE deliveryId=:deliveryId and e.activityId is null "//
                + "ORDER BY personId, schoolId";
    }

}
