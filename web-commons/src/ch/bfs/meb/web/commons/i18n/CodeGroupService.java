/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: web-commons

  $Id$

 */
package ch.bfs.meb.web.commons.i18n;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import ch.bfs.meb.exception.MebUncheckedException;
import ch.bfs.meb.util.StringUtils;

/**
 * Implementation of the common client side code group service. Contains the
 * code group caching for the client.
 */
public class CodeGroupService implements ICodeGroupService, InitializingBean {
    private final static Logger LOG = LoggerFactory.getLogger(CodeGroupService.class);

    private static final Long FIRST_CANTON = 1L;
    private static final Long LAST_CANTON = 26L;

    private ICodeGroupServiceProvider _codeGroupServiceProvider;

    private String[] _hourlyRefreshedCodegroups;

    private String[] _dailyRefreshedCodegroups;

    private int _delayMinutes = 5;

    private final Map<String, ICodeGroupCache> _businessCache = new HashMap<>();
    private final Map<String, Date> _businessCacheRefreshDate = new HashMap<>();

    /**
     * Check whether all required properties have been set.
     */
    public void afterPropertiesSet() {
        Assert.notNull(_codeGroupServiceProvider, "codeGroupServiceProvider must be set");
    }

    /**
     * @param codeGroupServiceProvider the codeGroupServiceProvider to set
     */
    public void setCodeGroupServiceProvider(ICodeGroupServiceProvider codeGroupServiceProvider) {
        _codeGroupServiceProvider = codeGroupServiceProvider;
    }

    /**
     * @param hourlyRefreshedCodegroups the hourlyRefreshedCodegroups to set
     */
    public void setHourlyRefreshedCodegroups(String[] hourlyRefreshedCodegroups) {
        _hourlyRefreshedCodegroups = hourlyRefreshedCodegroups;
    }

    /**
     * @param dailyRefreshedCodegroups the dailyRefreshedCodegroups to set
     */
    public void setDailyRefreshedCodegroups(String[] dailyRefreshedCodegroups) {
        _dailyRefreshedCodegroups = dailyRefreshedCodegroups;
    }

    /**
     * @param delayMinutes the delayMinutes to set
     */
    public void setDelayMinutes(String delayMinutes) {
        try {
            _delayMinutes = Integer.parseInt(delayMinutes);
        } catch (NumberFormatException e) {
            throw new MebUncheckedException("delayMinutes is not a number: '" + delayMinutes + "'");
        }
    }

    public void refreshCache() {
        // set date of current refresh cycle
        Calendar refreshCalendar = Calendar.getInstance();
        // remove minor time component
        refreshCalendar.set(Calendar.SECOND, 0);
        refreshCalendar.set(Calendar.MILLISECOND, 0);
        Date refreshDate = refreshCalendar.getTime();

        String[] hourlyRefreshedCodegroups = _hourlyRefreshedCodegroups;
        String[] dailyRefreshedCodegroups = _dailyRefreshedCodegroups;

        LOG.debug("refreshCache");
        // refresh hourly cache
        if (hourlyRefreshedCodegroups != null && hourlyRefreshedCodegroups.length > 0) {
            LOG.debug("refreshCache hourlyRefreshedCodegroups != null && hourlyRefreshedCodegroups.length > 0");
            // create reference time for hourly schedule
            Calendar refCalendar = Calendar.getInstance();
            refCalendar.setTime(refreshDate);
            refCalendar.set(Calendar.MINUTE, _delayMinutes);
            // check if date is in the future, then switch one hour back
            if (refCalendar.compareTo(refreshCalendar) > 0) {
                refCalendar.add(Calendar.HOUR_OF_DAY, -1);
            }
            Date refDate = refCalendar.getTime();

            for (String codeGroupId : hourlyRefreshedCodegroups) {
                long start = System.currentTimeMillis();

                if (StringUtils.isEmpty(codeGroupId)) {
                    continue;
                }
                codeGroupId = codeGroupId.toUpperCase();

                Date lastHourlyRefresh = _businessCacheRefreshDate.get(codeGroupId);
                if (lastHourlyRefresh == null || refDate.compareTo(lastHourlyRefresh) > 0) {
                    Map<String, ICodeGroupCache> cacheMap = _codeGroupServiceProvider.getCodeGroupsByGroupId(codeGroupId);
                    if (cacheMap != null) {
                        synchronized (_businessCache) // always synchronize the business cache, not the refresh date cache
                        {
                            _businessCache.putAll(cacheMap);
                            _businessCacheRefreshDate.put(codeGroupId, refreshDate);
                            LOG.debug("hourly: " + refreshDate + " : " + (System.currentTimeMillis() - start));
                        }
                    }
                }
            }
        }

        // refresh daily cache
        if (dailyRefreshedCodegroups != null && dailyRefreshedCodegroups.length > 0) {
            LOG.debug("refreshCache dailyRefreshedCodegroups != null && dailyRefreshedCodegroups.length > 0");
            // create reference time for daily schedule (at 3:05 o'clock)
            Calendar refCalendar = Calendar.getInstance();
            refCalendar.setTime(refreshDate);
            // remove minor time component
            refCalendar.set(Calendar.MINUTE, 0);
            // set to cache refresh time of the current day
            refCalendar.set(Calendar.HOUR_OF_DAY, 3);
            refCalendar.set(Calendar.MINUTE, _delayMinutes);
            // check if date is in the future, then switch one day back
            if (refCalendar.compareTo(refreshCalendar) > 0) {
                refCalendar.add(Calendar.DAY_OF_MONTH, -1);
            }
            Date refDate = refCalendar.getTime();

            for (String codeGroupId : dailyRefreshedCodegroups) {
                long start = System.currentTimeMillis();

                if (StringUtils.isEmpty(codeGroupId)) {
                    continue;
                }
                codeGroupId = codeGroupId.toUpperCase();

                Date lastDailyRefresh = _businessCacheRefreshDate.get(codeGroupId);
                if (lastDailyRefresh == null || refDate.compareTo(lastDailyRefresh) > 0) {
                    Map<String, ICodeGroupCache> cacheMap = _codeGroupServiceProvider.getCodeGroupsByGroupId(codeGroupId);
                    if (cacheMap != null) {
                        synchronized (_businessCache) // always synchronize the business cache, not the refresh date cache
                        {
                            _businessCache.putAll(cacheMap);
                            _businessCacheRefreshDate.put(codeGroupId, refreshDate);
                            LOG.debug("daily: " + refreshDate + " : " + (System.currentTimeMillis() - start));
                        }
                    }
                }
            }
        }
    }

    public boolean isInitialized() {

        String[] hourlyRefreshedCodegroups = _hourlyRefreshedCodegroups;
        String[] dailyRefreshedCodegroups = _dailyRefreshedCodegroups;

        LOG.debug("isInitialized hourlyRefreshedCodegroups.length {}", hourlyRefreshedCodegroups != null ? hourlyRefreshedCodegroups.length : 0);
        LOG.debug("isInitialized dailyRefreshedCodegroups.length {}", dailyRefreshedCodegroups != null ? dailyRefreshedCodegroups.length : 0);
        List<String> codeGroups = new ArrayList<>();
        if (hourlyRefreshedCodegroups != null && hourlyRefreshedCodegroups.length > 0) {
            codeGroups.addAll(Arrays.asList(hourlyRefreshedCodegroups));
        }
        if (dailyRefreshedCodegroups != null && dailyRefreshedCodegroups.length > 0) {
            codeGroups.addAll(Arrays.asList(dailyRefreshedCodegroups));
        }
        if (codeGroups.size() > 0) {
            synchronized (_businessCache) // always synchronize the business cache, not the refresh date cache
            {
                return _businessCacheRefreshDate.keySet().containsAll(codeGroups);
            }
        }

        return true;
    }

    public ILocalizedCode getLocalizedCodeById(String codeGroup, Long id, Long canton, Locale locale) {
        ICodeGroupCache cache = getCache(codeGroup, canton, locale);
        return cache.get(id);
    }

    public String getValueById(String codeGroup, Long id, Long canton, Locale locale) {
        ILocalizedCode code = getLocalizedCodeById(codeGroup, id, canton, locale);
        return (code == null) ? null : code.getValue();
    }

    public String searchValueInAllCantons(String codeGroup, Long id, Locale locale) {
        ILocalizedCode code = null;
        for (Long canton = FIRST_CANTON; canton <= LAST_CANTON; canton++) {
            ICodeGroupCache cache = getCache(codeGroup, canton, locale);
            code = cache.get(id);
            if (code != null) {
                break;
            }
        }
        return code == null ? null : code.getValue();
    }

    /**
     * Gets the defined cache for a sepcific codegroup and locale. If the cache
     * cannot be found, the german cache is used. If also the default cache is
     * not available, an exception is thrown.
     *
     * @param codeGroup The codegroup of the cache
     * @param locale    the locale of the cache
     * @return The requested cache
     */
    private ICodeGroupCache getCache(String codeGroup, Long canton, Locale locale) {
        String cacheName = CodeGroupCache.getCacheName(codeGroup, canton, locale);
        synchronized (_businessCache) {
            if (_businessCache.containsKey(cacheName)) {
                return _businessCache.get(cacheName);
            } else {
                LOG.error("codegroup.cache.error.message: Cache '" + cacheName + "' is not cached (missing).");
                throw new MebUncheckedException("codegroup.cache.error.message");
            }
        }
    }

    public Collection<ILocalizedCode> getAllValues(String codeGroup, Long canton, boolean sortByKey, Locale locale) {
        ICodeGroupCache cache = getCache(codeGroup, canton, locale);

        SortedSet<ILocalizedCode> values = new TreeSet<>(new LocalizedCodeComparator(sortByKey, locale));
        for (ILocalizedCode value : cache.values()) {
            values.add(value);
        }
        return values;
    }
}
