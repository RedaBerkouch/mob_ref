/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: server-commons
 */
package ch.bfs.meb.server.commons.util;

import java.util.List;

import ch.bfs.meb.server.commons.integration.dto.Filter;

public interface ISecurityFilters {
    public int getNrOfFilters();

    public Filter getFilter(int index);

    public String getTableName(Filter filter);

    public List<Long> getFilterCantonsForActUser();

    public List<Long> getCantonsForUser(String userName);

    public Long getActVersion();

    public Long getInitVersion();
}
