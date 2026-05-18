/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sdlserver

 */
package ch.bfs.meb.sdl.server.integration.repository;

import ch.bfs.meb.exception.MebUncheckedException;
import ch.bfs.meb.exception.MebUncheckedNotMonitoredException;
import ch.bfs.meb.integration.dto.ResultBase;
import ch.bfs.meb.sdl.server.integration.dto.SdlClass;
import ch.bfs.meb.sdl.server.integration.dto.SdlLearner;
import ch.bfs.meb.sdl.server.integration.dto.SdlPlausiError;
import ch.bfs.meb.sdl.server.integration.dto.SdlSchool;
import ch.bfs.meb.security.MebUser;
import ch.bfs.meb.server.commons.integration.dto.FilterContext;
import ch.bfs.meb.server.commons.integration.dto.SortContext;
import ch.bfs.meb.server.commons.util.IFilterUtility;
import ch.bfs.meb.util.BigDecimalUtils;
import ch.bfs.meb.util.CodegroupUtility;
import ch.bfs.meb.util.SecurityConstants;
import ch.bfs.meb.util.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.NonUniqueResultException;
import org.hibernate.query.NativeQuery;
import org.hibernate.query.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate5.HibernateCallback;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Repository for SdlSchools.
 *
 * @author $Author$
 * @version $Revision$
 */
public class SchoolRepository extends HibernateDaoSupport implements ISchoolRepository {
    // property constants
    public static final String DELIVERYCODE = "deliveryCode";
    public static final String IDTYPE = "idType";
    public static final String ID = "id";
    public static final String CREATION_USER = "creation_user";
    public static final String MODIFICATION_USER = "modification_user";
    public static final String PREVALIDATION_USER = "prevalidation_user";
    public static final String VALIDATION_USER = "validation_user";
    public static final String USERTEXT = "userText";
    //
    public static final String CREATION_DATE = "creation_date";
    public static final String MODIFICATION_DATE = "modification_date";
    public static final String PREVALIDATION_DATE = "prevalidation_date";
    public static final String VALIDATION_DATE = "validation_date";
    //
    public static final String DELIVERYSTATUS = "deliveryStatus";
    public static final String PLAUSISTATUS = "plausiStatus";
    //
    public static final String SCHOOL_LABEL = "label";
    public static final String IS_PUBLIC_SCHOOL = "char_publ_flg";
    public static final String IS_PRIVATE_SUBSIDISED_SCHOOL = "char_priv_sub_flg";
    public static final String IS_PRIVATE_NOT_SUBSIDISED_SCHOOL = "char_priv_no_sub_flg";
    public static final String IS_SPECIAL_SCHOOL = "inst_typ_blp_flg";
    public static final String BURSCHOOLLABEL_CLASS_ATTRIBUTE_NAME = "burSchoolLabel";
    public static final String IS_SPECIAL_SCHOOL_CLASS_ATTRIBUTE_NAME = "isSpecialSchool";
    //
    private static final Map<String, String> COLUMN_MAPPING = new HashMap<String, String>();

    static {
        COLUMN_MAPPING.put(BURSCHOOLLABEL_CLASS_ATTRIBUTE_NAME, SCHOOL_LABEL);
        COLUMN_MAPPING.put(IS_SPECIAL_SCHOOL_CLASS_ATTRIBUTE_NAME, IS_SPECIAL_SCHOOL);
    }

    private static final Log LOGGER = LogFactory.getLog(SchoolRepository.class);
    private IFilterUtility _filterUtility;

    public void setFilterUtility(IFilterUtility filterUtility) {
        _filterUtility = filterUtility;
    }

    @Override
    public SdlSchool getSchoolById(Long schoolId) {
        Query query = currentSession().createQuery(
                "from SdlSchool s left join fetch s.plausierrors pe left join fetch pe.plausi where s.schoolId=:schoolId");
        query.setParameter("schoolId", schoolId);
        return addBurSchoolInfo((SdlSchool) query.uniqueResult());
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<SdlPlausiError> getTopPlausiErrorsForSchool(final Long schoolId) {
        return (List<SdlPlausiError>) getHibernateTemplate().execute(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                Query q = session.createQuery("from SdlSchool s where s.schoolId=:schoolId");
                q.setLong("schoolId", schoolId);
                if ((SdlSchool) q.uniqueResult() == null) {
                    return null;
                }

                q = session.createQuery(
                        "from SdlPlausiError as pe left join fetch pe.plausi where pe.classId is null and pe.schoolId=:schoolId order by pe.isConfirmed, pe.plausi, pe.errorId");
                q.setLong("schoolId", schoolId);
                q.setMaxResults(ResultBase.MAX_NUMBER_ERRORS + 1);
                return q.list();
            }
        });
    }

    @Override
    public void clearSchoolFromCache(SdlSchool school) {
        getHibernateTemplate().evict(school);
        for (SdlPlausiError error : school.getPlausierrors()) {
            getHibernateTemplate().evict(error);
        }
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sdl.server.integration.repository.ISchoolRepository#getDeliveryStatus(java.lang.Long)
     */
    @Override
    public Long getDeliveryStatus(Long schoolId) {
        Query query = currentSession().createQuery(
            "select deliveryStatus from SdlSchool where schoolId=:schoolId");
        query.setParameter("schoolId", schoolId);

        return (Long) query.uniqueResult();
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sdl.server.integration.repository.ISchoolRepository#getConfigDeliveryCode(java.lang.Long)
     */
    @Override
    public String getConfigDeliveryCode(Long schoolId) {
     Query query = currentSession().createQuery(
                "select configDeliveryCode from SdlSchool where schoolId=:schoolId");
        query.setParameter("schoolId", schoolId);

        return (String) query.uniqueResult();
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<SdlSchool> getSchools(int start, int buffer, SortContext sortContext, FilterContext filterContext, Long version, Long canton) {
        List<String> joinColumns = getJoinColumns();
        String queryString;
        String sortColumn = _filterUtility.adaptColumnName(sortContext.getSortColumn(), "model", joinColumns, "s", COLUMN_MAPPING, getUnderscoreColumns());
        String codegroupId = getCodegroupId(sortColumn);
        String whereSelection = _filterUtility.getWhereFilterSelection(filterContext, "model", joinColumns, "s", COLUMN_MAPPING, getStringColumns(),
                getDateColumns(), getUnderscoreColumns());
        String schoolSubquery = _filterUtility.getPredefinedFilterSubquery(filterContext, SecurityFilters.SDL_SCHOOLS_TABLE, true);

        if (whereSelection.length() > 0) {
            whereSelection += " and ";
        }
        whereSelection += "model.isToDelete <> 1";

        if (canton > 0L) {
            whereSelection += " and model.canton=" + canton;
        }

        if (version != null) {
            whereSelection += " and model.version=" + version;
        }

        if (sortContext.getLocale() != null && codegroupId != null) {
            // sort according to locale dependent code texts
            String mainLocale = sortContext.getLocale();
            queryString = "select distinct model.schoolId, s.label, s.char_publ_flg, s.char_priv_sub_flg, s.char_priv_no_sub_flg, s." + IS_SPECIAL_SCHOOL + ", "
                    + "(case when " + sortColumn + " is null then '' " + "      when meb_cg.code is null then to_char(" + sortColumn + ") "
                    + "      else  meb_cg.codetext end) sorttext " + " from " + schoolSubquery + " model "
                    + " left outer join schools s on (model.idType = 'CH.BUR' and model.id = s.burNr) or (model.idType <> 'CH.BUR' and model.id = s.cantonalcode_sdl)"
                    + " left outer join  " + " (select cg1.* from Codegroups cg1, "
                    + "   (select codegroupid, code, language, max(validFromYear) validFromYear from Codegroups where codegroupId = '" + codegroupId
                    + "' and language = '" + mainLocale + "' group by codegroupid, code, language" + "   ) cg2"
                    + "  where cg1.codegroupId = cg2.codegroupId and cg1.code = cg2.code and cg1.language = cg2.language and cg1.validFromYear = cg2.validFromYear   "
                    + " ) meb_cg on meb_cg.code = " + sortColumn + " where " + whereSelection + " order by sorttext "
                    + ((sortContext.getAscSortOrder()) ? "asc" : "desc") + ", model.schoolId asc";
        } else {
            queryString = "select distinct model.schoolId, s.label, s.char_publ_flg, s.char_priv_sub_flg, s.char_priv_no_sub_flg, s." + IS_SPECIAL_SCHOOL + ", "
                    + sortColumn + " sorttext " + " from " + schoolSubquery + " model "
                    + " left outer join schools s on (model.idType = 'CH.BUR' and model.id = s.burNr) or (model.idType <> 'CH.BUR' and model.id = s.cantonalcode_sdl) "
                    + "where " + whereSelection + " order by sorttext " + ((sortContext.getAscSortOrder()) ? "asc" : "desc") + ", model.schoolId asc";
        }

        // SQL-Query: Because of HQL-Problems with from-subqueries..
        Query query= currentSession().createNativeQuery (queryString);
        if (start >= 0) {
            query.setFirstResult(start);
        }
        if (buffer > 0) {
            query.setMaxResults(buffer);
        }

        // get list of school ids as long
        List<Object[]> queryIdsList = query.list();
        List<Long> schoolIds = new ArrayList<Long>(queryIdsList.size());
        List<String> burSchoolLabels = new ArrayList<String>(queryIdsList.size());
        List<Long> isPublicSchoolList = new ArrayList<Long>(queryIdsList.size());
        List<Long> isPrivateSubsidisedSchoolList = new ArrayList<Long>(queryIdsList.size());
        List<Long> isPrivateNotSubsidisedSchoolList = new ArrayList<Long>(queryIdsList.size());
        List<Boolean> isSpecialSchoolList = new ArrayList<Boolean>(queryIdsList.size());
        for (Object[] row : queryIdsList) {
            schoolIds.add(((BigDecimal) row[0]).longValue());
            burSchoolLabels.add((String) row[1]);
            isPublicSchoolList.add(row[2] == null ? null : ((BigDecimal) row[2]).longValue());
            isPrivateSubsidisedSchoolList.add(row[3] == null ? null : ((BigDecimal) row[3]).longValue());
            isPrivateNotSubsidisedSchoolList.add(row[4] == null ? null : ((BigDecimal) row[4]).longValue());
            isSpecialSchoolList.add(row[5] == null ? null : parseBooleanValue(((BigDecimal) row[5]).longValue()));
        }

        return setBurSchoolLabel(getSchoolsByIds(schoolIds), burSchoolLabels, isPublicSchoolList, isPrivateSubsidisedSchoolList,
                isPrivateNotSubsidisedSchoolList, isSpecialSchoolList);
    }

    private Boolean parseBooleanValue(long longValue) {
        if (longValue == 0) {
            return new Boolean(false);
        }
        if (longValue == 1) {
            return new Boolean(true);
        }
        return null;
    }

    @Override
    public Long getMaxNrOfSchools(FilterContext filterContext, Long version, Long canton) {
        List<String> joinColumns = getJoinColumns();
        String queryString;
        String whereSelection = _filterUtility.getWhereFilterSelection(filterContext, "model", joinColumns, "s", COLUMN_MAPPING, getStringColumns(),
                getDateColumns(), getUnderscoreColumns());
        String schoolSubquery = _filterUtility.getPredefinedFilterSubquery(filterContext, SecurityFilters.SDL_SCHOOLS_TABLE, true);

        if (whereSelection.length() > 0) {
            whereSelection += " and ";
        }
        whereSelection += "model.isToDelete <> 1";

        if (canton > 0L) {
            whereSelection += " and model.canton=" + canton;
        }

        if (version != null) {
            whereSelection += " and model.version=" + version;
        }

        queryString = "select count (*) nrSchools from " + schoolSubquery + " model "
                + (_filterUtility.containsJoinField(filterContext, joinColumns)
                        ? "left outer join schools s on (model.idType = 'CH.BUR' and model.id = s.burNr) or (model.idType <> 'CH.BUR' and model.id = s.cantonalcode_sdl) "
                        : "")
                + "where " + whereSelection;

        // SQL-Query: Because of HQL-Problems with from-subqueries..
        Query query= currentSession().createNativeQuery (queryString).addScalar("nrSchools");

        return ((BigDecimal) query.uniqueResult()).longValue();
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sdl.server.integration.repository.ISchoolRepository#loadWholeDelivery(java.lang.Long)
     */
    @SuppressWarnings("unchecked")
    @Override
    public Set<SdlSchool> loadWholeDelivery(final Long deliveryId) {
        return (Set<SdlSchool>) getHibernateTemplate().execute(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                Query query = session.createQuery(
                        "from SdlSchool s left join fetch s.plausierrors where s.deliveryId=:deliveryId order by s.schoolId");
                query.setParameter("deliveryId", deliveryId);

                return new LinkedHashSet(query.list());
            }
        });
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sdl.server.integration.repository.ISchoolRepository#getSchoolsOwnedByClasses(java.util.List<java.lang.Long>)
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<SdlSchool> getSchoolsOwnedByClasses(List<Long> classIds, SortContext sortContext) {
        String queryString;
        String sortColumn = _filterUtility.adaptColumnName(sortContext.getSortColumn(), "model", getJoinColumns(), "s", COLUMN_MAPPING, getUnderscoreColumns());
        String codegroupId = getCodegroupId(sortColumn);
        String cidsSelection = _filterUtility.createSqlInExpression("meb_c.classId", classIds);

        if (sortContext.getLocale() != null && codegroupId != null) {
            // sort according to locale dependent code texts
            String mainLocale = sortContext.getLocale();
            queryString = "select distinct model.schoolId, s.label, s.char_publ_flg, s.char_priv_sub_flg, s.char_priv_no_sub_flg, " + "s." + IS_SPECIAL_SCHOOL
                    + ", " + "(case when " + sortColumn + " is null then '' " + "      when meb_cg.code is null then to_char(" + sortColumn + ") "
                    + "      else  meb_cg.codetext end) sorttext " + " from Sdl_Classes meb_c, Sdl_Schools model "
                    + " left outer join schools s on (model.idType = 'CH.BUR' and model.id = s.burNr) or (model.idType <> 'CH.BUR' and model.id = s.cantonalcode_sdl)"
                    + " left outer join " + " (select cg1.* from Codegroups cg1, "
                    + "   (select codegroupid, code, language, max(validFromYear) validFromYear from Codegroups where codegroupId = '" + codegroupId
                    + "' and language = '" + mainLocale + "' group by codegroupid, code, language" + "   ) cg2"
                    + "  where cg1.codegroupId = cg2.codegroupId and cg1.code = cg2.code and cg1.language = cg2.language and cg1.validFromYear = cg2.validFromYear   "
                    + " ) meb_cg on meb_cg.code = " + sortColumn + " where model.schoolId = meb_c.schoolId " + ((cidsSelection.length() == 0) ? "" : " and ")
                    + cidsSelection + " order by sorttext " + ((sortContext.getAscSortOrder()) ? "asc" : "desc");
        } else {
            queryString = "select distinct model.schoolId, s.label, s.char_publ_flg, s.char_priv_sub_flg, s.char_priv_no_sub_flg, " + "s." + IS_SPECIAL_SCHOOL
                    + ", " + sortColumn + " sorttext " + " from Sdl_Classes meb_c, Sdl_Schools model"
                    + " left outer join schools s on (model.idType = 'CH.BUR' and model.id = s.burNr) or (model.idType <> 'CH.BUR' and model.id = s.cantonalcode_sdl) "
                    + " where model.schoolId=meb_c.schoolId" + ((cidsSelection.length() == 0) ? "" : " and ") + cidsSelection + " order by sorttext "
                    + ((sortContext.getAscSortOrder()) ? "asc" : "desc");
        }

        Query query = currentSession().createNativeQuery (queryString);

        // get list of school ids as long
        List<Object[]> queryIdsList = query.list();
        List<Long> schoolIds = new ArrayList<Long>(queryIdsList.size());
        List<String> burSchoolLabels = new ArrayList<String>(queryIdsList.size());
        List<Long> charPublFlgs = new ArrayList<Long>(queryIdsList.size());
        List<Long> charPrivSubFlgs = new ArrayList<Long>(queryIdsList.size());
        List<Long> charPrivNoSubFlgs = new ArrayList<Long>(queryIdsList.size());
        List<Boolean> isSpecialSchoolList = new ArrayList<Boolean>(queryIdsList.size());
        for (Object[] row : queryIdsList) {
            schoolIds.add(((BigDecimal) row[0]).longValue());
            burSchoolLabels.add((String) row[1]);
            charPublFlgs.add(row[2] == null ? null : ((BigDecimal) row[2]).longValue());
            charPrivSubFlgs.add(row[3] == null ? null : ((BigDecimal) row[3]).longValue());
            charPrivNoSubFlgs.add(row[4] == null ? null : ((BigDecimal) row[4]).longValue());
            isSpecialSchoolList.add(row[5] == null ? null : parseBooleanValue(((BigDecimal) row[5]).longValue()));
        }

        return setBurSchoolLabel(getSchoolsByIds(schoolIds), burSchoolLabels, charPublFlgs, charPrivSubFlgs, charPrivNoSubFlgs, isSpecialSchoolList);
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sdl.server.integration.repository.ISchoolRepository#getSchoolsByDeliveryId(java.lang.Long)
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<SdlSchool> getSchoolsByDeliveryId(final Long deliveryId) {
        return (List<SdlSchool>) getHibernateTemplate().execute(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                Query query = session.createQuery(
                        "from SdlSchool where deliveryId=:deliveryId and isToDelete=0");
                query.setParameter("deliveryId", deliveryId);

                return query.list();
            }
        });
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sdl.server.integration.repository.ISchoolRepository#getSchoolByIdentification(java.lang.Long, java.lang.String, java.lang.String)
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<SdlSchool> getSchoolByIdentification(final Long deliveryId, final String idType, final String id) {
        return (List<SdlSchool>) getHibernateTemplate().execute(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
               Query<SdlSchool> query = session.createQuery(
                        "from SdlSchool where deliveryId= :deliveryId and idType in (:idType, :unknownSchool, :unauthorizedSchool) and id= :id and isToDelete=0", SdlSchool.class);
                query.setParameter("deliveryId", deliveryId);
                query.setParameter("idType", idType);
                query.setParameter("unknownSchool", CodegroupUtility.MEB_SCHOOL_UNKNOWN);
                query.setParameter("unauthorizedSchool", CodegroupUtility.MEB_SCHOOL_NOT_AUTHORIZED_USER);
                query.setParameter("id", id);
                return query.list();
            }
        });
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sdl.server.integration.repository.ISchoolRepository#insertSchool(ch.bfs.meb.sdl.server.integration.dto.SdlSchool)
     */
    @Override
    public SdlSchool insertSchool(SdlSchool school) {
        getHibernateTemplate().save(school);
        getHibernateTemplate().flush();
        return addBurSchoolInfo(school);
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sdl.server.integration.repository.ISchoolRepository#updateSchool(ch.bfs.meb.sdl.server.integration.dto.SdlSchool)
     */
    @Override
    public SdlSchool updateSchool(SdlSchool school) {
        school = (SdlSchool) getHibernateTemplate().merge(school);
        getHibernateTemplate().flush();
        return addBurSchoolInfo(school);
    }

    private SdlSchool addBurSchoolInfo(SdlSchool school) {
        if (school == null || CodegroupUtility.MEB_SCHOOL_UNKNOWN.equalsIgnoreCase(school.getIdType())) {
            return school;
        }

        String sql;
        if (CodegroupUtility.MEB_SCHOOL_CH_BUR.equalsIgnoreCase(school.getIdType())) {
            sql = "select s.label, s.char_publ_flg, s.char_priv_sub_flg, s.char_priv_no_sub_flg, s." + IS_SPECIAL_SCHOOL +
                    " from Schools s where s.burNr = :identifier";
        } else {
            sql = "select s.label, s.char_publ_flg, s.char_priv_sub_flg, s.char_priv_no_sub_flg, s." + IS_SPECIAL_SCHOOL +
                    " from Schools s where s.cantonalcode_sdl = :identifier";
        }

        NativeQuery query = currentSession().createNativeQuery(sql);
        query.setParameter("identifier", school.getId());

        Object[] result;
        try {
            result = (Object[]) query.uniqueResult();
        } catch (NonUniqueResultException nonUniqueResultException) {
            LOGGER.warn("More than one entry found in Schools-Table for '" + school.getIdType() + "' with id='" + school.getId() + "'!",
                    nonUniqueResultException);
            throw nonUniqueResultException;
        }

        if (result != null) {
            school.setBurSchoolLabel((String) result[0]);
            school.setCharPublFlg(result[1] == null ? null : ((BigDecimal) result[1]).longValue());
            school.setCharPrivSubFlg(result[2] == null ? null : ((BigDecimal) result[2]).longValue());
            school.setCharPrivNoSubFlg(result[3] == null ? null : ((BigDecimal) result[3]).longValue());
            school.setIsSpecialSchool(result[4] == null ? null : BigDecimalUtils.convertToBoolean((BigDecimal) result[4]));
        }

        return school;
    }


    /* (non-Javadoc)
     * @see ch.bfs.meb.sdl.server.integration.repository.ISchoolRepository#deleteSchool(ch.bfs.meb.sdl.server.integration.dto.SdlSchool)
     */
    @Override
    public void deleteSchool(SdlSchool school) {
        // check if the school can be deleted
        MebUser user = (MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        long status = CodegroupUtility.MEB_DATASTATUS_PREVALIDATED;
        if (user.isInRole(SecurityConstants.ROLE_SDL_DV)) {
            status = CodegroupUtility.MEB_DATASTATUS_VALIDATED;
        }

// check classes
        Query query = currentSession().createQuery(
                "from SdlClass where schoolId= :schoolId and deliveryStatus>= :status", SdlClass.class);
        query.setParameter("schoolId", school.getSchoolId());
        query.setParameter("status", status);
        query.setMaxResults(1);
        if (query.list().size() > 0) {
            throw new MebUncheckedNotMonitoredException("maintain.delete.childrenValidatedError.messages");
        }

// check learners
        query = currentSession().createQuery(
                "from SdlLearner where learnerId in (select l.learnerId from SdlLearner l, SdlClass c where l.classId=c.classId and c.schoolId= :schoolId) and deliveryStatus>= :status", SdlLearner.class);
        query.setParameter("schoolId", school.getSchoolId());
        query.setParameter("status", status);
        query.setMaxResults(1);
        if (query.list().size() > 0) {
            throw new MebUncheckedNotMonitoredException("maintain.delete.childrenValidatedError.messages");
        }

        getHibernateTemplate().execute(session -> {
            String queryString = "delete from SdlLearner where learnerId in (select l.learnerId from SdlLearner l, SdlClass c where l.classId=c.classId and c.schoolId= :schoolId)";
            Query queryDelete = session.createQuery(queryString);
            queryDelete.setParameter("schoolId", school.getSchoolId());
            return queryDelete.executeUpdate();
        });

        getHibernateTemplate().execute(new HibernateCallback<Integer>() {
            @Override
            public Integer doInHibernate(Session session) throws HibernateException {
                Query query = session.createQuery("delete from SdlClass where schoolId=:schoolId");
                query.setParameter("schoolId", school.getSchoolId());
                return query.executeUpdate();
            }
        });
        getHibernateTemplate().delete(school);
        getHibernateTemplate().flush();
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sdl.server.integration.repository.ISchoolRepository#updatePlausistatus(java.lang.Long)
     */
    @Override
    public void updatePlausistatus(Long schoolId) {
        getHibernateTemplate().execute(session -> {
            String queryString = "update SdlSchool set plausiStatus=case " +
                    "when (select count(e) from SdlPlausiError e where e.schoolId= :schoolId1 and e.classId is null)=0 then 2 " +
                    "when (select count(e) from SdlPlausiError e where e.schoolId= :schoolId2 and e.classId is null and e.isConfirmed=0)>0 then 1 " +
                    "else 3 end where schoolId= :schoolId3";
            Query query = session.createQuery(queryString);
            query.setParameter("schoolId1", schoolId);
            query.setParameter("schoolId2", schoolId);
            query.setParameter("schoolId3", schoolId);
            return query.executeUpdate();
        });
        getHibernateTemplate().flush();
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sdl.server.integration.repository.ISchoolRepository#updateConfigDeliveryCode(ch.bfs.meb.sdl.server.integration.dto.SdlSchool, java.lang.String)
     */
    @Override
    public void updateConfigDeliveryCode(SdlSchool school, String configDeliveryCode) {
        getHibernateTemplate().execute(session -> {
            String queryString = "update SdlLearner set configDeliveryCode= :configCode where learnerId in (select l.learnerId from SdlLearner l, SdlClass c where l.classId=c.classId and c.schoolId= :schoolId)";
            Query query = session.createQuery(queryString);
            query.setParameter("configCode", configDeliveryCode);
            query.setParameter("schoolId", school.getSchoolId());
            return query.executeUpdate();
        });
        getHibernateTemplate().execute(session -> {
            String queryString = "update SdlClass set configDeliveryCode= :configCode where schoolId= :schoolId";
           Query query = session.createQuery(queryString);
            query.setParameter("configCode", configDeliveryCode);
            query.setParameter("schoolId", school.getSchoolId());
            return query.executeUpdate();
        });
        school.setConfigDeliveryCode(configDeliveryCode);
        getHibernateTemplate().flush();
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sdl.server.integration.repository.ISchoolRepository#allPlausibel(ch.bfs.meb.sdl.server.integration.dto.SdlClass)
     */
    @Override
    public boolean allPlausibel(SdlSchool school) {
        final Long schoolId = school.getSchoolId();
        // check learner
        Long notPlausibel = (Long) getHibernateTemplate().execute(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {


                Query<Long> query = session.createQuery(
                        "select count(l) from SdlClass c, SdlLearner l where l.classId=c.classId and c.schoolId= :schoolId and l.plausiStatus= :plausiStatus", Long.class);
                query.setParameter("schoolId", schoolId);
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

                Query<Long> query = session.createQuery(
                        "select count(c) from SdlClass c where c.schoolId= :schoolId and c.plausiStatus= :plausiStatus", Long.class);
                query.setParameter("schoolId", schoolId);
                query.setParameter("plausiStatus", CodegroupUtility.MEB_PLAUSISTATUS_NOTVALID);
                return query.uniqueResult();
            }
        });
        if (notPlausibel > 0L) {
            return false;
        }
        return !school.getPlausiStatus().equals(CodegroupUtility.MEB_PLAUSISTATUS_NOTVALID);
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sdl.server.integration.repository.ISchoolRepository#prevalidate(java.util.List, java.lang.String)
     */
    @Override
    public void prevalidate(List<Long> schoolList, String userEmail) {
        String cIdList = _filterUtility.createSqlInExpression("c.schoolId", schoolList);
        String idList = _filterUtility.createSqlInExpression("schoolId", schoolList);
        Date now = new Date();

        String learnerUpdateQuery = "UPDATE SdlLearner SET deliveryStatus = :deliveryStatus, prevalidation_user = :userEmail, prevalidation_date = :now " +
                "WHERE learnerId IN (SELECT l.learnerId FROM SdlLearner l, SdlClass c WHERE l.classId = c.classId AND " + cIdList + " AND l.deliveryStatus = :currentStatus)";
        String classUpdateQuery = "UPDATE SdlClass SET deliveryStatus = :deliveryStatus, prevalidation_user = :userEmail, prevalidation_date = :now " +
                "WHERE classId IN (SELECT c.classId FROM SdlClass c WHERE " + cIdList + " AND c.deliveryStatus = :currentStatus)";
        String schoolUpdateQuery = "UPDATE SdlSchool SET deliveryStatus = :deliveryStatus, prevalidation_user = :userEmail, prevalidation_date = :now WHERE " + idList;

        getHibernateTemplate().execute(session -> {
            Query learnerQuery = session.createQuery(learnerUpdateQuery);
            learnerQuery.setParameter("deliveryStatus", CodegroupUtility.MEB_DATASTATUS_PREVALIDATED);
            learnerQuery.setParameter("userEmail", userEmail);
            learnerQuery.setParameter("now", now);
            learnerQuery.setParameter("currentStatus", CodegroupUtility.MEB_DATASTATUS_DELIVERED);
            learnerQuery.executeUpdate();

            Query classQuery = session.createQuery(classUpdateQuery);
            classQuery.setParameter("deliveryStatus", CodegroupUtility.MEB_DATASTATUS_PREVALIDATED);
            classQuery.setParameter("userEmail", userEmail);
            classQuery.setParameter("now", now);
            classQuery.setParameter("currentStatus", CodegroupUtility.MEB_DATASTATUS_DELIVERED);
            classQuery.executeUpdate();

            Query schoolQuery = session.createQuery(schoolUpdateQuery);
            schoolQuery.setParameter("deliveryStatus", CodegroupUtility.MEB_DATASTATUS_PREVALIDATED);
            schoolQuery.setParameter("userEmail", userEmail);
            schoolQuery.setParameter("now", now);
            schoolQuery.executeUpdate();

            return null;
        });

        getHibernateTemplate().flush();
    }


    /* (non-Javadoc)
     * @see ch.bfs.meb.sdl.server.integration.repository.ISchoolRepository#validate(java.util.List, java.lang.String)
     */
    @Override
    public void validate(List<Long> schoolList, String userEmail) {
        String cIdList = _filterUtility.createSqlInExpression("c.schoolId", schoolList);
        String idList = _filterUtility.createSqlInExpression("schoolId", schoolList);
        Date now = new Date();

        getHibernateTemplate().execute(session -> {
            String learnerUpdateQuery = "UPDATE SdlLearner SET deliveryStatus = :deliveryStatus, validation_user = :userEmail, validation_date = :now " +
                    "WHERE learnerId IN (SELECT l.learnerId FROM SdlLearner l, SdlClass c WHERE l.classId = c.classId AND " + cIdList + " AND l.deliveryStatus IN (:deliveredStatus, :prevalidatedStatus))";
            Query<?> learnerQuery = session.createQuery(learnerUpdateQuery);
            learnerQuery.setParameter("deliveryStatus", CodegroupUtility.MEB_DATASTATUS_VALIDATED);
            learnerQuery.setParameter("userEmail", userEmail);
            learnerQuery.setParameter("now", now);
            learnerQuery.setParameter("deliveredStatus", CodegroupUtility.MEB_DATASTATUS_DELIVERED);
            learnerQuery.setParameter("prevalidatedStatus", CodegroupUtility.MEB_DATASTATUS_PREVALIDATED);
            learnerQuery.executeUpdate();

            String learnerPrevalidationUpdateQuery = "UPDATE SdlLearner SET prevalidation_user = CASE WHEN prevalidation_user IS NULL THEN validation_user ELSE prevalidation_user END, " +
                    "prevalidation_date = CASE WHEN prevalidation_date IS NULL THEN validation_date ELSE prevalidation_date END " +
                    "WHERE learnerId IN (SELECT l.learnerId FROM SdlLearner l, SdlClass c WHERE l.classId = c.classId AND " + cIdList + " AND l.deliveryStatus = :validatedStatus)";
            Query<?> learnerPrevalidationQuery = session.createQuery(learnerPrevalidationUpdateQuery);
            learnerPrevalidationQuery.setParameter("validatedStatus", CodegroupUtility.MEB_DATASTATUS_VALIDATED);
            learnerPrevalidationQuery.executeUpdate();

            String classUpdateQuery = "UPDATE SdlClass SET deliveryStatus = :deliveryStatus, validation_user = :userEmail, validation_date = :now " +
                    "WHERE classId IN (SELECT c.classId FROM SdlClass c WHERE " + cIdList + " AND c.deliveryStatus IN (:deliveredStatus, :prevalidatedStatus))";
            Query<?> classQuery = session.createQuery(classUpdateQuery);
            classQuery.setParameter("deliveryStatus", CodegroupUtility.MEB_DATASTATUS_VALIDATED);
            classQuery.setParameter("userEmail", userEmail);
            classQuery.setParameter("now", now);
            classQuery.setParameter("deliveredStatus", CodegroupUtility.MEB_DATASTATUS_DELIVERED);
            classQuery.setParameter("prevalidatedStatus", CodegroupUtility.MEB_DATASTATUS_PREVALIDATED);
            classQuery.executeUpdate();

            String classPrevalidationUpdateQuery = "UPDATE SdlClass SET prevalidation_user = CASE WHEN prevalidation_user IS NULL THEN validation_user ELSE prevalidation_user END, " +
                    "prevalidation_date = CASE WHEN prevalidation_date IS NULL THEN validation_date ELSE prevalidation_date END " +
                    "WHERE classId IN (SELECT c.classId FROM SdlClass c WHERE " + cIdList + " AND c.deliveryStatus = :validatedStatus)";
            Query<?> classPrevalidationQuery = session.createQuery(classPrevalidationUpdateQuery);
            classPrevalidationQuery.setParameter("validatedStatus", CodegroupUtility.MEB_DATASTATUS_VALIDATED);
            classPrevalidationQuery.executeUpdate();

            String schoolUpdateQuery = "UPDATE SdlSchool SET deliveryStatus = :deliveryStatus, validation_user = :userEmail, validation_date = :now WHERE " + idList;
            Query<?> schoolQuery = session.createQuery(schoolUpdateQuery);
            schoolQuery.setParameter("deliveryStatus", CodegroupUtility.MEB_DATASTATUS_VALIDATED);
            schoolQuery.setParameter("userEmail", userEmail);
            schoolQuery.setParameter("now", now);
            schoolQuery.executeUpdate();

            String schoolPrevalidationUpdateQuery = "UPDATE SdlSchool SET prevalidation_user = CASE WHEN prevalidation_user IS NULL THEN validation_user ELSE prevalidation_user END, " +
                    "prevalidation_date = CASE WHEN prevalidation_date IS NULL THEN validation_date ELSE prevalidation_date END WHERE " + idList;
            Query<?> schoolPrevalidationQuery = session.createQuery(schoolPrevalidationUpdateQuery);
            schoolPrevalidationQuery.executeUpdate();

            return null;
        });

        getHibernateTemplate().flush();
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sdl.server.integration.repository.ISchoolRepository#undoPrevalidate(java.util.List)
     */
    @Override
    public void undoPrevalidate(List<Long> schoolList) {
        String cIdList = _filterUtility.createSqlInExpression("c.schoolId", schoolList);
        String idList = _filterUtility.createSqlInExpression("schoolId", schoolList);

        getHibernateTemplate().execute(session -> {
            String learnerUpdateQuery = "UPDATE SdlLearner SET deliveryStatus = :deliveryStatus, prevalidation_user = NULL, prevalidation_date = NULL " +
                    "WHERE learnerId IN (SELECT l.learnerId FROM SdlLearner l, SdlClass c WHERE l.classId = c.classId AND " + cIdList + " AND l.deliveryStatus = :prevalidatedStatus)";
            Query<?> learnerQuery = session.createQuery(learnerUpdateQuery);
            learnerQuery.setParameter("deliveryStatus", CodegroupUtility.MEB_DATASTATUS_DELIVERED);
            learnerQuery.setParameter("prevalidatedStatus", CodegroupUtility.MEB_DATASTATUS_PREVALIDATED);
            learnerQuery.executeUpdate();

            String classUpdateQuery = "UPDATE SdlClass SET deliveryStatus = :deliveryStatus, prevalidation_user = NULL, prevalidation_date = NULL " +
                    "WHERE classId IN (SELECT c.classId FROM SdlClass c WHERE " + cIdList + " AND c.deliveryStatus = :prevalidatedStatus)";
            Query<?> classQuery = session.createQuery(classUpdateQuery);
            classQuery.setParameter("deliveryStatus", CodegroupUtility.MEB_DATASTATUS_DELIVERED);
            classQuery.setParameter("prevalidatedStatus", CodegroupUtility.MEB_DATASTATUS_PREVALIDATED);
            classQuery.executeUpdate();

            String schoolUpdateQuery = "UPDATE SdlSchool SET deliveryStatus = :deliveryStatus, prevalidation_user = NULL, prevalidation_date = NULL WHERE " + idList;
            Query<?> schoolQuery = session.createQuery(schoolUpdateQuery);
            schoolQuery.setParameter("deliveryStatus", CodegroupUtility.MEB_DATASTATUS_DELIVERED);
            schoolQuery.executeUpdate();

            return null;
        });

        getHibernateTemplate().flush();
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.sdl.server.integration.repository.ISchoolRepository#undoValidate(java.util.List)
     */
    @Override
    public void undoValidate(List<Long> schoolList) {
        String cIdList = _filterUtility.createSqlInExpression("c.schoolId", schoolList);
        String idList = _filterUtility.createSqlInExpression("schoolId", schoolList);

        getHibernateTemplate().execute(session -> {
            String learnerUpdateQuery = "UPDATE SdlLearner SET deliveryStatus = :deliveryStatus, validation_user = NULL, validation_date = NULL " +
                    "WHERE learnerId IN (SELECT l.learnerId FROM SdlLearner l, SdlClass c WHERE l.classId = c.classId AND " + cIdList + " AND l.deliveryStatus = :validatedStatus)";
            Query<?> learnerQuery = session.createQuery(learnerUpdateQuery);
            learnerQuery.setParameter("deliveryStatus", CodegroupUtility.MEB_DATASTATUS_PREVALIDATED);
            learnerQuery.setParameter("validatedStatus", CodegroupUtility.MEB_DATASTATUS_VALIDATED);
            learnerQuery.executeUpdate();

            String classUpdateQuery = "UPDATE SdlClass SET deliveryStatus = :deliveryStatus, validation_user = NULL, validation_date = NULL " +
                    "WHERE classId IN (SELECT c.classId FROM SdlClass c WHERE " + cIdList + " AND c.deliveryStatus = :validatedStatus)";
            Query<?> classQuery = session.createQuery(classUpdateQuery);
            classQuery.setParameter("deliveryStatus", CodegroupUtility.MEB_DATASTATUS_PREVALIDATED);
            classQuery.setParameter("validatedStatus", CodegroupUtility.MEB_DATASTATUS_VALIDATED);
            classQuery.executeUpdate();

            String schoolUpdateQuery = "UPDATE SdlSchool SET deliveryStatus = :deliveryStatus, validation_user = NULL, validation_date = NULL WHERE " + idList;
            Query<?> schoolQuery = session.createQuery(schoolUpdateQuery);
            schoolQuery.setParameter("deliveryStatus", CodegroupUtility.MEB_DATASTATUS_PREVALIDATED);
            schoolQuery.executeUpdate();

            return null;
        });

        getHibernateTemplate().flush();
    }

    @SuppressWarnings({ "unchecked" })
    private List<SdlSchool> getSchoolsByIds(List<Long> schoolIds) {
        if (schoolIds == null || schoolIds.isEmpty()) {
            return new ArrayList<>();
        }

        CriteriaBuilder criteriaBuilder = currentSession().getCriteriaBuilder();
        CriteriaQuery<SdlSchool> criteriaQuery = criteriaBuilder.createQuery(SdlSchool.class);
        Root<SdlSchool> root = criteriaQuery.from(SdlSchool.class);
        criteriaQuery.select(root).where(root.get("schoolId").in(schoolIds));

        List<SdlSchool> tempList = currentSession().createQuery(criteriaQuery).setMaxResults(1000).getResultList();

        Map<Long, SdlSchool> mapById = tempList.stream().collect(Collectors.toMap(SdlSchool::getSchoolId, Function.identity()));
        List<SdlSchool> resultList = schoolIds.stream().map(mapById::get).filter(Objects::nonNull).collect(Collectors.toList());

        return resultList;
    }

    private List<SdlSchool> setBurSchoolLabel(List<SdlSchool> schools, List<String> burSchoolLabels, List<Long> charPublFlgs, List<Long> charPrivSubFlgs,
            List<Long> charPrivNoSubFlgs, List<Boolean> isSpecialSchoolList) {
        if (burSchoolLabels.size() != schools.size()) {
            throw new MebUncheckedException("internal error, sizes may not differ");
        }

        for (int i = 0; i < schools.size(); i++) {
            SdlSchool school = schools.get(i);
            school.setBurSchoolLabel(burSchoolLabels.get(i));
            school.setCharPublFlg(charPublFlgs.get(i));
            school.setCharPrivSubFlg(charPrivSubFlgs.get(i));
            school.setCharPrivNoSubFlg(charPrivNoSubFlgs.get(i));
            school.setIsSpecialSchool(isSpecialSchoolList.get(i));
        }

        return schools;
    }

    /**
     * Gets the physical code group id as stored in database for a given column
     * name.
     *
     * @param colName column id of database table
     * @return physical code group id as stored in database
     */
    protected String getCodegroupId(String colName) {
        if (colName.equals("model." + DELIVERYSTATUS)) {
            return CodegroupUtility.MEB_DATASTATUS;
        } else if (colName.equals("model." + PLAUSISTATUS)) {
            return CodegroupUtility.MEB_PLAUSISTATUS;
        }

        return null;
    }

    /**
     * Returns all sql columns with underscores of according db table
     */
    protected List<String> getUnderscoreColumns() {
        ArrayList<String> underscoreColumns = new ArrayList<String>();
        underscoreColumns.add(CREATION_USER);
        underscoreColumns.add(CREATION_DATE);
        underscoreColumns.add(MODIFICATION_USER);
        underscoreColumns.add(MODIFICATION_DATE);
        underscoreColumns.add(PREVALIDATION_USER);
        underscoreColumns.add(PREVALIDATION_DATE);
        underscoreColumns.add(VALIDATION_USER);
        underscoreColumns.add(VALIDATION_DATE);
        underscoreColumns.add(IS_PUBLIC_SCHOOL);
        underscoreColumns.add(IS_PRIVATE_SUBSIDISED_SCHOOL);
        underscoreColumns.add(IS_PRIVATE_NOT_SUBSIDISED_SCHOOL);
        underscoreColumns.add(IS_SPECIAL_SCHOOL);
        return underscoreColumns;
    }

    /**
     * Returns all string columns of according db table
     */
    protected List<String> getStringColumns() {
        ArrayList<String> stringColumns = new ArrayList<String>();
        stringColumns.add(DELIVERYCODE);
        stringColumns.add(IDTYPE);
        stringColumns.add(ID);
        stringColumns.add(StringUtils.asCamelCase(CREATION_USER));
        stringColumns.add(StringUtils.asCamelCase(MODIFICATION_USER));
        stringColumns.add(StringUtils.asCamelCase(PREVALIDATION_USER));
        stringColumns.add(StringUtils.asCamelCase(VALIDATION_USER));
        stringColumns.add(USERTEXT);
        stringColumns.add(BURSCHOOLLABEL_CLASS_ATTRIBUTE_NAME);
        return stringColumns;
    }

    /**
     * Returns all date columns of according db table
     */
    protected List<String> getDateColumns() {
        ArrayList<String> dateColumns = new ArrayList<String>();
        dateColumns.add(StringUtils.asCamelCase(CREATION_DATE));
        dateColumns.add(StringUtils.asCamelCase(MODIFICATION_DATE));
        dateColumns.add(StringUtils.asCamelCase(PREVALIDATION_DATE));
        dateColumns.add(StringUtils.asCamelCase(VALIDATION_DATE));
        return dateColumns;
    }

    protected List<String> getJoinColumns() {
        ArrayList<String> joinColumns = new ArrayList<String>();
        joinColumns.add(BURSCHOOLLABEL_CLASS_ATTRIBUTE_NAME);
        joinColumns.add(StringUtils.asCamelCase(IS_PUBLIC_SCHOOL));
        joinColumns.add(StringUtils.asCamelCase(IS_PRIVATE_SUBSIDISED_SCHOOL));
        joinColumns.add(StringUtils.asCamelCase(IS_PRIVATE_NOT_SUBSIDISED_SCHOOL));
        joinColumns.add(IS_SPECIAL_SCHOOL_CLASS_ATTRIBUTE_NAME);
        return joinColumns;
    }
}
