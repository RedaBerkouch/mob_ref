package ch.bfs.meb.sba.server.integration.repository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.bfs.meb.exception.MebUncheckedException;
import org.hibernate.HibernateException;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.query.NativeQuery;
import org.hibernate.query.Query;
import org.springframework.orm.hibernate5.HibernateCallback;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;

import ch.bfs.meb.sba.server.business.plausi.PlausierrorBO;
import ch.bfs.meb.sba.server.business.plausi.PlausireportFactory;
import ch.bfs.meb.sba.server.integration.dto.SbaPlausiError;
import ch.bfs.meb.util.CodegroupUtility;
import lombok.extern.slf4j.Slf4j;

@Slf4j
/* Repository for SbaDeliveries. */
public class PlausiErrorRepository extends HibernateDaoSupport implements IPlausiErrorRepository {
    private final static long IN_CLAUSE_LIMIT = 500;

    private List<SbaPlausiError> buildErrorListFromSQLResults(
            List<SbaPlausiError> errorList, List<Object[]> additionalDataList, List<Object[]> additionalSchoolDataList) {
        List<SbaPlausiError> plausierrors = new ArrayList<>();
        // Build complete error information and remove the to be deleted errors
        for (int i = 0; i < errorList.size(); i++) {
            SbaPlausiError error = errorList.get(i);
            Object[] additionalData = additionalDataList.get(i);
            addPersonInfo(error, additionalData);
            addQualificationAndSchoolInfo(error, additionalData, additionalSchoolDataList);
            //  Person.isToDelete != null?
            boolean isPersonToDelete = (additionalData[6] != null && additionalData[6] instanceof BigDecimal && ((BigDecimal) additionalData[6]).intValue() == 1);
            if (!isPersonToDelete && !error.getIsToDelete()) {
                plausierrors.add(error);
            }
        }
        return plausierrors;
    }

    private void addPersonInfo(SbaPlausiError error, Object[] additionalData) {
        error.addPersonInfo((String) additionalData[0], (String) additionalData[1], (String) additionalData[2]);
    }

    private void addQualificationAndSchoolInfo(SbaPlausiError error, Object[] additionalData, List<Object[]> additionalSchoolDataList) {
        if (error.getQualificationId() != null) {
            error.addQualificationInfoWithLabel((String) additionalData[3], (String) additionalData[4],
                    ((additionalData[7] != null) ? additionalData[7].toString() : null),
                    ((additionalData[5] != null) ? additionalData[5].toString() : null));
        } else {
            addMultipleSchoolInfo(error, additionalSchoolDataList);
        }
    }

    private void addMultipleSchoolInfo(SbaPlausiError error, List<Object[]> additionalSchoolDataList) {
        if (additionalSchoolDataList != null) {
            List<SbaPlausiError.SchoolInfo> relevantSchoolDataList = new ArrayList<>();
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
                            new SbaPlausiError.SchoolInfo(personId,
                                    schoolData[1] != null ? schoolData[1].toString() : null,
                                    schoolData[2] != null ? schoolData[2].toString() : null,
                                    schoolData[3] != null ? schoolData[3].toString() : null));
                }
            }
            error.addMultipleSchoolInfo(relevantSchoolDataList);
        }
    }

    /* @see ch.bfs.meb.sba.server.integration.repository.IPlausiErrorRepository#getPlausiErrorsForCanton(java.lang.Long) */
    @SuppressWarnings("unchecked")
    @Override
    public List<SbaPlausiError> getPlausiErrorsForCanton(final Long cantonId) {
        return (List<SbaPlausiError>) getHibernateTemplate().execute(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                String errorQueryString = "SELECT e FROM SbaPlausiError e "//
                        + "WHERE e.cantonId = :cantonId AND e.isConfirmed = 0 AND e.plausi.objectLevel = :objectLevel "//
                        + "ORDER BY e.personId desc, e.errorId asc";
                org.hibernate.query.Query errorQuery = session.createQuery(errorQueryString);
                errorQuery.setParameter("cantonId", cantonId);
                errorQuery.setParameter("objectLevel", CodegroupUtility.SBA_OBJECTTYPE_CANTON);
                errorQuery.setMaxResults(PlausireportFactory.MAX_ERRORS);
                String additionalSqlQueryString = getAdditionalQueryString("WHERE e.cantonId = :cantonId and e.isConfirmed = 0 and plausi.objectLevel = :objectLevel");
                org.hibernate.query.NativeQuery sqlQuery = session.createNativeQuery(additionalSqlQueryString);
                sqlQuery.setParameter("cantonId", cantonId);
                sqlQuery.setParameter("objectLevel", CodegroupUtility.SBA_OBJECTTYPE_CANTON);
                sqlQuery.setMaxResults(PlausireportFactory.MAX_ERRORS);
                List<SbaPlausiError> errorList = (List<SbaPlausiError>) errorQuery.list();
                List<Object[]> additionalDataList = sqlQuery.list();
                return buildErrorListFromSQLResults(errorList, additionalDataList, null);
            }
        });
    }

    /* @see ch.bfs.meb.sba.server.integration.repository.IPlausiErrorRepository#getPlausiErrorsForDelivery(java.lang.Long) */
    @SuppressWarnings("unchecked")
    @Override
    public List<SbaPlausiError> getPlausiErrorsForDelivery(final Long deliveryId) {
        return (List<SbaPlausiError>) getHibernateTemplate().execute(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                // Select all errors of level delivery and below - Mantis 1646: Order by newest persons first!
                String errorQueryString = "SELECT e FROM SbaPlausiError e "//
                        + "WHERE e.deliveryId=:deliveryId AND e.isConfirmed = 0 AND e.plausi.objectLevel >= :objectLevel "//
                        + "ORDER BY e.personId desc, e.errorId asc ";
                // change Query to org.hibernate.query.Query
                org.hibernate.query.Query<SbaPlausiError> errorQuery = session.createQuery(errorQueryString, SbaPlausiError.class);
                errorQuery.setParameter("deliveryId", deliveryId);
                errorQuery.setParameter("objectLevel", CodegroupUtility.SBA_OBJECTTYPE_DELIVERY);
                errorQuery.setMaxResults(PlausireportFactory.MAX_ERRORS);
                String additionalQueryString = getAdditionalQueryString(
                        "WHERE e.deliveryId=:deliveryId AND e.isConfirmed = 0 AND plausi.objectLevel >= :objectLevel ");
                NativeQuery sqlQuery = session.createNativeQuery(additionalQueryString); // SQLQuery is now NativeQuery
                sqlQuery.setParameter("deliveryId", deliveryId);
                sqlQuery.setParameter("objectLevel", CodegroupUtility.SBA_OBJECTTYPE_DELIVERY);
                sqlQuery.setMaxResults(PlausireportFactory.MAX_ERRORS);
                String additionalSchoolDataSqlQueryString = getAdditionalSchoolDataSqlQueryString();
                NativeQuery additionalSchoolDataSqlQuery = session.createNativeQuery(additionalSchoolDataSqlQueryString); // SQLQuery is now NativeQuery
                additionalSchoolDataSqlQuery.setParameter("deliveryId", deliveryId);
                additionalSchoolDataSqlQuery.setMaxResults(PlausireportFactory.MAX_ERRORS);
                List<SbaPlausiError> errorList = errorQuery.list();
                List<Object[]> additionalDataList = sqlQuery.list();
                List<Object[]> additionalSchoolDataList = additionalSchoolDataSqlQuery.list();
                if (errorList.size() != additionalDataList.size()) {
                    throw new MebUncheckedException("inconsistent BUR data (deliveryId=" + deliveryId + ")");
                }
                return buildErrorListFromSQLResults(errorList, additionalDataList, additionalSchoolDataList);
            }
        });
    }

    /* @see ch.bfs.meb.sba.server.integration.repository.IPlausiErrorRepository#getPlausiErrorsForDelivery(java.lang.Long) */
    @SuppressWarnings("unchecked")
    @Override
    public List<SbaPlausiError> getAllPlausiErrorsForDelivery(final Long deliveryId) {
        return (List<SbaPlausiError>) getHibernateTemplate().execute(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                String errorQueryString = "SELECT e from SbaPlausiError e "//
                        + "WHERE e.deliveryId= :deliveryId AND e.plausi.objectLevel >= :level "//
                        + "ORDER BY e.personId desc, e.errorId asc ";
                org.hibernate.query.Query errorQuery = session.createQuery(errorQueryString);
                errorQuery.setParameter("deliveryId", deliveryId);
                errorQuery.setParameter("level", CodegroupUtility.SBA_OBJECTTYPE_DELIVERY);
                errorQuery.setMaxResults(PlausireportFactory.MAX_ERRORS);

                String additionalQueryString = getAdditionalQueryString("WHERE e.deliveryId= :deliveryId AND plausi.objectLevel >= :level ");
                org.hibernate.query.NativeQuery sqlQuery = session.createNativeQuery(additionalQueryString);
                sqlQuery.setParameter("deliveryId", deliveryId);
                sqlQuery.setParameter("level", CodegroupUtility.SBA_OBJECTTYPE_DELIVERY);
                sqlQuery.setMaxResults(PlausireportFactory.MAX_ERRORS);

                String additionalSchoolDataSqlQueryString = getAdditionalSchoolDataSqlQueryString();
                org.hibernate.query.NativeQuery additionalSchoolDataSqlQuery = session.createNativeQuery(additionalSchoolDataSqlQueryString);
                additionalSchoolDataSqlQuery.setParameter("deliveryId", deliveryId);
                additionalSchoolDataSqlQuery.setMaxResults(PlausireportFactory.MAX_ERRORS);

                List<SbaPlausiError> errorList = (List<SbaPlausiError>) errorQuery.list();
                List<Object[]> additionalDataList = sqlQuery.list();
                List<Object[]> additionalSchoolDataList = additionalSchoolDataSqlQuery.list();

                if (errorList.size() != additionalDataList.size()) {
                    throw new MebUncheckedException("inconsistent BUR data (deliveryId=" + deliveryId + ")");
                }
                return buildErrorListFromSQLResults(errorList, additionalDataList, additionalSchoolDataList);
            }
        });
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sba.server.integration.repository.IPlausiErrorRepository#findForCanton(java.lang.Long, java.lang.Long)
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<SbaPlausiError> findForCanton(final Long cantonId, final Long plausiId, final Boolean toDelete) {
        return (List<SbaPlausiError>) getHibernateTemplate().execute(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                org.hibernate.query.Query<SbaPlausiError> query = session.createQuery("FROM SbaPlausiError WHERE cantonId=:cantonId AND plausiId=:plausiId AND isToDelete=:toDelete", SbaPlausiError.class);
                query.setParameter("cantonId", cantonId);
                query.setParameter("plausiId", plausiId);
                query.setParameter("toDelete", toDelete);
                return query.list();
            }
        });
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sba.server.integration.repository.IPlausiErrorRepository#findForDelivery(java.lang.Long, java.lang.Long)
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<SbaPlausiError> findExternalErrorsForDelivery(final Long deliveryId, final Boolean toDelete) {
        return (List<SbaPlausiError>) getHibernateTemplate().execute(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                // Load all errors of external plausis for deliveryId
                String queryString = "FROM SbaPlausiError e WHERE e.deliveryId=:delId AND e.isToDelete=:toDel AND e.plausi.type=:type ";
                org.hibernate.query.Query<SbaPlausiError> query = session.createQuery(queryString, SbaPlausiError.class); // note the change here
                query.setParameter("delId", deliveryId);
                query.setParameter("toDel", toDelete);
                query.setParameter("type", CodegroupUtility.MEB_PLAUSITYPE_EXTERNAL);
                return query.list();
            }
        });
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sba.server.integration.repository.IPlausiErrorRepository#findForDelivery(java.lang.Long, java.lang.Long)
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<SbaPlausiError> findForDelivery(final Long deliveryId, final Long plausiId, final Boolean toDelete) {
        return (List<SbaPlausiError>) getHibernateTemplate().execute(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                String queryString = "FROM SbaPlausiError e "//
                        + "WHERE e.deliveryId=:deliveryId AND e.isToDelete=:isToDelete AND e.plausi in " + "(SELECT p.plausiId FROM SbaPlausi p WHERE p.type=:type)";
                org.hibernate.query.Query<SbaPlausiError> query = session.createQuery(queryString, SbaPlausiError.class);
                query.setParameter("deliveryId", deliveryId);
                query.setParameter("isToDelete", toDelete);
                query.setParameter("type", CodegroupUtility.MEB_PLAUSITYPE_EXTERNAL);
                return query.list();
            }
        });
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sba.server.integration.repository.IPlausiErrorRepository#findForPerson(java.lang.Long, java.lang.Long)
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<SbaPlausiError> findForPerson(final Long personId, final Long plausiId) {
        return (List<SbaPlausiError>) getHibernateTemplate().execute(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                org.hibernate.query.Query<SbaPlausiError> query = session.createQuery("FROM SbaPlausiError WHERE personId=:personId AND plausiId=:plausiId", SbaPlausiError.class);
                query.setParameter("personId", personId);
                query.setParameter("plausiId", plausiId);
                return query.list();
            }
        });
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sba.server.integration.repository.IPlausiErrorRepository#findForQualification(java.lang.Long, java.lang.Long)
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<SbaPlausiError> findForQualification(final Long qualificationId, final Long plausiId) {
        return (List<SbaPlausiError>) getHibernateTemplate().execute(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                org.hibernate.query.Query<SbaPlausiError> query = session.createQuery("FROM SbaPlausiError WHERE qualificationId=:qualificationId AND plausiId=:plausiId", SbaPlausiError.class);
                query.setParameter("qualificationId", qualificationId);
                query.setParameter("plausiId", plausiId);
                return query.list();
            }
        });
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sba.server.integration.repository.IPlausiErrorRepository#getPlausierrorById(java.lang.Long)
     */
    @Override
    public SbaPlausiError getPlausierrorById(Long plausierrorId) {
        return getHibernateTemplate().get(SbaPlausiError.class, plausierrorId);
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sba.server.integration.repository.IPlausiErrorRepository#getPlausiErrorsByPlausiId(java.lang.Long)
     */
    @Override
    public Long getNofPlausiErrorsByPlausiId(final Long plausiId) {
        return (Long) getHibernateTemplate().execute(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                org.hibernate.query.Query<Long> query = session.createQuery("SELECT count(*) FROM SbaPlausiError e WHERE e.plausi.plausiId=:plausiId", Long.class);
                query.setParameter("plausiId", plausiId);
                return (Long)query.uniqueResult();
            }
        });
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sba.server.integration.repository.IPlausiErrorRepository#isDeliveryWithUnconfirmedErrors(java.lang.Long)
     */
    @Override
    public boolean isDeliveryWithUnconfirmedErrors(final Long deliveryId) {
        return (Boolean) getHibernateTemplate().execute(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                NativeQuery query = session.createNativeQuery(
                        "SELECT * FROM dual WHERE exists (SELECT * FROM sba_plausierrors e, sba_persons p WHERE e.deliveryId=:deliveryId AND p.deliveryId=:deliveryId AND e.ISCONFIRMED = 0 AND ((e.personId is null AND e.isToDelete=0) OR (e.personId=p.personId AND p.isToDelete=0)))");
                query.setParameter("deliveryId", deliveryId);
                return query.list().size() > 0;
            }
        });
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sba.server.integration.repository.IPlausiErrorRepository#insertPlausiError(ch.bfs.meb.sba.server.integration.dto.SbaPlausiError)
     */
    @Override
    public Long insertPlausiError(SbaPlausiError plausiError) {
        Long plausiErrorId = (Long) getHibernateTemplate().save(plausiError);
        getHibernateTemplate().flush();
        return plausiErrorId;
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sba.server.integration.repository.IPlausiErrorRepository#updatePlausiError(ch.bfs.meb.sba.server.integration.dto.SbaPlausiError)
     */
    @Override
    public SbaPlausiError updatePlausiError(SbaPlausiError plausiError) {
        if (plausiError.getPlausi() == null) {
            // update from client
            SbaPlausiError save = getHibernateTemplate().load(SbaPlausiError.class, plausiError.getErrorId());
            plausiError.resetPlausi(save.getPlausi());
        }
        plausiError = getHibernateTemplate().merge(plausiError);
        getHibernateTemplate().flush();
        return plausiError;
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sbal.server.integration.repository.IPlausiErrorRepository#deletePlausiError(ch.bfs.meb.sba.server.integration.dto.SbaPlausiError)
     */
    @Override
    public void deletePlausiError(final SbaPlausiError plausiError) {
        getHibernateTemplate().execute(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                // Delete plausi error
                Query<Long> query = session.createQuery("DELETE FROM SbaPlausiError e WHERE e.errorId = :errorId");
                query.setParameter("errorId", plausiError.getErrorId());
                return query.executeUpdate();
            }
        });
        // getHibernateTemplate().delete (plausiError); doesn't work -> Mantis 1405: a different object with the same identifier value was already associated with the session
        getHibernateTemplate().flush();
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sba.server.integration.repository.IPlausiErrorRepository#deletePlausiErrors(java.util.List)
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
                getHibernateTemplate().bulkUpdate("DELETE FROM SbaPlausiError WHERE errorId in (" + idList + ")");
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
        return (Map<Long, Long>) getHibernateTemplate().execute(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                String errorNumbersQueryString =
                        "SELECT p.plausiId, count(*) " +
                                "FROM Sba_PlausiErrors e " +
                                "LEFT OUTER JOIN Sba_Plausis p on e.plausiId = p.plausiId " +
                                "LEFT OUTER JOIN Sba_Persons pers on e.personId = pers.personId " +
                                "WHERE e.cantonId=:cantonId AND e.isConfirmed=0 AND p.objectLevel = :objectLevel AND (pers.isToDelete = 0 OR pers.isToDelete is null) AND (e.isToDelete = 0 OR e.isToDelete is null) " +
                                "GROUP BY p.plausiId ";

                NativeQuery errorNumbersQuery = session.createNativeQuery(errorNumbersQueryString);

                errorNumbersQuery.setParameter("cantonId", cantonId);
                errorNumbersQuery.setParameter("objectLevel", CodegroupUtility.SBA_OBJECTTYPE_CANTON);

                List<Object[]> errorNumbersList = errorNumbersQuery.getResultList();

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
                // Select number of all errors per plausi
                String errorNumbersQueryString =
                        "SELECT p.plausiId, count(*) " +
                                "FROM Sba_PlausiErrors e " +
                                "LEFT OUTER JOIN Sba_Plausis p on e.plausiId = p.plausiId " +
                                "LEFT OUTER JOIN Sba_Persons pers on e.personId = pers.personId " +
                                "WHERE e.deliveryId=:deliveryId AND e.isConfirmed=0 AND p.objectLevel >= :objectLevel AND (pers.isToDelete = 0 OR pers.isToDelete is null) AND (e.isToDelete = 0 OR e.isToDelete is null) " +
                                "GROUP BY p.plausiId ";

                NativeQuery<Object[]> errorNumbersQuery = session.createNativeQuery(errorNumbersQueryString);
                errorNumbersQuery.setParameter("deliveryId", deliveryId);
                errorNumbersQuery.setParameter("objectLevel", CodegroupUtility.SBA_OBJECTTYPE_DELIVERY);

                List<Object[]> errorNumbersList = errorNumbersQuery.getResultList();

                return buildErrorNumbersMap(errorNumbersList);
            }
        });
    }

    /*
     * Mantis 1783: load confirmed errors to enable taking over confirmation info in amend/replace use case
     * 
     * @see ch.bfs.meb.sba.server.integration.repository.IPlausiErrorRepository#findConfirmedInternalErrors(java.lang.Long)
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<SbaPlausiError> findConfirmedInternalErrors(final Long deliveryId) {
        Session session = getHibernateTemplate().getSessionFactory().getCurrentSession();
        // Load all confirmed internal errors  for deliveryId
        String queryString = "SELECT e.* "//
                + "FROM Sba_PlausiErrors e "//
                + "INNER JOIN Sba_Plausis plausi on e.PlausiId = plausi.plausiId "//
                + "WHERE e.deliveryId = :deliveryId AND e.isConfirmed = 1 AND plausi.type = " + CodegroupUtility.MEB_PLAUSITYPE_INTERNAL + " "//
                + "ORDER BY e.errorId asc ";
        SQLQuery sqlQuery = session.createSQLQuery(queryString);
        sqlQuery.setLong("deliveryId", deliveryId);
        sqlQuery.addEntity(SbaPlausiError.class);
        log.info("Execute SQL-Query with deliveryId=" + deliveryId + queryString);

        List plausiErrorRecords = sqlQuery.list();
        log.info(plausiErrorRecords.size() + " PlausiError records found.");
        List<SbaPlausiError> plausiErrors = (List<SbaPlausiError>) plausiErrorRecords;

        String additionalSqlQueryString = "SELECT p.idType as personIdType, p.id as personId, q.schoolIdType, q.schoolId, q.examNr as qualificationId "//
                + "FROM Sba_PlausiErrors e "//
                + "LEFT OUTER JOIN Sba_Plausis plausi on e.plausiId = plausi.plausiId "//
                + "LEFT OUTER JOIN Sba_Persons p on e.personId = p.personId "//
                + "LEFT OUTER JOIN Sba_Qualifications q on e.qualificationId = q.qualificationId "//
                + "WHERE e.deliveryId=:deliveryId AND e.isConfirmed = 1 AND plausi.type = " + CodegroupUtility.MEB_PLAUSITYPE_INTERNAL + " "//
                + "ORDER BY e.errorId asc ";
        SQLQuery additionalSqlQuery = session.createSQLQuery(additionalSqlQueryString);
        additionalSqlQuery.setLong("deliveryId", deliveryId);

        List<Object[]> additionalDataList = additionalSqlQuery.list();

        // Build complete error information for enabling logicalKey()-function on plausierror
        for (int i = 0; i < plausiErrors.size(); i++) {
            SbaPlausiError error = plausiErrors.get(i);
            Object[] additionalData = additionalDataList.get(i);
            error.addPersonInfo((String) additionalData[0], (String) additionalData[1], null);
            error.addQualificationInfo((String) additionalData[2], (String) additionalData[3],
                    ((additionalData[4] != null) ? additionalData[4].toString() : null));
        }
        return plausiErrors;
    }


    private String getAdditionalQueryString(String whereClause) {
        return "SELECT p.idType, p.id, p.origDeliveryData, "//
                + "q.schoolIdType, q.schoolId, q.examNr, p.isToDelete, b.label "//
                + "FROM Sba_PlausiErrors e "//
                + "LEFT OUTER JOIN Sba_Plausis plausi on e.plausiId = plausi.plausiId "//
                + "LEFT OUTER JOIN Sba_Persons p on e.personId = p.personId "//
                + "LEFT OUTER JOIN Sba_Qualifications q on e.qualificationId = q.qualificationId "//
                + "LEFT OUTER JOIN SCHOOLS b ON TO_CHAR(b.burnr) = q.schoolid AND q.schoolIdType = 'CH.BUR' "//
                + whereClause + " "//
                + "ORDER BY e.personId desc, e.errorId asc ";
        }

    private String getAdditionalSchoolDataSqlQueryString() {
        return "SELECT DISTINCT e.personId, schoolIdType, q.schoolId, label "//
                + "FROM Sba_Qualifications q "//
                + "JOIN Sba_Plausierrors e ON e.personId = q.personId "//
                + "LEFT OUTER JOIN SCHOOLS b ON TO_CHAR(b.burnr) = q.schoolid "//
                + "WHERE deliveryId=:deliveryId and e.qualificationId is null "//
                + "ORDER BY personId, schoolId";
    }

}
