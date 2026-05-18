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

import ch.bfs.meb.sba.server.integration.dto.SbaCantonIntervention;
import ch.bfs.meb.server.commons.integration.dto.CantonIntervention;
import ch.bfs.meb.util.CodegroupUtility;

/**
 * Repository for SbaCantonInterventions.
 * 
 * @author $Author: dzw $
 * @version $Revision: 834 $
 */
public class CantonInterventionRepository extends HibernateDaoSupport implements ICantonInterventionRepository {
    /* (non-Javadoc)
     * @see ch.bfs.meb.sba.server.integration.repository.ICantonInterventionRepository#findLastPlausireport(java.lang.Long)
     */
    @Override
    public SbaCantonIntervention findLastPlausireport(final Long cantonId) {
        return (SbaCantonIntervention) getHibernateTemplate().execute(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                javax.persistence.Query query = session.createQuery("from SbaCantonIntervention where cantonId= :cantonId and type= :type and intervention_date=(select max (intervention_date) from SbaCantonIntervention where cantonId= :cantonId and type= :type) order by interventionId desc");

                query.setParameter("cantonId", cantonId);
                query.setParameter("type", CodegroupUtility.SBA_CANTONINTERVENTIONTYPE_CREATE_PLAUSIREPORT);

                List<?> res = query.getResultList();
                if (res.isEmpty()) {
                    return null;
                } else {
                    return res.get(0);
                }
            }
        });
    }

    @Override
    public SbaCantonIntervention getInterventionById(Long interventionId) {
        return (SbaCantonIntervention) getHibernateTemplate().get(SbaCantonIntervention.class, interventionId);
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sba.server.integration.repository.ICantonInterventionRepository#getLastInterventionTypeForCanton(java.lang.Long)
     */
    @Override
    public Long getLastInterventionTypeForCanton(final Long cantonId) {
        return (Long) getHibernateTemplate().execute(new HibernateCallback() {
            @SuppressWarnings("unchecked")
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
javax.persistence.Query query = session.createQuery(
        "select type from SbaCantonIntervention where cantonId= :cantonId and intervention_date=(select max (intervention_date) from SbaCantonIntervention where cantonId= :cantonId) order by interventionId desc");
                query.setParameter("cantonId", cantonId);

                List res = query.getResultList();
                if (res.isEmpty()) {
                    return null;
                } else {
                    return res.get(0);
                }
            }
        });
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sba.server.integration.repository.ICantonInterventionRepository#existsInterventionOfType(java.lang.Long, java.lang.Long)
     */
    @Override
    public boolean existsInterventionOfType(final Long cantonId, final Long interventionType) {
        Long nrInterventions = (Long) getHibernateTemplate().execute(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                javax.persistence.Query query = session.createQuery("select count(*) from SbaCantonIntervention where cantonId= :cantonId and type= :type");
                query.setParameter("cantonId", cantonId);
                query.setParameter("type", interventionType);
                return query.getSingleResult();
            }
        });
        return nrInterventions > 0L;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<CantonIntervention> getInterventionsForCanton(final Long cantonId) {
        return (List<CantonIntervention>) getHibernateTemplate().execute(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                List<CantonIntervention> result = new ArrayList<>();

                Query<Object[]> query = session.createNativeQuery ("select i.interventionId, i.cantonId, i.type, i.intervention_user, i.intervention_date, "
                        + " i.text, c.canton, c.version " + " from Sba_CantonInterventions i, Sba_Cantons c "
                        + " where i.cantonId = c.cantonId and i.cantonId = :cantonId order by i.intervention_date desc, i.interventionId desc");
                query.setParameter("cantonId", cantonId);
                Iterator<Object[]> queryResults = query.list().iterator();
                while (queryResults.hasNext()) {
                    Object[] row = queryResults.next();

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

                Query<byte[]> query = session.createQuery("select plausireport_" + locale + " from SbaCantonIntervention where interventionId = :interventionId", byte[].class);
                query.setParameter("interventionId", interventionId);

                return query.uniqueResult();
            }
        });
    }


    @Override
    public SbaCantonIntervention insertIntervention(SbaCantonIntervention intervention) {
        intervention = (SbaCantonIntervention) getHibernateTemplate().merge(intervention);
        getHibernateTemplate().flush();
        return intervention;
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sba.server.integration.repository.ICantonInterventionRepository#updateIntervention(ch.bfs.meb.sba.server.integration.dto.SbaCantonIntervention)
     */
    @Override
    public SbaCantonIntervention updateIntervention(SbaCantonIntervention intervention) {
        intervention = (SbaCantonIntervention) getHibernateTemplate().merge(intervention);
        getHibernateTemplate().flush();
        return intervention;
    }

    @Override
    public void deleteIntervention(SbaCantonIntervention intervention) {
        getHibernateTemplate().delete(intervention);
        getHibernateTemplate().flush();
    }
}