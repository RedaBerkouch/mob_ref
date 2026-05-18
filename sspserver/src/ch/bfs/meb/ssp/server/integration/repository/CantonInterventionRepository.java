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
import org.hibernate.query.NativeQuery;
import org.hibernate.query.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate5.HibernateCallback;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;

import ch.bfs.meb.server.commons.integration.dto.CantonIntervention;
import ch.bfs.meb.ssp.server.integration.dto.SspCantonIntervention;
import ch.bfs.meb.util.CodegroupUtility;

/**
 * Repository for SspCantonInterventions.
 * 
 * @author $Author: dzw $
 * @version $Revision: 834 $
 */
public class CantonInterventionRepository extends HibernateDaoSupport implements ICantonInterventionRepository {
    /* (non-Javadoc)
     * @see ch.bfs.meb.ssp.server.integration.repository.ICantonInterventionRepository#findLastPlausireport(java.lang.Long)
     */
    @Override
    public SspCantonIntervention findLastPlausireport(final Long cantonId) {
        return getHibernateTemplate().execute(new HibernateCallback<SspCantonIntervention>() {
            @Override
            public SspCantonIntervention doInHibernate(Session session) throws HibernateException {
                String hql = "from SspCantonIntervention sci " +
                        "where sci.cantonId = :cantonId " +
                        "and sci.type = :type " +
                        "and sci.intervention_date = (select max(sci2.intervention_date) " +
                        "from SspCantonIntervention sci2 " +
                        "where sci2.cantonId = :cantonId " +
                        "and sci2.type = :type) " +
                        "order by sci.interventionId desc";

                Query<SspCantonIntervention> query = session.createQuery(hql, SspCantonIntervention.class);
                query.setParameter("cantonId", cantonId);
                query.setParameter("type", CodegroupUtility.SSP_CANTONINTERVENTIONTYPE_CREATE_PLAUSIREPORT);

                List<SspCantonIntervention> results = query.getResultList();
                return results.isEmpty() ? null : results.get(0);
            }
        });
    }

    @Override
    public SspCantonIntervention getInterventionById(Long interventionId) {
        return (SspCantonIntervention) getHibernateTemplate().get(SspCantonIntervention.class, interventionId);
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.ssp.server.integration.repository.ICantonInterventionRepository#getLastInterventionTypeForCanton(java.lang.Long)
     */
    @Override
    public Long getLastInterventionTypeForCanton(final Long cantonId) {
        return getHibernateTemplate().execute(session -> {
            Query<Long> query = session.createQuery(
                    "select type from SspCantonIntervention where cantonId = :cantonId " +
                            "and intervention_date = (select max(intervention_date) from SspCantonIntervention where cantonId = :cantonId) " +
                            "order by interventionId desc", Long.class);
            query.setParameter("cantonId", cantonId);
            List<Long> res = query.list();
            return res.isEmpty() ? null : res.get(0);
        });
    }


    /* (non-Javadoc)
     * @see ch.bfs.meb.ssp.server.integration.repository.ICantonInterventionRepository#existsInterventionOfType(java.lang.Long, java.lang.Long)
     */
    @Override
    public boolean existsInterventionOfType(final Long cantonId, final Long interventionType) {
        Long nrInterventions = getHibernateTemplate().execute(session -> {
            Query<Long> query = session.createQuery(
                    "select count(*) from SspCantonIntervention where cantonId = :cantonId and type = :type", Long.class);
            query.setParameter("cantonId", cantonId);
            query.setParameter("type", interventionType);
            return query.uniqueResult();
        });
        return nrInterventions > 0L;
    }


    @SuppressWarnings("unchecked")
    @Override
    public List<CantonIntervention> getInterventionsForCanton(final Long cantonId) {
        try (Session session = currentSession()) {
            String sql = "SELECT i.interventionId, i.cantonId, i.type, i.intervention_user, i.intervention_date, " +
                    " i.text, c.canton, c.version " +
                    " FROM Ssp_CantonInterventions i " +
                    " JOIN Ssp_Cantons c ON i.cantonId = c.cantonId " +
                    " WHERE i.cantonId = :cantonId " +
                    " ORDER BY i.intervention_date DESC, i.interventionId DESC";

            NativeQuery<Object[]> query = session.createNativeQuery(sql);
            query.setParameter("cantonId", cantonId);

            List<Object[]> queryResults = query.list();
            List<CantonIntervention> result = new ArrayList<>();

            for (Object[] row : queryResults) {
                CantonIntervention intervention = new CantonIntervention();
                intervention.setInterventionId(((BigDecimal) row[0]).longValue());
                intervention.setCantonId(((BigDecimal) row[1]).longValue());
                intervention.setType(((BigDecimal) row[2]).longValue());
                intervention.setIntervention_user((String) row[3]);
                intervention.setIntervention_date((Date) row[4]);
                intervention.setText((String) row[5]);

                // canton
                intervention.setCanton(((BigDecimal) row[6]).longValue());
                intervention.setVersion(((BigDecimal) row[7]).longValue());

                result.add(intervention);
            }

            return result;
        }
    }

    @Override
    public byte[] getPlausiReportFile(final Long interventionId, final String locale) {
        return getHibernateTemplate().execute(new HibernateCallback<byte[]>() {
            @Override
            public byte[] doInHibernate(Session session) throws HibernateException {
                // Validate locale
                if (!Locale.GERMAN.getLanguage().equals(locale) &&
                        !Locale.FRENCH.getLanguage().equals(locale) &&
                        !Locale.ITALIAN.getLanguage().equals(locale)) {
                    throw new IllegalArgumentException("Unknown language for getting plausi report");
                }

                // Construct the HQL query with named parameters
                String hql = String.format("select plausireport_%s from SspCantonIntervention where interventionId = :interventionId", locale);
                Query<byte[]> query = session.createQuery(hql, byte[].class);
                query.setParameter("interventionId", interventionId);

                // Execute the query and return the result
                byte[] result = query.uniqueResult();
                if (result == null) {
                    throw new RuntimeException("No report found for the given interventionId");
                }
                return result;
            }
        });
    }

    @Override
    public SspCantonIntervention insertIntervention(SspCantonIntervention intervention) {
        intervention = (SspCantonIntervention) getHibernateTemplate().merge(intervention);
        getHibernateTemplate().flush();
        return intervention;
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.ssp.server.integration.repository.ICantonInterventionRepository#updateIntervention(ch.bfs.meb.ssp.server.integration.dto.SspCantonIntervention)
     */
    @Override
    public SspCantonIntervention updateIntervention(SspCantonIntervention intervention) {
        intervention = (SspCantonIntervention) getHibernateTemplate().merge(intervention);
        getHibernateTemplate().flush();
        return intervention;
    }

    @Override
    public void deleteIntervention(SspCantonIntervention intervention) {
        getHibernateTemplate().delete(intervention);
        getHibernateTemplate().flush();
    }
}