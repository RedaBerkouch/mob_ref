/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sbaserver

 */
package ch.bfs.meb.sba.server.integration.repository;

import java.math.BigDecimal;
import java.util.*;

import ch.bfs.meb.sba.server.integration.dto.SbaQualification;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.springframework.orm.hibernate5.HibernateCallback;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;
import org.springframework.security.core.context.SecurityContextHolder;

import ch.bfs.meb.exception.MebUncheckedNotMonitoredException;
import ch.bfs.meb.integration.dto.ResultBase;
import ch.bfs.meb.sba.server.integration.dto.SbaPerson;
import ch.bfs.meb.sba.server.integration.dto.SbaPlausiError;
import ch.bfs.meb.security.MebUser;
import ch.bfs.meb.server.commons.integration.dto.FilterContext;
import ch.bfs.meb.server.commons.integration.dto.SortContext;
import ch.bfs.meb.server.commons.util.IFilterUtility;
import ch.bfs.meb.util.CodegroupUtility;
import ch.bfs.meb.util.SecurityConstants;
import ch.bfs.meb.util.StringUtils;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

/**
 * Repository for SbaPersons.
 * 
 * @author $Author: lsc $
 * @version $Revision: 995 $
 */
public class PersonRepository extends HibernateDaoSupport implements IPersonRepository {
    // property constants
    public static final String DELIVERYCODE = "deliveryCode";
    public static final String IDTYPE = "idType";
    public static final String ID = "id";
    public static final String CREATION_USER = "creation_user";
    public static final String MODIFICATION_USER = "modification_user";
    public static final String PREVALIDATION_USER = "prevalidation_user";
    public static final String VALIDATION_USER = "validation_user";
    public static final String USERTEXT = "userText";

    public static final String BIRTHDATE = "birthdate";
    public static final String CREATION_DATE = "creation_date";
    public static final String MODIFICATION_DATE = "modification_date";
    public static final String PREVALIDATION_DATE = "prevalidation_date";
    public static final String VALIDATION_DATE = "validation_date";

    public static final String DELIVERYSTATUS = "deliveryStatus";
    public static final String PLAUSISTATUS = "plausiStatus";
    private static final String RESIDENCE = "residence";
    private static final String HISTORIC_RESIDENCE = "historic_residence";
    private static final String COUNTRY = "country";

    private IFilterUtility _filterUtility;

    public void setFilterUtility(IFilterUtility filterUtility) {
        _filterUtility = filterUtility;
    }

    @Override
    public SbaPerson getPersonById(Long personId) {
        return (SbaPerson) getHibernateTemplate().get(SbaPerson.class, personId);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<SbaPlausiError> getTopPlausiErrorsForPerson(final Long personId) {
        return (List<SbaPlausiError>) getHibernateTemplate().execute(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                org.hibernate.query.Query<SbaPerson> query = session.createQuery("from SbaPerson p where p.personId=:personId", SbaPerson.class);
                query.setParameter("personId", personId);
                if (query.uniqueResult() == null) {
                    return null;
                }

                org.hibernate.query.Query<SbaPlausiError> q = session.createQuery(
                        "from SbaPlausiError as pe left join fetch pe.plausi where pe.qualificationId is null and pe.personId=:personId order by pe.isConfirmed, pe.plausi, pe.errorId", SbaPlausiError.class);
                q.setParameter("personId", personId);
                q.setMaxResults(ResultBase.MAX_NUMBER_ERRORS + 1);
                return q.list();
            }
        });
    }

    @Override
    public void clearPersonFromCache(SbaPerson person) {
        getHibernateTemplate().evict(person);
        for (SbaPlausiError error : person.getPlausierrors()) {
            getHibernateTemplate().evict(error);
        }
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sba.server.integration.repository.IPersonRepository#getDeliveryStatus(java.lang.Long)
     */
    @Override
    public Long getDeliveryStatus(Long personId) {
        org.hibernate.query.Query<Long> query = currentSession().createQuery("select deliveryStatus from SbaPerson where personId=:personId", Long.class);
        query.setParameter("personId", personId);
        return query.uniqueResult();
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sba.server.integration.repository.IPersonRepository#getPersonsByIdentification(java.lang.Long, java.lang.String, java.lang.String)
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<SbaPerson> getPersonsByIdentification(final Long deliveryId, final String idType, final String id) {
        return (List<SbaPerson>) getHibernateTemplate().execute(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                org.hibernate.query.Query<SbaPerson> query = session.createQuery("from SbaPerson where deliveryId=:deliveryId and idType=:idType and id=:id and isToDelete=0", SbaPerson.class);
                query.setParameter("deliveryId", deliveryId);
                query.setParameter("idType", idType);
                query.setParameter("id", id);
                return query.list();
            }
        });
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sba.server.integration.repository.IPersonRepository#getPersonsByIdentification(java.lang.Long, java.lang.Long, java.lang.String, java.lang.String)
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<SbaPerson> getPersonsByIdentification(final Long canton, final Long version, final String idType, final String id) {
        return (List<SbaPerson>) getHibernateTemplate().execute(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                org.hibernate.query.Query<SbaPerson> query = session.createQuery("from SbaPerson where canton=:canton and version=:version and idType=:idType and id=:id", SbaPerson.class);
                query.setParameter("canton", canton);
                query.setParameter("version", version);
                query.setParameter("idType", idType);
                query.setParameter("id", id);
                return query.list();
            }
        });
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<SbaPerson> getPersonsForDelivery(final Long deliveryId) {
        return (List<SbaPerson>) getHibernateTemplate().execute(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                Query<SbaPerson> query = session.createQuery("from SbaPerson where deliveryId = :deliveryId order by personId desc", SbaPerson.class);
                query.setParameter("deliveryId", deliveryId);
                return query.list();
            }
        });
    }


    /* (non-Javadoc)
     * @see ch.bfs.meb.sba.server.integration.repository.IPersonRepository#loadWholeDelivery(java.lang.Long)
     */
    @SuppressWarnings("unchecked")
    @Override
    public Set<SbaPerson> loadWholeDelivery(final Long deliveryId) {
        return (Set<SbaPerson>) getHibernateTemplate().execute(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                org.hibernate.query.Query query = session.createQuery(
                        "from SbaPerson p left join fetch p.qualifications q left join fetch p.plausierrors left join fetch q.plausierrors where p.deliveryId=:deliveryId order by p.personId, q.qualificationId",
                        SbaPerson.class);
                query.setParameter("deliveryId", deliveryId);
                return new LinkedHashSet(query.list());
            }
        });
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<SbaPerson> getPersons(int start, int buffer, SortContext sortContext, FilterContext filterContext, Long version, Long canton) {
        String queryString;
        String sortColumn = _filterUtility.adaptColumnName(sortContext.getSortColumn(), "model", getUnderscoreColumns());
        String codegroupId = getCodegroupId(sortColumn);
        String whereSelection = _filterUtility.getWhereFilterSelection(filterContext, "model", getStringColumns(), getDateColumns(), getUnderscoreColumns());
        String personSubquery = _filterUtility.getPredefinedFilterSubquery(filterContext, SecurityFilters.SBA_PERSONS_TABLE, true);

        if (whereSelection.length() > 0) {
            whereSelection += " and ";
        }
        whereSelection += "model.isToDelete <> 1";

        if (canton > 0L) {
            whereSelection += " and model.canton=" + canton;
        }

        if (version != null) {
            whereSelection += " and model.version=" + version;
        }

        if (sortContext.getLocale() != null && codegroupId != null) {
            // sort according to locale dependent code texts
            String mainLocale = sortContext.getLocale();
            queryString = "select distinct model.personId, " + "(case when " + sortColumn + " is null then '' " + "      when meb_cg.code is null then to_char("
                    + sortColumn + ") " + "      else  meb_cg.codetext end) sorttext " + " from " + personSubquery + " model " + " left outer join  "
                    + " (select cg1.* from Codegroups cg1, "
                    + "   (select codegroupid, code, language, max(validFromYear) validFromYear from Codegroups where codegroupId = '" + codegroupId
                    + "' and language = '" + mainLocale + "' group by codegroupid, code, language" + "   ) cg2"
                    + "  where cg1.codegroupId = cg2.codegroupId and cg1.code = cg2.code and cg1.language = cg2.language and cg1.validFromYear = cg2.validFromYear   "
                    + " ) meb_cg on meb_cg.code = " + sortColumn + " where " + whereSelection + " order by sorttext "
                    + ((sortContext.getAscSortOrder()) ? "asc" : "desc") + ", model.personId asc";
        } else {
            queryString = "select distinct model.personId, " + sortColumn + " from " + personSubquery + " model where " + whereSelection + " order by "
                    + sortColumn + " " + ((sortContext.getAscSortOrder()) ? "asc" : "desc") + ", model.personId asc";
        }

        // SQL-Query: Because of HQL-Problems with from-subqueries..
        Query query = currentSession().createNativeQuery(queryString);
        query.setFetchSize(500);
        if (start >= 0) {
            query.setFirstResult(start);
        }
        if (buffer > 0) {
            query.setMaxResults(buffer);
        }

        // get list of person ids as long
        List<Long> personIds = new ArrayList<Long>();
        List<Object[]> queryIdsList = query.list();
        for (Object[] row : queryIdsList) {
            personIds.add(((BigDecimal) row[0]).longValue());
        }

        return getPersonsByIds(personIds);
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sba.server.integration.repository.IPersonRepository#getPersonsOwnedByQualifications(java.util.List<java.lang.Long>)
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<SbaPerson> getPersonsOwnedByQualifications(List<Long> qualificationIds, SortContext sortContext) {
        String queryString;
        String sortColumn = _filterUtility.adaptColumnName(sortContext.getSortColumn(), "model", getUnderscoreColumns());
        String codegroupId = getCodegroupId(sortColumn);
        String cidsSelection = _filterUtility.createSqlInExpression("meb_q.qualificationId", qualificationIds);

        if (sortContext.getLocale() != null && codegroupId != null) {
            // sort according to locale dependent code texts
            String mainLocale = sortContext.getLocale();
            queryString = "select distinct model.personId, " + "(case when " + sortColumn + " is null then '' " + "      when meb_cg.code is null then to_char("
                    + sortColumn + ") " + "      else  meb_cg.codetext end) sorttext " + " from Sba_Qualifications meb_q, Sba_Persons model "
                    + " left outer join  " + " (select cg1.* from Codegroups cg1, "
                    + "   (select codegroupid, code, language, max(validFromYear) validFromYear from Codegroups where codegroupId = '" + codegroupId
                    + "' and language = '" + mainLocale + "' group by codegroupid, code, language" + "   ) cg2"
                    + "  where cg1.codegroupId = cg2.codegroupId and cg1.code = cg2.code and cg1.language = cg2.language and cg1.validFromYear = cg2.validFromYear   "
                    + " ) meb_cg on meb_cg.code = " + sortColumn + " where model.personId = meb_q.personId " + ((cidsSelection.length() == 0) ? "" : " and ")
                    + cidsSelection + " order by sorttext " + ((sortContext.getAscSortOrder()) ? "asc" : "desc");
        } else {
            queryString = "select distinct model.personId, " + sortColumn
                    + " from Sba_Persons model, Sba_Qualifications meb_q where model.personId=meb_q.personId" + ((cidsSelection.length() == 0) ? "" : " and ")
                    + cidsSelection + " order by " + sortColumn + " " + ((sortContext.getAscSortOrder()) ? "asc" : "desc");
        }

        Query query = currentSession().createNativeQuery(queryString);
        query.setFetchSize(500);

        // get list of person ids as long
        List<Long> personIds = new ArrayList<Long>();
        List<Object[]> queryIdsList = query.list();
        for (Object[] row : queryIdsList) {
            personIds.add(((BigDecimal) row[0]).longValue());
        }

        return getPersonsByIds(personIds);
    }

    @Override
    public Long getMaxNrOfPersons(FilterContext filterContext, Long version, Long canton) {
        String queryString;
        String whereSelection = _filterUtility.getWhereFilterSelection(filterContext, "model", getStringColumns(), getDateColumns(), getUnderscoreColumns());
        String classSubquery = _filterUtility.getPredefinedFilterSubquery(filterContext, SecurityFilters.SBA_PERSONS_TABLE, true);

        if (whereSelection.length() > 0) {
            whereSelection += " and ";
        }
        whereSelection += "model.isToDelete <> 1";

        if (canton > 0L) {
            whereSelection += " and model.canton=" + canton;
        }

        if (version != null) {
            whereSelection += " and model.version=" + version;
        }

        queryString = "select count (*) nrPersons from " + classSubquery + " model where " + whereSelection;

        // SQL-Query: Because of HQL-Problems with from-subqueries..
        Query query = currentSession().createNativeQuery (queryString).addScalar("nrPersons");

        return ((BigDecimal) query.uniqueResult()).longValue();
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sba.server.integration.repository.IPersonRepository#updatePerson(ch.bfs.meb.sba.server.integration.dto.SbaPerson)
     */
    @Override
    public SbaPerson updatePerson(SbaPerson person) {
        person = (SbaPerson) getHibernateTemplate().merge(person);
        getHibernateTemplate().flush();
        return person;
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sba.server.integration.repository.IPersonRepository#deletePerson(ch.bfs.meb.sba.server.integration.dto.SbaPerson)
     */
    @Override
    public void deletePerson(SbaPerson person) {
        // Récupération de l'utilisateur courant
        MebUser user = (MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        // Détermination du statut selon le rôle
        long status;
        if (user.isInRole(SecurityConstants.ROLE_SBA_DV)) {
            status = CodegroupUtility.MEB_DATASTATUS_VALIDATED;
        } else {
            status = CodegroupUtility.MEB_DATASTATUS_PREVALIDATED;
        }

        // Vérification des qualifications liées à la personne
        getHibernateTemplate().execute(session -> {
            Query<SbaQualification> query = session.createQuery(
                    "from SbaQualification where personId = :personId and deliveryStatus >= :status",
                    SbaQualification.class
            );
            query.setParameter("personId", person.getPersonId());
            query.setParameter("status", status);
            query.setMaxResults(1);

            if (!query.list().isEmpty()) {
                throw new MebUncheckedNotMonitoredException("maintain.delete.childrenValidatedError.messages");
            }
            return null;
        });

        // Suppression des qualifications associées
        getHibernateTemplate().execute(session -> {
            Query<?> deleteQuery = session.createQuery("delete from SbaQualification where personId = :personId");
            deleteQuery.setParameter("personId", person.getPersonId());
            deleteQuery.executeUpdate();
            return null;
        });

        // Suppression de la personne
        getHibernateTemplate().delete(person);
    }



    /* (non-Javadoc)
     * @see ch.bfs.meb.sba.server.integration.repository.IPersonRepository#insertPerson(ch.bfs.meb.sba.server.integration.dto.SbaPerson)
     */
    @Override
    public SbaPerson insertPerson(SbaPerson person) {
        getHibernateTemplate().save(person);
        getHibernateTemplate().flush();
        return person;
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sba.server.integration.repository.IPersonRepository#getNumberOfPersonsForCanton(Long, Long)
     */
    @Override
    public Long getNumberOfPersonsForCanton(final Long canton, final Long version) {
        return (Long) getHibernateTemplate().execute(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                Query<Long> query = session.createQuery("select count(*) from SbaPerson p where canton = :canton and version = :version and p.isToDelete = 0", Long.class);
                query.setParameter("canton", canton);
                query.setParameter("version", version);

                return query.uniqueResult();
            }
        });
    }


    /* (non-Javadoc)
     * @see ch.bfs.meb.sba.server.integration.repository.IPersonRepository#getNumberOfPersonsForDelivery(long)
     */
    @Override
    public Long getNumberOfPersonsForDelivery(final Long deliveryId) {
        return (Long) getHibernateTemplate().execute(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                org.hibernate.query.Query<Long> query = session.createQuery("select count(*) from SbaPerson p where p.deliveryId=:deliveryId and p.isToDelete=0", Long.class);
                query.setParameter("deliveryId", deliveryId);

                return query.uniqueResult();
            }
        });
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sba.server.integration.repository.IPersonRepository#updatePlausistatus(java.lang.Long)
     */
    @Override
    public void updatePlausistatus(Long personId) {
        getHibernateTemplate().bulkUpdate(
                "update SbaPerson set plausiStatus=case when (select count(e) from SbaPlausiError e where e.personId=:personIdParam1)=0 then 2 when (select count(e) from SbaPlausiError e where e.personId=:personIdParam2 and e.isConfirmed=0)>0 then 1 else 3 end where personId=:personIdParam3",
                new String[] { "personIdParam1", "personIdParam2","personIdParam3" },
                new Object[] { personId, personId, personId }
        );
        getHibernateTemplate().flush();
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sba.server.integration.repository.IPersonRepository#allPlausibel(ch.bfs.meb.sba.server.integration.dto.SbaPerson)
     */
    @Override
    public boolean allPlausibel(SbaPerson person) {
        final Long personId = person.getPersonId();
        // check learner
        Long notPlausibel = (Long) getHibernateTemplate().execute(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                org.hibernate.query.Query<Long> query = session.createQuery("select count(q) from SbaQualification q where q.personId=:personId and q.plausiStatus=:status", Long.class);
                query.setParameter("personId", personId);
                query.setParameter("status", CodegroupUtility.MEB_PLAUSISTATUS_NOTVALID);
                return query.uniqueResult();
            }
        });
        if (notPlausibel > 0L) {
            return false;
        }
        return !person.getPlausiStatus().equals(CodegroupUtility.MEB_PLAUSISTATUS_NOTVALID);
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sba.server.integration.repository.IPersonRepository#prevalidate(java.util.List, java.lang.String)
     */
    @Override
    public void prevalidate(List<Long> personList, String userEmail) {
        String idList = _filterUtility.createSqlInExpression("personId", personList);
        Date now = new Date();

        getHibernateTemplate().execute(session -> {
            // SbaQualification: set prevalidated only if current status is DELIVERED
            String qualifUpdate = "UPDATE SbaQualification SET deliveryStatus = :status, prevalidation_user = :userEmail, prevalidation_date = :now " +
                    "WHERE " + idList + " AND deliveryStatus = :currentStatus";
            Query<?> q1 = session.createQuery(qualifUpdate);
            q1.setParameter("status", CodegroupUtility.MEB_DATASTATUS_PREVALIDATED);
            q1.setParameter("userEmail", userEmail);
            q1.setParameter("now", now);
            q1.setParameter("currentStatus", CodegroupUtility.MEB_DATASTATUS_DELIVERED);
            q1.executeUpdate();

            // SbaPerson: set prevalidated only if current status is DELIVERED
            String personUpdate = "UPDATE SbaPerson SET deliveryStatus = :status, prevalidation_user = :userEmail, prevalidation_date = :now " +
                    "WHERE " + idList + " AND deliveryStatus = :currentStatus";
            Query<?> q2 = session.createQuery(personUpdate);
            q2.setParameter("status", CodegroupUtility.MEB_DATASTATUS_PREVALIDATED);
            q2.setParameter("userEmail", userEmail);
            q2.setParameter("now", now);
            q2.setParameter("currentStatus", CodegroupUtility.MEB_DATASTATUS_DELIVERED);
            q2.executeUpdate();

            return null;
        });

        getHibernateTemplate().flush();
    }


    /* (non-Javadoc)
     * @see ch.bfs.meb.sba.server.integration.repository.IPersonRepository#validate(java.util.List, java.lang.String)
     */

    @Override
    public void validate(List<Long> personList, String userEmail) {
        String idList = _filterUtility.createSqlInExpression("personId", personList);
        Date now = new Date();

        getHibernateTemplate().execute(session -> {
            // SbaQualification: set validated
            String qualifUpdate = "UPDATE SbaQualification SET deliveryStatus = :status, validation_user = :userEmail, validation_date = :now WHERE " + idList;
            Query<?> q1 = session.createQuery(qualifUpdate);
            q1.setParameter("status", CodegroupUtility.MEB_DATASTATUS_VALIDATED);
            q1.setParameter("userEmail", userEmail);
            q1.setParameter("now", now);
            q1.executeUpdate();

            // SbaQualification: set prevalidation if null
            String qualifPrevalUpdate = "UPDATE SbaQualification SET prevalidation_user = CASE WHEN prevalidation_user IS NULL THEN validation_user ELSE prevalidation_user END, " +
                    "prevalidation_date = CASE WHEN prevalidation_date IS NULL THEN validation_date ELSE prevalidation_date END WHERE " + idList;
            Query<?> q2 = session.createQuery(qualifPrevalUpdate);
            q2.executeUpdate();

            // SbaPerson: set validated
            String personUpdate = "UPDATE SbaPerson SET deliveryStatus = :status, validation_user = :userEmail, validation_date = :now WHERE " + idList;
            Query<?> q3 = session.createQuery(personUpdate);
            q3.setParameter("status", CodegroupUtility.MEB_DATASTATUS_VALIDATED);
            q3.setParameter("userEmail", userEmail);
            q3.setParameter("now", now);
            q3.executeUpdate();

            // SbaPerson: set prevalidation if null
            String personPrevalUpdate = "UPDATE SbaPerson SET prevalidation_user = CASE WHEN prevalidation_user IS NULL THEN validation_user ELSE prevalidation_user END, " +
                    "prevalidation_date = CASE WHEN prevalidation_date IS NULL THEN validation_date ELSE prevalidation_date END WHERE " + idList;
            Query<?> q4 = session.createQuery(personPrevalUpdate);
            q4.executeUpdate();

            return null;
        });

        getHibernateTemplate().flush();
    }


    /* (non-Javadoc)
     * @see ch.bfs.meb.sba.server.integration.repository.IPersonRepository#undoPrevalidate(java.util.List)
     */
    @Override
    public void undoPrevalidate(List<Long> personList) {
        String idList = _filterUtility.createSqlInExpression("personId", personList);

        getHibernateTemplate().execute(session -> {
            // SbaQualification : reset prevalidation info and set status to DELIVERED
            String qualifUndoQuery = "UPDATE SbaQualification SET deliveryStatus = :status, prevalidation_user = null, prevalidation_date = null WHERE " + idList;
            Query<?> q1 = session.createQuery(qualifUndoQuery);
            q1.setParameter("status", CodegroupUtility.MEB_DATASTATUS_DELIVERED);
            q1.executeUpdate();

            // SbaPerson : reset prevalidation info and set status to DELIVERED
            String personUndoQuery = "UPDATE SbaPerson SET deliveryStatus = :status, prevalidation_user = null, prevalidation_date = null WHERE " + idList;
            Query<?> q2 = session.createQuery(personUndoQuery);
            q2.setParameter("status", CodegroupUtility.MEB_DATASTATUS_DELIVERED);
            q2.executeUpdate();

            return null;
        });

        getHibernateTemplate().flush();
    }


    /* (non-Javadoc)
     * @see ch.bfs.meb.sba.server.integration.repository.IPersonRepository#undoValidate(java.util.List)
     */
    @Override
    public void undoValidate(List<Long> personList) {
        String idList = _filterUtility.createSqlInExpression("personId", personList);

        getHibernateTemplate().execute(session -> {
            // SbaQualification : annuler la validation (retour à PREVALIDATED)
            String qualifUndoValidationQuery = "UPDATE SbaQualification SET deliveryStatus = :status, validation_user = null, validation_date = null WHERE " + idList;
            Query<?> q1 = session.createQuery(qualifUndoValidationQuery);
            q1.setParameter("status", CodegroupUtility.MEB_DATASTATUS_PREVALIDATED);
            q1.executeUpdate();

            // SbaPerson : idem
            String personUndoValidationQuery = "UPDATE SbaPerson SET deliveryStatus = :status, validation_user = null, validation_date = null WHERE " + idList;
            Query<?> q2 = session.createQuery(personUndoValidationQuery);
            q2.setParameter("status", CodegroupUtility.MEB_DATASTATUS_PREVALIDATED);
            q2.executeUpdate();

            return null;
        });

        getHibernateTemplate().flush();
    }


    @SuppressWarnings({ "unchecked" })
    private List<SbaPerson> getPersonsByIds(List<Long> personIds) {
        if (personIds == null || personIds.isEmpty()) {
            return new ArrayList<SbaPerson>();
        }

        // Get current Hibernate Session
        Session session = this.getSessionFactory().getCurrentSession();

        // Create CriteriaBuilder
        CriteriaBuilder builder = session.getCriteriaBuilder();

        // Create CriteriaQuery
        CriteriaQuery<SbaPerson> criteria = builder.createQuery(SbaPerson.class);
        Root<SbaPerson> personRoot = criteria.from(SbaPerson.class);
        criteria.select(personRoot);

        // Create In expression for ids
        CriteriaBuilder.In<Long> inClause = builder.in(personRoot.get("personId"));
        for (Long id : personIds) {
            inClause.value(id);
        }
        criteria.where(inClause);

        // Execute the query
        List<SbaPerson> personsTemp = session.createQuery(criteria).getResultList();

        Map<Long, SbaPerson> mapById = new HashMap<Long, SbaPerson>(personsTemp.size());
        for (SbaPerson person : personsTemp) {
            mapById.put(person.getPersonId(), person);
        }

        List<SbaPerson> resultList = new ArrayList<SbaPerson>(mapById.size());
        for (Long personId : personIds) {
            SbaPerson person = mapById.get(personId);
            if (person != null) {
                resultList.add(person);
            }
        }
        return resultList;
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
            return CodegroupUtility.MEB_DATASTATUS;
        } else if (colName.equals("model." + PLAUSISTATUS)) {
            return CodegroupUtility.MEB_PLAUSISTATUS;
        } else if (colName.equals("model." + RESIDENCE)) {
            return CodegroupUtility.MUNICIPALITY;
        } else if (colName.equals("model." + HISTORIC_RESIDENCE)) {
            return CodegroupUtility.MUNICIPALITY_HIST;
        } else if (colName.equals("model." + COUNTRY)) {
            return CodegroupUtility.COUNTRY;
        }

        return null;
    }

    /**
     * Returns all columns with underscores of according db table
     */
    protected List<String> getUnderscoreColumns() {
        ArrayList<String> underscoreColumns = new ArrayList<String>();
        underscoreColumns.add(HISTORIC_RESIDENCE);
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
        stringColumns.add(IDTYPE);
        stringColumns.add(ID);
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
        dateColumns.add(BIRTHDATE);
        dateColumns.add(StringUtils.asCamelCase(CREATION_DATE));
        dateColumns.add(StringUtils.asCamelCase(MODIFICATION_DATE));
        dateColumns.add(StringUtils.asCamelCase(PREVALIDATION_DATE));
        dateColumns.add(StringUtils.asCamelCase(VALIDATION_DATE));
        return dateColumns;
    }
}
