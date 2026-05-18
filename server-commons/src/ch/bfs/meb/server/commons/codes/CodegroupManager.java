/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: server-commons

  $Id$
 */
package ch.bfs.meb.server.commons.codes;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.bfs.meb.exception.MebUncheckedException;
import ch.bfs.meb.server.commons.integration.dto.CodeGroup;
import ch.bfs.meb.server.commons.integration.repository.ICodeGroupRepository;
import ch.bfs.meb.util.StringUtils;

/**
 * Utility class for cached access to codegroups. Actual implementation doesn't
 * use Ehcache.
 *
 * @author lsc
 * @version $Revision$
 */
public class CodegroupManager implements ICodegroupManager {
    private final static Logger LOG = LoggerFactory.getLogger(CodegroupManager.class);

    private ICodeGroupRepository _codegroupRepository;

    private String[] _hourlyRefreshedCodegroups;

    private String[] _dailyRefreshedCodegroups;

    private final Map<String, Map<Long, List<CodeGroup>>> _businessCache = new HashMap<String, Map<Long, List<CodeGroup>>>();

    private Date _lastHourlyRefresh;
    private Date _lastDailyRefresh;

    public void setCodegroupRepository(ICodeGroupRepository codegroupRepository) {
        _codegroupRepository = codegroupRepository;
    }

    public void setHourlyRefreshedCodegroups(String[] hourlyRefreshedCodegroups) {
        _hourlyRefreshedCodegroups = hourlyRefreshedCodegroups;
    }

    public void setDailyRefreshedCodegroups(String[] dailyRefreshedCodegroups) {
        _dailyRefreshedCodegroups = dailyRefreshedCodegroups;
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.server.commons.codes.ICodegroupManager#refreshCache()
     */
    public void refreshCache() {
        // set date of current refresh cycle
        Calendar refreshCalendar = Calendar.getInstance();
        // remove minor time component
        refreshCalendar.set(Calendar.SECOND, 0);
        refreshCalendar.set(Calendar.MILLISECOND, 0);
        Date refreshDate = refreshCalendar.getTime();

        String[] hourlyRefreshedCodegroups = _hourlyRefreshedCodegroups;
        String[] dailyRefreshedCodegroups = _dailyRefreshedCodegroups;

        // refresh hourly cache
        if (hourlyRefreshedCodegroups != null && hourlyRefreshedCodegroups.length > 0) {
            // create reference time for hourly schedule
            Calendar refCalendar = Calendar.getInstance();
            refCalendar.setTime(refreshDate);
            // remove minor time component
            refCalendar.set(Calendar.MINUTE, 0);
            Date refDate = refCalendar.getTime();

            if (_lastHourlyRefresh == null || refDate.compareTo(_lastHourlyRefresh) > 0) {
                for (String codeGroupId : hourlyRefreshedCodegroups) {
                    if (StringUtils.isEmpty(codeGroupId)) {
                        continue;
                    }

                    List<CodeGroup> codeGroups = _codegroupRepository.getCodesForGroup(codeGroupId);

                    LOG.info("Codegroups loaded (hourly refresh): {}, size is {}.", codeGroupId, codeGroups.size());

                    Map<Long, List<CodeGroup>> cachedMap = new HashMap<Long, List<CodeGroup>>();
                    for (CodeGroup codeGroup : codeGroups) {
                        List<CodeGroup> codeGroupsByCode = cachedMap.get(codeGroup.getCode());
                        if (codeGroupsByCode == null) {
                            codeGroupsByCode = new ArrayList<CodeGroup>();
                            cachedMap.put(codeGroup.getCode(), codeGroupsByCode);
                        }
                        codeGroupsByCode.add(codeGroup);
                    }

                    // lock cache during replacement
                    synchronized (_businessCache) {
                        _businessCache.put(codeGroupId.toUpperCase(), cachedMap);
                    }
                }

                _lastHourlyRefresh = refreshDate;
                LOG.debug("hourly: " + _lastHourlyRefresh);
            }
        }

        // refresh daily cache
        if (dailyRefreshedCodegroups != null && dailyRefreshedCodegroups.length > 0) {
            // create reference time for daily schedule (at 3:00 o'clock)
            Calendar refCalendar = Calendar.getInstance();
            refCalendar.setTime(refreshDate);
            // remove minor time component
            refCalendar.set(Calendar.MINUTE, 1);
            // set to cache refresh time of the current day
            refCalendar.set(Calendar.HOUR_OF_DAY, 3);
            // check if date is in the future, then switch one day back
            if (refCalendar.compareTo(refreshCalendar) > 0) {
                refCalendar.add(Calendar.DAY_OF_MONTH, -1);
            }
            Date refDate = refCalendar.getTime();

            if (_lastDailyRefresh == null || refDate.compareTo(_lastDailyRefresh) > 0) {
                for (String codeGroupId : dailyRefreshedCodegroups) {
                    if (StringUtils.isEmpty(codeGroupId)) {
                        continue;
                    }

                    List<CodeGroup> codeGroups = _codegroupRepository.getCodesForGroup(codeGroupId);

                    LOG.info("Codegroups loaded (last daily refresh): {}, size is {}.", codeGroupId, codeGroups.size());

                    Map<Long, List<CodeGroup>> cachedMap = new HashMap<Long, List<CodeGroup>>();
                    for (CodeGroup codeGroup : codeGroups) {
                        List<CodeGroup> codeGroupsByCode = cachedMap.get(codeGroup.getCode());
                        if (codeGroupsByCode == null) {
                            codeGroupsByCode = new ArrayList<CodeGroup>();
                            cachedMap.put(codeGroup.getCode(), codeGroupsByCode);
                        }
                        codeGroupsByCode.add(codeGroup);
                    }

                    // lock cache during replacement
                    synchronized (_businessCache) {
                        _businessCache.put(codeGroupId.toUpperCase(), cachedMap);
                    }
                }

                _lastDailyRefresh = refreshDate;
                LOG.debug("daily: " + _lastDailyRefresh);
            }
        }
    }

    private List<CodeGroup> getActualCodeGroupsList(Map<Long, List<CodeGroup>> cachedMap, String language) {
        List<CodeGroup> actualCodeGroups = new ArrayList<CodeGroup>();

        // one iteration for each code
        for (List<CodeGroup> codeList : cachedMap.values()) {
            Map<String, CodeGroup> tempMap = new HashMap<String, CodeGroup>();
            for (CodeGroup cg : codeList) {
                // filter by language
                if (!StringUtils.isEmpty(language) && !language.equalsIgnoreCase(cg.getLanguage())) {
                    continue;
                }

                // filter for most current
                String tempMapId = (cg.getCanton() == null ? "" : cg.getCanton().toString() + "_") + cg.getLanguage();
                CodeGroup cgInMap = tempMap.get(tempMapId);
                if (cgInMap == null) {
                    tempMap.put(tempMapId, cg);
                } else {
                    if (cg.getValidFromYear() == null) {
                        continue;
                    }

                    if (cgInMap.getValidFromYear() != null && cgInMap.getValidFromYear().compareTo(cg.getValidFromYear()) > 0) {
                        continue;
                    }

                    tempMap.put(tempMapId, cg);
                }
            }

            actualCodeGroups.addAll(tempMap.values());
        }

        return actualCodeGroups;
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.server.commons.codes.ICodegroupManager#isInitialized()
     */
    public boolean isInitialized() {

        String[] hourlyRefreshedCodegroups = _hourlyRefreshedCodegroups;
        String[] dailyRefreshedCodegroups = _dailyRefreshedCodegroups;

        List<String> codeGroups = new ArrayList<String>();
        if (hourlyRefreshedCodegroups != null && hourlyRefreshedCodegroups.length > 0) {
            for (String hourlyRefreshedCodegroup : hourlyRefreshedCodegroups) {
                if (!StringUtils.isEmpty(hourlyRefreshedCodegroup)) {
                    codeGroups.add(hourlyRefreshedCodegroup.toUpperCase());
                }
            }
        }
        if (dailyRefreshedCodegroups != null && dailyRefreshedCodegroups.length > 0) {
            for (String dailyRefreshedCodegroup : dailyRefreshedCodegroups) {
                if (!StringUtils.isEmpty(dailyRefreshedCodegroup)) {
                    codeGroups.add(dailyRefreshedCodegroup.toUpperCase());
                }
            }
        }

        if (codeGroups.size() > 0) {
            synchronized (_businessCache) {
                return _businessCache.keySet().containsAll(codeGroups);
            }
        }

        return true;
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.server.commons.codes.ICodegroupManager#isInitialized(java.lang.String)
     */
    public boolean isInitialized(String groupId) {
        if (StringUtils.isEmpty(groupId)) {
            return false;
        }
        groupId = groupId.toUpperCase();

        synchronized (_businessCache) {
            return _businessCache.keySet().contains(groupId);
        }
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.server.commons.codes.ICodegroupManager#getCodeGroupsByGroupId(java.lang.String, java.lang.String)
     */
    public List<CodeGroup> getCodeGroupsByGroupId(String groupId, String language) {
        if (StringUtils.isEmpty(groupId)) {
            return null;
        }
        groupId = groupId.toUpperCase();

        Map<Long, List<CodeGroup>> cachedMap = null;
        synchronized (_businessCache) {
            if (_businessCache.containsKey(groupId)) {
                cachedMap = _businessCache.get(groupId);
            }
        }

        if (cachedMap == null || cachedMap.size() == 0) {
            return null;
        }

        List<CodeGroup> codeGroups = new ArrayList<CodeGroup>();
        if (StringUtils.isEmpty(language)) {
            for (List<CodeGroup> localCodeGroups : cachedMap.values()) {
                codeGroups.addAll(localCodeGroups);
            }
        } else {
            // filter by language
            for (List<CodeGroup> localCodeGroups : cachedMap.values()) {
                for (CodeGroup codeGroup : localCodeGroups) {
                    if (language.equalsIgnoreCase(codeGroup.getLanguage())) {
                        codeGroups.add(codeGroup);
                    }

                }
            }
        }
        return codeGroups;
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.server.commons.codes.ICodegroupManager#getActualCodeGroupsByGroupId(java.lang.String, java.lang.String)
     */
    public List<CodeGroup> getActualCodeGroupsByGroupId(String groupId, String language) {
        if (StringUtils.isEmpty(groupId)) {
            LOG.error("codegroup.cache.error.message: GroupID is empty ('" + groupId + "').");
            throw new MebUncheckedException("codegroup.cache.error.message");
        }
        groupId = groupId.toUpperCase();

        Map<Long, List<CodeGroup>> cachedMap = null;
        synchronized (_businessCache) {
            if (_businessCache.containsKey(groupId)) {
                cachedMap = _businessCache.get(groupId);
            }
        }

        List<CodeGroup> codeGroupsList = getActualCodeGroupsList(cachedMap, language);
        if (codeGroupsList == null || codeGroupsList.size() == 0) {
            return null;
        }

        return codeGroupsList;
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.server.commons.codes.ICodegroupManager#getCodeGroupsByGroupIdAndLanguage(java.lang.String, java.lang.String)
     */
    public List<CodeGroup> getCodeGroupsByGroupIdAndLanguage(String groupId, String language) {
        return getCodeGroupsByGroupIdAndLanguage(groupId, language, false);
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.server.commons.codes.ICodegroupManager#getCodeGroupsByGroupIdAndLanguage(java.lang.String, java.lang.String, boolean)
     */
    public List<CodeGroup> getCodeGroupsByGroupIdAndLanguage(String groupId, String language, boolean onlyCurrent) {
        if (StringUtils.isEmpty(groupId)) {
            return null;
        }
        groupId = groupId.toUpperCase();

        Map<Long, List<CodeGroup>> cachedMap = null;
        synchronized (_businessCache) {
            if (_businessCache.containsKey(groupId)) {
                cachedMap = _businessCache.get(groupId);
            }
        }

        if (cachedMap == null || cachedMap.size() == 0) {
            return null;
        }

        List<CodeGroup> codeGroups = new ArrayList<CodeGroup>();
        if (StringUtils.isEmpty(language)) {
            for (List<CodeGroup> localCodeGroups : cachedMap.values()) {
                codeGroups.addAll(localCodeGroups);
            }
        } else {
            // filter by language
            for (List<CodeGroup> localCodeGroups : cachedMap.values()) {
                for (CodeGroup codeGroup : localCodeGroups) {
                    if (language.equalsIgnoreCase(codeGroup.getLanguage())) {
                        codeGroups.add(codeGroup);
                    }

                }
            }
        }

        if (onlyCurrent) {
            List<CodeGroup> currentCodegroups = new ArrayList<CodeGroup>();
            for (CodeGroup c : codeGroups) {
                if (c.getValidToYear() == null) {
                    currentCodegroups.add(c);
                }
            }
            // Sort by code
            Collections.sort(currentCodegroups, new Comparator<CodeGroup>() {
                public int compare(CodeGroup codegroup1, CodeGroup codegroup2) {
                    return codegroup1.getCode().compareTo(codegroup2.getCode());
                }
            });
            return currentCodegroups;
        }

        return codeGroups;
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.server.commons.codes.ICodegroupManager#getCode(java.lang.String, java.lang.Long, java.lang.String, java.lang.Long)
     */
    public CodeGroup getCode(String groupId, Long code, String language, Long version) {

        if (StringUtils.isEmpty(groupId)) {
            return null;
        }
        groupId = groupId.toUpperCase();

        if (code == null) {
            return null;
        }

        if (StringUtils.isEmpty(language)) {
            return null;
        }

        if (version == null) {
            return null;
        }

        Map<Long, List<CodeGroup>> cachedMap = null;
        synchronized (_businessCache) {
            if (_businessCache.containsKey(groupId)) {
                cachedMap = _businessCache.get(groupId);
            }
        }

        if (cachedMap == null || cachedMap.size() == 0) {
            return null;
        }

        List<CodeGroup> codeGroupsByCode = cachedMap.get(code);
        if (codeGroupsByCode != null) {
            for (CodeGroup codeGroup : codeGroupsByCode) {
                if (language.equalsIgnoreCase(codeGroup.getLanguage())) {
                    if ((codeGroup.getValidFromYear() == null || codeGroup.getValidFromYear() <= version)
                            && (codeGroup.getValidToYear() == null || codeGroup.getValidToYear() >= version)) {
                        return codeGroup;
                    }
                }
            }
        }

        return null;
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.server.commons.codes.ICodegroupManager#contains(java.lang.String, java.lang.Long, java.lang.Long, java.lang.Long, boolean)
     */
    public boolean contains(String groupId, Long code, Long canton, Long version, boolean searchInAllCantons) {
        if (StringUtils.isEmpty(groupId)) {
            return false;
        }
        groupId = groupId.toUpperCase();

        Map<Long, List<CodeGroup>> cachedMap;
        synchronized (_businessCache) {
            if (_businessCache.containsKey(groupId)) {
                cachedMap = _businessCache.get(groupId);
            } else {
                return false;
            }
        }

        List<CodeGroup> codeGroupsByCode = cachedMap.get(code);
        if (codeGroupsByCode != null) {
            for (CodeGroup codegroupEntry : codeGroupsByCode) {
                if (searchInAllCantons || canton != null && canton.equals(codegroupEntry.getCanton()) || canton == null && codegroupEntry.getCanton() == null) {
                    // is the code valid?
                    if ((codegroupEntry.getValidFromYear() == null || version >= codegroupEntry.getValidFromYear())
                            && (codegroupEntry.getValidToYear() == null || version <= codegroupEntry.getValidToYear())) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.server.commons.codes.ICodegroupManager#contains(java.lang.String, java.lang.Long, java.lang.Long, java.lang.Long)
     */
    public boolean contains(String groupId, Long code, Long canton, Long version) {
        return contains(groupId, code, canton, version, false);
    }
}