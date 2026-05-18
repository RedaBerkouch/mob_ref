/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sdlserver

 */
package ch.bfs.meb.sdl.server.integration.repository;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.springframework.orm.hibernate5.HibernateCallback;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;
import org.springframework.stereotype.Repository;

import ch.bfs.meb.integration.dto.ResultBase;
import ch.bfs.meb.sdl.server.integration.dto.SdlCanton;
import ch.bfs.meb.sdl.server.integration.dto.SdlPlausiError;
import ch.bfs.meb.server.commons.util.IFilterUtility;
import ch.bfs.meb.util.CodegroupUtility;

import javax.transaction.Transactional;

@Repository
public class CantonRepository extends HibernateDaoSupport implements ICantonRepository {
    private IFilterUtility _filterUtility;

    public void setFilterUtility(IFilterUtility filterUtility) {
        _filterUtility = filterUtility;
    }


    @Override
    @Transactional
    public SdlCanton getCantonById(Long cantonId) {
        String hql = "from SdlCanton c left join fetch c.plausierrors pe left join fetch pe.plausi where c.cantonId= :cantonId";
        org.hibernate.query.Query<SdlCanton> query = currentSession().createQuery(hql, SdlCanton.class);
        query.setParameter("cantonId", cantonId);

        return query.uniqueResult();
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<SdlPlausiError> getTopPlausiErrorsForCanton(final Long cantonId) {
        return (List<SdlPlausiError>) getHibernateTemplate().execute(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                Query q = session.createQuery("from SdlCanton c where c.cantonId=:cantonId");
                q.setLong("cantonId", cantonId);
                if ((SdlCanton) q.uniqueResult() == null) {
                    return null;
                }

                q = session.createQuery(
                        "from SdlPlausiError as pe left join fetch pe.plausi where pe.deliveryId is null and pe.cantonId=:cantonId order by pe.isConfirmed, pe.plausi, pe.errorId");
                q.setLong("cantonId", cantonId);
                q.setMaxResults(ResultBase.MAX_NUMBER_ERRORS + 1);
                return q.list();
            }
        });
    }

    @Override
    public SdlCanton getCantonById(Long cantonId, LockMode lockMode) {
        return (SdlCanton) getHibernateTemplate().get(SdlCanton.class, cantonId, lockMode);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<SdlCanton> getCantons(final Long version, final Long canton) {
        return (List<SdlCanton>) getHibernateTemplate().execute(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                Query q;
                if (canton > 0L) {
                    q = session.createQuery("from SdlCanton as c where c.version=:version and c.canton=:canton");
                    q.setLong("canton", canton);
                } else {
                    q = session.createQuery("from SdlCanton as c where c.version=:version");
                }
                q.setLong("version", version);
                return q.list();
            }
        });
    }

    @Override
    public SdlCanton getCanton(final Long version, final Long canton) {
        return (SdlCanton) getHibernateTemplate().execute(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                Query q;
                q = session.createQuery("from SdlCanton as c where c.version=:version and c.canton=:canton");
                q.setLong("version", version);
                q.setLong("canton", canton);
                return q.uniqueResult();
            }
        });
    }

    @Override
    public SdlCanton getCantonWithConfigDeliveryAndSchoolByMaxVersion(final Long version, final Long canton) {
        return (SdlCanton) getHibernateTemplate().execute(new HibernateCallback() {
            @SuppressWarnings("unchecked")
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                Query q;
                q = session.createQuery(
                        "from SdlCanton as c, SdlConfigDelivery as cd where c.version <= :version and c.canton = :canton and c.version = cd.version and c.canton = cd.canton and cd.burSchools.size > 0 order by c.version desc");
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
    public SdlCanton insertCanton(SdlCanton canton) {
        canton = (SdlCanton) getHibernateTemplate().merge(canton);
        getHibernateTemplate().flush();
        return canton;
    }

    @Override
    public SdlCanton updateCanton(SdlCanton canton) {
        canton = (SdlCanton) getHibernateTemplate().merge(canton);
        getHibernateTemplate().flush();
        return canton;
    }

    @Override
    public void deleteCanton(SdlCanton canton) {
        getHibernateTemplate().delete(canton);
        getHibernateTemplate().flush();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Long getInitialVersion() {
        String queryString = "select {model.*} from Sdl_Cantons model where model.version = (select max(version) from Sdl_Cantons)";
        Query query = currentSession().createNativeQuery (queryString).addEntity("model", SdlCanton.class);

        List<SdlCanton> cantons = (List<SdlCanton>) query.list();

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
     * @see ch.bfs.meb.sdl.server.integration.repository.ICantonRepository#getNumberOfSchools(ch.bfs.meb.sdl.server.integration.dto.SdlCanton)
     */
    @Override
    public Long getNumberOfSchools(SdlCanton canton) {
        final Long cantonCode = canton.getCanton();
        final Long version = canton.getVersion();
        return (Long) getHibernateTemplate().execute(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                String hql = "select count(s) from SdlSchool s where s.canton= :cantonCode and s.version= :version";
                org.hibernate.query.Query query = session.createQuery(hql);
                query.setParameter("cantonCode", cantonCode);
                query.setParameter("version", version);

                return query.uniqueResult();
            }
        });
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sdl.server.integration.repository.ICantonRepository#allPlausibel(ch.bfs.meb.sdl.server.integration.dto.SdlCanton)
     */
    @Override
    public boolean allPlausibel(SdlCanton canton) {
        final Long cantonCode = canton.getCanton();
        final Long version = canton.getVersion();
        // check learner
        Long notPlausibel = (Long) getHibernateTemplate().execute(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                String hql = "select count(l) from SdlLearner l where l.canton= :cantonCode and l.version= :version and l.plausiStatus= :plausiStatus";
                org.hibernate.query.Query<?> query = session.createQuery(hql);
                query.setParameter("cantonCode", cantonCode);
                query.setParameter("version", version);
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
                String hql = "select count(c) from SdlClass c where c.canton= :cantonCode and c.version= :version and c.plausiStatus= :plausiStatus";
                org.hibernate.query.Query<?> query = session.createQuery(hql);
                query.setParameter("cantonCode", cantonCode);
                query.setParameter("version", version);
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
                String hql = "select count(s) from SdlSchool s where s.canton= :cantonCode and s.version= :version and s.plausiStatus= :plausiStatus";
                org.hibernate.query.Query<?> query = session.createQuery(hql);
                query.setParameter("cantonCode", cantonCode);
                query.setParameter("version", version);
                query.setParameter("plausiStatus", CodegroupUtility.MEB_PLAUSISTATUS_NOTVALID);

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
                String hql = "select count(d) from SdlDelivery d where d.canton= :cantonCode and d.version= :version and d.plausiStatus= :plausiStatus";
                org.hibernate.query.Query<?> query = session.createQuery(hql);
                query.setParameter("cantonCode", cantonCode);
                query.setParameter("version", version);
                query.setParameter("plausiStatus", CodegroupUtility.MEB_PLAUSISTATUS_NOTVALID);

                return query.uniqueResult();
            }
        });
        if (notPlausibel > 0L) {
            return false;
        }
        return !canton.getPlausiStatus().equals(CodegroupUtility.MEB_PLAUSISTATUS_NOTVALID);
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sdl.server.integration.repository.ICantonRepository#validateAll(java.lang.Long, java.lang.String)
     */
    @Override
    public void validateAll(SdlCanton canton, String username) {
        Date now = new Date();
        Long cantonCode = canton.getCanton();
        Long version = canton.getVersion();
        // prevalidation user and date have to be set in a second update statement because Hql cannot 
        // handle parameter in then branch of case statement (Hibernate bug HHH-4700)
        getHibernateTemplate().execute(new HibernateCallback<Void>() {
            public Void doInHibernate(Session session) {
                Query query = session.createQuery(
                        "update SdlLearner set deliveryStatus=:status, validation_user=:user, validation_date=:date " +
                                "where canton=:canton and version=:version and deliveryStatus in (:status1, :status2)");
                query.setParameter("status", CodegroupUtility.MEB_DATASTATUS_VALIDATED);
                query.setParameter("user", username);
                query.setParameter("date", now);
                query.setParameter("canton", cantonCode);
                query.setParameter("version", version);
                query.setParameter("status1", CodegroupUtility.MEB_DATASTATUS_DELIVERED);
                query.setParameter("status2", CodegroupUtility.MEB_DATASTATUS_PREVALIDATED);
                query.executeUpdate();
                return null;
            }
        });
        getHibernateTemplate().execute(new HibernateCallback<Void>() {
            @Override
            public Void doInHibernate(Session session) {
                Query query = session.createQuery(
                        "update SdlLearner set prevalidation_user = case when prevalidation_user is null then validation_user else prevalidation_user end, " +
                                "prevalidation_date = case when prevalidation_date is null then validation_date else prevalidation_date end " +
                                "where canton= :cantonCode and version= :version and deliveryStatus= :deliveryStatus"
                );
                query.setParameter("cantonCode", cantonCode);
                query.setParameter("version", version);
                query.setParameter("deliveryStatus", CodegroupUtility.MEB_DATASTATUS_VALIDATED);
                query.executeUpdate();
                return null;
            }
        });
        getHibernateTemplate().execute(new HibernateCallback<Void>() {
            @Override
            public Void doInHibernate(Session session) {
                Query query = session.createQuery(
                        "update SdlClass set deliveryStatus= :status, validation_user= :user, validation_date= :date " +
                                "where canton= :canton and version= :version and deliveryStatus in (:status1, :status2)"
                );
                query.setParameter("status", CodegroupUtility.MEB_DATASTATUS_VALIDATED);
                query.setParameter("user", username);
                query.setParameter("date", now);
                query.setParameter("canton", cantonCode);
                query.setParameter("version", version);
                query.setParameter("status1", CodegroupUtility.MEB_DATASTATUS_DELIVERED);
                query.setParameter("status2", CodegroupUtility.MEB_DATASTATUS_PREVALIDATED);

                query.executeUpdate();
                return null;
            }
        });
        getHibernateTemplate().execute(new HibernateCallback<Integer>() {
            @Override
            public Integer doInHibernate(Session session) throws HibernateException {
                Query query = session.createQuery(
                        "update SdlClass set prevalidation_user = case when prevalidation_user is null then validation_user else prevalidation_user end, prevalidation_date = case when prevalidation_date is null then validation_date else prevalidation_date end where canton = :cantonCode and version = :version and deliveryStatus = :deliveryStatus");

                query.setParameter("cantonCode", cantonCode);
                query.setParameter("version", version);
                query.setParameter("deliveryStatus", CodegroupUtility.MEB_DATASTATUS_VALIDATED);

                return query.executeUpdate();
            }
        });
        getHibernateTemplate().execute(new HibernateCallback<Object>() {
            public Object doInHibernate(Session session) {
                org.hibernate.query.Query query = session.createQuery(
                        "update SdlSchool set deliveryStatus=:deliveryStatus, validation_user=:username, validation_date=:now where canton=:cantonCode and version=:version and deliveryStatus in (:statusDelivered,:statusPrevalidated)");
                query.setParameter("deliveryStatus", CodegroupUtility.MEB_DATASTATUS_VALIDATED);
                query.setParameter("username", username);
                query.setParameter("now", now);
                query.setParameter("cantonCode", cantonCode);
                query.setParameter("version", version);
                query.setParameter("statusDelivered", CodegroupUtility.MEB_DATASTATUS_DELIVERED);
                query.setParameter("statusPrevalidated", CodegroupUtility.MEB_DATASTATUS_PREVALIDATED);

                return query.executeUpdate();
            }
        });
        getHibernateTemplate().execute(new HibernateCallback<Object>() {
            public Object doInHibernate(Session session) throws HibernateException {
                Query query = session.createQuery(
                        "update SdlSchool set prevalidation_user = case when prevalidation_user is null then validation_user else prevalidation_user end, prevalidation_date = case when prevalidation_date is null then validation_date else prevalidation_date end where canton = :canton and version = :version and deliveryStatus = :deliveryStatus");
                query.setParameter("canton", cantonCode);
                query.setParameter("version", version);
                query.setParameter("deliveryStatus", CodegroupUtility.MEB_DATASTATUS_VALIDATED);
                int result = query.executeUpdate();
                return result; // This result indicates the number of objects affected by the operation.
            }
        });
        getHibernateTemplate().execute(new HibernateCallback<Boolean>() {
            @Override
            public Boolean doInHibernate(Session session) throws HibernateException {
                Query query = session.createQuery("update SdlDelivery set deliveryStatus=:status, validation_user=:user, validation_date=:date where canton=:canton and version=:version and deliveryStatus in (:status1, :status2)");
                query.setParameter("status", CodegroupUtility.MEB_DELIVERYSTATUS_VALIDATED);
                query.setParameter("user", username);
                query.setParameter("date", now);
                query.setParameter("canton", cantonCode);
                query.setParameter("version", version);
                query.setParameter("status1", CodegroupUtility.MEB_DELIVERYSTATUS_DELIVERED);
                query.setParameter("status2", CodegroupUtility.MEB_DELIVERYSTATUS_PREVALIDATED);
                return query.executeUpdate() > 0;
            }
        });
        getHibernateTemplate().execute(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                Query query = session.createQuery(
                        "update SdlDelivery set prevalidation_user = case "
                                + "when prevalidation_user is null then validation_user "
                                + "else prevalidation_user end, prevalidation_date = case "
                                + "when prevalidation_date is null then validation_date "
                                + "else prevalidation_date end where canton = :cantonCode "
                                + "and version = :version and deliveryStatus = :status");

                query.setParameter("cantonCode", cantonCode);
                query.setParameter("version", version);
                query.setParameter("status", CodegroupUtility.MEB_DELIVERYSTATUS_VALIDATED);

                return query.executeUpdate();
            }
        });
        canton.setDeliveryStatus(CodegroupUtility.MEB_CANTONSTATUS_VALIDATED);
        canton.setValidation_user(username);
        canton.setValidation_date(now);
        getHibernateTemplate().merge(canton);
        getHibernateTemplate().flush();
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sdl.server.integration.repository.ICantonRepository#undoValidate(java.lang.Long)
     */
    @Override
    public void undoValidate(SdlCanton canton) {
        Long cantonCode = canton.getCanton();
        Long version = canton.getVersion();
        getHibernateTemplate().execute(new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException {
                Query query = session.createQuery(
                                "update SdlLearner set deliveryStatus=:deliveryStatus, validation_user=null, validation_date=null where canton=:canton and version=:version and deliveryStatus=:oldDeliveryStatus")
                        .setParameter("deliveryStatus", CodegroupUtility.MEB_DATASTATUS_PREVALIDATED)
                        .setParameter("canton", cantonCode)
                        .setParameter("version", version)
                        .setParameter("oldDeliveryStatus", CodegroupUtility.MEB_DATASTATUS_VALIDATED);
                return query.executeUpdate();
            }
        });
        getHibernateTemplate().execute(new HibernateCallback<Integer>() {
            @Override
            public Integer doInHibernate(Session session) throws HibernateException {
                String hql = "update SdlClass set deliveryStatus = :newStatus, " +
                        "validation_user = null, " +
                        "validation_date = null " +
                        "where canton = :canton " +
                        "and version = :version " +
                        "and deliveryStatus = :oldStatus";

                return session.createQuery(hql)
                        .setParameter("newStatus", CodegroupUtility.MEB_DATASTATUS_PREVALIDATED)
                        .setParameter("canton", cantonCode)
                        .setParameter("version", version)
                        .setParameter("oldStatus", CodegroupUtility.MEB_DATASTATUS_VALIDATED)
                        .executeUpdate();
            }
        });
        getHibernateTemplate().execute(new HibernateCallback<Object>() {
            public Object doInHibernate(Session session) throws HibernateException {
                String hql = "update SdlSchool set deliveryStatus = :deliStatus, validation_user = null, validation_date = null" +
                         " where canton = :canton_cd and version = :version_number and deliveryStatus = :existing_status";
                Query query = session.createQuery(hql);
                query.setParameter("deliStatus", CodegroupUtility.MEB_DATASTATUS_PREVALIDATED);
                query.setParameter("canton_cd", cantonCode);
                query.setParameter("version_number", version);
                query.setParameter("existing_status", CodegroupUtility.MEB_DATASTATUS_VALIDATED);
                return query.executeUpdate();
            }
        });
        getHibernateTemplate().execute(new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException {
                Query q = session.createQuery("update SdlDelivery set deliveryStatus = :newStatus, validation_user = null, validation_date = null where canton = :cantonCode and version = :version and deliveryStatus = :oldStatus");
                q.setParameter("newStatus", CodegroupUtility.MEB_DELIVERYSTATUS_PREVALIDATED);
                q.setParameter("cantonCode", cantonCode);
                q.setParameter("version", version);
                q.setParameter("oldStatus", CodegroupUtility.MEB_DELIVERYSTATUS_VALIDATED);
                return q.executeUpdate();
            }
        });
        canton.setDeliveryStatus(CodegroupUtility.MEB_CANTONSTATUS_DELIVERED);
        canton.setValidation_user(null);
        canton.setValidation_date(null);
        getHibernateTemplate().merge(canton);
        getHibernateTemplate().flush();
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sdl.server.integration.repository.ICantonRepository#finalizeCanton(ch.bfs.meb.sdl.server.integration.dto.SdlCanton, java.lang.String)
     */
    @Override
    public void finalizeCanton(SdlCanton canton, String userEmail) {
        Date now = new Date();
        Long cantonCode = canton.getCanton();
        Long version = canton.getVersion();
        getHibernateTemplate().execute(new HibernateCallback<Object>() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                Query query = session.createQuery("update SdlLearner set deliveryStatus=:newStatus where canton=:cantonCode and version=:version and deliveryStatus=:oldStatus");
                query.setParameter("newStatus", CodegroupUtility.MEB_DATASTATUS_FINALIZED);
                query.setParameter("cantonCode", cantonCode);
                query.setParameter("version", version);
                query.setParameter("oldStatus", CodegroupUtility.MEB_DATASTATUS_VALIDATED);
                int result = query.executeUpdate();
                return null;
            }
        });
        getHibernateTemplate().execute(new HibernateCallback<Object>() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                Query query = session.createQuery("update SdlClass set deliveryStatus=:newStatus where canton=:cantonCode and version=:version and deliveryStatus=:oldStatus");
                query.setParameter("newStatus", CodegroupUtility.MEB_DATASTATUS_FINALIZED);
                query.setParameter("cantonCode", cantonCode);
                query.setParameter("version", version);
                query.setParameter("oldStatus", CodegroupUtility.MEB_DATASTATUS_VALIDATED);
                int result = query.executeUpdate();
                return null;
            }
        });
        getHibernateTemplate().execute(new HibernateCallback<Object>() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                Query query = session.createQuery("update SdlSchool set deliveryStatus=:newStatus where canton=:cantonCode and version=:version and deliveryStatus=:oldStatus");
                query.setParameter("newStatus", CodegroupUtility.MEB_DATASTATUS_FINALIZED);
                query.setParameter("cantonCode", cantonCode);
                query.setParameter("version", version);
                query.setParameter("oldStatus", CodegroupUtility.MEB_DATASTATUS_VALIDATED);
                int result = query.executeUpdate();
                return null;
            }
        });
        getHibernateTemplate().execute(new HibernateCallback<Object>() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                Query query = session.createQuery("update SdlDelivery set deliveryStatus=:newStatus where canton=:cantonCode and version=:version and deliveryStatus=:oldStatus");
                query.setParameter("newStatus", CodegroupUtility.MEB_DELIVERYSTATUS_FINALIZED);
                query.setParameter("cantonCode", cantonCode);
                query.setParameter("version", version);
                query.setParameter("oldStatus", CodegroupUtility.MEB_DELIVERYSTATUS_VALIDATED);
                int result = query.executeUpdate();
                return null;
            }
        });
        canton.setDeliveryStatus(CodegroupUtility.MEB_CANTONSTATUS_FINALIZED);
        canton.setFinalisation_user(userEmail);
        canton.setFinalisation_date(now);
        getHibernateTemplate().merge(canton);
        getHibernateTemplate().flush();
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sdl.server.integration.repository.ICantonRepository#undoFinalize(ch.bfs.meb.sdl.server.integration.dto.SdlCanton)
     */
    @Override
    public void undoFinalize(SdlCanton canton) {
        Long cantonCode = canton.getCanton();
        Long version = canton.getVersion();
        getHibernateTemplate().execute(new HibernateCallback<Object>() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                Query query = session.createQuery("update SdlLearner set deliveryStatus=:newStatus where canton=:cantonCode and version=:version and deliveryStatus=:oldStatus");
                query.setParameter("newStatus", CodegroupUtility.MEB_DATASTATUS_VALIDATED);
                query.setParameter("cantonCode", cantonCode);
                query.setParameter("version", version);
                query.setParameter("oldStatus", CodegroupUtility.MEB_DATASTATUS_FINALIZED);
                int result = query.executeUpdate();
                return null;
            }
        });
        getHibernateTemplate().execute(new HibernateCallback<Object>() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                Query query = session.createQuery("update SdlClass set deliveryStatus=:newStatus where canton=:cantonCode and version=:version and deliveryStatus=:oldStatus");
                query.setParameter("newStatus", CodegroupUtility.MEB_DATASTATUS_VALIDATED);
                query.setParameter("cantonCode", cantonCode);
                query.setParameter("version", version);
                query.setParameter("oldStatus", CodegroupUtility.MEB_DATASTATUS_FINALIZED);
                int result = query.executeUpdate();
                return null;
            }
        });
        getHibernateTemplate().execute(new HibernateCallback<Object>() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                Query query = session.createQuery("update SdlSchool set deliveryStatus=:newStatus where canton=:cantonCode and version=:version and deliveryStatus=:oldStatus");
                query.setParameter("newStatus", CodegroupUtility.MEB_DATASTATUS_VALIDATED);
                query.setParameter("cantonCode", cantonCode);
                query.setParameter("version", version);
                query.setParameter("oldStatus", CodegroupUtility.MEB_DATASTATUS_FINALIZED);
                int result = query.executeUpdate();
                return null;
            }
        });
        getHibernateTemplate().execute(new HibernateCallback<Object>() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                Query query = session.createQuery("update SdlDelivery set deliveryStatus=:newStatus where canton=:cantonCode and version=:version and deliveryStatus=:oldStatus");
                query.setParameter("newStatus", CodegroupUtility.MEB_DELIVERYSTATUS_VALIDATED);
                query.setParameter("cantonCode", cantonCode);
                query.setParameter("version", version);
                query.setParameter("oldStatus", CodegroupUtility.MEB_DELIVERYSTATUS_FINALIZED);
                int result = query.executeUpdate();
                return null;
            }
        });
        canton.setDeliveryStatus(CodegroupUtility.MEB_CANTONSTATUS_VALIDATED);
        canton.setFinalisation_user(null);
        canton.setFinalisation_date(null);
        getHibernateTemplate().merge(canton);
        getHibernateTemplate().flush();
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sdl.server.integration.repository.ICantonRepository#setCantonErrorsToDelete(java.lang.Long)
     */
    @Override
    public void setCantonErrorsToDelete(Long cantonId) {
        getHibernateTemplate().execute(new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException {
                Query query = session.createQuery(
                        "update SdlPlausiError set isToDelete = 1 where errorId in " +
                                "(select e.errorId from SdlPlausiError e, SdlPlausi p where " +
                                "e.cantonId = :cantonId and e.plausi = p.plausiId and p.objectLevel = :objectLevel)"
                );
                query.setParameter("cantonId", cantonId);
                query.setParameter("objectLevel", CodegroupUtility.SDL_OBJECTTYPE_CANTON);
                int result = query.executeUpdate();
                return result; // return number of rows affected
            }
        });
        getHibernateTemplate().flush();
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sdl.server.integration.repository.ICantonRepository#deleteMarkedErrors(java.lang.Long)
     */
    @Override
    public void deleteMarkedErrors(Long cantonId) {
        getHibernateTemplate().execute(new HibernateCallback<Integer>() {
            public Integer doInHibernate(Session session) throws HibernateException {
                Query query = session.createQuery("delete from SdlPlausiError where isToDelete=:isToDelete and cantonId=:cantonId");
                query.setParameter("isToDelete", true);
                query.setParameter("cantonId", cantonId);
                return query.executeUpdate();
            }
        });
        getHibernateTemplate().flush();
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sdl.server.integration.repository.ICantonRepository#updatePlausistatus(java.lang.Long)
     */
    @Override
    public void updatePlausistatus(Long cantonId) {
        getHibernateTemplate().execute(session -> {
            String updateQuery = "update SdlCanton set plausiStatus = "
                    + "case "
                    + "when (select count(e) from SdlPlausiError e where e.cantonId = :cantonId and e.deliveryId is null) = 0 then 2 "
                    + "when (select count(e) from SdlPlausiError e where e.cantonId = :cantonId and e.deliveryId is null and e.isConfirmed = false) > 0 then 1 "
                    + "else 3 "
                    + "end "
                    + "where cantonId = :cantonId";
            Query query = session.createQuery(updateQuery);
            query.setParameter("cantonId", cantonId);
            return query.executeUpdate();
        });
        getHibernateTemplate().flush();
    }
}
