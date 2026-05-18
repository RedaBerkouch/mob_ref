/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: web-commons

  $Id$

 */
package ch.bfs.meb.web.commons.dhtmlx;

import java.util.ArrayList;
import java.util.HashMap;

import javax.annotation.PostConstruct;

import org.apache.xmlbeans.XmlError;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.bfs.meb.web.commons.dhtmlx.javascript.IJavaScriptFunction;

/**
 * TODO Describe this class
 * 
 * @author $Author$
 * @version $Revision$
 */
public abstract class DhtmlxManagerBase implements IDhtmlxManager {
    private final static Logger LOG = LoggerFactory.getLogger(DhtmlxManagerBase.class);

    protected String _callbackURL;

    protected boolean _master;

    /**
     * Registry with javascript functions, that can be referenced from a jsp
     * page
     */
    protected final HashMap<String, IJavaScriptFunction> _callbackRegistry = new HashMap<String, IJavaScriptFunction>();

    public abstract String getName();

    @Override
    @PostConstruct
    public abstract void create() throws DhtmlxException;

    public void registerCallback(IJavaScriptFunction function) {

        _callbackRegistry.put(function.getMethodName(), function);
    }

    public void unregisterCallback(String function) {

        _callbackRegistry.remove(function);
    }

    public IJavaScriptFunction getRegisteredCallback(String callback) throws DhtmlxException {

        IJavaScriptFunction callbackFunc = _callbackRegistry.get(callback);

        if (callbackFunc != null) {
            return callbackFunc;
        }

        throw new DhtmlxException("Callback '" + callback + "' was not registered by control '" + getControlName() + "'");
    }

    public String getScriptingPart() throws DhtmlxException {
        StringBuffer buf = new StringBuffer();

        // Get all callbacks
        for (IJavaScriptFunction callback : _callbackRegistry.values()) {
            buf.append(callback.getScriptingPart());
        }
        return buf.toString();
    }

    public String toString() {
        return "Control: " + getControlName() + ", name: " + getName();
    }

    /**
     * <p>
     * Validates the XML, printing error messages when the XML is invalid. Note
     * that this method will properly validate any instance of a compiled schema
     * type because all of these types extend XmlObject.
     * </p>
     * <p/>
     * <p>
     * Note that in actual practice, you'll probably want to use an assertion
     * when validating if you want to ensure that your code doesn't pass along
     * invalid XML. This sample prints the generated XML whether or not it's
     * valid so that you can see the result in both cases.
     * </p>
     * 
     * @param xml
     *            The XML to validate.
     * @return <code>true</code> if the XML is valid; otherwise,
     *         <code>false</code>
     */
    @SuppressWarnings("unchecked")
    public static boolean validateXml(XmlObject xml) {
        boolean isXmlValid;

        // A collection instance to hold validation error messages.
        ArrayList validationMessages = new ArrayList();

        // Validate the XML, collecting messages.
        isXmlValid = xml.validate(new XmlOptions().setErrorListener(validationMessages));

        if (!isXmlValid) {
            LOG.warn("Invalid XML: ");
            for (int i = 0; i < validationMessages.size(); i++) {
                XmlError error = (XmlError) validationMessages.get(i);
                LOG.warn(error.getMessage());
                LOG.warn("" + error.getObjectLocation());
            }
        }
        return isXmlValid;
    }

    public boolean isMaster() {
        return _master;
    }

    public void setMaster(boolean master) {
        this._master = master;
    }

    public boolean isServerSort() {
        // Default is sorting by dhtmlx grid
        return false;
    }

    public String getExtraHtml(String partName) {
        return "";
    }

    public boolean showIfTagBody(String condition) {
        return false;
    }
}
