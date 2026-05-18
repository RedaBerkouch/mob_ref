package ch.bfs.meb.sba.server.integration.repository;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.springframework.orm.hibernate5.HibernateCallback;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;
import org.springframework.stereotype.Repository;

import ch.bfs.meb.sba.server.integration.dto.SbaBurSchool;
import ch.bfs.meb.sba.server.integration.dto.SbaConfigDelivery;
import ch.bfs.meb.server.commons.integration.dto.*;
import ch.bfs.meb.server.commons.util.IFilterUtility;
import ch.bfs.meb.util.CodegroupUtility;
import ch.bfs.meb.util.StringUtils;

@Repository
public class BurSchoolRepository extends HibernateDaoSupport implements IBurSchoolRepository {
    // property constants
    public static final String SCHOOLS_TABLE = "Schools";

    public static final String LABEL = "label";
    public static final String MUNICIPALITY = "municipality";

    public static final String VALIDFROM_SDL_SSP = "validFrom_sdl_ssp";
    public static final String VALIDTO_SDL_SSP = "validTo_sdl_ssp";
    public static final String VALIDFROM_SSP = "validFrom_ssp";
    public static final String VALIDTO_SSP = "validTo_ssp";
    public static final String VALIDFROM_SBA = "validFrom_sba";
    public static final String VALIDTO_SBA = "validTo_sba";

    public static final String BUR_CANTON = "bur_canton";
    public static final String BUR_LABEL = "bur_label";
    public static final String BUR_MUNICIPALITY = "bur_municipality";
    public static final String BUR_ACTIVITYSTATUS = "bur_activityStatus";
    public static final String SYNCHSTATUS_SDL = "synchStatus_sdl";
    public static final String CANTONALCODE_SDL = "cantonalCode_sdl";
    public static final String BUR_CANTONALCODE_SDL = "bur_cantonalCode_sdl";
    public static final String IS_SDL = "is_sdl";
    public static final String BUR_IS_SDL = "bur_is_sdl";
    public static final String BUR_VALIDFROM_SDL_SSP = "bur_validFrom_sdl_ssp";
    public static final String BUR_VALIDTO_SDL_SSP = "bur_validTo_sdl_ssp";
    public static final String BUR_VALIDFROM_SSP = "bur_validFrom_ssp";
    public static final String BUR_VALIDTO_SSP = "bur_validTo_ssp";
    public static final String SYNCHSTATUS_SSP = "synchStatus_ssp";
    public static final String CANTONALCODE_SSP = "cantonalCode_ssp";
    public static final String BUR_CANTONALCODE_SSP = "bur_cantonalCode_ssp";
    public static final String IS_SSP = "is_ssp";
    public static final String BUR_IS_SSP = "bur_is_ssp";
    public static final String SYNCHSTATUS_SBA = "synchStatus_sba";
    public static final String CANTONALCODE_SBA = "cantonalCode_sba";
    public static final String BUR_CANTONALCODE_SBA = "bur_cantonalCode_sba";
    public static final String IS_SBA = "is_sba";
    public static final String BUR_IS_SBA = "bur_is_sba";
    public static final String BUR_VALIDFROM_SBA = "bur_validFrom_sba";
    public static final String BUR_VALIDTO_SBA = "bur_validTo_sba";
    public static final String IS_PUBLIC_SCHOOL = "char_publ_flg";
    public static final String IS_PRIVATE_SUBSIDISED_SCHOOL = "char_priv_sub_flg";
    public static final String IS_PRIVATE_NOT_SUBSIDISED_SCHOOL = "char_priv_no_sub_flg";
    public static final String IS_SPECIAL_SCHOOL = "inst_typ_blp_flg";
    public static final String IS_PUBLIC_SCHOOL_BUR = "bur_char_publ_flg";
    public static final String IS_PRIVATE_SUBSIDISED_SCHOOL_BUR = "bur_char_priv_sub_flg";
    public static final String IS_PRIVATE_NOT_SUBSIDISED_SCHOOL_BUR = "bur_char_priv_no_sub_flg";
    public static final String IS_SPECIAL_SCHOOL_BUR = "bur_inst_typ_blp_flg";

    public static final String DELIVERYCODE = "deliveryCode";
    public static final String SYNCHSTATUS_BUR_CLASS_ATTRIBUTE_NAME = "synchStatusBur";
    public static final String NAME_BUR_CLASS_ATTRIBUTE_NAME = "nameBur";
    public static final String CANTON_BUR_CLASS_ATTRIBUTE_NAME = "cantonBur";
    public static final String MUNICIPALITY_BUR_CLASS_ATTRIBUTE_NAME = "municipalityBur";
    public static final String VALID_FROM_BUR_CLASS_ATTRIBUTE_NAME = "validFromBur";
    public static final String VALID_TO_BUR_CLASS_ATTRIBUTE_NAME = "validToBur";
    public static final String IS_SPECIAL_SCHOOL_ATTRIBUTE_NAME = "isSpecialSchool";
    public static final String IS_SPECIAL_SCHOOL_BUR_ATTRIBUTE_NAME = "isSpecialSchoolBur";

    public static final int MAX_EXPRESSIONS_ORACLE = 1000;
    private static final Map<String, String> COLUMN_MAPPING = new HashMap<String, String>();

    static {
        COLUMN_MAPPING.put(SYNCHSTATUS_BUR_CLASS_ATTRIBUTE_NAME, SYNCHSTATUS_SBA);
        COLUMN_MAPPING.put(NAME_BUR_CLASS_ATTRIBUTE_NAME, BUR_LABEL);
        COLUMN_MAPPING.put(CANTON_BUR_CLASS_ATTRIBUTE_NAME, BUR_CANTON);
        COLUMN_MAPPING.put(MUNICIPALITY_BUR_CLASS_ATTRIBUTE_NAME, BUR_MUNICIPALITY);
        COLUMN_MAPPING.put(VALID_FROM_BUR_CLASS_ATTRIBUTE_NAME, BUR_VALIDFROM_SBA);
        COLUMN_MAPPING.put(VALID_TO_BUR_CLASS_ATTRIBUTE_NAME, BUR_VALIDTO_SBA);
        COLUMN_MAPPING.put(IS_SPECIAL_SCHOOL_ATTRIBUTE_NAME, IS_SPECIAL_SCHOOL);
        COLUMN_MAPPING.put(IS_SPECIAL_SCHOOL_BUR_ATTRIBUTE_NAME, IS_SPECIAL_SCHOOL_BUR);
    }

    private IFilterUtility _filterUtility;

    public void setFilterUtility(IFilterUtility filterUtility) {
        _filterUtility = filterUtility;
    }

    @Override
    public SbaBurSchool getBurSchoolById(Long burSchoolId) {
        return (SbaBurSchool) getHibernateTemplate().get(SbaBurSchool.class, burSchoolId);
    }

    @SuppressWarnings("unchecked")
    @Override
    public SbaBurSchool getBurSchoolByIdAndType(final String schoolId, final String schoolType) {
        return (SbaBurSchool) getHibernateTemplate().execute(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                if (schoolType != null && schoolType.equals(CodegroupUtility.MEB_SCHOOL_CH_BUR)) {
                    Query q = session.createQuery("from SbaBurSchool as bs where bs.burNr=:burNr");
                    try {
                        q.setLong("burNr", Long.parseLong(schoolId));
                    } catch (NumberFormatException e) {
                        return null;
                    }
                    List<SbaBurSchool> burSchools = (List<SbaBurSchool>) q.list();
                    return burSchools.size() == 0 ? null : burSchools.get(0);
                } else {
                    Query q = session.createQuery("from SbaBurSchool as bs where bs.cantonalCode_sba=:cantonalCode");
                    q.setString("cantonalCode", schoolId);
                    List<SbaBurSchool> burSchools = (List<SbaBurSchool>) q.list();
                    return burSchools.size() == 0 ? null : burSchools.get(0);
                }
            }
        });
    }

    /**
     * Charge toutes les écoles en batch selon leurs IDs et types
     * @return Map avec clé "type:id" et valeur SbaBurSchool
     */
    public Map<String, SbaBurSchool> getBurSchoolsByIdsAndTypes(Map<String, Set<String>> schoolIdsByType) {
        Map<String, SbaBurSchool> result = new HashMap<>();

        // Charger les écoles BUR
        Set<String> burIds = schoolIdsByType.get(CodegroupUtility.MEB_SCHOOL_CH_BUR);
        if (burIds != null && !burIds.isEmpty()) {
            List<Long> burNrs = burIds.stream()
                    .map(id -> {
                        try {
                            return Long.parseLong(id);
                        } catch (NumberFormatException e) {
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            if (!burNrs.isEmpty()) {
                List<SbaBurSchool> burSchools = getHibernateTemplate().execute(session -> {
                    Query q = session.createQuery(
                            "from SbaBurSchool where burNr in (:burNrs)"
                    );
                    q.setParameterList("burNrs", burNrs);
                    return (List<SbaBurSchool>) q.list();
                });

                for (SbaBurSchool school : burSchools) {
                    result.put(CodegroupUtility.MEB_SCHOOL_CH_BUR + ":" + school.getBurNr(), school);
                }
            }
        }

        // Charger les écoles cantonales
        Set<String> cantonalIds = new HashSet<>();
        for (Map.Entry<String, Set<String>> entry : schoolIdsByType.entrySet()) {
            if (!entry.getKey().equals(CodegroupUtility.MEB_SCHOOL_CH_BUR)) {
                cantonalIds.addAll(entry.getValue());
            }
        }

        if (!cantonalIds.isEmpty()) {
            List<SbaBurSchool> cantonalSchools = getHibernateTemplate().execute(session -> {
                Query q = session.createQuery(
                        "from SbaBurSchool where cantonalCode_sba in (:cantonalCodes)"
                );
                q.setParameterList("cantonalCodes", cantonalIds);
                return (List<SbaBurSchool>) q.list();
            });

            for (SbaBurSchool school : cantonalSchools) {
                // Trouver le type correspondant
                for (Map.Entry<String, Set<String>> entry : schoolIdsByType.entrySet()) {
                    if (!entry.getKey().equals(CodegroupUtility.MEB_SCHOOL_CH_BUR)
                            && entry.getValue().contains(school.getCantonalCode_sba())) {
                        result.put(entry.getKey() + ":" + school.getCantonalCode_sba(), school);
                    }
                }
            }
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<SbaBurSchool> getBurSchools() {
        return (List<SbaBurSchool>) getHibernateTemplate().execute(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                return (List<SbaBurSchool>) session.createQuery("from SbaBurSchool").list();
            }
        });
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<SbaBurSchool> getBurSchoolsForCsvExport() {
        return (List<SbaBurSchool>) getHibernateTemplate().execute(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                return (List<SbaBurSchool>) session.createQuery("from SbaBurSchool order by canton, burNr").list();
            }
        });
    }

    protected String getWhereVisible(boolean isBur) {
        String bur = isBur ? "bur_" : "";
        Calendar cal = new GregorianCalendar();
        long now = cal.get(Calendar.YEAR);

        return "model." + bur + "is_sba=1" + " and (model." + bur + "validfrom_sba is null or model." + bur + "validfrom_sba <= " + now + ")" + " and (model."
                + bur + "validto_sba is null or model." + bur + "validto_sba >= " + now + ")" + " and (model." + bur + "activitystatus = "
                + CodegroupUtility.MEB_BUR_ACTIVITY_STATUS_ACTIVE + " or model." + bur + "activitystatus = " + CodegroupUtility.MEB_BUR_ACTIVITY_STATUS_VIRTUAL
                + " or model." + bur + "activitystatus = " + CodegroupUtility.MEB_BUR_ACTIVITY_STATUS_UNKNOWN + ")";
    }

    protected boolean hasConfigDeliveryFilter(FilterContext filterContext) {
        for (WhereFilter whereFilter : filterContext.getWhereFilter()) {
            if (whereFilter.getAttribute().equals(DELIVERYCODE)) {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<SbaBurSchool> getBurSchools(int start, int buffer, SortContext sortContext, FilterContext filterContext, Long version, Long canton,
            boolean showBurSynch) {
        String appFilter;
        String queryString;
        String sortColumn = _filterUtility.adaptColumnName(sortContext.getSortColumn(), "model", COLUMN_MAPPING, getUnderscoreColumns());
        String codegroupId = getCodegroupId(sortColumn);
        String whereSelection = _filterUtility.getWhereFilterSelection(filterContext, "model", COLUMN_MAPPING, getStringColumns(), getDateColumns(),
                getUnderscoreColumns());
        String schoolsSubquery = _filterUtility.getPredefinedFilterSubquery(filterContext, SCHOOLS_TABLE, !showBurSynch);

        boolean hasCdFilter = hasConfigDeliveryFilter(filterContext);
        boolean hasCdSort = sortColumn.equals("model." + DELIVERYCODE);
        String cdField = "(case when exists (select * from sba_configdeliveries meb_cd, sba_schools_configdeliveries meb_s_cd where meb_s_cd.schoolId = model.schoolId and meb_s_cd.deliveryid = meb_cd.deliveryid and meb_cd.version = "
                + version
                + ") then (select meb_cd.deliverycode from sba_configdeliveries meb_cd, sba_schools_configdeliveries meb_s_cd where meb_s_cd.schoolId = model.schoolId and meb_s_cd.deliveryid = meb_cd.deliveryid and meb_cd.version = "
                + version + ") else '' end)";

        if (hasCdFilter) {
            whereSelection = whereSelection.replaceAll("model\\." + DELIVERYCODE, cdField);
        }

        if (showBurSynch) {
            appFilter = "((" + getWhereVisible(false) + ") or (" + getWhereVisible(true) + "))";
        } else {
            appFilter = "model.synchstatus_sba <> " + CodegroupUtility.MEB_SYNCHSTATUS_NEW + " and " + getWhereVisible(false);
        }

        if (canton > 0L) {
            if (showBurSynch) {
                appFilter += " and (model.canton=" + canton + " or model.bur_canton=" + canton + ")";
            } else {
                appFilter += " and model.canton=" + canton;
            }
        }

        if (!whereSelection.equals("")) {
            appFilter += " and ";
        }

        if (sortContext.getLocale() != null && codegroupId != null) {
            // sort according to locale dependent code texts
            String mainLocale = sortContext.getLocale();
            queryString = "select distinct model.schoolId, " + "(case when " + sortColumn + " is null then ''" + "      when meb_cg.code is null then to_char("
                    + sortColumn + ")" + "      else  meb_cg.codetext end) sorttext" + " from " + schoolsSubquery + " model" + " left outer join"
                    + " (select cg1.* from Codegroups cg1,"
                    + "   (select codegroupid, code, language, max(validFromYear) validFromYear from Codegroups where codegroupId = '" + codegroupId
                    + "' and language = '" + mainLocale + "' group by codegroupid, code, language" + "   ) cg2"
                    + "  where cg1.codegroupId = cg2.codegroupId and cg1.code = cg2.code and cg1.language = cg2.language and cg1.validFromYear = cg2.validFromYear   "
                    + " ) meb_cg on meb_cg.code = " + sortColumn + " where " + appFilter + whereSelection + " order by sorttext "
                    + ((sortContext.getAscSortOrder()) ? "asc" : "desc") + ", model.schoolId asc";
        } else {
            if (hasCdSort) {
                queryString = "select distinct model.schoolId, " + cdField + " sorttext from " + schoolsSubquery + " model where " + appFilter + whereSelection
                        + " order by sorttext " + ((sortContext.getAscSortOrder()) ? "asc" : "desc") + ", model.schoolId asc";
            } else {
                queryString = "select distinct model.schoolId, " + sortColumn + " from " + schoolsSubquery + " model where " + appFilter + whereSelection
                        + " order by " + sortColumn + " " + ((sortContext.getAscSortOrder()) ? "asc" : "desc") + ", model.schoolId asc";
            }
        }

        // SQL-Query: Because of HQL-Problems with from-subqueries..
        Query query = currentSession().createNativeQuery (queryString);
        if (start >= 0) {
            query.setFirstResult(start);
        }
        if (buffer > 0) {
            query.setMaxResults(buffer);
        }

        // get list of bur school ids as long
        List<Long> schoolIds = new ArrayList<Long>();
        List<Object[]> queryIdsList = query.list();
        for (Object[] row : queryIdsList) {
            schoolIds.add(((BigDecimal) row[0]).longValue());
        }

        return reloadSortedBurSchools(schoolIds);
    }

    @SuppressWarnings("unchecked")
    private List<SbaBurSchool> reloadSortedBurSchools(List<Long> schoolIds) {
        if (schoolIds == null || schoolIds.isEmpty()) {
            return new ArrayList<SbaBurSchool>();
        }

        // query deliveries including the plausi errors (in blocks of max. 1000 - restriction oracle, see Mantis 1548)
        List<Long> chunkSchoolIds = new ArrayList<Long>();
        Criterion criterion = null;
        for (int i = 0; i < schoolIds.size(); i++) {
            chunkSchoolIds.add(schoolIds.get(i));
            if ((i + 1) % MAX_EXPRESSIONS_ORACLE == 0 || i == schoolIds.size() - 1) {
                if (criterion == null) {
                    criterion = Restrictions.in("schoolId", chunkSchoolIds);
                } else {
                    criterion = Restrictions.or(criterion, Restrictions.in("schoolId", chunkSchoolIds));
                }
                chunkSchoolIds = new ArrayList<Long>();
            }
        }
        Criteria query = currentSession().createCriteria(SbaBurSchool.class).add(criterion);
        // DistinctRootEntityResultTransformer not required => order by map below already does the same
        //		query.setResultTransformer(DistinctRootEntityResultTransformer.INSTANCE);

        List<SbaBurSchool> tempList = query.list();

        // reestablish old sort order
        Map<Long, SbaBurSchool> mapById = new HashMap<Long, SbaBurSchool>(tempList.size());
        for (SbaBurSchool entity : tempList) {
            mapById.put(entity.getSchoolId(), entity);
        }
        List<SbaBurSchool> resultList = new ArrayList<SbaBurSchool>(mapById.size());
        for (Long id : schoolIds) {
            SbaBurSchool entity = mapById.get(id);
            if (entity != null) {
                resultList.add(entity);
            }
        }

        return resultList;
    }

    @Override
    public Long getMaxNrOfBurSchools(FilterContext filterContext, Long version, Long canton, boolean showBurSynch) {
        String appFilter;
        String queryString = null;
        String whereSelection = _filterUtility.getWhereFilterSelection(filterContext, "model", COLUMN_MAPPING, getStringColumns(), getDateColumns(),
                getUnderscoreColumns());
        String schoolsSubquery = _filterUtility.getPredefinedFilterSubquery(filterContext, SCHOOLS_TABLE, !showBurSynch);

        boolean hasCdFilter = hasConfigDeliveryFilter(filterContext);
        String cdField = "(case when exists (select * from sba_configdeliveries meb_cd, sba_schools_configdeliveries meb_s_cd where meb_s_cd.schoolId = model.schoolId and meb_s_cd.deliveryid = meb_cd.deliveryid and meb_cd.version = "
                + version
                + ") then (select meb_cd.deliverycode from sba_configdeliveries meb_cd, sba_schools_configdeliveries meb_s_cd where meb_s_cd.schoolId = model.schoolId and meb_s_cd.deliveryid = meb_cd.deliveryid and meb_cd.version = "
                + version + ") else '' end)";

        if (hasCdFilter) {
            whereSelection = whereSelection.replaceAll("model\\." + DELIVERYCODE, cdField);
        }

        if (showBurSynch) {
            appFilter = "((" + getWhereVisible(false) + ") or (" + getWhereVisible(true) + "))";
        } else {
            appFilter = "model.synchstatus_sba <> " + CodegroupUtility.MEB_SYNCHSTATUS_NEW + " and " + getWhereVisible(false);
        }

        if (canton > 0L) {
            if (showBurSynch) {
                appFilter += " and (model.canton=" + canton + " or model.bur_canton=" + canton + ")";
            } else {
                appFilter += " and model.canton=" + canton;
            }
        }

        if (!whereSelection.equals("")) {
            appFilter += " and ";
        }

        queryString = "select count (*) nrSchools from " + schoolsSubquery + " model where " + appFilter + whereSelection;

        // SQL-Query: Because of HQL-Problems with from-subqueries..
        Query query = currentSession().createNativeQuery (queryString).addScalar("nrSchools");

        return ((BigDecimal) query.uniqueResult()).longValue();
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<SbaBurSchool> getBurSchoolsOwnedByConfigDeliveries(List<Long> configDeliveryIds, SortContext sortContext, boolean showBurSynch) {
        String appFilter;
        String queryString = null;
        String sortColumn = _filterUtility.adaptColumnName(sortContext.getSortColumn(), "model", COLUMN_MAPPING, getUnderscoreColumns());
        String codegroupId = getCodegroupId(sortColumn);
        String cdidsSelection = configDeliveryIds.isEmpty() ? "" : " and meb_cd.deliveryId in (";
        boolean firstCdid = true;
        for (Long cdid : configDeliveryIds) {
            if (!firstCdid) {
                cdidsSelection += ",";
            }
            cdidsSelection += cdid.toString();
            firstCdid = false;
        }
        cdidsSelection += (cdidsSelection.length() == 0) ? "" : ")";

        if (showBurSynch) {
            appFilter = "((" + getWhereVisible(false) + ") or (" + getWhereVisible(true) + "))";
        } else {
            appFilter = "model.synchstatus_sba <> " + CodegroupUtility.MEB_SYNCHSTATUS_NEW + " and " + getWhereVisible(false);
        }

        if (sortContext.getLocale() != null && codegroupId != null) {
            // sort according to locale dependent code texts
            String mainLocale = sortContext.getLocale();
            queryString = "select distinct model.schoolId, " + "(case when " + sortColumn + " is null then '' " + "      when meb_cg.code is null then to_char("
                    + sortColumn + ") " + "      else  meb_cg.codetext end) sorttext "
                    + " from Sba_Schools_ConfigDeliveries meb_s_cd, Sba_ConfigDeliveries meb_cd, Schools model " + " left outer join  "
                    + " (select cg1.* from Codegroups cg1, "
                    + "   (select codegroupid, code, language, max(validFromYear) validFromYear from Codegroups where codegroupId = '" + codegroupId
                    + "' and language = '" + mainLocale + "' group by codegroupid, code, language" + "   ) cg2"
                    + "  where cg1.codegroupId = cg2.codegroupId and cg1.code = cg2.code and cg1.language = cg2.language and cg1.validFromYear = cg2.validFromYear   "
                    + " ) meb_cg on meb_cg.code = " + sortColumn + " where " + appFilter + cdidsSelection + ((cdidsSelection.length() == 0) ? "" : " and ")
                    + "model.schoolId=meb_s_cd.schoolId and meb_s_cd.deliveryId=meb_cd.deliveryId " + " order by sorttext "
                    + ((sortContext.getAscSortOrder()) ? "asc" : "desc");
        } else {
            if (sortColumn.equals("model." + DELIVERYCODE)) {
                queryString = "select distinct model.schoolId, meb_cd.deliveryCode from Schools model, Sba_Schools_ConfigDeliveries meb_s_cd, Sba_ConfigDeliveries meb_cd"
                        + " where model.schoolId=meb_s_cd.schoolId and meb_s_cd.deliveryId=meb_cd.deliveryId and " + appFilter + cdidsSelection
                        + " order by meb_cd.deliveryCode " + ((sortContext.getAscSortOrder()) ? "asc" : "desc");
            } else {
                queryString = "select distinct model.schoolId, " + sortColumn
                        + " from Schools model, Sba_Schools_ConfigDeliveries meb_s_cd, Sba_ConfigDeliveries meb_cd"
                        + " where model.schoolId=meb_s_cd.schoolId and meb_s_cd.deliveryId=meb_cd.deliveryId and " + appFilter + cdidsSelection + " order by "
                        + sortColumn + " " + ((sortContext.getAscSortOrder()) ? "asc" : "desc");
            }
        }

        Query query = currentSession().createNativeQuery (queryString);
        // get list of bur school ids as long
        List<Long> schoolIds = new ArrayList<Long>();
        List<Object[]> queryIdsList = query.list();
        for (Object[] row : queryIdsList) {
            schoolIds.add(((BigDecimal) row[0]).longValue());
        }

        return reloadSortedBurSchools(schoolIds);
    }

    protected void fillBurSchoolCantons(List<BurSchoolExt> extSchools, List<CodeGroup> cantons) {
        for (BurSchoolExt extSchool : extSchools) {
            for (CodeGroup canton : cantons) {
                if (extSchool.getCanton_cd().equals(canton.getCodeTextAbbr())) {
                    extSchool.setCantonCode(canton.getCode());
                    break;
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<BurSchoolExt> getExternalBurSchools() {
        final List<CodeGroup> cantons = (List<CodeGroup>) getHibernateTemplate().execute(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                Query q = session.createQuery("from CodeGroup as cg where cg.id.codeGroupId=:codeGroupId and cg.id.language=:language");
                q.setString("codeGroupId", "CANTON");
                q.setString("language", "de");
                return q.list();
            }
        });

        return (List<BurSchoolExt>) getHibernateTemplate().execute(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                List<BurSchoolExt> extSchools = (List<BurSchoolExt>) session.createQuery("from BurSchoolExt where stat_act_matu_flg = 1").list();
                fillBurSchoolCantons(extSchools, cantons);
                return extSchools;
            }
        });
    }

    @Override
    public SbaBurSchool insertBurSchool(SbaBurSchool burSchool) {
        getHibernateTemplate().save(burSchool);
        getHibernateTemplate().flush();
        return burSchool;
    }

    @Override
    public SbaBurSchool updateBurSchool(SbaBurSchool burSchool) {
        burSchool = (SbaBurSchool) getHibernateTemplate().merge(burSchool);
        //		getHibernateTemplate().flush();
        return burSchool;
    }

    @Override
    public void deleteBurSchool(SbaBurSchool burSchool) {
        getHibernateTemplate().delete(burSchool);
        getHibernateTemplate().flush();
    }

    /* @see ch.bfs.meb.sba.server.integration.repository.IBurSchoolRepository#findActiveSchool(java.lang.String, java.lang.String, java.lang.Long) */
    @Override
    public SbaBurSchool findActiveSchool(final String idType, final String id, final Long canton, final Long year) {
        if (CodegroupUtility.MEB_SCHOOL_UNKNOWN.equalsIgnoreCase(idType)) {
            return null; //Mantis ticket 2040
        }

        if (idType.equals(CodegroupUtility.MEB_SCHOOL_CH_BUR)) {
            long nr;
            try {
                nr = Long.parseLong(id);
            } catch (NumberFormatException e) {
                return null;
            }
            final long burNr = nr;

            // bur number
            return (SbaBurSchool) getHibernateTemplate().execute(new HibernateCallback() {
                @Override
                public Object doInHibernate(Session session) throws HibernateException {
                    Query query = session.createQuery(
                            "from SbaBurSchool where burNr=:id and is_sba=1 and activityStatus in (1,4,5) and (validFrom_sba is null or :year>=validFrom_sba) and (validTo_sba is null or :year<=validTo_sba)");
                    query.setLong("id", burNr);
                    query.setLong("year", year);
                    return query.uniqueResult();
                }
            });
        } else {
            // cantonal code
            return (SbaBurSchool) getHibernateTemplate().execute(new HibernateCallback() {
                @Override
                public Object doInHibernate(Session session) throws HibernateException {
                    Query query = session.createQuery(
                            "from SbaBurSchool where cantonalCode_sba=:id and canton=:canton and is_sba=1 and activityStatus in (1,4,5) and (validFrom_sba is null or :year>=validFrom_sba) and (validTo_sba is null or :year<=validTo_sba)");
                    query.setString("id", id);
                    query.setLong("canton", canton);
                    query.setLong("year", year);
                    return query.uniqueResult();
                }
            });
        }
    }

    /**
     * Gets the physical code group id as stored in database for a given column
     * name.
     *
     * @param colName column id of database table
     * @return physical code group id as stored in database
     */
    protected String getCodegroupId(String colName) {
        if (colName.equals("model." + MUNICIPALITY)) {
            return CodegroupUtility.MUNICIPALITY;
        } else if (colName.equals("model." + SYNCHSTATUS_SBA)) {
            return CodegroupUtility.MEB_SYNCHSTATUS;
        } else if (colName.equals("model." + BUR_MUNICIPALITY)) {
            return CodegroupUtility.MUNICIPALITY;
        }

        return null;
    }

    /** Returns all columns with underscores of according db table. */
    protected List<String> getUnderscoreColumns() {
        ArrayList<String> underscoreColumns = new ArrayList<String>();
        underscoreColumns.add(VALIDFROM_SDL_SSP);
        underscoreColumns.add(VALIDTO_SDL_SSP);
        underscoreColumns.add(VALIDFROM_SSP);
        underscoreColumns.add(VALIDTO_SSP);
        underscoreColumns.add(VALIDFROM_SBA);
        underscoreColumns.add(VALIDTO_SBA);
        underscoreColumns.add(BUR_CANTON);
        underscoreColumns.add(BUR_LABEL);
        underscoreColumns.add(BUR_MUNICIPALITY);
        underscoreColumns.add(BUR_ACTIVITYSTATUS);
        underscoreColumns.add(SYNCHSTATUS_SDL);
        underscoreColumns.add(CANTONALCODE_SDL);
        underscoreColumns.add(BUR_CANTONALCODE_SDL);
        underscoreColumns.add(IS_SDL);
        underscoreColumns.add(BUR_IS_SDL);
        underscoreColumns.add(BUR_VALIDFROM_SDL_SSP);
        underscoreColumns.add(BUR_VALIDTO_SDL_SSP);
        underscoreColumns.add(BUR_VALIDFROM_SSP);
        underscoreColumns.add(BUR_VALIDTO_SSP);
        underscoreColumns.add(SYNCHSTATUS_SBA);
        underscoreColumns.add(CANTONALCODE_SBA);
        underscoreColumns.add(BUR_CANTONALCODE_SBA);
        underscoreColumns.add(IS_SBA);
        underscoreColumns.add(BUR_IS_SBA);
        underscoreColumns.add(SYNCHSTATUS_SBA);
        underscoreColumns.add(CANTONALCODE_SBA);
        underscoreColumns.add(BUR_CANTONALCODE_SBA);
        underscoreColumns.add(IS_SBA);
        underscoreColumns.add(BUR_IS_SBA);
        underscoreColumns.add(BUR_VALIDFROM_SBA);
        underscoreColumns.add(BUR_VALIDTO_SBA);
        underscoreColumns.add(IS_PUBLIC_SCHOOL);
        underscoreColumns.add(IS_PRIVATE_SUBSIDISED_SCHOOL);
        underscoreColumns.add(IS_PRIVATE_NOT_SUBSIDISED_SCHOOL);
        underscoreColumns.add(IS_SPECIAL_SCHOOL);
        underscoreColumns.add(IS_PUBLIC_SCHOOL_BUR);
        underscoreColumns.add(IS_PRIVATE_SUBSIDISED_SCHOOL_BUR);
        underscoreColumns.add(IS_PRIVATE_NOT_SUBSIDISED_SCHOOL_BUR);
        underscoreColumns.add(IS_SPECIAL_SCHOOL_BUR);
        return underscoreColumns;
    }

    /** Returns all string columns of according db table. */
    protected List<String> getStringColumns() {
        ArrayList<String> stringColumns = new ArrayList<String>();
        stringColumns.add(LABEL);
        stringColumns.add(MUNICIPALITY);
        stringColumns.add(DELIVERYCODE);
        return stringColumns;
    }

    /** Returns all date columns of according db table. */
    protected List<String> getDateColumns() {
        ArrayList<String> dateColumns = new ArrayList<String>();
        dateColumns.add(StringUtils.asCamelCase(VALIDFROM_SDL_SSP));
        dateColumns.add(StringUtils.asCamelCase(VALIDTO_SDL_SSP));
        dateColumns.add(StringUtils.asCamelCase(VALIDFROM_SSP));
        dateColumns.add(StringUtils.asCamelCase(VALIDTO_SSP));
        dateColumns.add(StringUtils.asCamelCase(VALIDFROM_SBA));
        dateColumns.add(StringUtils.asCamelCase(VALIDTO_SBA));
        return dateColumns;
    }

    private boolean isConfiguredForVersion(SbaBurSchool school, Long version) {
        if (school.getConfigDeliveries() == null) {
            return false;
        }
        for (SbaConfigDelivery configDelivery : school.getConfigDeliveries()) {
            if (configDelivery.getVersion().equals(version)) {
                return true;
            }
        }
        return false;
    }

    /* @see ch.bfs.meb.sba.server.integration.repository.IBurSchoolRepository#getNotConfiguredSchoolsForVersion(java.lang.Long) */
    @SuppressWarnings("unchecked")
    @Override
    public List<SbaBurSchool> getNotConfiguredSchoolsForVersion(final Long version) {
        return (List<SbaBurSchool>) getHibernateTemplate().execute(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                Query query = session.createQuery(
                        "from SbaBurSchool s where s.is_sba=1 and s.activityStatus in (1, 4, 5) and (s.validFrom_sba is null or :version>= s.validFrom_sba) and (s.validTo_sba is null or :version <= s.validTo_sba) order by s.canton, s.label");
                query.setLong("version", version);
                // Remove Schools where a configdelivery is set
                List<SbaBurSchool> sbaSchools = (List<SbaBurSchool>) query.list();
                List<SbaBurSchool> notConfiguredSchools = new ArrayList<SbaBurSchool>();
                for (SbaBurSchool school : sbaSchools) {
                    if (!isConfiguredForVersion(school, version)) {
                        notConfiguredSchools.add(school);
                    }
                }

                return notConfiguredSchools;
            }
        });
    }

    /* @see ch.bfs.meb.sba.server.integration.repository.IBurSchoolRepository#existsSchoolForBurSchool(ch.bfs.meb.sba.server.integration.dto.SbaBurSchool) */
    @Override
    public boolean existsQualificationForBurSchool(final SbaBurSchool burSchool) {
        Long nrSchools = (Long) getHibernateTemplate().execute(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
org.hibernate.query.Query<Long> query = session.createQuery("select count(q) from SbaQualification q " +
        "where q.canton=:canton and q.version=:version and " +
        "((q.schoolIdType=:schoolIdType and q.schoolId=:schoolId) or " +
        "(q.schoolIdType<>:schoolIdType and q.schoolId=:cantonalCode))", Long.class);

                query.setParameter("canton", burSchool.getCanton());
                query.setParameter("version", burSchool.getVersion());
                query.setParameter("schoolIdType", CodegroupUtility.MEB_SCHOOL_CH_BUR);
                query.setParameter("schoolId", burSchool.getBurNr().toString());
                query.setParameter("cantonalCode", burSchool.getCantonalCode_sba());

                return query.uniqueResult();
            }
        });
        return nrSchools > 0;
    }

    @Override
    public void lockBurSchools() throws HibernateException {
        Query query = currentSession().createNativeQuery ("lock table Schools in exclusive mode nowait");
        query.executeUpdate();
    }
}
