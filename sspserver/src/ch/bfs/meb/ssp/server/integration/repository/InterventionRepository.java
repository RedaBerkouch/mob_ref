/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sspserver

 */
package ch.bfs.meb.ssp.server.integration.repository;

import java.math.BigDecimal;
import java.util.*;

import org.hibernate.HibernateException;
import org.hibernate.query.Query;
import org.hibernate.Session;
import org.hibernate.query.NativeQuery;
import org.springframework.orm.hibernate5.HibernateCallback;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;

import ch.bfs.meb.server.commons.integration.dto.Intervention;
import ch.bfs.meb.ssp.server.integration.dto.SspIntervention;
import ch.bfs.meb.util.CodegroupUtility;

/**
 * Repository for SspInterventions.
 * 
 * @author $Author: dzw $
 * @version $Revision: 834 $
 */
public class InterventionRepository extends HibernateDaoSupport implements IInterventionRepository {
    /* (non-Javadoc)
     * @see ch.bfs.meb.ssp.server.integration.repository.IInterventionRepository#findLastUploadForDelivery(java.lang.Long)
     */
    @Override
    public SspIntervention findLastUploadForDelivery(final Long deliveryId) {
        return getHibernateTemplate().execute(new HibernateCallback<SspIntervention>() {
            @Override
            public SspIntervention doInHibernate(Session session) throws HibernateException {
                String hql = "from SspIntervention si where si.deliveryId = :deliveryId and si.type = :type " +
                        "and si.intervention_date = (" +
                        "    select max(si2.intervention_date) from SspIntervention si2 " +
                        "    where si2.deliveryId = :deliveryId and si2.type = :type" +
                        ") order by si.interventionId desc";

                Query<SspIntervention> query = session.createQuery(hql, SspIntervention.class);
                query.setParameter("deliveryId", deliveryId);
                query.setParameter("type", CodegroupUtility.MEB_INTERVENTIONTYPE_DELIVER_FILE);

                List<SspIntervention> results = query.getResultList();
                return results.isEmpty() ? null : results.get(0);
            }
        });
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.ssp.server.integration.repository.IInterventionRepository#findLastPlausireport(java.lang.Long)
     */
    @Override
    public SspIntervention findLastPlausireport(final Long deliveryId) {
        return getHibernateTemplate().execute(new HibernateCallback<SspIntervention>() {
            @Override
            public SspIntervention doInHibernate(Session session) throws HibernateException {
                String hql = "from SspIntervention si where si.deliveryId = :deliveryId and si.type = :type " +
                        "and si.intervention_date = (" +
                        "    select max(si2.intervention_date) from SspIntervention si2 " +
                        "    where si2.deliveryId = :deliveryId and si2.type = :type" +
                        ") order by si.interventionId desc";

                Query<SspIntervention> query = session.createQuery(hql, SspIntervention.class);
                query.setParameter("deliveryId", deliveryId);
                query.setParameter("type", CodegroupUtility.MEB_INTERVENTIONTYPE_CREATE_PLAUSIREPORT);

                List<SspIntervention> results = query.getResultList();
                return results.isEmpty() ? null : results.get(0);
            }
        });
    }


    @Override
    public SspIntervention getInterventionById(Long interventionId) {
        return (SspIntervention) getHibernateTemplate().get(SspIntervention.class, interventionId);
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.ssp.server.integration.repository.IInterventionRepository#getLastInterventionTypeForDelivery(java.lang.Long)
     */
    @Override
    public Long getLastInterventionTypeForDelivery(final Long deliveryId) {
        return getHibernateTemplate().execute(new HibernateCallback<Long>() {
            @Override
            public Long doInHibernate(Session session) throws HibernateException {
                String hql = "select si.type from SspIntervention si where si.deliveryId = :deliveryId " +
                        "and si.intervention_date = (" +
                        "    select max(si2.intervention_date) from SspIntervention si2 " +
                        "    where si2.deliveryId = :deliveryId" +
                        ") order by si.interventionId desc";

                Query<Long> query = session.createQuery(hql, Long.class);
                query.setParameter("deliveryId", deliveryId);

                List<Long> results = query.getResultList();
                return results.isEmpty() ? null : results.get(0);
            }
        });
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.ssp.server.integration.repository.IInterventionRepository#existsInterventionOfType(java.lang.Long, java.lang.Long)
     */
    @Override
    public boolean existsInterventionOfType(final Long deliveryId, final Long interventionType) {
        return getHibernateTemplate().execute(new HibernateCallback<Boolean>() {
            @Override
            public Boolean doInHibernate(Session session) throws HibernateException {
                String hql = "select count(*) from SspIntervention where deliveryId = :deliveryId and type = :interventionType";

                Query<Long> query = session.createQuery(hql, Long.class);
                query.setParameter("deliveryId", deliveryId);
                query.setParameter("interventionType", interventionType);

                Long count = query.uniqueResult();
                return count != null && count > 0;
            }
        });
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Intervention> getInterventionsForDelivery(final Long deliveryId) {
        return getHibernateTemplate().execute(new HibernateCallback<List<Intervention>>() {
            @Override
            public List<Intervention> doInHibernate(Session session) throws HibernateException {
                String sql = "select i.interventionId, i.deliveryId, i.type, i.intervention_user, i.intervention_date, " +
                        "i.report_de, i.report_fr, i.report_it, i.text, d.canton, d.version " +
                        "from Ssp_Interventions i join Ssp_Deliveries d on i.deliveryId = d.deliveryId " +
                        "where i.deliveryId = :deliveryId " +
                        "order by i.intervention_date desc, i.interventionId desc";

                NativeQuery<Object[]> query = session.createNativeQuery(sql);
                query.setParameter("deliveryId", deliveryId);

                List<Object[]> results = query.getResultList();
                List<Intervention> interventions = new ArrayList<>();

                for (Object[] row : results) {
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

                    interventions.add(intervention);
                }

                return interventions;
            }
        });
    }

    public byte[] getDeliveryFile(final Long interventionId) {
        return getHibernateTemplate().execute(session -> {
            Query<byte[]> query = session.createQuery(
                    "select deliveryfile from SspIntervention where interventionId = :interventionId", byte[].class);
            query.setParameter("interventionId", interventionId);

            byte[] result = query.uniqueResult();
            if (result == null) {
                throw new RuntimeException("NO DELIVERY FILE FOUND!");
            }

            return result;
        });
    }

    @Override
    public byte[] getPlausiReportFile(final Long interventionId, final String locale) {
        return getHibernateTemplate().execute(session -> {
            // Validate the locale parameter
            if (!Locale.GERMAN.getLanguage().equals(locale) &&
                    !Locale.FRENCH.getLanguage().equals(locale) &&
                    !Locale.ITALIAN.getLanguage().equals(locale)) {
                throw new RuntimeException("Unknown language for getting plausi report");
            }

            // Construct the query dynamically based on the locale
            String queryString = "select plausireport_" + locale + " from SspIntervention where interventionId = :interventionId";
            Query<byte[]> query = session.createQuery(queryString, byte[].class);
            query.setParameter("interventionId", interventionId);

            return query.uniqueResult();
        });
    }

    @Override
    public SspIntervention insertIntervention(SspIntervention intervention) {
        intervention = (SspIntervention) getHibernateTemplate().merge(intervention);
        getHibernateTemplate().flush();
        return intervention;
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.ssp.server.integration.repository.IInterventionRepository#updateIntervention(ch.bfs.meb.ssp.server.integration.dto.SspIntervention)
     */
    @Override
    public SspIntervention updateIntervention(SspIntervention intervention) {
        intervention = (SspIntervention) getHibernateTemplate().merge(intervention);
        getHibernateTemplate().flush();
        return intervention;
    }

    @Override
    public void deleteIntervention(SspIntervention intervention) {
        getHibernateTemplate().delete(intervention);
        getHibernateTemplate().flush();
    }
}