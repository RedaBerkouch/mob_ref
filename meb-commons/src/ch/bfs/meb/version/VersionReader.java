/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: meb-commons

  $Id: VersionReader.java  03.03.2010 09:04:46 jfu $

 */
package ch.bfs.meb.version;

import java.io.IOException;
import java.util.Properties;

import ch.bfs.meb.exception.MebUncheckedException;
import ch.bfs.meb.util.PropertiesLoader;
import ch.bfs.meb.util.StringUtils;

/**
 * This class is in charge of getting various version informations from a
 * properties file.
 * 
 * @author  $Author: dwi $ 
 * @version $Revision: 52 $ 
 */
public class VersionReader {
    private static final String PROPERTIES_FILE = "version.properties";

    private static final String MAJOR_VERSION = "version.major";
    private static final String MINOR_VERSION = "version.minor";

    private static String _majorVersion;
    private static String _minorVersion;

    static {
        try {
            Properties properties = PropertiesLoader.loadRootProperties(PROPERTIES_FILE);
            setMajorVersion(properties.getProperty(MAJOR_VERSION));
            setMinorVersion(properties.getProperty(MINOR_VERSION));
        } catch (IOException e) {
            throw new MebUncheckedException("Could not load version.properties", e);
        }
    }

    /**
     * Gets the applications major version.
     * 
     * @return the applications major version.
     */
    public static String getMajorVersion() {
        return _majorVersion;
    }

    private static void setMajorVersion(String majorVersion) {
        if (StringUtils.isEmpty(majorVersion)) {
            _majorVersion = "XX";
        } else {
            _majorVersion = majorVersion;
        }
    }

    /**
     * Gets the applications minor version.
     * 
     * @return the applications minor version.
     */
    public static String getMinorVersion() {
        return _minorVersion;
    }

    private static void setMinorVersion(String minorVersion) {
        if (StringUtils.isEmpty(minorVersion)) {
            _minorVersion = "XX";
        } else {
            _minorVersion = minorVersion;
        }
    }

    /**
     * Gets the applications version.
     * 
     * @return the applications version.
     */
    public static String getVersion() {
        return _majorVersion + "." + _minorVersion;
    }
}