package ch.bfs.meb.sbg.server.integration.repository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;

import ch.admin.bfs.sbg.transfer.FilterContext;
import ch.admin.bfs.sbg.transfer.Plausierror;
import ch.admin.bfs.sbg.transfer.SortContext;
import ch.admin.bfs.sbg.util.FilterUtility;
import ch.bfs.meb.sbg.server.integration.dto.SbgEvent;
import ch.bfs.meb.util.CodegroupUtility;

/**
 * Repository for SBG Events.
 *
 * @author $Author: jfu $
 * @version $Revision: 995 $
 */
public class EventRepository extends HibernateDaoSupport implements IEventRepository {
    // property constants
    public static final String PID = "pid";
    public static final String TYPE = "type";
    public static final String CONTRACTNR = "contractNr";
    public static final String PROFESSIONCODE = "professionCode";
    public static final String CONTRACTTYPE = "contractType";
    public static final String CONTRACTDATE = "contractDate";
    public static final String USERCOMMENT = "userComment";
    public static final String EXAMTYPE = "examType";
    public static final String EXAMRESULT = "examResult";
    public static final String CANCELREASON = "cancelReason";
    public static final String CANCELDATE = "cancelDate";
    public static final String BURNR = "burnr";
    public static final String KANTLBCODE = "kantLbCode";
    public static final String FIRMNAME = "firmName";
    public static final String FIRMSTREET = "firmStreet";
    public static final String FIRMSTREETNR = "firmStreetNr";
    public static final String FIRMPLZ = "firmPlz";
    public static final String FIRMMUNICIPALITY = "firmMunicipality";
    public static final String FLAGLBV = "flagLbv";
    public static final String MODUSER = "modUser";
    public static final String MODDATE = "modDate";
    public static final String PLAUSISTATUS = "plausiStatus";

    @Override
    public SbgEvent insertEvent(SbgEvent event) {
        getHibernateTemplate().save(event);
        getHibernateTemplate().flush();
        return event;
    }

    @Override
    public SbgEvent updateEvent(SbgEvent event) {
        event = (SbgEvent) getHibernateTemplate().merge(event);
        getHibernateTemplate().flush();
        return event;
    }

    @Override
    public void deleteEvent(SbgEvent event) {
        getHibernateTemplate().delete(event);
        getHibernateTemplate().flush();
    }

    @Override
    public void clearEventFromCache(SbgEvent persistentEvent) {
        currentSession().evict(persistentEvent);
        for (Plausierror error : persistentEvent.getPlausierrors()) {
            getHibernateTemplate().evict(error);
        }
    }

    @Override
    public SbgEvent findById(Long eventId) {
        return (SbgEvent) getHibernateTemplate().get(SbgEvent.class, eventId);
    }

    @Override
    public List<SbgEvent> findByPids(List<Long> pids, SortContext sortContext) {
        String queryString;
        String codegroupId = getCodegroupId(sortContext.getSortColumn());

        String pidsSelection = pids.isEmpty() ? "" : "model.pid in (";
        boolean firstPid = true;
        for (Long pid : pids) {
            if (!firstPid) {
                pidsSelection += ",";
            }
            pidsSelection += pid.toString();
            firstPid = false;
        }
        pidsSelection += (pidsSelection.length() == 0) ? "" : ")";

        if (sortContext.getLocale() != null && codegroupId != null) {
            // sort according to locale dependent code texts
            // Tricky, see above
            // Even trickier: HQL does not support union, so we have to do
            // it in SQL!!
            String mainLocale = sortContext.getLocale().substring(0, 2);
            queryString = "select {model.*}, cg.codetext sorttext from " + "Event model, " + "Codegroups cg where " + pidsSelection
                    + ((pidsSelection.length() <= 0) ? "" : " and") + " (model." + sortContext.getSortColumn() + " = cg.code" + " and cg.codegroupid = '"
                    + codegroupId + "' and cg.language = '" + mainLocale + "')" + " union " + "select {model.*}, to_char(model." + sortContext.getSortColumn()
                    + ") sorttext from " + "Event model where " + pidsSelection + ((pidsSelection.length() <= 0) ? "" : " and") + " ((select count(*) from "
                    + "Codegroups cg where model." + sortContext.getSortColumn() + " = cg.code" + " and cg.codegroupId = '" + codegroupId + "') = 0)"
                    + " union " + "select {model.*}, '' sorttext from " + "Event model where " + pidsSelection + ((pidsSelection.length() <= 0) ? "" : " and")
                    + " (model." + sortContext.getSortColumn() + " is null)" + " order by sorttext " + ((sortContext.getAscSortOrder()) ? "ASC" : "DESC");
            SQLQuery query = currentSession().createSQLQuery(queryString).addEntity("model", SbgEvent.class);
            return query.list();
        } else {
            queryString = "select distinct model from SbgEvent as model left join fetch model.plausierrors where " + pidsSelection + " order by model."
                    + sortContext.getSortColumn() + " " + ((sortContext.getAscSortOrder()) ? "ASC" : "DESC");
            return currentSession().createQuery(queryString).list();
        }
    }

    /**
     * Gets a list with events from the database
     *
     * @param start         Startrow
     * @param buffer        Bufferlength
     * @param sortContext   Context used for sorting
     * @param filterContext Context used fro filtering
     * @return
     */
    @Override
    public List<SbgEvent> getPartial(int start, int buffer, SortContext sortContext, FilterContext filterContext, Long version, Long canton) {
        String queryString;
        String codegroupId = getCodegroupId(sortContext.getSortColumn());
        String whereSelection = FilterUtility.getWhereFilterSelection(filterContext, getStringColumns(), getDateColumns());
        String eventSubquery = FilterUtility.getPredefinedFilterSubquery(filterContext, FilterUtility.EVENT_TABLE);

        if (canton != null && canton > 0L) {
            if (whereSelection.length() > 0) {
                whereSelection += " and ";
            }
            whereSelection += "model.canton=" + canton;
        }

        if (version != null) {
            if (whereSelection.length() > 0) {
                whereSelection += " and ";
            }
            whereSelection += "model.version=" + version;
        }

        if (sortContext.getLocale() != null && codegroupId != null) {
            // sort according to locale dependent code texts
            // Now, this is quite tricky. We have to take care of three
            // different cases:
            // 1. codes with a referenced value in the respective codegroup
            // 2. codes without a referenced value in the codegroup (some
            // inconsistency, but we have to
            // return those values anyway)
            // 3. null values
            // We do this with a sql union over these three cases
            String mainLocale = sortContext.getLocale().substring(0, 2);
            queryString = "select {model.*}, model.eventid sortid, cg.codetext sorttext from " + eventSubquery + " model, " + "Codegroups cg where "
                    + whereSelection + ((whereSelection.length() <= 0) ? "" : " and") + " (model." + sortContext.getSortColumn() + " = cg.code"
                    + " and cg.codegroupId = '" + codegroupId + "' and cg.language = '" + mainLocale + "')" + " union "
                    + "select {model.*}, model.eventid sortid, to_char(model." + sortContext.getSortColumn() + ") sorttext from " + eventSubquery
                    + " model where " + whereSelection + ((whereSelection.length() <= 0) ? "" : " and") + " ((select count(*) from "
                    + "Codegroups cg where model." + sortContext.getSortColumn() + " = cg.code" + " and cg.codegroupId = '" + codegroupId + "') = 0)"
                    + " union " + "select {model.*}, model.eventid sortid, '' sorttext from " + eventSubquery + " model where " + whereSelection
                    + ((whereSelection.length() <= 0) ? "" : " and") + " (model." + sortContext.getSortColumn() + " is null)" + " order by sorttext "
                    + ((sortContext.getAscSortOrder()) ? "ASC" : "DESC") + ", sortid asc";
        } else {
            // SQL hint inserted in order to improve performance on oracle 11.2.0.4.0
            queryString = "select /*+ INDEX(model EVENT_VERSION) */ {model.*} from " + eventSubquery + " model "
                    + ((whereSelection.length() <= 0) ? "" : " where " + whereSelection) + " order by " + "model." + sortContext.getSortColumn() + " "
                    + ((sortContext.getAscSortOrder()) ? "asc" : "desc") + ", model.eventid asc";
        }
        SQLQuery query = currentSession().createSQLQuery(queryString).addEntity("model", SbgEvent.class);

        query.setFetchSize(500);
        if (start >= 0) {
            query.setFirstResult(start);
        }
        if (buffer > 0) {
            query.setMaxResults(buffer);
        }

        return (List<SbgEvent>) query.list();
    }

    /**
     * Gets the number of events from the database, filtered with filterContext
     *
     * @param filterContext Context used for filtering
     * @return number of events
     */
    @Override
    public Long getNrEvents(FilterContext filterContext, Long version, Long canton) {
        String queryString;
        String whereSelection = FilterUtility.getWhereFilterSelection(filterContext, getStringColumns(), getDateColumns());
        String eventSubquery = FilterUtility.getPredefinedFilterSubquery(filterContext, FilterUtility.EVENT_TABLE);

        if (canton != null && canton > 0L) {
            if (whereSelection.length() > 0) {
                whereSelection += " and ";
            }
            whereSelection += "model.canton=" + canton;
        }

        if (version != null) {
            if (whereSelection.length() > 0) {
                whereSelection += " and ";
            }
            whereSelection += "model.version=" + version;
        }

        queryString = "select count (*) nrEvents from " + eventSubquery + " model " + ((whereSelection.length() <= 0) ? "" : " where " + whereSelection);

        // SQL-Query: Because of HQL-Problems with from-subqueries..
        SQLQuery query = currentSession().createSQLQuery(queryString).addScalar("nrEvents");

        return ((BigDecimal) query.uniqueResult()).longValue();
    }

    /**
     * Gets the contract number and profession code from all the events of a
     * delivery.
     *
     * @param canton the canton of the delivery
     * @param year   the year of the delivery
     * @return a map with key contract number and value profession code
     */
    @Override
    public Map<Long, Long> getProfCodeForContracts(Long canton, Long year) {
        Query query = currentSession().createQuery("select e.contractNr, e.professionCode " + "from SbgEvent e, PersistPerson p, PersistDelivery d "
                + "where e.pid=p.pid and p.deliveryId=d.deliveryid and " + "d.canton=? and d.version=?");
        query.setParameter(0, canton);
        query.setParameter(1, year);
        List<Object[]> resultList = query.list();

        Map<Long, Long> resultMap = new HashMap<Long, Long>(resultList.size());

        for (Object[] row : resultList) {
            resultMap.put((Long) row[0], (Long) row[1]);
        }

        return resultMap;
    }

    public void updateValidationStatus(Long personId, boolean isValidated) {
        Query query = currentSession().createQuery("update SbgEvent set isValidated = ? where pid = ?");
        query.setParameter(0, isValidated);
        query.setParameter(1, personId);
        query.executeUpdate();
    }

    /**
     * Gets the physical code group id as stored in database for a given column
     * name.
     *
     * @param colName column id of database table
     * @return physical code group id as stored in database
     */
    protected String getCodegroupId(String colName) {
        String codegroupId = null;
        if (colName.equals(TYPE)) {
            codegroupId = CodegroupUtility.SBG_EVENTTYPE;
        } else if (colName.equals(PROFESSIONCODE)) {
            codegroupId = CodegroupUtility.PROFESSIONCODE;
        } else if (colName.equals(CONTRACTTYPE)) {
            codegroupId = CodegroupUtility.CONTRACTTYPE;
        } else if (colName.equals(EXAMTYPE)) {
            codegroupId = CodegroupUtility.EXAMTYPE;
        } else if (colName.equals(EXAMRESULT)) {
            codegroupId = CodegroupUtility.EXAMRESULT;
        } else if (colName.equals(CANCELREASON)) {
            codegroupId = CodegroupUtility.CANCELREASON;
        } else if (colName.equals(PLAUSISTATUS)) {
            codegroupId = CodegroupUtility.SBG_PLAUSISTATUS;
        }

        return codegroupId;
    }

    /**
     * Returns all string columns of according db table
     */
    protected List<String> getStringColumns() {
        ArrayList<String> stringColumns = new ArrayList<String>();
        stringColumns.add(USERCOMMENT);
        stringColumns.add(MODUSER);
        stringColumns.add(KANTLBCODE);
        stringColumns.add(FIRMNAME);
        stringColumns.add(FIRMSTREET);
        stringColumns.add(FIRMSTREETNR);
        stringColumns.add(FIRMMUNICIPALITY);
        return stringColumns;
    }

    /**
     * Returns all date columns of according db table
     */
    protected List<String> getDateColumns() {
        ArrayList<String> dateColumns = new ArrayList<String>();
        dateColumns.add(CONTRACTDATE);
        dateColumns.add(CANCELDATE);
        dateColumns.add(MODDATE);
        return dateColumns;
    }
}
