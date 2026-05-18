/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sbaserver

 */
package ch.bfs.meb.sba.server.integration.repository;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Objects;

import ch.bfs.meb.sba.server.integration.dto.SbaQualification;
import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.hibernate.query.NativeQuery;
import org.springframework.orm.hibernate5.HibernateCallback;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;
import org.springframework.stereotype.Repository;

import ch.bfs.meb.integration.dto.ResultBase;
import ch.bfs.meb.sba.server.integration.dto.SbaCanton;
import ch.bfs.meb.sba.server.integration.dto.SbaPlausiError;
import ch.bfs.meb.server.commons.integration.dto.PlausiError;
import ch.bfs.meb.server.commons.util.IFilterUtility;
import ch.bfs.meb.util.CodegroupUtility;

import javax.persistence.TypedQuery;

import javax.transaction.Transactional;

@Repository
public class CantonRepository extends HibernateDaoSupport implements ICantonRepository {
    private IFilterUtility _filterUtility;

    public void setFilterUtility(IFilterUtility filterUtility) {
        _filterUtility = filterUtility;
    }

    @Override
    @Transactional
    public SbaCanton getCantonById(Long cantonId) {
        TypedQuery<SbaCanton> typedQuery = currentSession().createQuery("from SbaCanton c left join fetch c.plausierrors pe left join fetch pe.plausi where c.cantonId=:cantonId", SbaCanton.class);
        typedQuery.setParameter("cantonId", cantonId);
        return typedQuery.getSingleResult();
    }

    @Override
    @Transactional
    public SbaCanton getCantonById(Long cantonId, LockMode lockMode) {
        return (SbaCanton) getHibernateTemplate().get(SbaCanton.class, cantonId, lockMode);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<SbaPlausiError> getTopPlausiErrorsForCanton(final Long cantonId) {
        return (List<SbaPlausiError>) getHibernateTemplate().execute(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                org.hibernate.query.Query<SbaCanton> q = session.createQuery("from SbaCanton c where c.cantonId=:cantonId", SbaCanton.class);
                q.setParameter("cantonId", cantonId);
                if (q.uniqueResult() == null) {
                    return null;
                }
                org.hibernate.query.Query<SbaPlausiError> q2 = session.createQuery(
                        "from SbaPlausiError as pe left join fetch pe.plausi where pe.deliveryId is null and pe.cantonId=:cantonId order by pe.isConfirmed, pe.plausi, pe.errorId", SbaPlausiError.class);
                q2.setParameter("cantonId", cantonId);
                q2.setMaxResults(ResultBase.MAX_NUMBER_ERRORS + 1);
                return q2.list();
            }
        });
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<SbaCanton> getCantons(final Long version, final Long canton) {
        return (List<SbaCanton>) getHibernateTemplate().execute(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                org.hibernate.query.Query<SbaCanton> q;
                if (canton > 0L) {
                    q = session.createQuery("from SbaCanton as c where c.version=:version and c.canton=:canton", SbaCanton.class);
                    q.setParameter("canton", canton);
                } else {
                    q = session.createQuery("from SbaCanton as c where c.version=:version", SbaCanton.class);
                }
                q.setParameter("version", version);
                return q.list();
            }
        });
    }

    @Override
    public SbaCanton getCanton(final Long version, final Long canton) {
        return getHibernateTemplate().execute(new HibernateCallback<SbaCanton>() {
            @Override
            public SbaCanton doInHibernate(Session session) throws HibernateException {
                org.hibernate.query.Query<SbaCanton> q = session.createQuery(
                        "from SbaCanton as c where c.version=:version and c.canton=:canton", SbaCanton.class
                );
                q.setParameter("version", version);
                q.setParameter("canton", canton);
                return q.uniqueResult();
            }
        });
    }

    @Override
    public SbaCanton getCantonWithConfigDeliveryAndSchoolByMaxVersion(final Long version, final Long canton) {
        return (SbaCanton) getHibernateTemplate().execute(new HibernateCallback() {
            @SuppressWarnings("unchecked")
            @Override
            public Object doInHibernate(Session session) throws HibernateException {

                org.hibernate.query.Query<SbaCanton> q = session.createQuery(
                        "select c " +
                                "from SbaCanton as c, SbaConfigDelivery as cd " +
                                "where c.version <= :version " +
                                "and c.canton = :canton " +
                                "and c.version = cd.version " +
                                "and c.canton = cd.canton " +
                                "and cd.burSchools.size > 0 " +
                                "order by c.version desc",
                        SbaCanton.class
                );

                q.setParameter("canton", canton);
                q.setParameter("version", version);
                q.setMaxResults(1);

                List<SbaCanton> l = q.getResultList();

                if (l.isEmpty()) {
                    return null;
                }

                return l.get(0);
            }
        });
    }

    @Override
    public SbaCanton insertCanton(SbaCanton canton) {
        canton = (SbaCanton) getHibernateTemplate().merge(canton);
        getHibernateTemplate().flush();
        return canton;
    }

    @Override
    public SbaCanton updateCanton(SbaCanton canton) {
        canton = (SbaCanton) getHibernateTemplate().merge(canton);
        getHibernateTemplate().flush();
        return canton;
    }

    @Override
    public void deleteCanton(SbaCanton canton) {
        getHibernateTemplate().delete(canton);
        getHibernateTemplate().flush();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Long getInitialVersion() {
        String queryString = "select {model.*} from Sba_Cantons model where model.version = (select max(version) from Sba_Cantons)";
        NativeQuery<SbaCanton> query = currentSession().createNativeQuery(queryString, SbaCanton.class);

        List<SbaCanton> cantons = (List<SbaCanton>) query.list();

        if (cantons.size() > 0) {
            return cantons.get(0).getVersion();
        } else {
            Calendar now = new GregorianCalendar();
            return (long) now.get(Calendar.YEAR);
        }
    }

    @Override
    public List<Long> getFilterCantonsForActUser() {
        return _filterUtility.getFilterCantonsForActUser();
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sba.server.integration.repository.ICantonRepository#getNumberOfPersons(ch.bfs.meb.sba.server.integration.dto.SbaCanton)
     */
    @Override
    public Long getNumberOfPersons(SbaCanton canton) {
        final Long cantonCode = canton.getCanton();
        final Long version = canton.getVersion();
        return (Long) getHibernateTemplate().execute(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {

                javax.persistence.Query query = session.createQuery("select count(p) from SbaPerson p where p.canton=:cantonCode and p.version=:version", Long.class);
                query.setParameter("cantonCode", cantonCode);
                query.setParameter("version", version);
                return query.getSingleResult();
            }
        });
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sba.server.integration.repository.ICantonRepository#allPlausibel(ch.bfs.meb.sba.server.integration.dto.SbaCanton)
     */
    @Override
    public boolean allPlausibel(SbaCanton canton) {
        final Long cantonCode = canton.getCanton();
        final Long version = canton.getVersion();
        // check qualifications
        Long notPlausibel = (Long) getHibernateTemplate().execute(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                Query query = session.createQuery("select count(a) from SbaQualification a where a.canton=:cantonCode and a.version=:version and a.plausiStatus=:plausiStatus");
                query.setLong("cantonCode", cantonCode);
                query.setLong("version", version);
                query.setLong("plausiStatus", CodegroupUtility.MEB_PLAUSISTATUS_NOTVALID);
                return query.uniqueResult();
            }
        });
        if (notPlausibel > 0L) {
            return false;
        }
        // check persons
        notPlausibel = (Long) getHibernateTemplate().execute(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                Query query = session.createQuery("select count(p) from SbaPerson p where p.canton=:cantonCode and p.version=:version and p.plausiStatus=:plausiStatus");
                query.setLong("cantonCode", cantonCode);
                query.setLong("version", version);
                query.setLong("plausiStatus", CodegroupUtility.MEB_PLAUSISTATUS_NOTVALID);
                return query.uniqueResult();
            }
        });
        if (notPlausibel > 0L) {
            return false;
        }
        // check deliveries
        notPlausibel = (Long) getHibernateTemplate().execute(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                Query query = session.createQuery("select count(d) from SbaDelivery d where d.canton=:cantonCode and d.version=:version and d.plausiStatus=:plausiStatus");
                query.setLong("cantonCode", cantonCode);
                query.setLong("version", version);
                query.setLong("plausiStatus", CodegroupUtility.MEB_PLAUSISTATUS_NOTVALID);
                return query.uniqueResult();
            }
        });
        if (notPlausibel > 0L) {
            return false;
        }
        return !canton.getPlausiStatus().equals(CodegroupUtility.MEB_PLAUSISTATUS_NOTVALID);
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sba.server.integration.repository.ICantonRepository#validateAll(java.lang.Long, java.lang.String)
     */
    @Override
    public void validateAll(SbaCanton canton, String userEmail) {
        Date now = new Date();
        Long cantonCode = canton.getCanton();
        Long version = canton.getVersion();
        // prevalidation user and date have to be set in a second update statement because Hql cannot 
        // handle parameter in then branch of case statement (Hibernate bug HHH-4700)
        String hql = "update SbaQualification set deliveryStatus=:status, validation_user=:userEmail, validation_date=:now where canton=:cantonCode and version=:version and deliveryStatus in (:deliveredStatus, :prevalidatedStatus)";
        javax.persistence.Query query = Objects.requireNonNull(getSessionFactory()).getCurrentSession().createQuery(hql);
        query.setParameter("status", CodegroupUtility.MEB_DATASTATUS_VALIDATED);
        query.setParameter("userEmail", userEmail);
        query.setParameter("now", now);
        query.setParameter("cantonCode", cantonCode);
        query.setParameter("version", version);
        query.setParameter("deliveredStatus", CodegroupUtility.MEB_DATASTATUS_DELIVERED);
        query.setParameter("prevalidatedStatus", CodegroupUtility.MEB_DATASTATUS_PREVALIDATED);
        query.executeUpdate();

        String hql2 = "update SbaQualification set prevalidation_user = case when prevalidation_user is null then validation_user else prevalidation_user end, prevalidation_date = case when prevalidation_date is null then validation_date else prevalidation_date end where canton = :cantonCode and version = :version and deliveryStatus = :deliveryStatus";
        javax.persistence.Query query2 = Objects.requireNonNull(getSessionFactory()).getCurrentSession().createQuery(hql2);
        query2.setParameter("cantonCode", cantonCode);
        query2.setParameter("version", version);
        query2.setParameter("deliveryStatus", CodegroupUtility.MEB_DATASTATUS_VALIDATED);
        query2.executeUpdate();

        String hql3 = "update SbaPerson set deliveryStatus=:status, validation_user=:userEmail, validation_date=:now where canton=:cantonCode and version=:version and deliveryStatus in (:deliveredStatus, :prevalidatedStatus)";
        javax.persistence.Query query3= Objects.requireNonNull(getSessionFactory()).getCurrentSession().createQuery(hql3);
        query3.setParameter("status", CodegroupUtility.MEB_DATASTATUS_VALIDATED);
        query3.setParameter("userEmail", userEmail);
        query3.setParameter("now", now);
        query3.setParameter("cantonCode", cantonCode);
        query3.setParameter("version", version);
        query3.setParameter("deliveredStatus", CodegroupUtility.MEB_DATASTATUS_DELIVERED);
        query3.setParameter("prevalidatedStatus", CodegroupUtility.MEB_DATASTATUS_PREVALIDATED);
        query3.executeUpdate();

        String hql4 = "update SbaPerson set prevalidation_user=case when prevalidation_user is null then validation_user else prevalidation_user end, prevalidation_date=case when prevalidation_date is null then validation_date else prevalidation_date end where canton=:cantonCode and version=:version and deliveryStatus=:status";
        javax.persistence.Query query4 = Objects.requireNonNull(getSessionFactory()).getCurrentSession().createQuery(hql4);
        query4.setParameter("cantonCode", cantonCode);
        query4.setParameter("version", version);
        query4.setParameter("status", CodegroupUtility.MEB_DATASTATUS_VALIDATED);
        query4.executeUpdate();

        String hql5 = "update SbaDelivery set deliveryStatus=:deliveryStatus, validation_user=:userEmail, validation_date=:date where canton=:cantonCode and version=:version and deliveryStatus in (:statusDelivered, :statusPrevalidated)";

        javax.persistence.Query query5 = Objects.requireNonNull(getSessionFactory()).getCurrentSession().createQuery(hql5);
        query5.setParameter("deliveryStatus", CodegroupUtility.MEB_DELIVERYSTATUS_VALIDATED);
        query5.setParameter("userEmail", userEmail);
        query5.setParameter("date", now);
        query5.setParameter("cantonCode", cantonCode);
        query5.setParameter("version", version);
        query5.setParameter("statusDelivered", CodegroupUtility.MEB_DELIVERYSTATUS_DELIVERED);
        query5.setParameter("statusPrevalidated", CodegroupUtility.MEB_DELIVERYSTATUS_PREVALIDATED);
        query5.executeUpdate();

        String hql6 = "update SbaDelivery set " +
                "prevalidation_user = case when prevalidation_user is null then validation_user else prevalidation_user end, " +
                "prevalidation_date = case when prevalidation_date is null then validation_date else prevalidation_date end " +
                "where canton= :cantonCode and version= :version and deliveryStatus= :deliveryStatus";

        javax.persistence.Query query6 = Objects.requireNonNull(getSessionFactory()).getCurrentSession().createQuery(hql6);
        query6.setParameter("cantonCode", cantonCode);
        query6.setParameter("version", version);
        query6.setParameter("deliveryStatus", CodegroupUtility.MEB_DELIVERYSTATUS_VALIDATED);
        query6.executeUpdate();


        canton.setDeliveryStatus(CodegroupUtility.MEB_CANTONSTATUS_VALIDATED);
        canton.setValidation_user(userEmail);
        canton.setValidation_date(now);
        getHibernateTemplate().merge(canton);
        getHibernateTemplate().flush();
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sba.server.integration.repository.ICantonRepository#undoValidate(java.lang.Long)
     */
    @Override
    public void undoValidate(SbaCanton canton) {
        final Long cantonCode = canton.getCanton();
        final Long version = canton.getVersion();
        getHibernateTemplate().execute(new HibernateCallback<Object>() {
            public Object doInHibernate(Session session) throws HibernateException {
                org.hibernate.query.Query query = session.createQuery(
                        "update SbaQualification set deliveryStatus=:newStatus, validation_user=null, validation_date=null where canton=:cantonCode and version=:version and deliveryStatus=:oldStatus");
                query.setParameter("newStatus", CodegroupUtility.MEB_DATASTATUS_PREVALIDATED);
                query.setParameter("cantonCode", cantonCode);
                query.setParameter("version", version);
                query.setParameter("oldStatus", CodegroupUtility.MEB_DATASTATUS_VALIDATED);
                int result = query.executeUpdate();
                return null;
            }
        });
        getHibernateTemplate().execute(session -> {
            String queryString = "update SbaPerson set deliveryStatus=:deliveryStatus, validation_user=null, validation_date=null where canton=:canton and version=:version and deliveryStatus=:oldDeliveryStatus";
            org.hibernate.query.Query query=session.createQuery(queryString);
            query.setParameter("deliveryStatus", CodegroupUtility.MEB_DATASTATUS_PREVALIDATED);
            query.setParameter("canton", cantonCode);
            query.setParameter("version", version);
            query.setParameter("oldDeliveryStatus", CodegroupUtility.MEB_DATASTATUS_VALIDATED);
            return query.executeUpdate();
        });
        getHibernateTemplate().execute(new HibernateCallback<Object>() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                String hql = "update SbaDelivery set deliveryStatus=:status, validation_user=null, "
                        + "validation_date=null where canton=:canton and version=:version and deliveryStatus=:status2";
                org.hibernate.query.Query query = session.createQuery(hql);
                query.setParameter("status", CodegroupUtility.MEB_DELIVERYSTATUS_PREVALIDATED);
                query.setParameter("canton", cantonCode);
                query.setParameter("version", version);
                query.setParameter("status2", CodegroupUtility.MEB_DELIVERYSTATUS_VALIDATED);
                return query.executeUpdate();
            }
        });
        canton.setDeliveryStatus(CodegroupUtility.MEB_CANTONSTATUS_DELIVERED);
        canton.setValidation_user(null);
        canton.setValidation_date(null);
        getHibernateTemplate().merge(canton);
        getHibernateTemplate().flush();
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sba.server.integration.repository.ICantonRepository#finalizeCanton(ch.bfs.meb.sba.server.integration.dto.SbaCanton, java.lang.String)
     */
    @Override
    public void finalizeCanton(SbaCanton canton, String username) {
        Date now = new Date();
        Long cantonCode = canton.getCanton();
        Long version = canton.getVersion();
        getHibernateTemplate().execute(session -> {
            String updateHql = "update SbaQualification " +
                    "set deliveryStatus = :newStatus " +
                    "where canton= :cantonCode " +
                    "and version= :version " +
                    "and deliveryStatus= :oldStatus";

            org.hibernate.query.Query query = session.createQuery(updateHql);
            query.setParameter("newStatus", CodegroupUtility.MEB_DATASTATUS_FINALIZED);
            query.setParameter("cantonCode", cantonCode);
            query.setParameter("version", version);
            query.setParameter("oldStatus", CodegroupUtility.MEB_DATASTATUS_VALIDATED);

            return query.executeUpdate();
        });
        getHibernateTemplate().execute(session -> {
            org.hibernate.query.Query query = session.createQuery(
                    "update SbaPerson set deliveryStatus=:status where canton=:canton and version=:version and deliveryStatus=:validStatus");
            query.setParameter("status", CodegroupUtility.MEB_DATASTATUS_FINALIZED);
            query.setParameter("canton", cantonCode);
            query.setParameter("version", version);
            query.setParameter("validStatus", CodegroupUtility.MEB_DATASTATUS_VALIDATED);
            return query.executeUpdate();
        });
        getHibernateTemplate().execute(session -> {
            org.hibernate.query.Query query = session.createQuery(
                    "update SbaDelivery set deliveryStatus=:status where canton=:canton and version=:version and deliveryStatus=:oldStatus");
            query.setParameter("status", CodegroupUtility.MEB_DELIVERYSTATUS_FINALIZED);
            query.setParameter("canton", cantonCode);
            query.setParameter("version", version);
            query.setParameter("oldStatus", CodegroupUtility.MEB_DELIVERYSTATUS_VALIDATED);
            return query.executeUpdate();
        });
        canton.setDeliveryStatus(CodegroupUtility.MEB_CANTONSTATUS_FINALIZED);
        canton.setFinalisation_user(username);
        canton.setFinalisation_date(now);
        getHibernateTemplate().merge(canton);
        getHibernateTemplate().flush();
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sba.server.integration.repository.ICantonRepository#undoFinalize(ch.bfs.meb.sba.server.integration.dto.SbaCanton)
     */
    @Override
    public void undoFinalize(SbaCanton canton) {
        Long cantonCode = canton.getCanton();
        Long version = canton.getVersion();
        getHibernateTemplate().execute(session -> {
            org.hibernate.query.Query query = session.createQuery("update SbaQualification set deliveryStatus=:status where canton=:canton and version=:version and deliveryStatus=:deliveryStatus");
            query.setParameter("status", CodegroupUtility.MEB_DATASTATUS_VALIDATED);
            query.setParameter("canton", cantonCode);
            query.setParameter("version", version);
            query.setParameter("deliveryStatus", CodegroupUtility.MEB_DATASTATUS_FINALIZED);
            return query.executeUpdate();
        });
        getHibernateTemplate().execute(new HibernateCallback<Integer>() {
            public Integer doInHibernate(Session session) throws HibernateException {
                org.hibernate.query.Query query = session.createQuery("update SbaPerson set deliveryStatus = :status where canton = :canton and version = :version and deliveryStatus = :deliveryStatus");
                query.setParameter("status", CodegroupUtility.MEB_DATASTATUS_VALIDATED);
                query.setParameter("canton", cantonCode);
                query.setParameter("version", version);
                query.setParameter("deliveryStatus", CodegroupUtility.MEB_DATASTATUS_FINALIZED);
                return query.executeUpdate();
            }
        });

        getHibernateTemplate().execute(session -> {
            String hqlQuery = "update SbaDelivery set deliveryStatus = :status where canton = :canton and version = :version and deliveryStatus = :finalisedStatus";
            org.hibernate.query.Query query = session.createQuery(hqlQuery);
            query.setParameter("status", CodegroupUtility.MEB_DELIVERYSTATUS_VALIDATED);
            query.setParameter("canton", cantonCode);
            query.setParameter("version", version);
            query.setParameter("finalisedStatus", CodegroupUtility.MEB_DELIVERYSTATUS_FINALIZED);

            return query.executeUpdate();
        });
        canton.setDeliveryStatus(CodegroupUtility.MEB_CANTONSTATUS_VALIDATED);
        canton.setFinalisation_user(null);
        canton.setFinalisation_date(null);
        getHibernateTemplate().merge(canton);
        getHibernateTemplate().flush();
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sba.server.integration.repository.ICantonRepository#setCantonErrorsToDelete(java.lang.Long)
     */
    @Override
    public void setCantonErrorsToDelete(Long cantonId) {
        String hql = "update SbaPlausiError set isToDelete=1 where errorId in (select e.errorId from SbaPlausiError e, SbaPlausi p where e.cantonId= :cantonId and e.plausi=p.plausiId and p.objectLevel= :objectLevel)";
        getHibernateTemplate().execute(session -> {
            Query query = session.createQuery(hql);
            query.setParameter("cantonId", cantonId);
            query.setParameter("objectLevel", CodegroupUtility.SBA_OBJECTTYPE_CANTON);
            return query.executeUpdate();
        });
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sba.server.integration.repository.ICantonRepository#deleteMarkedErrors(java.lang.Long)
     */
    @SuppressWarnings("unchecked")
    @Override
    public void deleteMarkedErrors(final Long cantonId) {
        List<PlausiError> errors = (List<PlausiError>) getHibernateTemplate().execute(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                org.hibernate.query.Query<PlausiError> query = session.createQuery("from SbaPlausiError where isToDelete=1 and cantonId= :cantonId", PlausiError.class);
                query.setParameter("cantonId", cantonId);
                return query.list();
            }
        });
        for (PlausiError error : errors) {
            try {
                getHibernateTemplate().delete(error);
            } catch (Exception e) {
                // do nothing
            }
        }
        //getHibernateTemplate().bulkUpdate ("delete from SbaPlausiError where isToDelete=1 and cantonId= :cantonId", cantonId);
        getHibernateTemplate().flush();
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sba.server.integration.repository.ICantonRepository#updatePlausistatus(java.lang.Long)
     */
    @Override
    public void updatePlausistatus(Long cantonId) {
        String hql = "update SbaCanton set plausiStatus=case "+
                "when (select count(e) from SbaPlausiError e where e.cantonId= :cantonId1 and e.deliveryId is null)=0 then 2 "+
                "when (select count(e) from SbaPlausiError e where e.cantonId= :cantonId2 and e.deliveryId is null and e.isConfirmed=0)>0 then 1 "+
                "else 3 "+
                "end where cantonId= :cantonId3";
        getHibernateTemplate().execute(session -> {
            org.hibernate.query.Query<?> query = session.createQuery(hql);
            query.setParameter("cantonId1", cantonId);
            query.setParameter("cantonId2", cantonId);
            query.setParameter("cantonId3", cantonId);
            return query.executeUpdate();
        });
        getHibernateTemplate().flush();
    }
}
