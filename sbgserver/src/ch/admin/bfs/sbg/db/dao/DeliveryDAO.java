package ch.admin.bfs.sbg.db.dao;

import java.math.BigDecimal;
import java.util.*;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.LockOptions;
import org.hibernate.query.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.criterion.Example;
import org.hibernate.criterion.Restrictions;
import org.springframework.orm.hibernate5.HibernateCallback;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StopWatch;

import ch.admin.bfs.sbg.psist.PersistDelivery;
import ch.admin.bfs.sbg.transfer.FilterContext;
import ch.admin.bfs.sbg.transfer.Macro;
import ch.admin.bfs.sbg.transfer.Plausierror;
import ch.admin.bfs.sbg.util.FilterUtility;
import ch.bfs.meb.util.CodegroupUtility;

/**
 * Data access object (DAO) for domain model class Delivery.
 *
 * @author MyEclipse - Hibernate Tools
 * @see ch.admin.bfs.sbg.transfer.SbgDelivery
 */
@Repository
public class DeliveryDAO extends BaseHibernateDAO {
    // property constants
    private static final String DELIVERYUSER = "deliveryuser";
    private static final String DELIVERYDATE = "deliverydate";

    public void save(PersistDelivery transientInstance) {
        currentSession().save(transientInstance);
    }

    @Transactional
    public PersistDelivery insertDelivery(PersistDelivery instance){
        instance = (PersistDelivery) getHibernateTemplate().merge(instance);
        getHibernateTemplate().flush();
        return instance;
    }

    public void delete(PersistDelivery persistentInstance) {
        currentSession().delete(persistentInstance);
    }

    public PersistDelivery updateDelivery(PersistDelivery delivery) {
        delivery = getHibernateTemplate().merge(delivery);
        getHibernateTemplate().flush();
        return delivery;
    }

    /**
     * Validiert eine Lieferung und setzt die Status der Personen und Events
     * entsprechend der Validierung.
     */
    public ValidationResult validateDelivery(PersistDelivery delivery, String username) {
        Date now = new Date();
        Long deliveryId = delivery.getDeliveryid();

        // @formatter:off

		// Falls valide Person, dann setze Status auf SBG_PERSONSTATUS_VALIDATED
		// Eine Person ist valide wenn:
		// - Person.status = SBG_PERSONSTATUS_DELIVERED
		// - Person.plausStatus = SBG_PLAUSISTATUS_VALID oder
		// SBG_PLAUSISTATUS_CONFIRMED
		// - keine Events der Person in Status SBG_PLAUSISTATUS_UNDEFINED oder
		// SBG_PLAUSISTATUS_NOTVALID
        getHibernateTemplate().execute(new HibernateCallback<Integer>() {
            public Integer doInHibernate(Session session) throws HibernateException {
                String hqlUpdate = "update PersistPerson p set p.status=:status, p.validationUser=:validationUser, p.validationDate=:validationDate "
                        + "where p.deliveryId = :deliveryId and p.status = :deliveredStatus and p.plausiStatus in (:validStatus, :confirmedStatus) and p.pid not in (select e.pid from SbgEvent e where e.pid=p.pid and e.plausiStatus in (:undefinedStatus,:notValidStatus))";
                return session.createQuery(hqlUpdate)
                        .setParameter("status", CodegroupUtility.SBG_PERSONSTATUS_VALIDATED)
                        .setParameter("validationUser", username)
                        .setParameter("validationDate", now)
                        .setParameter("deliveryId", deliveryId)
                        .setParameter("deliveredStatus", CodegroupUtility.SBG_PERSONSTATUS_DELIVERED)
                        .setParameter("validStatus", CodegroupUtility.SBG_PLAUSISTATUS_VALID)
                        .setParameter("confirmedStatus", CodegroupUtility.SBG_PLAUSISTATUS_CONFIRMED)
                        .setParameter("undefinedStatus", CodegroupUtility.SBG_PLAUSISTATUS_UNDEFINED)
                        .setParameter("notValidStatus", CodegroupUtility.SBG_PLAUSISTATUS_NOTVALID)
                        .executeUpdate();
            }
        });

		// Setze Event auf isValidated = true, wenn Event zu einer Person in
		// Status SBG_PERSONSTATUS_VALIDATED geh�rt
        getHibernateTemplate().execute(new HibernateCallback<Integer>() {
            public Integer doInHibernate(Session session) throws HibernateException {
                String hqlUpdate = "update SbgEvent set isValidated = :isValidated" +
                        " where pid in (select p.pid from PersistPerson p where p.deliveryId = :deliveryId " +
                        "and p.status = :status)";
                int updatedEntities = session.createQuery(hqlUpdate)
                        .setParameter("isValidated", true)
                        .setParameter("deliveryId", deliveryId)
                        .setParameter("status", CodegroupUtility.SBG_PERSONSTATUS_VALIDATED)
                        .executeUpdate();
                return updatedEntities;
            }
        });
				// @formatter:on

        Session session = getHibernateTemplate().getSessionFactory().getCurrentSession();

// Determine the number of valid persons: nrValid
        org.hibernate.query.Query<Long> q1 = session.createQuery("SELECT count(p) FROM PersistPerson p WHERE p.deliveryId=:deliveryId AND p.status=:status", Long.class);
        q1.setParameter("deliveryId", deliveryId);
        q1.setParameter("status", CodegroupUtility.SBG_PERSONSTATUS_VALIDATED);
        Long nrValid = q1.uniqueResult();

// Determine the number of invalid persons: nrNotValid
        org.hibernate.query.Query<Long> q2 = session.createQuery("SELECT count(p) FROM PersistPerson p WHERE p.deliveryId=:deliveryId AND p.status<>:status", Long.class);
        q2.setParameter("deliveryId", deliveryId);
        q2.setParameter("status", CodegroupUtility.SBG_PERSONSTATUS_VALIDATED);
        Long nrNotValid = q2.uniqueResult();

        // Delivery auf Status SBG_DELIVERYSTATUS_VALIDATED setzen, wenn alle
        // Personen valide
        if (delivery.getStatus().equals(CodegroupUtility.SBG_DELIVERYSTATUS_DELIVERED) && nrNotValid == 0
                && (delivery.getPlausistatus().equals(CodegroupUtility.SBG_PLAUSISTATUS_VALID)
                        || delivery.getPlausistatus().equals(CodegroupUtility.SBG_PLAUSISTATUS_CONFIRMED))) {
            delivery.setStatus(CodegroupUtility.SBG_DELIVERYSTATUS_VALIDATED);
            // TODO
            // MailService.getInstance().sendMail(new DeliveryValidationMail());
            merge(delivery);
        }

        getHibernateTemplate().flush();
        return new ValidationResult(nrValid, nrNotValid);
    }

    /**
     * UnValidiert eine Lieferung und setzt die Status der Personen und Events
     * entsprechend der Validierung.
     */
    public ValidationResult unValidateDelivery(PersistDelivery delivery, String username) {
        Date now = new Date();
        Long deliveryId = delivery.getDeliveryid();


        getHibernateTemplate().execute(new HibernateCallback<Object>() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                Query query = session.createQuery(
                        "update PersistPerson p set p.status=:status, p.validationUser=:username, p.validationDate=:now "
                                + "where p.deliveryId = :deliveryId and p.status = :statusValidated and p.plausiStatus in (:plausiStatusValid, :plausiStatusConfirmed) "
                                + "and p.pid not in (select e.pid from SbgEvent e where e.pid=p.pid and e.plausiStatus in (:plausiStatusUndefined, :plausiStatusNotValid))");

                query.setParameter("status", CodegroupUtility.SBG_PERSONSTATUS_DELIVERED);
                query.setParameter("username", username);
                query.setParameter("now", now);
                query.setParameter("deliveryId", deliveryId);
                query.setParameter("statusValidated", CodegroupUtility.SBG_PERSONSTATUS_VALIDATED);
                query.setParameter("plausiStatusValid", CodegroupUtility.SBG_PLAUSISTATUS_VALID);
                query.setParameter("plausiStatusConfirmed", CodegroupUtility.SBG_PLAUSISTATUS_CONFIRMED);
                query.setParameter("plausiStatusUndefined", CodegroupUtility.SBG_PLAUSISTATUS_UNDEFINED);
                query.setParameter("plausiStatusNotValid", CodegroupUtility.SBG_PLAUSISTATUS_NOTVALID);

                return query.executeUpdate();
            }
        });


        getHibernateTemplate().execute(new HibernateCallback<Object>() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                String queryStr = "update SbgEvent set isValidated=:validated where pid in (select p.pid from PersistPerson p where p.deliveryId = :deliveryId and p.status = :statusValidated)";
                Query query = session.createQuery(queryStr);
                query.setParameter("validated", false);
                query.setParameter("deliveryId", deliveryId);
                query.setParameter("statusValidated", CodegroupUtility.SBG_PERSONSTATUS_VALIDATED);

                return query.executeUpdate();
            }
        });


        // Bestimme Anzahl g�ltiger Personen: nrValid
        Query q = currentSession().createQuery("select count(*) from PersistPerson p where p.deliveryId=:deliveryId and p.status=:status");
        q.setParameter("deliveryId", deliveryId);
        q.setParameter("status", CodegroupUtility.SBG_PERSONSTATUS_VALIDATED);
        Long nrValid = (Long) q.uniqueResult();

        // Bestimme Anzahl ung�ltiger Personen: nrNotValid
        q = currentSession().createQuery("select count(*) from PersistPerson p where p.deliveryId=:deliveryId and p.status<>:status");
        q.setParameter("deliveryId", deliveryId);
        q.setParameter("status", CodegroupUtility.SBG_PERSONSTATUS_VALIDATED);
        Long nrNotValid = (Long) q.uniqueResult();

        // Delivery auf Status SBG_DELIVERYSTATUS_VALIDATED setzen, wenn alle
        // Personen valide
        if (delivery.getStatus().equals(CodegroupUtility.SBG_DELIVERYSTATUS_VALIDATED)) {
            delivery.setStatus(CodegroupUtility.SBG_DELIVERYSTATUS_DELIVERED);
            merge(delivery);
        }

        getHibernateTemplate().flush();
        return new ValidationResult(nrValid, nrNotValid);
    }

    @SuppressWarnings("unchecked")
    public List<PersistDelivery> getAllDeliveries(MacroDAO macroDAO) {
        // Consider internal canton filter for Datenlieferanten
        String deliverySubquery = FilterUtility.getPredefinedFilterSubquery(new FilterContext(), FilterUtility.DELIVERY_TABLE);
        String queryString = "select {model.*} from " + deliverySubquery + " model order by model.version desc, model.canton asc";

        Query query = currentSession().createNativeQuery (queryString).addEntity("model", PersistDelivery.class);
        List<PersistDelivery> result = query.list();

        loadAdditionalData(macroDAO, toMap(result), "");
        return result;
    }

    public PersistDelivery getDeliverieById(Long id) {
        return getHibernateTemplate().get(PersistDelivery.class, id);
    }

    @SuppressWarnings("unchecked")
    public List<PersistDelivery> getFilteredDeliveries(MacroDAO macroDAO, FilterContext filterContext) {

        StopWatch stopWatch = new StopWatch();
        stopWatch.start("Delivery Query");

        String whereSelection = FilterUtility.getWhereFilterSelection(filterContext, getStringColumns(), getDateColumns());
        String deliverySubquery = FilterUtility.getPredefinedFilterSubquery(filterContext, FilterUtility.DELIVERY_TABLE);
        String queryString = "select {model.*} from " + deliverySubquery + " model " + ((whereSelection.length() <= 0) ? "" : " where " + whereSelection + " ");

        Query query = currentSession().createNativeQuery (queryString).addEntity("model", PersistDelivery.class);
        List<PersistDelivery> result = query.list();

        stopWatch.stop();
        stopWatch.start("Additional Data");

        loadAdditionalData(macroDAO, toMap(result), whereSelection);
        stopWatch.stop();

        if (logger.isInfoEnabled()) {
            logger.info(stopWatch.prettyPrint());
        }

        return result;
    }

    private Map<Long, PersistDelivery> toMap(List<PersistDelivery> list) {
        Map<Long, PersistDelivery> result = new HashMap<>();

        for (PersistDelivery delivery : list) {
            result.put(delivery.getDeliveryid(), delivery);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private void loadAdditionalData(MacroDAO macroDAO, Map<Long, PersistDelivery> deliveryMap, String whereCondition) {

        StopWatch stopWatch = new StopWatch();

        // TODO schauen wie die ersten beiden Queries zu einem zusammengefasst werden können
        //        stopWatch.start("Person Event Plausi Query");
        //        
        //        Query personEventPlausiQuery = getSession().createNativeQuery (
        //                "select model.deliveryid, p.plausistatus, p.status, count(p.pid), e.plausiStatus, count(e.eventid) from Event e, Delivery model, Person p where e.pid=p.pid and p.deliveryId=model.deliveryid and p.isToDelete=0 and "
        //                        + whereCondition + " group by model.deliveryid,  p.PLAUSISTATUS, p.status, e.plausiStatus");
        //        List<Object[]> personEventPlausiList = personEventPlausiQuery.list();
        //
        //        Iterator<Object[]> personEventPlausiResults = personEventPlausiList.iterator();
        //        while (personEventPlausiResults.hasNext()) {
        //            Object[] row = personEventPlausiResults.next();
        //
        //            BigDecimal deliveryId = (BigDecimal) row[0];
        //            BigDecimal personPlausiStatus = (BigDecimal) row[1];
        //            BigDecimal personStatus = (BigDecimal) row[2];
        //            BigDecimal nrPersons = (BigDecimal) row[3];
        //
        //            BigDecimal eventPlausiStatus = (BigDecimal) row[4];
        //            BigDecimal nrEvents = (BigDecimal) row[5];
        //
        //            PersistDelivery delivery = deliveryMap.get(deliveryId.longValue());
        //            if (delivery != null) {
        //                delivery.addPlausiPersons(personPlausiStatus.longValue(), personStatus.longValue(), nrPersons.longValue());
        //                delivery.addPlausiEvents(eventPlausiStatus.longValue(), nrEvents.longValue());
        //            }
        //        }
        //
        //        stopWatch.stop();
        stopWatch.start("Person Plausi Query");

        Query personPlausiQuery = currentSession().createNativeQuery (
                "select model.deliveryid, p.plausiStatus, p.status, count (p.pid) from Person p, Delivery model where p.deliveryId=model.deliveryid and p.isToDelete=0 "
                        + (whereCondition.length() <= 0 ? "" : " and " + whereCondition) + " group by model.deliveryid, p.plausiStatus, p.status");
        List<Object[]> personPlausiList = personPlausiQuery.list();

        stopWatch.stop();
        stopWatch.start("Add Plausi Persons");

        Iterator<Object[]> personPlausiResults = personPlausiList.iterator();
        while (personPlausiResults.hasNext()) {
            Object[] row = personPlausiResults.next();
            BigDecimal deliveryId = (BigDecimal) row[0];
            BigDecimal personPlausiStatus = (BigDecimal) row[1];
            BigDecimal personStatus = (BigDecimal) row[2];
            BigDecimal nrPersons = (BigDecimal) row[3];

            PersistDelivery delivery = deliveryMap.get(deliveryId.longValue());
            if (delivery != null) {
                delivery.addPlausiPersons(personPlausiStatus.longValue(), personStatus.longValue(), nrPersons.longValue());
            }
        }

        stopWatch.stop();
        stopWatch.start("Event Plausi Query");

        Query eventPlausiQuery = currentSession().createNativeQuery (
                "select model.deliveryid, e.plausiStatus, count (e.eventid) from Event e, Delivery model, Person p where e.pid=p.pid and p.deliveryId=model.deliveryid and p.isToDelete=0 "
                        + (whereCondition.length() <= 0 ? "" : " and " + whereCondition) + " group by model.deliveryid, e.plausiStatus");
        List<Object[]> eventPlausiList = eventPlausiQuery.list();

        stopWatch.stop();
        stopWatch.start("Add Plausi Events");

        Iterator<Object[]> eventPlausiResults = eventPlausiList.iterator();
        while (eventPlausiResults.hasNext()) {
            Object[] row = eventPlausiResults.next();
            BigDecimal deliveryId = (BigDecimal) row[0];
            BigDecimal eventPlausiStatus = (BigDecimal) row[1];
            BigDecimal nrEvents = (BigDecimal) row[2];

            PersistDelivery delivery = deliveryMap.get(deliveryId.longValue());
            if (delivery != null) {
                delivery.addPlausiEvents(eventPlausiStatus.longValue(), nrEvents.longValue());
            }
        }

        stopWatch.stop();
        stopWatch.start("Plausi Error Query");

        List<Plausierror> errorList = new ArrayList<>();
        if (!deliveryMap.keySet().isEmpty()) {
            Query plausiErrorQuery = currentSession().createQuery(
                    "select p from Plausierror p where p.deliveryId in (:idList) and p.pid is null and p.isToDelete=false order by p.isConfirmed, p.plausiId, p.errorId");
            plausiErrorQuery.setParameterList("idList", deliveryMap.keySet());
            errorList = plausiErrorQuery.list();
        }

        stopWatch.stop();
        stopWatch.start("Set Plausi Errors");

        Map<Long, List<Plausierror>> errorMap = new HashMap<>();
        for (Plausierror error : errorList) {
            List<Plausierror> list = errorMap.get(error.getDeliveryId());
            if (list == null) {
                list = new ArrayList<>();
                errorMap.put(error.getDeliveryId(), list);
            }
            list.add(error);
        }

        if (!errorMap.isEmpty()) {

            List<Macro> allPlausis = macroDAO.findAllPlausis(false);

            for (PersistDelivery delivery : deliveryMap.values()) {
                List<Plausierror> deliveryErrorList = errorMap.get(delivery.getDeliveryid());

                if (deliveryErrorList != null) {
                    for (Plausierror error : deliveryErrorList) {
                        error.loadMacroData(allPlausis);
                    }

                    delivery.setPlausiErrors(deliveryErrorList);
                }
            }
        }

        stopWatch.stop();

        if (logger.isInfoEnabled()) {
            logger.info(stopWatch.prettyPrint());
        }

    }

    public Long nrPersonsValidated(Long deliveryId) {
        String queryString = "select count (pid) from PersistPerson where deliveryId = :deliveryId and status = :status";
        org.hibernate.query.Query<Long> query = currentSession().createQuery(queryString, Long.class);

        query.setParameter("deliveryId", deliveryId);
        query.setParameter("status", CodegroupUtility.SBG_PERSONSTATUS_VALIDATED);

        return query.uniqueResult();
    }

    public Long nrPersonsNotValidated(Long deliveryId) {
        String queryString = "select count (pid) from PersistPerson where deliveryId = :deliveryId and status <> :status";
        org.hibernate.query.Query<Long> query = currentSession().createQuery(queryString, Long.class);
        query.setParameter("deliveryId", deliveryId);
        query.setParameter("status", CodegroupUtility.SBG_PERSONSTATUS_VALIDATED);

        return query.uniqueResult();
    }

    public boolean canBeFinalized(Long deliveryId) {
        String queryString = "select count (errorId) from Plausierror where deliveryId = :deliveryId and pid is not null and isConfirmed = true";
        org.hibernate.query.Query<Long> query = currentSession().createQuery(queryString, Long.class);
        query.setParameter("deliveryId", deliveryId);
        return query.uniqueResult() == 0L;
    }

    @SuppressWarnings("unchecked")
    public PersistDelivery refreshDelivery(MacroDAO macroDAO, PersistDelivery delivery) {
        delivery.resetPlausiNumbers();

        // --- Query personnes ---
        Query<Object[]> personPlausiQuery = currentSession().createQuery(
                "select plausiStatus, status, count(pid) " +
                        "from PersistPerson " +
                        "where deliveryId = :deliveryId and isToDelete = false " +
                        "group by plausiStatus, status",
                Object[].class
        );
        personPlausiQuery.setParameter("deliveryId", delivery.getDeliveryid());

        List<Object[]> personPlausiResults = personPlausiQuery.getResultList();
        for (Object[] row : personPlausiResults) {
            Long personPlausiStatus = (Long) row[0];
            Long personStatus = (Long) row[1];
            Long nrPersons = (Long) row[2];

            delivery.addPlausiPersons(personPlausiStatus, personStatus, nrPersons);
        }

        // --- Query événements ---
        Query<Object[]> eventPlausiQuery = currentSession().createQuery(
                "select e.plausiStatus, count(e.eventid) " +
                        "from SbgEvent e, PersistPerson p " +
                        "where e.pid = p.pid and p.deliveryId = :deliveryId and p.isToDelete = false " +
                        "group by e.plausiStatus",
                Object[].class
        );
        eventPlausiQuery.setParameter("deliveryId", delivery.getDeliveryid());

        List<Object[]> eventPlausiResults = eventPlausiQuery.getResultList();
        for (Object[] row : eventPlausiResults) {
            Long eventPlausiStatus = (Long) row[0];
            Long nrEvents = (Long) row[1];

            delivery.addPlausiEvents(eventPlausiStatus, nrEvents);
        }

        // --- Query plausi errors ---
        Query<Plausierror> plausiErrorQuery = currentSession().createQuery(
                "select p from Plausierror p " +
                        "where p.deliveryId = :deliveryId and p.pid is null " +
                        "order by p.isConfirmed, p.plausiId, p.errorId",
                Plausierror.class
        );
        plausiErrorQuery.setParameter("deliveryId", delivery.getDeliveryid());

        List<Plausierror> errorList = plausiErrorQuery.getResultList();

        // Charger tous les macros en une seule fois si nécessaire
        List<Macro> allPlausis = null;
        if (!errorList.isEmpty()) {
            allPlausis = macroDAO.findAllPlausis(false);
        }

        for (Plausierror error : errorList) {
            error.loadMacroData(allPlausis);
        }
        delivery.setPlausiErrors(new ArrayList<>(errorList));

        return delivery;
    }


    public void setAllPersonsToDelete(Long deliveryId) {
        org.hibernate.query.Query query = currentSession().createQuery("update PersistPerson set isToDelete = true where deliveryId = :deliveryId");
        query.setParameter("deliveryId", deliveryId);
        query.executeUpdate();
    }

    public void markReplacedPersonsToDelete(Long deliveryId) {
        Query<?> query = currentSession().createQuery(
                "update PersistPerson p " +
                        "set isToDelete = true " +
                        "where p.deliveryId = :deliveryId " +
                        "and p.status > :statusImported " +
                        "and exists (" +
                        "   select p2 from PersistPerson p2 " +
                        "   where p2.deliveryId = p.deliveryId " +
                        "   and p2.idType = p.idType " +
                        "   and p2.id = p.id " +
                        "   and p2.status <= :statusImported2" +
                        ")"
        );

        query.setParameter("deliveryId", deliveryId);
        query.setParameter("statusImported", CodegroupUtility.SBG_PERSONSTATUS_IMPORTED);
        query.setParameter("statusImported2", CodegroupUtility.SBG_PERSONSTATUS_IMPORTED);

        query.executeUpdate();
    }

    public int updateActivePersons(Long deliveryId, Long newStatus, Long oldStatus) {
        org.hibernate.query.Query queryCount = currentSession()
                .createQuery("select count(*) from PersistPerson where isToDelete = false and deliveryId = :deliveryId and status = :oldStatus");
        queryCount.setParameter("deliveryId", deliveryId);
        queryCount.setParameter("oldStatus", oldStatus);
        Long result = (Long) queryCount.uniqueResult();

        org.hibernate.query.Query queryUpdate = currentSession()
                .createQuery("update PersistPerson set status = :newStatus where isToDelete = false and deliveryId = :deliveryId and status = :oldStatus");
        queryUpdate.setParameter("newStatus", newStatus);
        queryUpdate.setParameter("deliveryId", deliveryId);
        queryUpdate.setParameter("oldStatus", oldStatus);
        queryUpdate.executeUpdate();

        return result.intValue();
    }

    public void setDeliveryErrorsToDelete(Long deliveryId, boolean deliveryOnly) {
        if (deliveryOnly) {
            String queryString = "update Plausierror set isToDelete=1 where "
                    + "errorId in (select e.errorId from Plausierror e where e.deliveryId=:deliveryId and e.pid is null and e.eventid is null and e.macroid in "
                    + "(select p.macroid from Macro p where p.type=:typeSimplePlausi)) or "
                    + "errorId in (select e.errorId from Plausierror e where e.deliveryId=:deliveryId and e.macroid in "
                    + "(select distinct(p1.macroid) from Macro p1, Macro p2 where p1.type=:typeComplexPlausi and (p1.objecttype=:objTypeDelivery or (p1.name_d = p2.name_d and p2.objecttype=:objTypeDelivery))))";
            org.hibernate.query.NativeQuery sqlQuery = currentSession().createNativeQuery (queryString);
            sqlQuery.setParameter("deliveryId", deliveryId);
            sqlQuery.setParameter("typeSimplePlausi", CodegroupUtility.SBG_MACROTYPE_SIMPLEPLAUSI);
            sqlQuery.setParameter("typeComplexPlausi", CodegroupUtility.SBG_MACROTYPE_COMPLEXPLAUSI);
            sqlQuery.setParameter("objTypeDelivery", CodegroupUtility.SBG_OBJECTTYPE_DELIVERY);
            sqlQuery.executeUpdate();
        } else {
            String queryString = "update Plausierror set isToDelete=1 where errorId in "
                    + "(select e.errorId from Plausierror e where e.deliveryId=:deliveryId and e.macroid in "
                    + "(select distinct(p1.macroid) from Macro p1, Macro p2 where (p1.objecttype>=:objTypeDeliveryMin and p1.objecttype<=:objTypeDeliveryMax) or (p1.name_d = p2.name_d and p2.objecttype>=:objTypeDeliveryMin and p2.objecttype<=:objTypeDeliveryMax)))";
            org.hibernate.query.NativeQuery sqlQuery = currentSession().createNativeQuery (queryString);
            sqlQuery.setParameter("deliveryId", deliveryId);
            sqlQuery.setParameter("objTypeDeliveryMin", CodegroupUtility.SBG_OBJECTTYPE_DELIVERY);
            sqlQuery.setParameter("objTypeDeliveryMax", CodegroupUtility.SBG_OBJECTTYPE_EVENT);
            sqlQuery.executeUpdate();
        }
    }

    public void deleteMarkedObjects(Long deliveryId) {
        Session session = currentSession();

        // Supprimer les plausierrors marqués
        Query<?> q1 = session.createQuery(
                "delete from Plausierror where isToDelete = true and deliveryId = :deliveryId"
        );
        q1.setParameter("deliveryId", deliveryId);
        q1.executeUpdate();

        // Supprimer les plausierrors liés à des personnes marquées
        Query<?> q2 = session.createQuery(
                "delete from Plausierror " +
                        "where errorId in (" +
                        "   select e.errorId " +
                        "   from Plausierror e, PersistPerson p " +
                        "   where e.pid = p.pid and p.isToDelete = true and p.deliveryId = :deliveryId" +
                        ")"
        );
        q2.setParameter("deliveryId", deliveryId);
        q2.executeUpdate();

        // Supprimer les événements liés à des personnes marquées
        Query<?> q3 = session.createQuery(
                "delete from SbgEvent " +
                        "where eventid in (" +
                        "   select e.eventid " +
                        "   from SbgEvent e, PersistPerson p " +
                        "   where e.pid = p.pid and p.isToDelete = true and p.deliveryId = :deliveryId" +
                        ")"
        );
        q3.setParameter("deliveryId", deliveryId);
        q3.executeUpdate();

        // Supprimer les personnes marquées
        Query<?> q4 = session.createQuery(
                "delete from PersistPerson where isToDelete = true and deliveryId = :deliveryId"
        );
        q4.setParameter("deliveryId", deliveryId);
        q4.executeUpdate();

        session.flush();
    }


    public void restoreMarkedObjects(Long deliveryId) {
        // delete new plausierrors. This one does not delete plausierrors of objecttype delivery
        getHibernateTemplate().execute(new HibernateCallback<Void>() {
            @Override
            public Void doInHibernate(final Session session) throws HibernateException {
                String hql = "delete from Plausierror where errorId in (select e.errorId from Plausierror e, PersistPerson p where e.pid=p.pid and p.isToDelete=0 and p.deliveryId= :deliveryId and p.status= :status)";

                Query query = session.createQuery(hql);
                query.setParameter("deliveryId", deliveryId);
                query.setParameter("status", CodegroupUtility.SBG_PERSONSTATUS_IMPORTED);

                int result = query.executeUpdate();
                return null;
            }
        });
        // Jira-Ticket MEB-122 "Plausierror auf Stufe Lieferung werden bei 'Lieferung verwerfen' nicht gelöscht"
        getHibernateTemplate().execute(new HibernateCallback<Integer>() {
            @Override
            public Integer doInHibernate(Session session) throws HibernateException {
                Query query = session.createQuery(
                        "delete from Plausierror where errorId in (select e.errorId from Plausierror e, Macro m where e.plausiId=m.macroid and e.pid is null and e.isToDelete=false and e.deliveryId=:deliveryId and m.objecttype=:objectType)");
                query.setParameter("deliveryId", deliveryId);
                query.setParameter("objectType", CodegroupUtility.SBG_OBJECTTYPE_DELIVERY);
                return query.executeUpdate();
            }
        });
        // delete new events
        getHibernateTemplate().execute(new HibernateCallback<Integer>() {
            @Override
            public Integer doInHibernate(Session session) throws HibernateException {
                Query query = session.createQuery(
                        "delete from SbgEvent where eventid in (select e.eventid from SbgEvent e, PersistPerson p where e.pid = p.pid and p.isToDelete=0 and p.deliveryId=:deliveryId and p.status=:status)");
                query.setParameter("deliveryId", deliveryId);
                query.setParameter("status", CodegroupUtility.SBG_PERSONSTATUS_IMPORTED);
                return query.executeUpdate();
            }
        });
        // delete new persons
        getHibernateTemplate().execute(new HibernateCallback<Object>() {
            public Object doInHibernate(Session session) throws HibernateException {
                Query query = session.createQuery("delete from PersistPerson where isToDelete=false and deliveryId=:deliveryId and status=:status");
                query.setParameter("deliveryId", deliveryId);
                query.setParameter("status", CodegroupUtility.SBG_PERSONSTATUS_IMPORTED);
                return query.executeUpdate();
            }
        });

        // restore old data
        getHibernateTemplate().execute(new HibernateCallback() {
            public Object doInHibernate(Session sess) throws HibernateException {
                Query qry = sess.createQuery("update Plausierror set isToDelete = false where isToDelete = true and deliveryId = :deliveryId");
                qry.setParameter("deliveryId", deliveryId);
                return qry.executeUpdate();
            }
        });
        getHibernateTemplate().execute(new HibernateCallback<Integer>() {
            @Override
            public Integer doInHibernate(Session session) {
                Query query = session.createQuery("update PersistPerson set isToDelete = false where isToDelete = true and deliveryId = :deliveryId");
                query.setParameter("deliveryId", deliveryId);
                return query.executeUpdate();
            }
        });
        getHibernateTemplate().flush();
    }

    public void updateDeliveredObjects(final Long deliveryId) {
org.hibernate.query.Query query = currentSession().createQuery("update PersistPerson set status=:newStatus where isToDelete=false and deliveryId=:deliveryId and status=:oldStatus");
        query.setParameter("newStatus", CodegroupUtility.SBG_PERSONSTATUS_DELIVERED);
        query.setParameter("deliveryId", deliveryId);
        query.setParameter("oldStatus", CodegroupUtility.SBG_PERSONSTATUS_IMPORTED);
        query.executeUpdate();
    }

    public void updateAllPlausistatus(Long deliveryId) {
        Query query = currentSession().createQuery(
        "update SbgEvent pe set plausiStatus=case when (select count(e) from Plausierror e where e.eventId=pe.eventid)=0 then 2 when (select count(e) from Plausierror e where e.eventId=pe.eventid and e.isConfirmed=false)>0 then 1 else 3 end "
                + "where eventid in (select pe.eventid from SbgEvent pe, PersistPerson p where pe.pid=p.pid and p.isToDelete=false and p.deliveryId=:deliveryId)");
        query.setParameter("deliveryId", deliveryId);
        query.executeUpdate();

        query = currentSession().createQuery(
                "update PersistPerson p set plausiStatus=case when (select count(e) from Plausierror e where e.pid=p.pid and e.eventId is null)=0 then 2 when (select count(e) from Plausierror e where e.pid=p.pid and e.eventId is null and e.isConfirmed=false)>0 then 1 else 3 end where isToDelete=false and deliveryId=:deliveryId");
        query.setParameter("deliveryId", deliveryId);
        query.executeUpdate();

        query = currentSession().createQuery(
                "update PersistDelivery set plausistatus=case when (select count(e) from Plausierror e where e.isToDelete=false and e.deliveryId=:deliveryId and e.pid is null)=0 then 2 when (select count(e) from Plausierror e where e.isToDelete=false and e.deliveryId=:deliveryId and e.pid is null and e.isConfirmed=false)>0 then 1 else 3 end where deliveryid=:deliveryId");
        query.setParameter("deliveryId", deliveryId);
        query.executeUpdate();
    }

    public void deleteAll(Long deliveryId) {

        // delete plausierrors on all objects of delivery
        org.hibernate.query.Query<?> query = currentSession().createQuery("delete from Plausierror where deliveryId = :deliveryId");
        query.setParameter("deliveryId", deliveryId);
        query.executeUpdate();

        // delete events
        query = currentSession().createQuery("delete from SbgEvent where pid in (select p.pid from PersistPerson p where p.deliveryId = :deliveryId)");
        query.setParameter("deliveryId", deliveryId);
        query.executeUpdate();

        // delete persons
        query = currentSession().createQuery("delete from PersistPerson where deliveryId = :deliveryId");
        query.setParameter("deliveryId", deliveryId);
        query.executeUpdate();
    }

    /**
     * Mark delivery object as updated
     */
    public void lockDelivery(Long id) {
        currentSession().get(PersistDelivery.class, id, LockOptions.UPGRADE);
    }

    public PersistDelivery findById(Long id) {
        return (PersistDelivery) currentSession().get(PersistDelivery.class, id);
    }

    public PersistDelivery getDeliveryById(Long deliveryId, LockMode lockMode) {
        org.hibernate.Query<PersistDelivery> query = currentSession().createQuery(
                "from PersistDelivery d where d.deliveryid = :deliveryId", PersistDelivery.class
        );
        query.setParameter("deliveryId", deliveryId);
        query.setLockMode("d", lockMode);
        return query.uniqueResult();
    }


    @SuppressWarnings("unchecked")
    public List<PersistDelivery> findByExample(PersistDelivery instance) {
        return currentSession().createCriteria(PersistDelivery.class).add(Example.create(instance)).list();
    }

    public PersistDelivery merge(PersistDelivery detachedInstance) {
        detachedInstance = (PersistDelivery) currentSession().merge(detachedInstance);
        currentSession().flush();
        return detachedInstance;
    }

    /**
     * Returns all string columns of according db table
     */
    private List<String> getStringColumns() {
        ArrayList<String> stringColumns = new ArrayList<>();
        stringColumns.add(DELIVERYUSER);
        return stringColumns;
    }

    /**
     * Returns all date columns of according db table
     */
    private List<String> getDateColumns() {
        ArrayList<String> dateColumns = new ArrayList<>();
        dateColumns.add(DELIVERYDATE);
        return dateColumns;
    }

    public class ValidationResult {
        final Long nrValid;
        final Long nrNotValid;

        ValidationResult(Long nrValid, Long nrNotValid) {
            this.nrNotValid = nrNotValid;
            this.nrValid = nrValid;
        }

        public Long getNrValid() {
            return nrValid;
        }

        public Long getNrNotValid() {
            return nrNotValid;
        }

    }

    @Transactional
    public PersistDelivery getDeliveryByCantonAndVersion(Long canton, Long version) {
        Session session = currentSession();
        if (session != null) {
            Query query = session.createQuery("from PersistDelivery d where d.canton = :canton and d.version = :version");
            query.setParameter("canton", canton);
            query.setParameter("version", version);
            return (PersistDelivery) query.uniqueResult();
        }
        return null;
    }
}
