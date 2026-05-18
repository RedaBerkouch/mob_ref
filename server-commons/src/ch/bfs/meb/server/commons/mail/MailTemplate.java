/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: server-commons

  $Id$
 */
package ch.bfs.meb.server.commons.mail;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ognl.OgnlException;

/**
 * Wrapper for mail templates. Mail templates are localized text files with user
 * defined fields. These fields are formatted like ${myGetter} and are solved
 * using reflexion. The fieldname must be a getter (f.a.
 * lookupData.getMyGetter()) in the lookupData object.
 * 
 * The index can be used to define different templates for specific user groups. 
 * If no template with an index is found by the templatelocator, the template
 * without index is taken.
 * 
 * @author $Author$
 * @version $Revision$
 */
public class MailTemplate {

    private static final Log LOG = LogFactory.getLog(MailTemplate.class);

    private static final String SUFFIX = ".mail";

    public static final String TEMPLATE_RESOURCE_LOCATION_SDL = "/ch/bfs/meb/sdl/server/mail/template/";
    public static final String TEMPLATE_RESOURCE_LOCATION_SSP = "/ch/bfs/meb/ssp/server/mail/template/";
    public static final String TEMPLATE_RESOURCE_LOCATION_SBA = "/ch/bfs/meb/sba/server/mail/template/";
    public static final String TEMPLATE_RESOURCE_LOCATION_SBG = "/ch/admin/bfs/sbg/mail/template/";

    private static final String FIELD_PATTERN = "\\$\\{[0-9a-zA-Z\\.]+\\}";

    private static final String CALL_PATTERN = "[0-9a-zA-Z\\.]+";

    private static final Pattern field_pattern = Pattern.compile(FIELD_PATTERN);

    private static final Pattern call_pattern = Pattern.compile(CALL_PATTERN);

    private final TemplateLocator _locator;

    private final Object _lookupData;

    /**
     * Creates a new Mailtemplate
     * 
     * @param templateName Template name without locale and extention
     * @param templateResourceLocaction	Base directory for the mail templates
     * @param locale The locale to use
     * @param lookupData The data object with field information
     */
    public MailTemplate(String templateName, String templateResourceLocation, Locale locale, Object lookupData) {

        _locator = new TemplateLocator(templateResourceLocation, templateName, locale, SUFFIX);
        _lookupData = lookupData;
    }

    /**
     * Creates a new Mailtemplate based on an indexed template
     * 
     * @param templateName Template name without locale and extention
     * @param templateResourceLocaction	Base directory for the mail templates
     * @param locale The locale to use
     * @param The templates index
     * @param lookupData The data object with field information
     */
    public MailTemplate(String templateName, String templateResourceLocation, Locale locale, long index, Object lookupData) {

        _locator = new TemplateLocator(templateResourceLocation, templateName, locale, SUFFIX, index);
        _lookupData = lookupData;
    }

    /**
     * Lookups fields
     * 
     * @param field The field that is used for the lookup (formatted ${myField}
     * @return The value or {ERROR} when the field is unknown
     */
    private String find(String field) {

        // first, strip leading ${ and tail }
        Matcher m = call_pattern.matcher(field);
        String value = "";
        String expression = null;

        if (m.find())
            expression = m.group();

        // use wxpression to get getter from lookup oblect
        try {
            if (expression != null) {

                Object data = ognl.Ognl.getValue(ognl.Ognl.parseExpression(expression), _lookupData);

                if (data != null) {
                    value = data.toString();
                }
            }
        } catch (OgnlException e) {

            LOG.error("Expression '" + expression + "' not found", e);

            value = "{ERROR}";
        }

        return value;
    }

    /**
     * Compiles a mail template (replacing fields)
     * 
     * @param mailTemplate The template to compile
     * @return The compiled template
     */
    private String compile(String mailTemplate) {

        Matcher m;

        StringBuffer myStringBuffer = new StringBuffer();
        m = field_pattern.matcher(mailTemplate);
        while (m.find()) {
            m.appendReplacement(myStringBuffer, find(m.group()));
        }
        m.appendTail(myStringBuffer);

        return myStringBuffer.toString();
    }

    /**
     * Locates and compiles the mail template
     * 
     * @return The parsed mail template
     */
    public String parse() {

        InputStream stream = _locator.getLocatorAsStream();
        String line;

        StringBuilder mail = new StringBuilder();

        if (stream != null) {
            try {
                InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8);
                BufferedReader f = new BufferedReader(reader);
                while ((line = f.readLine()) != null) {
                    mail.append(line).append("\n");
                }
                f.close();
            } catch (IOException e) {

                throw new MailException("Could not parse mail template " + _locator, e);
            }
        } else {
            throw new MailException("Could not parse mail template " + _locator);
        }

        return compile(mail.toString());
    }

    /**
     * @see ch.admin.bfs.sbg.mail.TemplateLocator#getLocale()
     */
    public Locale getLocale() {
        return _locator.getLocale();
    }

    /**
     * @see ch.admin.bfs.sbg.mail.TemplateLocator#setLocale(java.util.Locale)
     */
    public void setLocale(Locale locale) {
        _locator.setLocale(locale);
    }
}
