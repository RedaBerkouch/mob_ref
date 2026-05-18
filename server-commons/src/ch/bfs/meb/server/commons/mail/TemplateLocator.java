/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: server-commons

  $Id$
 */
package ch.bfs.meb.server.commons.mail;

import java.io.InputStream;
import java.util.Locale;

/**
 * Locator to get template files from the class path using names and locales The
 * index component can be used to select spezialised templates for different
 * groups. Name of indexed template <tempateName>_<index>_<locale><suffix>
 * Name of non indexed template <tempateName>_<locale><suffix>
 * 
 * @author $Author$
 * @version $Revision$
 */
public class TemplateLocator {

    private String _location;

    private String _suffix;

    private long _index;

    private String _templateName;

    private boolean _isIndexed;

    private Locale _locale;

    /**
     * Creates a new locator, using a classpath location and a file suffix
     * 
     * @param location classpath location
     * @param The templates name
     * @param locale The locale to use
     * @param suffix file suffix (f.a. ".mail")
     */
    public TemplateLocator(String location, String templateName, Locale locale, String suffix) {
        this._location = location;
        this._templateName = templateName;
        this._locale = locale;
        this._suffix = suffix;
        this._isIndexed = false;
    }

    /**
     * Creates a new locator, using a classpath location and a file suffix The
     * locator can be indexed using the index parameter
     * 
     * @param location classpath location
     * @param templatename The templates name
     * @param index the index of the filename
     * @param locale The locale to use
     * @param suffix file suffix (f.a. ".mail")
     */
    public TemplateLocator(String location, String templateName, Locale locale, String suffix, long index) {
        this._location = location;
        this._templateName = templateName;
        this._locale = locale;
        this._suffix = suffix;
        this._index = index;
        this._isIndexed = true;
    }

    /**
     * Gets the location of a the template
     * 
     * @return the location
     */
    public String getLocator() {

        StringBuilder locator = new StringBuilder();

        locator.append(getLocation());
        locator.append(getTemplateName());
        if (isIndexed()) {
            locator.append("_");
            locator.append(getIndex());
        }
        locator.append("_");
        locator.append(getLocale().getLanguage());
        locator.append(getSuffix());

        return locator.toString();
    }

    /**
     * returns localtion without index part
     * 
     * @return the location
     */
    public String getLocatorNI() {

        StringBuilder locator = new StringBuilder();

        locator.append(getLocation());
        locator.append(getTemplateName());
        locator.append("_");
        locator.append(getLocale().getLanguage());
        locator.append(getSuffix());

        return locator.toString();
    }

    /**
     * Gets the location as input stream using name, locale and index. When the
     * location with index is invalid, the location without index is returned.
     * 
     * @return the inputstream or null, when the location was invalid
     */
    public InputStream getLocatorAsStream() {

        String location = getLocator();

        InputStream stream = getClass().getResourceAsStream(location);

        // if no template available, try to get template without index
        if (stream == null && isIndexed()) {

            location = getLocatorNI();

            stream = getClass().getResourceAsStream(location);
        }

        // If the location is invalid, throw runtime exception
        if (stream == null) {
            throw new MailException("Location '" + location + "' not valid");
        }

        return stream;
    }

    /**
     * @return Returns the index.
     */
    public long getIndex() {
        return _index;
    }

    /**
     * @param index The index to set.
     */
    public void setIndex(long index) {
        this._index = index;
    }

    /**
     * @return Returns the isIndexed.
     */
    public boolean isIndexed() {
        return _isIndexed;
    }

    /**
     * @param isIndexed The isIndexed to set.
     */
    public void setIndexed(boolean isIndexed) {
        this._isIndexed = isIndexed;
    }

    /**
     * @return Returns the locale.
     */
    public Locale getLocale() {
        return _locale;
    }

    /**
     * @param locale The locale to set.
     */
    public void setLocale(Locale locale) {
        this._locale = locale;
    }

    /**
     * @return Returns the suffix.
     */
    public String getSuffix() {
        return _suffix;
    }

    /**
     * @param suffix The suffix to set.
     */
    public void setSuffix(String suffix) {
        this._suffix = suffix;
    }

    /**
     * @return Returns the templateName.
     */
    public String getTemplateName() {
        return _templateName;
    }

    /**
     * @param templateName The templateName to set.
     */
    public void setTemplateName(String templateName) {
        this._templateName = templateName;
    }

    /**
     * @return Returns the location.
     */
    public String getLocation() {
        return _location;
    }

    /**
     * @param location The location to set.
     */
    public void setLocation(String location) {
        this._location = location;
    }

}
