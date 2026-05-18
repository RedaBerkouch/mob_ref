/*
  MEB Portal

  adesso Schweiz AG

  Projekt: web-commons

  $Id$
 */
package ch.bfs.meb.web.commons.i18n;

/**
 * Interface for the structure to store the localized code groups in the code group cache
 * 
 */
public interface ILocalizedCode {
    public Long getKey();

    public void setKey(Long value);

    public String getValue();

    public void setValue(String value);
}
