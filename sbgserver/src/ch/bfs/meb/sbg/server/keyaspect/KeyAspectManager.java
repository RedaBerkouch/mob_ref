package ch.bfs.meb.sbg.server.keyaspect;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import ch.admin.bfs.sbg.db.dao.KeyAspectDAO;
import ch.admin.bfs.sbg.transfer.KeyAspect;
import lombok.Setter;

/**
 * Similar to the {@link ch.bfs.meb.server.commons.codes.CodegroupManager} but specific for {@link ch.admin.bfs.sbg.transfer.KeyAspect}.<br />
 * Responsible for caching these {@link ch.admin.bfs.sbg.transfer.KeyAspect}s.
 *
 * @author Simon Kaufmann
 */
public class KeyAspectManager {
    private final static Logger LOG = LoggerFactory.getLogger(KeyAspectManager.class);

    @Setter
    private KeyAspectDAO keyAspectDao;

    private Map<Long, Set<KeyAspect>> cachedKeyAspects = new HashMap<>();

    private Date lastDailyRefresh;

    /**
     * Refreshes the KeyAspectCache
     */
    @Transactional(readOnly = true)
    public void refreshCache() {
        // set date of current refresh cycle
        Calendar refreshCalendar = Calendar.getInstance();
        // remove minor time component
        refreshCalendar.set(Calendar.SECOND, 0);
        refreshCalendar.set(Calendar.MILLISECOND, 0);
        Date refreshDate = refreshCalendar.getTime();

        // refresh daily cache
        // create reference time for daily schedule (at 3:00 o'clock)
        Calendar refCalendar = Calendar.getInstance();
        refCalendar.setTime(refreshDate);
        // remove minor time component
        refCalendar.set(Calendar.MINUTE, 0);
        // set to cache refresh time of the current day
        refCalendar.set(Calendar.HOUR_OF_DAY, 3);
        // check if date is in the future, then switch one day back
        if (refCalendar.compareTo(refreshCalendar) > 0) {
            refCalendar.add(Calendar.DAY_OF_MONTH, -1);
        }
        Date refDate = refCalendar.getTime();

        if (lastDailyRefresh == null || refDate.compareTo(lastDailyRefresh) > 0) {
            Set<KeyAspect> keyAspects = new HashSet(keyAspectDao.getAll());

            synchronized (cachedKeyAspects) {
                cachedKeyAspects = new HashMap<>(); //renew cache
                for (KeyAspect keyAspect : keyAspects) {
                    Set<KeyAspect> keyAspectsPerSbfiCode = cachedKeyAspects.get(keyAspect.getSbfiCode());
                    if (keyAspectsPerSbfiCode == null) {
                        keyAspectsPerSbfiCode = new HashSet<KeyAspect>();
                        cachedKeyAspects.put(keyAspect.getSbfiCode(), keyAspectsPerSbfiCode);
                    }
                    keyAspectsPerSbfiCode.add(keyAspect);
                }
            }

            this.lastDailyRefresh = refreshDate;
            LOG.debug("daily: " + this.lastDailyRefresh);
        }
    }

    /**
     * @param sbfiCode
     * @return Cached Set of KeyAspects which belong to the given sbfiCode.
     */
    public Set<KeyAspect> getCachedKeyAspect(Long sbfiCode) {
        if (!isInitialized()) {
            LOG.warn("The cached KeyAspects were not initialised by the Quartz scheduler! Cache will be refreshed now.");
            refreshCache();
        }
        return cachedKeyAspects.get(sbfiCode);
    }

    /**
     * @return true if any {@link KeyAspect}s are cached, false otherwise.
     */
    public boolean isInitialized() {
        if (cachedKeyAspects != null && cachedKeyAspects.size() > 0) {
            return true;
        } else {
            return false;
        }

    }

    public boolean contains(Long version, Long sbfiCode, Long keyAspectCode) {
        if (keyAspectCode != null && keyAspectCode == 1L) {
            // On accepte keyAspect 1 seulement si version et sbfiCode sont non null
            return version != null && sbfiCode != null;
        }
        if (version == null || sbfiCode == null || keyAspectCode == null) {
            return false;
        }
        Set<KeyAspect> keyAspects = getCachedKeyAspect(sbfiCode);
        if (keyAspects != null) {
            return getCachedKeyAspect(sbfiCode).stream()
                    .filter(keyAspect -> (keyAspect.getValidFromYear() == null) || (keyAspect.getValidFromYear() <= version))
                    .filter(keyAspect -> (keyAspect.getValidToYear() == null) || (keyAspect.getValidToYear() >= version))
                    .anyMatch(keyAspect -> keyAspect.getKeyAspectCode().equals(keyAspectCode));
        } else {
            return false;
        }
    }

}
