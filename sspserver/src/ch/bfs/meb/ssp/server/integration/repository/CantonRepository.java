/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sspserver

 */
package ch.bfs.meb.ssp.server.integration.repository;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.hibernate.*;
import org.springframework.orm.hibernate5.HibernateCallback;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;
import org.springframework.stereotype.Repository;

import ch.bfs.meb.integration.dto.ResultBase;
import ch.bfs.meb.server.commons.util.IFilterUtility;
import ch.bfs.meb.ssp.server.integration.dto.SspCanton;
import ch.bfs.meb.ssp.server.integration.dto.SspPlausiError;
import ch.bfs.meb.util.CodegroupUtility;

@Repository
public class CantonRepository extends HibernateDaoSupport implements ICantonRepository {
    private IFilterUtility _filterUtility;

    public void setFilterUtility(IFilterUtility filterUtility) {
        _filterUtility = filterUtility;
    }

    @Override
    public SspCanton getCantonById(Long cantonId) {
        Query<SspCanton> query = currentSession().createQuery(
                "from SspCanton c left join fetch c.plausierrors pe left join fetch pe.plausi where c.cantonId = :cantonId",
                SspCanton.class
        );
        query.setParameter("cantonId", cantonId);
        return query.uniqueResult();
    }


    @Override
    public SspCanton getCantonById(Long cantonId, LockMode lockMode) {
        return (SspCanton) getHibernateTemplate().get(SspCanton.class, cantonId, lockMode);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<SspPlausiError> getTopPlausiErrorsForCanton(final Long cantonId) {
        return (List<SspPlausiError>) getHibernateTemplate().execute(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                Query q = session.createQuery("from SspCanton c where c.cantonId=:cantonId");
                q.setLong("cantonId", cantonId);
                if ((SspCanton) q.uniqueResult() == null) {
                    return null;
                }

                q = session.createQuery(
                        "from SspPlausiError as pe left join fetch pe.plausi where pe.deliveryId is null and pe.cantonId=:cantonId order by pe.isConfirmed, pe.plausi, pe.errorId");
                q.setLong("cantonId", cantonId);
                q.setMaxResults(ResultBase.MAX_NUMBER_ERRORS + 1);
                return q.list();
            }
        });
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<SspCanton> getCantons(final Long version, final Long canton) {
        return (List<SspCanton>) getHibernateTemplate().execute(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                Query q;
                if (canton > 0L) {
                    q = session.createQuery("from SspCanton as c where c.version=:version and c.canton=:canton");
                    q.setLong("canton", canton);
                } else {
                    q = session.createQuery("from SspCanton as c where c.version=:version");
                }
                q.setLong("version", version);
                return q.list();
            }
        });
    }

    @Override
    public SspCanton getCanton(final Long version, final Long canton) {
        return (SspCanton) getHibernateTemplate().execute(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                Query q;
                q = session.createQuery("from SspCanton as c where c.version=:version and c.canton=:canton");
                q.setLong("version", version);
                q.setLong("canton", canton);
                return q.uniqueResult();
            }
        });
    }

    @Override
    public SspCanton getCantonWithConfigDeliveryAndSchoolByMaxVersion(final Long version, final Long canton) {
        return (SspCanton) getHibernateTemplate().execute(new HibernateCallback() {
            @SuppressWarnings("unchecked")
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                Query q;
                q = session.createQuery(
                        "from SspCanton as c, SspConfigDelivery as cd where c.version <= :version and c.canton = :canton and c.version = cd.version and c.canton = cd.canton and cd.burSchools.size > 0 order by c.version desc");
                q.setLong("canton", canton);
                q.setLong("version", version);
                List l = q.list();
                if (l.size() == 0) {
                    return null;
                } else {
                    return ((Object[]) q.list().get(0))[0];
                }
            }
        });
    }

    @Override
    public SspCanton insertCanton(SspCanton canton) {
        canton = (SspCanton) getHibernateTemplate().merge(canton);
        getHibernateTemplate().flush();
        return canton;
    }

    @Override
    public SspCanton updateCanton(SspCanton canton) {
        canton = (SspCanton) getHibernateTemplate().merge(canton);
        getHibernateTemplate().flush();
        return canton;
    }

    @Override
    public void deleteCanton(SspCanton canton) {
        getHibernateTemplate().delete(canton);
        getHibernateTemplate().flush();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Long getInitialVersion() {
        String queryString = "select {model.*} from Ssp_Cantons model where model.version = (select max(version) from Ssp_Cantons)";
        Query query =currentSession().createNativeQuery (queryString).addEntity("model", SspCanton.class);

        List<SspCanton> cantons = (List<SspCanton>) query.list();

        if (cantons.size() > 0) {
            return cantons.get(0).getVersion();
        } else {
            Calendar now = new GregorianCalendar();
            return new Long(now.get(Calendar.YEAR));
        }
    }

    @Override
    public List<Long> getFilterCantonsForActUser() {
        return _filterUtility.getFilterCantonsForActUser();
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.ssp.server.integration.repository.ICantonRepository#getNumberOfPersons(ch.bfs.meb.ssp.server.integration.dto.SspCanton)
     */
    @Override
    public Long getNumberOfPersons(SspCanton canton) {
        final Long cantonCode = canton.getCanton();
        final Long version = canton.getVersion();
        return (Long) getHibernateTemplate().execute(session -> {
            Query<Long> query = session.createQuery(
                    "select count(p) from SspPerson p where p.canton = :cantonCode and p.version = :version",
                    Long.class
            );
            query.setParameter("cantonCode", cantonCode);
            query.setParameter("version", version);
            return query.uniqueResult();
        });
    }


    /* (non-Javadoc)
     * @see ch.bfs.meb.ssp.server.integration.repository.ICantonRepository#allPlausibel(ch.bfs.meb.ssp.server.integration.dto.SspCanton)
     */
    @Override
    public boolean allPlausibel(SspCanton canton) {
        final Long cantonCode = canton.getCanton();
        final Long version = canton.getVersion();
        // check activities
        Long notPlausibel = (Long) getHibernateTemplate().execute(session -> {
            Query<Long> query = session.createQuery(
                    "select count(a) from SspActivity a where a.canton = :cantonCode and a.version = :version and a.plausiStatus = :plausiStatus",
                    Long.class
            );
            query.setParameter("cantonCode", cantonCode);
            query.setParameter("version", version);
            query.setParameter("plausiStatus", CodegroupUtility.MEB_PLAUSISTATUS_NOTVALID);
            return query.uniqueResult();
        });
        if (notPlausibel > 0L) {
            return false;
        }
        // check persons
        notPlausibel = (Long) getHibernateTemplate().execute(session -> {
            Query<Long> query = session.createQuery(
                    "select count(p) from SspPerson p where p.canton = :cantonCode and p.version = :version and p.plausiStatus = :plausiStatus",
                    Long.class
            );
            query.setParameter("cantonCode", cantonCode);
            query.setParameter("version", version);
            query.setParameter("plausiStatus", CodegroupUtility.MEB_PLAUSISTATUS_NOTVALID);
            return query.uniqueResult();
        });
        if (notPlausibel > 0L) {
            return false;
        }
        // check deliveries
        notPlausibel = (Long) getHibernateTemplate().execute(session -> {
            Query<Long> query = session.createQuery(
                    "select count(d) from SspDelivery d where d.canton = :cantonCode and d.version = :version and d.plausiStatus = :plausiStatus",
                    Long.class
            );
            query.setParameter("cantonCode", cantonCode);
            query.setParameter("version", version);
            query.setParameter("plausiStatus", CodegroupUtility.MEB_PLAUSISTATUS_NOTVALID);
            return query.uniqueResult();
        });
        if (notPlausibel > 0L) {
            return false;
        }
        return !canton.getPlausiStatus().equals(CodegroupUtility.MEB_PLAUSISTATUS_NOTVALID);
    }


    /* (non-Javadoc)
     * @see ch.bfs.meb.ssp.server.integration.repository.ICantonRepository#validateAll(java.lang.Long, java.lang.String)
     */
    @Override
    public void validateAll(SspCanton canton, String userEmail) {
        Date now = new Date();
        Long cantonCode = canton.getCanton();
        Long version = canton.getVersion();

        // Prevalidation user and date have to be set in a second update statement because HQL cannot
        // handle parameter in the then branch of case statement (Hibernate bug HHH-4700)
        getHibernateTemplate().execute(session -> {
            Query<?> query1 = session.createQuery(
                    "update SspActivity set deliveryStatus = :deliveryStatus, validation_user = :validationUser, validation_date = :validationDate " +
                            "where canton = :cantonCode and version = :version and deliveryStatus in (:delivered, :prevalidated)"
            );
            query1.setParameter("deliveryStatus", CodegroupUtility.MEB_DATASTATUS_VALIDATED);
            query1.setParameter("validationUser", userEmail);
            query1.setParameter("validationDate", now);
            query1.setParameter("cantonCode", cantonCode);
            query1.setParameter("version", version);
            query1.setParameter("delivered", CodegroupUtility.MEB_DATASTATUS_DELIVERED);
            query1.setParameter("prevalidated", CodegroupUtility.MEB_DATASTATUS_PREVALIDATED);
            query1.executeUpdate();

            Query<?> query2 = session.createQuery(
                    "update SspActivity set prevalidation_user = case when prevalidation_user is null then validation_user else prevalidation_user end, " +
                            "prevalidation_date = case when prevalidation_date is null then validation_date else prevalidation_date end " +
                            "where canton = :cantonCode and version = :version and deliveryStatus = :validated"
            );
            query2.setParameter("cantonCode", cantonCode);
            query2.setParameter("version", version);
            query2.setParameter("validated", CodegroupUtility.MEB_DATASTATUS_VALIDATED);
            query2.executeUpdate();

            Query<?> query3 = session.createQuery(
                    "update SspPerson set deliveryStatus = :deliveryStatus, validation_user = :validationUser, validation_date = :validationDate " +
                            "where canton = :cantonCode and version = :version and deliveryStatus in (:delivered, :prevalidated)"
            );
            query3.setParameter("deliveryStatus", CodegroupUtility.MEB_DATASTATUS_VALIDATED);
            query3.setParameter("validationUser", userEmail);
            query3.setParameter("validationDate", now);
            query3.setParameter("cantonCode", cantonCode);
            query3.setParameter("version", version);
            query3.setParameter("delivered", CodegroupUtility.MEB_DATASTATUS_DELIVERED);
            query3.setParameter("prevalidated", CodegroupUtility.MEB_DATASTATUS_PREVALIDATED);
            query3.executeUpdate();

            Query<?> query4 = session.createQuery(
                    "update SspPerson set prevalidation_user = case when prevalidation_user is null then validation_user else prevalidation_user end, " +
                            "prevalidation_date = case when prevalidation_date is null then validation_date else prevalidation_date end " +
                            "where canton = :cantonCode and version = :version and deliveryStatus = :validated"
            );
            query4.setParameter("cantonCode", cantonCode);
            query4.setParameter("version", version);
            query4.setParameter("validated", CodegroupUtility.MEB_DATASTATUS_VALIDATED);
            query4.executeUpdate();

            Query<?> query5 = session.createQuery(
                    "update SspDelivery set deliveryStatus = :deliveryStatus, validation_user = :validationUser, validation_date = :validationDate " +
                            "where canton = :cantonCode and version = :version and deliveryStatus in (:delivered, :prevalidated)"
            );
            query5.setParameter("deliveryStatus", CodegroupUtility.MEB_DELIVERYSTATUS_VALIDATED);
            query5.setParameter("validationUser", userEmail);
            query5.setParameter("validationDate", now);
            query5.setParameter("cantonCode", cantonCode);
            query5.setParameter("version", version);
            query5.setParameter("delivered", CodegroupUtility.MEB_DELIVERYSTATUS_DELIVERED);
            query5.setParameter("prevalidated", CodegroupUtility.MEB_DELIVERYSTATUS_PREVALIDATED);
            query5.executeUpdate();

            Query<?> query6 = session.createQuery(
                    "update SspDelivery set prevalidation_user = case when prevalidation_user is null then validation_user else prevalidation_user end, " +
                            "prevalidation_date = case when prevalidation_date is null then validation_date else prevalidation_date end " +
                            "where canton = :cantonCode and version = :version and deliveryStatus = :validated"
            );
            query6.setParameter("cantonCode", cantonCode);
            query6.setParameter("version", version);
            query6.setParameter("validated", CodegroupUtility.MEB_DELIVERYSTATUS_VALIDATED);
            query6.executeUpdate();

            return null;
        });

        canton.setDeliveryStatus(CodegroupUtility.MEB_CANTONSTATUS_VALIDATED);
        canton.setValidation_user(userEmail);
        canton.setValidation_date(now);
        getHibernateTemplate().merge(canton);
        getHibernateTemplate().flush();
    }


    /* (non-Javadoc)
     * @see ch.bfs.meb.ssp.server.integration.repository.ICantonRepository#undoValidate(java.lang.Long)
     */
    @Override
    public void undoValidate(SspCanton canton) {
        final Long cantonCode = canton.getCanton();
        final Long version = canton.getVersion();

        getHibernateTemplate().execute(session -> {
            Query<?> query1 = session.createQuery(
                    "update SspActivity set deliveryStatus = :deliveryStatus, validation_user = null, validation_date = null " +
                            "where canton = :cantonCode and version = :version and deliveryStatus = :validated"
            );
            query1.setParameter("deliveryStatus", CodegroupUtility.MEB_DATASTATUS_PREVALIDATED);
            query1.setParameter("cantonCode", cantonCode);
            query1.setParameter("version", version);
            query1.setParameter("validated", CodegroupUtility.MEB_DATASTATUS_VALIDATED);
            query1.executeUpdate();

            Query<?> query2 = session.createQuery(
                    "update SspPerson set deliveryStatus = :deliveryStatus, validation_user = null, validation_date = null " +
                            "where canton = :cantonCode and version = :version and deliveryStatus = :validated"
            );
            query2.setParameter("deliveryStatus", CodegroupUtility.MEB_DATASTATUS_PREVALIDATED);
            query2.setParameter("cantonCode", cantonCode);
            query2.setParameter("version", version);
            query2.setParameter("validated", CodegroupUtility.MEB_DATASTATUS_VALIDATED);
            query2.executeUpdate();

            Query<?> query3 = session.createQuery(
                    "update SspDelivery set deliveryStatus = :deliveryStatus, validation_user = null, validation_date = null " +
                            "where canton = :cantonCode and version = :version and deliveryStatus = :validated"
            );
            query3.setParameter("deliveryStatus", CodegroupUtility.MEB_DELIVERYSTATUS_PREVALIDATED);
            query3.setParameter("cantonCode", cantonCode);
            query3.setParameter("version", version);
            query3.setParameter("validated", CodegroupUtility.MEB_DELIVERYSTATUS_VALIDATED);
            query3.executeUpdate();

            return null;
        });

        canton.setDeliveryStatus(CodegroupUtility.MEB_CANTONSTATUS_DELIVERED);
        canton.setValidation_user(null);
        canton.setValidation_date(null);
        getHibernateTemplate().merge(canton);
        getHibernateTemplate().flush();
    }


    /* (non-Javadoc)
     * @see ch.bfs.meb.ssp.server.integration.repository.ICantonRepository#finalizeCanton(ch.bfs.meb.ssp.server.integration.dto.SspCanton, java.lang.String)
     */
    @Override
    public void finalizeCanton(SspCanton canton, String userEmail) {
        Date now = new Date();
        Long cantonCode = canton.getCanton();
        Long version = canton.getVersion();

        getHibernateTemplate().execute(session -> {
            Query<?> query1 = session.createQuery(
                    "update SspActivity set deliveryStatus = :deliveryStatus where canton = :cantonCode and version = :version and deliveryStatus = :validated"
            );
            query1.setParameter("deliveryStatus", CodegroupUtility.MEB_DATASTATUS_FINALIZED);
            query1.setParameter("cantonCode", cantonCode);
            query1.setParameter("version", version);
            query1.setParameter("validated", CodegroupUtility.MEB_DATASTATUS_VALIDATED);
            query1.executeUpdate();

            Query<?> query2 = session.createQuery(
                    "update SspPerson set deliveryStatus = :deliveryStatus where canton = :cantonCode and version = :version and deliveryStatus = :validated"
            );
            query2.setParameter("deliveryStatus", CodegroupUtility.MEB_DATASTATUS_FINALIZED);
            query2.setParameter("cantonCode", cantonCode);
            query2.setParameter("version", version);
            query2.setParameter("validated", CodegroupUtility.MEB_DATASTATUS_VALIDATED);
            query2.executeUpdate();

            Query<?> query3 = session.createQuery(
                    "update SspDelivery set deliveryStatus = :deliveryStatus where canton = :cantonCode and version = :version and deliveryStatus = :validated"
            );
            query3.setParameter("deliveryStatus", CodegroupUtility.MEB_DELIVERYSTATUS_FINALIZED);
            query3.setParameter("cantonCode", cantonCode);
            query3.setParameter("version", version);
            query3.setParameter("validated", CodegroupUtility.MEB_DELIVERYSTATUS_VALIDATED);
            query3.executeUpdate();

            return null;
        });

        canton.setDeliveryStatus(CodegroupUtility.MEB_CANTONSTATUS_FINALIZED);
        canton.setFinalisation_user(userEmail);
        canton.setFinalisation_date(now);
        getHibernateTemplate().merge(canton);
        getHibernateTemplate().flush();
    }


    /* (non-Javadoc)
     * @see ch.bfs.meb.ssp.server.integration.repository.ICantonRepository#undoFinalize(ch.bfs.meb.ssp.server.integration.dto.SspCanton)
     */
    @Override
    public void undoFinalize(SspCanton canton) {
        Long cantonCode = canton.getCanton();
        Long version = canton.getVersion();

        getHibernateTemplate().execute(session -> {
            Query<?> query1 = session.createQuery(
                    "update SspActivity set deliveryStatus = :deliveryStatus where canton = :cantonCode and version = :version and deliveryStatus = :finalized"
            );
            query1.setParameter("deliveryStatus", CodegroupUtility.MEB_DATASTATUS_VALIDATED);
            query1.setParameter("cantonCode", cantonCode);
            query1.setParameter("version", version);
            query1.setParameter("finalized", CodegroupUtility.MEB_DATASTATUS_FINALIZED);
            query1.executeUpdate();

            Query<?> query2 = session.createQuery(
                    "update SspPerson set deliveryStatus = :deliveryStatus where canton = :cantonCode and version = :version and deliveryStatus = :finalized"
            );
            query2.setParameter("deliveryStatus", CodegroupUtility.MEB_DATASTATUS_VALIDATED);
            query2.setParameter("cantonCode", cantonCode);
            query2.setParameter("version", version);
            query2.setParameter("finalized", CodegroupUtility.MEB_DATASTATUS_FINALIZED);
            query2.executeUpdate();

            Query<?> query3 = session.createQuery(
                    "update SspDelivery set deliveryStatus = :deliveryStatus where canton = :cantonCode and version = :version and deliveryStatus = :finalized"
            );
            query3.setParameter("deliveryStatus", CodegroupUtility.MEB_DELIVERYSTATUS_VALIDATED);
            query3.setParameter("cantonCode", cantonCode);
            query3.setParameter("version", version);
            query3.setParameter("finalized", CodegroupUtility.MEB_DELIVERYSTATUS_FINALIZED);
            query3.executeUpdate();

            return null;
        });

        canton.setDeliveryStatus(CodegroupUtility.MEB_CANTONSTATUS_VALIDATED);
        canton.setFinalisation_user(null);
        canton.setFinalisation_date(null);
        getHibernateTemplate().merge(canton);
        getHibernateTemplate().flush();
    }


    /* (non-Javadoc)
     * @see ch.bfs.meb.ssp.server.integration.repository.ICantonRepository#setCantonErrorsToDelete(java.lang.Long)
     */
    @Override
    public void setCantonErrorsToDelete(Long cantonId) {
        getHibernateTemplate().execute(session -> {
            Query<?> query = session.createQuery(
                    "update SspPlausiError set isToDelete = 1 where errorId in " +
                            "(select e.errorId from SspPlausiError e, SspPlausi p where e.cantonId = :cantonId and e.plausi = p.plausiId and p.objectLevel = :objectLevel)"
            );
            query.setParameter("cantonId", cantonId);
            query.setParameter("objectLevel", CodegroupUtility.SSP_OBJECTTYPE_CANTON);
            query.executeUpdate();
            return null;
        });
        getHibernateTemplate().flush();
    }


    /* (non-Javadoc)
     * @see ch.bfs.meb.ssp.server.integration.repository.ICantonRepository#deleteMarkedErrors(java.lang.Long)
     */
    @Override
    public void deleteMarkedErrors(Long cantonId) {
        getHibernateTemplate().execute(session -> {
            Query<?> query = session.createQuery(
                    "delete from SspPlausiError where isToDelete = 1 and cantonId = :cantonId"
            );
            query.setParameter("cantonId", cantonId);
            query.executeUpdate();
            return null;
        });
        getHibernateTemplate().flush();
    }


    /* (non-Javadoc)
     * @see ch.bfs.meb.ssp.server.integration.repository.ICantonRepository#updatePlausistatus(java.lang.Long)
     */
    @Override
    public void updatePlausistatus(Long cantonId) {
        getHibernateTemplate().execute(session -> {
            Query<?> query = session.createQuery(
                    "update SspCanton set plausiStatus = " +
                            "case when (select count(e) from SspPlausiError e where e.cantonId = :cantonId and e.deliveryId is null) = 0 then 2 " +
                            "when (select count(e) from SspPlausiError e where e.cantonId = :cantonId and e.deliveryId is null and e.isConfirmed = 0) > 0 then 1 " +
                            "else 3 end " +
                            "where cantonId = :cantonId"
            );
            query.setParameter("cantonId", cantonId);
            query.executeUpdate();
            return null;
        });
        getHibernateTemplate().flush();
    }

}
