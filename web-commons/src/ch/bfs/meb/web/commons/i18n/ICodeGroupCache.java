/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: web-commons

  $Id: ICodeGroupCache.java  07.04.2010 16:34:58 jfu $

 */
package ch.bfs.meb.web.commons.i18n;

import java.util.Collection;

public interface ICodeGroupCache {
    public void put(Long key, ILocalizedCode value);

    public Collection<ILocalizedCode> values();

    public ILocalizedCode get(Long key);

    public boolean isExpired();

    public boolean isEmpty();
}
