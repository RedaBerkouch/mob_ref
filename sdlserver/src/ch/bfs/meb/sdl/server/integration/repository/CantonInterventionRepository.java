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
import org.hibernate.Session;
import org.hibernate.query.NativeQuery;
import org.hibernate.query.Query;
import org.springframework.orm.hibernate5.HibernateCallback;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;

import ch.bfs.meb.sdl.server.integration.dto.SdlCantonIntervention;
import ch.bfs.meb.server.commons.integration.dto.CantonIntervention;
import ch.bfs.meb.util.CodegroupUtility;

/**
 * Repository for SdlCantonInterventions.
 * 
 * @author $Author: dzw $
 * @version $Revision: 834 $
 */
public class CantonInterventionRepository extends HibernateDaoSupport implements ICantonInterventionRepository {
    /* (non-Javadoc)
     * @see ch.bfs.meb.sdl.server.integration.repository.ICantonInterventionRepository#findLastPlausireport(java.lang.Long)
     */
    @Override
    public SdlCantonIntervention findLastPlausireport(final Long cantonId) {
        return (SdlCantonIntervention) getHibernateTemplate().execute(new HibernateCallback() {

            @Override
            public Object doInHibernate(Session session) throws HibernateException {
org.hibernate.query.Query<SdlCantonIntervention> query = session.createQuery(
        "from SdlCantonIntervention where cantonId=:cantonId and type=:type and intervention_date=(select max (intervention_date) from SdlCantonIntervention where cantonId=:cantonId and type=:type) order by interventionId desc",
        SdlCantonIntervention.class);
                query.setParameter("cantonId", cantonId);
                query.setParameter("type", CodegroupUtility.SDL_CANTONINTERVENTIONTYPE_CREATE_PLAUSIREPORT);

                List<SdlCantonIntervention> res = query.list();
                if (res.isEmpty()) {
                    return null;
                } else {
                    return res.get(0);
                }
            }
        });
    }

    @Override
    public SdlCantonIntervention getInterventionById(Long interventionId) {
        return (SdlCantonIntervention) getHibernateTemplate().get(SdlCantonIntervention.class, interventionId);
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sdl.server.integration.repository.ICantonInterventionRepository#getLastInterventionTypeForCanton(java.lang.Long)
     */
    @Override
    public Long getLastInterventionTypeForCanton(final Long cantonId) {
        return (Long) getHibernateTemplate().execute(new HibernateCallback() {
            @SuppressWarnings("unchecked")
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
org.hibernate.query.Query<Integer> query = session.createQuery(
        "select type from SdlCantonIntervention where cantonId=:cantonId and intervention_date=(select max (intervention_date) from SdlCantonIntervention where cantonId=:cantonId) order by interventionId desc",
        Integer.class);
                query.setParameter("cantonId", cantonId);

                List<Integer> res = query.list();
                if (res.isEmpty()) {
                    return null;
                } else {
                    return res.get(0);
                }
            }
        });
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sdl.server.integration.repository.ICantonInterventionRepository#existsInterventionOfType(java.lang.Long, java.lang.Long)
     */
    @Override
    public boolean existsInterventionOfType(final Long cantonId, final Long interventionType) {
        Long nrInterventions = (Long) getHibernateTemplate().execute(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
org.hibernate.query.Query<Long> query = session.createQuery(
        "select count(*) from SdlCantonIntervention where cantonId=:cantonId and type=:interventionType",
        Long.class
);
                query.setParameter("cantonId", cantonId);
                query.setParameter("interventionType", interventionType);

                return query.uniqueResult();
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

                String sqlQuery = "select i.interventionId, i.cantonId, i.type, i.intervention_user, i.intervention_date, " +
                        " i.text, c.canton, c.version " +
                        " from Sdl_CantonInterventions i, Sdl_Cantons c " +
                        " where i.cantonId = c.cantonId and i.cantonId = :cantonId order by i.intervention_date desc, i.interventionId desc";

                NativeQuery<?> query = session.createNativeQuery(sqlQuery);
                query.setParameter("cantonId", cantonId);

                Iterator<?> queryResults = query.list().iterator();
                while (queryResults.hasNext()) {
                    Object[] row = (Object[]) queryResults.next();

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

                Query<byte[]> query = session.createQuery("select plausireport_" + locale + " from SdlCantonIntervention where interventionId = :interventionId", byte[].class);
                query.setParameter("interventionId", interventionId);

                return query.uniqueResult();
            }
        });
    }

    @Override
    public SdlCantonIntervention insertIntervention(SdlCantonIntervention intervention) {
        intervention = (SdlCantonIntervention) getHibernateTemplate().merge(intervention);
        getHibernateTemplate().flush();
        return intervention;
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sdl.server.integration.repository.ICantonInterventionRepository#updateIntervention(ch.bfs.meb.sdl.server.integration.dto.SdlCantonIntervention)
     */
    @Override
    public SdlCantonIntervention updateIntervention(SdlCantonIntervention intervention) {
        intervention = (SdlCantonIntervention) getHibernateTemplate().merge(intervention);
        getHibernateTemplate().flush();
        return intervention;
    }

    @Override
    public void deleteIntervention(SdlCantonIntervention intervention) {
        getHibernateTemplate().delete(intervention);
        getHibernateTemplate().flush();
    }
}