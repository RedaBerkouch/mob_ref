/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sbgserver

  $Id: IEventRepository.java 948 2010-03-08 18:40:41Z jfu $
 */

package ch.bfs.meb.sbg.server.integration.repository;

import java.util.List;
import java.util.Map;

import ch.bfs.meb.sbg.server.integration.dto.SbgEvent;

/**
 * Interface for repository for SBG events.
 *
 * @author $Author: jfu $
 * @version $Revision: 948 $
 */
public interface IEventRepository {
    public List<SbgEvent> getPartial(int start, int buffer, ch.admin.bfs.sbg.transfer.SortContext sortContext,
            ch.admin.bfs.sbg.transfer.FilterContext filterContext, Long version, Long canton);

    public Long getNrEvents(ch.admin.bfs.sbg.transfer.FilterContext filterContext, Long version, Long canton);

    public Map<Long, Long> getProfCodeForContracts(Long canton, Long year);

    public SbgEvent findById(Long eventId);

    public List<SbgEvent> findByPids(List<Long> pids, ch.admin.bfs.sbg.transfer.SortContext sortContext);

    public SbgEvent insertEvent(SbgEvent event);

    public SbgEvent updateEvent(SbgEvent event);

    public void deleteEvent(SbgEvent event);

    public void clearEventFromCache(SbgEvent event);

    public void updateValidationStatus(Long personId, boolean isValidated);
}
