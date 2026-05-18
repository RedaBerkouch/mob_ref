/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sdlserver

 */
package ch.bfs.meb.sdl.server.integration.repository;

import java.math.BigDecimal;
import java.util.*;

import org.hibernate.HibernateException;
import org.hibernate.query.NativeQuery;
import org.hibernate.query.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate5.HibernateCallback;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;

import ch.bfs.meb.sdl.server.integration.dto.SdlIntervention;
import ch.bfs.meb.server.commons.integration.dto.Intervention;
import ch.bfs.meb.util.CodegroupUtility;

import javax.persistence.TypedQuery;

/**
 * Repository for SdlInterventions.
 * 
 * @author $Author$
 * @version $Revision$
 */
public class InterventionRepository extends HibernateDaoSupport implements IInterventionRepository {
    /* (non-Javadoc)
     * @see ch.bfs.meb.sdl.server.integration.repository.IInterventionRepository#findLastUploadForDelivery(java.lang.Long)
     */
    @Override
    public SdlIntervention findLastUploadForDelivery(final Long deliveryId) {
        return (SdlIntervention) getHibernateTemplate().execute(new HibernateCallback() {
            @SuppressWarnings("unchecked")
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                TypedQuery<SdlIntervention> typedQuery = session.createQuery(
                        "from SdlIntervention where deliveryId=:deliveryId and type=:type and intervention_date=(select max (intervention_date) from SdlIntervention where deliveryId=:deliveryId and type=:type) order by interventionId desc", SdlIntervention.class);
                typedQuery.setParameter("deliveryId", deliveryId);
                typedQuery.setParameter("type", CodegroupUtility.MEB_INTERVENTIONTYPE_DELIVER_FILE);
                List<SdlIntervention> res = typedQuery.getResultList();
                if (res.isEmpty()) {
                    return null;
                } else {
                    return res.get(0);
                }
            }
        });
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sdl.server.integration.repository.IInterventionRepository#findLastPlausireport(java.lang.Long)
     */
    @Override
    public SdlIntervention findLastPlausireport(final Long deliveryId) {
        return (SdlIntervention) getHibernateTemplate().execute(new HibernateCallback() {
            @SuppressWarnings("unchecked")
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                org.hibernate.query.Query query = session.createQuery(
                        "from SdlIntervention where deliveryId= :deliveryId and type= :typeId and intervention_date=(select max (intervention_date) from SdlIntervention where deliveryId= :deliveryId2 and type= :typeId2) order by interventionId desc");
                query.setParameter("deliveryId", deliveryId);
                query.setParameter("typeId", CodegroupUtility.MEB_INTERVENTIONTYPE_CREATE_PLAUSIREPORT);
                query.setParameter("deliveryId2", deliveryId);
                query.setParameter("typeId2", CodegroupUtility.MEB_INTERVENTIONTYPE_CREATE_PLAUSIREPORT);
                List res = query.list();
                if (res.isEmpty()) {
                    return null;
                } else {
                    return res.get(0);
                }
            }
        });
    }

    @Override
    public SdlIntervention getInterventionById(Long interventionId) {
        return (SdlIntervention) getHibernateTemplate().get(SdlIntervention.class, interventionId);
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sdl.server.integration.repository.IInterventionRepository#getLastInterventionTypeForDelivery(java.lang.Long)
     */
    @Override
    public Long getLastInterventionTypeForDelivery(final Long deliveryId) {
        return (Long) getHibernateTemplate().execute(new HibernateCallback() {
            @SuppressWarnings("unchecked")
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                org.hibernate.query.Query<SdlIntervention> query = session.createQuery(
                        "select type from SdlIntervention where deliveryId= :deliveryId and intervention_date=(select max (intervention_date) from SdlIntervention where deliveryId= :deliveryId2) order by interventionId desc");
                query.setParameter("deliveryId", deliveryId);
                query.setParameter("deliveryId2", deliveryId);
                List<SdlIntervention> res = query.getResultList();
                if (res.isEmpty()) {
                    return null;
                } else {
                    return res.get(0);
                }
            }
        });
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sdl.server.integration.repository.IInterventionRepository#existsInterventionOfType(java.lang.Long, java.lang.Long)
     */
    @Override
    public boolean existsInterventionOfType(final Long deliveryId, final Long interventionType) {
        Long nrInterventions = (Long) getHibernateTemplate().execute(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                org.hibernate.query.Query query = session.createQuery(
                        "select count(*) from SdlIntervention where deliveryId= :deliveryId and type= :typeId");
                query.setParameter("deliveryId", deliveryId);
                query.setParameter("typeId", interventionType);
                return (Long) query.uniqueResult();
            }
        });
        return nrInterventions > 0L;
    }

    @Override
    public List<Intervention> getInterventionsForDelivery(final Long deliveryId) {
        return (List<Intervention>) getHibernateTemplate().execute((HibernateCallback<List<Intervention>>) session -> {
            List<Intervention> result = new ArrayList<>();

            NativeQuery query = session.createNativeQuery(
                    "select i.interventionId, i.deliveryId, i.type, i.intervention_user, i.intervention_date, " +
                            "i.report_de, i.report_fr, i.report_it, i.text, d.canton, d.version " +
                            "from Sdl_Interventions i, Sdl_Deliveries d " +
                            "where i.deliveryId = d.deliveryId and i.deliveryId = ?1 " +
                            "order by i.intervention_date desc, i.interventionId desc");

            query.setParameter(1, deliveryId);


            List<Object[]> queryResults = query.getResultList();

            for (Object[] row : queryResults) {
                Intervention intervention = new Intervention();

                intervention.setInterventionId(((BigDecimal) row[0]).longValue());
                intervention.setDeliveryId(((BigDecimal) row[1]).longValue());
                intervention.setType(((BigDecimal) row[2]).longValue());
                intervention.setIntervention_user((String) row[3]);
                intervention.setIntervention_date((Date) row[4]);
                intervention.setReport_de((String) row[5]);
                intervention.setReport_fr((String) row[6]);
                intervention.setReport_it((String) row[7]);
                intervention.setText((String) row[8]);
                intervention.setCanton(((BigDecimal) row[9]).longValue());
                intervention.setVersion(((BigDecimal) row[10]).longValue());

                result.add(intervention);
            }

            return result;
        });
    }

    public byte[] getDeliveryFile(final Long interventionId) {
        return (byte[]) getHibernateTemplate().execute(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                org.hibernate.query.Query<byte[]> query = session.createQuery(
                        "select deliveryfile from SdlIntervention where interventionId = :interventionId", byte[].class);
                query.setParameter("interventionId", interventionId);

                byte[] result = query.uniqueResult();
                if (result == null) {
                    // TODO replace by specific message
                    throw new RuntimeException("NO DELIVERY FILE FOUND!");
                }

                return result;
            }
        });
    }

    @Override
    public byte[] getPlausiReportFile(final Long interventionId, final String locale) {
        return (byte[]) getHibernateTemplate().execute(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                if (!Locale.GERMAN.getLanguage().equals(locale) && !Locale.FRENCH.getLanguage().equals(locale)
                        && !Locale.ITALIAN.getLanguage().equals(locale)) {
                    throw new RuntimeException("Unknown language for getting plausi report");
                }

                org.hibernate.query.Query<byte[]> query = session.createQuery(
                        "select plausireport_" + locale + " from SdlIntervention where interventionId = :interventionId", byte[].class);
                query.setParameter("interventionId", interventionId);

                return query.uniqueResult();
            }
        });
    }

    @Override
    public SdlIntervention insertIntervention(SdlIntervention intervention) {
        intervention = (SdlIntervention) getHibernateTemplate().merge(intervention);
        getHibernateTemplate().flush();
        return intervention;
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sdl.server.integration.repository.IInterventionRepository#updateIntervention(ch.bfs.meb.sdl.server.integration.dto.SdlIntervention)
     */
    @Override
    public SdlIntervention updateIntervention(SdlIntervention intervention) {
        intervention = (SdlIntervention) getHibernateTemplate().merge(intervention);
        getHibernateTemplate().flush();
        return intervention;
    }

    @Override
    public void deleteIntervention(SdlIntervention intervention) {
        getHibernateTemplate().delete(intervention);
        getHibernateTemplate().flush();
    }
}