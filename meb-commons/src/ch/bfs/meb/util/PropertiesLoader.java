/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: meb-commons

  $Id: PropertiesLoader.java  03.03.2010 09:12:56 jfu $

 */
package ch.bfs.meb.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@SuppressWarnings("unchecked")
public class PropertiesLoader {
    public static final void addRootProperties(Properties properties, String filename) throws IOException {

        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(filename);

        if (is == null) {
            throw new FileNotFoundException("Resource '" + filename + "' not found!");
        }

        try {
            properties.load(is);
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                // ignore
            }
        }
    }

    public static final void addClassProperties(Properties properties, Class clazz, String filename) throws IOException {

        InputStream is = clazz.getResourceAsStream(filename);

        if (is == null) {
            throw new FileNotFoundException("Resource '" + filename + "' not found!");
        }

        try {
            properties.load(is);
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                // ignore
            }
        }
    }

    public static final Properties loadRootProperties(String filename) throws IOException {
        Properties properties = new Properties();

        addRootProperties(properties, filename);

        return properties;
    }

    public static final Properties loadClassProperties(Class clazz, String filename) throws IOException {
        Properties properties = new Properties();

        addClassProperties(properties, clazz, filename);

        return properties;
    }
}
