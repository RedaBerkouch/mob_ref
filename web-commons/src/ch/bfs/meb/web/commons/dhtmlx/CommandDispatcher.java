/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: web-commons

  $Id$

 */
package ch.bfs.meb.web.commons.dhtmlx;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import ch.bfs.meb.exception.MebUncheckedException;
import ch.bfs.meb.integration.dto.ResultBase;
import ch.bfs.meb.security.MebAccessDeniedException;
import ch.bfs.meb.util.StringUtils;
import ch.bfs.meb.web.commons.dhtmlx.table.DhtmlxTableXML;
import ch.bfs.meb.web.commons.dhtmlx.table.ParameterList;
import ch.bfs.meb.web.commons.dhtmlx.table.TableManagerBase;
import ch.bfs.meb.web.commons.exception.InputValidationException;
import ch.bfs.meb.web.commons.exception.MebDhtmlxFileException;
import ch.bfs.meb.web.commons.exception.MebDhtmlxTableDataXMLException;
import ognl.Ognl;
import ognl.OgnlContext;
import ognl.OgnlException;
import ognl.SimpleNode;

/**
 * Maps commands to java methods.
 * 
 * @author $Author$
 * @version $Revision$
 */
public class CommandDispatcher {

    private final static OgnlContext context = (OgnlContext) Ognl.createDefaultContext(null);

    private final static Logger LOG = LoggerFactory.getLogger(CommandDispatcher.class);

    /**
     * Predefined edit states
     * 
     * @author $Author$
     * @version $Revision$
     */
    public static class EDIT {

        public static String UPDATE = "update";

        public static final String INSERT = "inserted";

        public static final String DELETE = "deleted";
    }

    /**
     * Calls a method of the manager using the requests "command" parameter to
     * obtain data from the server
     * 
     * @param manager
     *            The manager, that contains the requested method
     * @param request
     *            The ajac request
     * @return An XML-Object as String, that contains the requested data
     * @throws DhtmlxException
     */
    public IHttpResult run(IDhtmlxManager manager, HttpServletRequest request) throws DhtmlxException {

        OgnlContext context = (OgnlContext) Ognl.createDefaultContext(null);

        ParameterList parameterList = new ParameterList(request);

        String command = parameterList.getCommand();

        if (!StringUtils.isEmpty(command)) {

            try {
                SimpleNode expression;

                // Handle command save
                if (CommandConstants.SAVE.equals(command)) {

                    String editCommand = CommandConstants.UPDATE;
                    String editStatus = parameterList.getEditorStatus();

                    if (EDIT.INSERT.equals(editStatus)) {
                        editCommand = CommandConstants.INSERT;
                    } else if (EDIT.DELETE.equals(editStatus)) {
                        editCommand = CommandConstants.DELETE;
                    }

                    expression = (SimpleNode) Ognl.parseExpression(editCommand + "(#paramlist)");
                } else {
                    expression = (SimpleNode) Ognl.parseExpression(command + "(#paramlist)");
                }
                context.put("paramlist", parameterList);

                return (IHttpResult) (Ognl.getValue(expression, context, manager));
            } catch (OgnlException e) {
                String message = "Internal error";

                Throwable cause = e.getCause();

                boolean fileException = false;
                if (cause instanceof MebDhtmlxFileException) {
                    fileException = true;
                    cause = cause.getCause();
                }

                boolean singleRow = false;
                if (cause instanceof MebDhtmlxTableDataXMLException) {
                    cause = cause.getCause();
                    singleRow = true;
                }

                if (cause instanceof InputValidationException || cause instanceof MebAccessDeniedException) {
                    message = cause.getMessage();
                } else if (cause instanceof MebUncheckedException) {
                    try {
                        message = manager.getLocalizationManager().getMessage(cause.getMessage());
                    } catch (Exception ex) {
                        message = cause.getMessage();
                    }
                } else if (cause instanceof DhtmlxException) {
                    LOG.error("Illegal manager access", e);
                } else {
                    LOG.error("Command '" + command + "' not valid: ", e);
                }

                Throwable rootCause = getRootCause(e);
                if ("com.sun.xml.ws.developer.ServerSideException".equals(rootCause.getClass().getName())
                        && (rootCause.toString().startsWith("org.springframework.transaction.UnexpectedRollbackException")
                                || rootCause.toString().startsWith("weblogic.transaction.internal.TimedOutException"))) {
                    message = "Server side timeout";
                }

                if (fileException) {
                    return new FileHttpResult(message.getBytes(), "error.txt", message, ResultBase.FAILURE);
                }
                if (singleRow) {
                    return TableManagerBase.toXMLDataErrorStream(message, parameterList.getRowId());
                } else {
                    return new DhtmlxTableXML(message) {
                        public String getDocument() {
                            return _document;
                        }
                    };

                }
            }
        }

        throw new DhtmlxException("Command not found or empty - parameters: " + parameterList.toString());
    }

    private Throwable getRootCause(Throwable current) {
        if (current.getCause() == null || current.getCause().equals(current)) {
            return current;
        } else {
            return getRootCause(current.getCause());
        }
    }

    /**
     * Calls a method of the manager using the requests "goal" parameter to
     * upload data to the server
     * 
     * @param goal
     *            The ajax request
     * @return An XML-Object as String, that contains the requested data
     * @throws DhtmlxException
     */
    public IHttpResult upload(IDhtmlxControl control, String goal, MultipartFile file) throws DhtmlxException {
        if (!StringUtils.isEmpty(goal)) {

            try {
                SimpleNode expression;

                expression = (SimpleNode) Ognl.parseExpression(goal + "(#file)");

                context.put("file", file);

                return (IHttpResult) (Ognl.getValue(expression, context, control));
            } catch (OgnlException e) {
                throw new DhtmlxException("Goal '" + goal + "' not valid", e);
            }
        }

        throw new DhtmlxException("Upload goal not found or empty - goal: " + goal);
    }

    public IHttpResult uploadFile(IDhtmlxControl control, String goal, MultipartFile file, HttpServletRequest request) throws DhtmlxException {
        if (!StringUtils.isEmpty(goal)) {

            try {
                SimpleNode expression;
                ParameterList parameterList = new ParameterList(request);
                expression = (SimpleNode) Ognl.parseExpression(goal + "(#file,#paramlist)");

                context.put("file", file);
                context.put("paramlist", parameterList);

                return (IHttpResult) (Ognl.getValue(expression, context, control));

            } catch (OgnlException e) {
                throw new DhtmlxException("Goal '" + goal + "' not valid", e);
            }
        }

        throw new DhtmlxException("Upload goal not found or empty - goal: " + goal);
    }
}
