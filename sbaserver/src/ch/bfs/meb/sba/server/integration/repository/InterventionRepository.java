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

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate5.HibernateCallback;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;

import ch.bfs.meb.sba.server.integration.dto.SbaIntervention;
import ch.bfs.meb.server.commons.integration.dto.Intervention;
import ch.bfs.meb.util.CodegroupUtility;

/**
 * Repository for SbaInterventions.
 * 
 * @author $Author: dzw $
 * @version $Revision: 834 $
 */
public class InterventionRepository extends HibernateDaoSupport implements IInterventionRepository {
    /* (non-Javadoc)
     * @see ch.bfs.meb.sba.server.integration.repository.IInterventionRepository#findLastUploadForDelivery(java.lang.Long)
     */
    @Override
    public SbaIntervention findLastUploadForDelivery(final Long deliveryId) {
        return (SbaIntervention) getHibernateTemplate().execute(new HibernateCallback<SbaIntervention>() {
            @Override
            public SbaIntervention doInHibernate(Session session) throws HibernateException {
                String queryString = "from SbaIntervention where deliveryId=:deliveryId and type=:type and intervention_date=" +
                        "(select max (intervention_date) from SbaIntervention where deliveryId=:deliveryId2 and type=:type2) order by interventionId desc";
                org.hibernate.query.Query<SbaIntervention> query = session.createQuery(queryString, SbaIntervention.class);
                query.setParameter("deliveryId", deliveryId);
                query.setParameter("type", CodegroupUtility.MEB_INTERVENTIONTYPE_DELIVER_FILE);
                query.setParameter("deliveryId2", deliveryId);
                query.setParameter("type2", CodegroupUtility.MEB_INTERVENTIONTYPE_DELIVER_FILE);
                return query.stream().findFirst().orElse(null);
            }
        });
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sba.server.integration.repository.IInterventionRepository#findLastPlausireport(java.lang.Long)
     */
    @Override
    public SbaIntervention findLastPlausireport(final Long deliveryId) {
        return (SbaIntervention) getHibernateTemplate().execute(new HibernateCallback<SbaIntervention>() {
            @Override
            public SbaIntervention doInHibernate(Session session) throws HibernateException {
                String queryString = "from SbaIntervention where deliveryId=:deliveryId and type=:type and intervention_date=" +
                        "(select max (intervention_date) from SbaIntervention where deliveryId=:deliveryId2 and type=:type2) order by interventionId desc";
                org.hibernate.query.Query<SbaIntervention> query = session.createQuery(queryString, SbaIntervention.class);
                query.setParameter("deliveryId", deliveryId);
                query.setParameter("type", CodegroupUtility.MEB_INTERVENTIONTYPE_CREATE_PLAUSIREPORT);
                query.setParameter("deliveryId2", deliveryId);
                query.setParameter("type2", CodegroupUtility.MEB_INTERVENTIONTYPE_CREATE_PLAUSIREPORT);
                return query.uniqueResultOptional().orElse(null);
            }
        });
    }

    @Override
    public SbaIntervention getInterventionById(Long interventionId) {
        return (SbaIntervention) getHibernateTemplate().get(SbaIntervention.class, interventionId);
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sba.server.integration.repository.IInterventionRepository#getLastInterventionTypeForDelivery(java.lang.Long)
     */
    @Override
    public Long getLastInterventionTypeForDelivery(final Long deliveryId) {
        return (Long) getHibernateTemplate().execute(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                org.hibernate.query.Query<Long> query = session.createQuery(
                        "select type from SbaIntervention where deliveryId=:deliveryId and intervention_date=" +
                                "(select max (intervention_date) from SbaIntervention where deliveryId=:deliveryId2) order by interventionId desc",
                        Long.class);
                query.setParameter("deliveryId", deliveryId);
                query.setParameter("deliveryId2", deliveryId);
                return query.stream().findFirst().orElse(null);
            }
        });
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sba.server.integration.repository.IInterventionRepository#existsInterventionOfType(java.lang.Long, java.lang.Long)
     */
    @Override
    public boolean existsInterventionOfType(final Long deliveryId, final Long interventionType) {
        Long nrInterventions = (Long) getHibernateTemplate().execute(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                org.hibernate.query.Query<Long> query = session.createQuery("select count(*) from SbaIntervention where deliveryId=:deliveryId and type=:interventionType", Long.class);
                query.setParameter("deliveryId", deliveryId);
                query.setParameter("interventionType", interventionType);
                return query.uniqueResult();
            }
        });
        return nrInterventions > 0L;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Intervention> getInterventionsForDelivery(final Long deliveryId) {
        return (List<Intervention>) getHibernateTemplate().execute(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                List<Intervention> result = new ArrayList<>();

                Query<Object[]> query = session.createNativeQuery ("select i.interventionId, i.deliveryId, i.type, i.intervention_user, i.intervention_date, "
                        + " i.report_de, i.report_fr, i.report_it, i.text, d.canton, d.version " + " from Sba_Interventions i, Sba_Deliveries d "
                        + " where i.deliveryId = d.deliveryId and i.deliveryId = :deliveryId order by i.intervention_date desc, i.interventionId desc");
                query.setParameter("deliveryId", deliveryId);
                Iterator<Object[]> queryResults = query.list().iterator();
                while (queryResults.hasNext()) {
                    Object[] row = queryResults.next();

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

                    // delivery
                    intervention.setCanton(((BigDecimal) row[9]).longValue());
                    intervention.setVersion(((BigDecimal) row[10]).longValue());

                    result.add(intervention);
                }
                return result;
            }
        });
    }


    public byte[] getDeliveryFile(final Long interventionId) {
        return (byte[]) getHibernateTemplate().execute(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                Query<byte[]> query = session.createQuery("select deliveryfile from SbaIntervention where interventionId = :interventionId", byte[].class);
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

                Query<byte[]> query = session.createQuery("select plausireport_" + locale + " from SbaIntervention where interventionId = :interventionId", byte[].class);
                query.setParameter("interventionId", interventionId);

                return (byte[]) query.uniqueResult();
            }
        });
    }


    @Override
    public SbaIntervention insertIntervention(SbaIntervention intervention) {
        intervention = (SbaIntervention) getHibernateTemplate().merge(intervention);
        getHibernateTemplate().flush();
        return intervention;
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sba.server.integration.repository.IInterventionRepository#updateIntervention(ch.bfs.meb.sba.server.integration.dto.SbaIntervention)
     */
    @Override
    public SbaIntervention updateIntervention(SbaIntervention intervention) {
        intervention = (SbaIntervention) getHibernateTemplate().merge(intervention);
        getHibernateTemplate().flush();
        return intervention;
    }

    @Override
    public void deleteIntervention(SbaIntervention intervention) {
        getHibernateTemplate().delete(intervention);
        getHibernateTemplate().flush();
    }
}