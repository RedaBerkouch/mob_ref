/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: meb-commons

  $Id$

 */
package ch.bfs.meb.server.commons.integration.sas;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.annotation.PostConstruct;
import javax.management.Notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import com.jcraft.jsch.*;
import com.sas.iom.SAS.ILanguageService;
import com.sas.iom.SAS.IWorkspace;
import com.sas.iom.SAS.IWorkspaceHelper;
import com.sas.iom.SAS.ILanguageServicePackage.CarriageControlSeqHolder;
import com.sas.iom.SAS.ILanguageServicePackage.LineTypeSeqHolder;
import com.sas.iom.SASIOMDefs.GenericError;
import com.sas.iom.SASIOMDefs.StringSeqHolder;
import com.sas.services.connection.*;

import ch.bfs.meb.configuration.IConfigurationChangedListener;
import ch.bfs.meb.exception.MebUncheckedException;
import ch.bfs.meb.server.commons.configuration.IServerConfiguration;
import ch.bfs.meb.server.commons.integration.sas.SASResult.Status;

/**
 * Service interface to the SAS
 */
public class SasService implements ISasService, IConfigurationChangedListener {
    private final static Logger LOGGER = LoggerFactory.getLogger(SasService.class);

    private ConnectionFactoryInterface connectionFactoryInterface;
    private Credential credential;
    private IServerConfiguration configuration;
    private boolean fatalErrorDetected = false;
    private int numberOfFatalErrorsDetected = 0;

    public void setConfiguration(IServerConfiguration configuration) {
        this.configuration = configuration;
    }

    @PostConstruct
    public void afterPropertiesSet() {
        Assert.notNull(configuration, "Configuration must be set");
    }

    public void configurationChanged(Notification notification) {
        // do nothing - next call will be done with new workspace
    }

    private synchronized void refreshConnectionPool() throws SASException, InterruptedException {
        LOGGER.debug("refresh con pool");
        handleFatalError();
        if (connectionFactoryInterface == null) {
            try {
                LOGGER.info("pool empty");

                // Configure the SAS server-object
                LOGGER.info("host: " + configuration.getSasHost() + ", port: " + configuration.getSasPortInt());
                Server server = new BridgeServer(Server.CLSID_SAS, configuration.getSasHost(), configuration.getSasPortInt());
                LOGGER.info("max size: " + configuration.getSasPoolMaxSizeInt() + ", shutdown after: " + configuration.getSasPoolServerShutdownAfterInt());
                server.setMaxClients(configuration.getSasPoolMaxSizeInt());
                server.setShutdownAfterMinutes(configuration.getSasPoolServerShutdownAfterInt());

                LOGGER.info("set credentials");
                // create credentials
                credential = new PasswordCredential(configuration.getSasUser(), configuration.getSasPassword());

                LOGGER.info("create puddle");
                // create a puddle and configure it
                Puddle puddle = new Puddle(server, credential);
                LOGGER.info(
                        "set puddle config, min avail: " + configuration.getSasPoolMinAvailableInt() + ", min size: " + configuration.getSasPoolMinSizeInt());
                puddle.setMinAvail(configuration.getSasPoolMinAvailableInt());
                puddle.setMinSize(configuration.getSasPoolMinSizeInt());

                LOGGER.info("create con factory");
                // make a connection factory configuration with the puddle
                ConnectionFactoryConfiguration cxfConfig = new ManualConnectionFactoryConfiguration(puddle);

                // create the connection factory and associated objects
                LOGGER.info("init manager");
                ConnectionFactoryManager cxfManager = new ConnectionFactoryManager();
                LOGGER.info("save con factory");
                connectionFactoryInterface = cxfManager.getFactory(cxfConfig);
            } catch (ConnectionFactoryException e) {
                throw new SASException("SAS fatal error in method refreshConnectionPool: " + e.toString(), e);
            }
        }
    }

    public void testConnection() throws SASException {
        ConnectionInterface cx = null;
        try {
            // make sure the connection pool is initialized
            refreshConnectionPool();

            cx = connectionFactoryInterface.getConnection(credential);
        } catch (ConnectionFactoryException e) {
            throw new SASException("SAS fatal error while calling testConnection: " + e.toString(), e);
        } catch (InterruptedException e) {
            LOGGER.error(e.getMessage());
        } finally {
            if (cx != null) {
                cx.close();
            }
        }
    }

    public SASResult run(String code) throws SASException {

        long start = System.currentTimeMillis();

        LOGGER.info("run: Code: " + code);
        ConnectionInterface cx = null;
        SASResult result = null;
        try {
            // make sure the connection pool is initialized
            refreshConnectionPool();

            LOGGER.debug("get connection");
            cx = connectionFactoryInterface.getConnection(credential);
            LOGGER.debug("get object");
            org.omg.CORBA.Object obj = cx.getObject();
            LOGGER.debug("get workspace");
            IWorkspace iWorkspace = IWorkspaceHelper.narrow(obj);
            LOGGER.debug("get language service");
            ILanguageService iLanguageService = iWorkspace.LanguageService();
            LOGGER.debug("submit code");
            iLanguageService.Submit(code);

            // Get different logs and check for errors
            LOGGER.debug("read log lines");
            CarriageControlSeqHolder logCarriageControlHldr = new CarriageControlSeqHolder();
            LineTypeSeqHolder logLineTypeHldr = new LineTypeSeqHolder();
            StringSeqHolder logHldr = new StringSeqHolder();

            iLanguageService.FlushLogLines(Integer.MAX_VALUE, logCarriageControlHldr, logLineTypeHldr, logHldr);

            String[] logLines = logHldr.value;

            LOGGER.debug("read list lines");
            CarriageControlSeqHolder listCarriageControlHldr = new CarriageControlSeqHolder();
            LineTypeSeqHolder listLineTypeHldr = new LineTypeSeqHolder();
            StringSeqHolder listHldr = new StringSeqHolder();
            iLanguageService.FlushListLines(Integer.MAX_VALUE, listCarriageControlHldr, listLineTypeHldr, listHldr);

            String[] listLines = listHldr.value;

            LOGGER.debug("create result");
            result = new SASResult(logLines, listLines);

            LOGGER.debug("check status");
            if (result.getStatus() != Status.OK) {
                LOGGER.debug("SAS call returned not ok.");
            }
        } catch (GenericError | ConnectionFactoryException | RuntimeException e) {
            String error = String.format("SAS fatal error (%s}): %s", e.getClass(), e.toString());
            LOGGER.error(error);
            fatalErrorDetected = true;
            throw new SASException(error, e);
        } catch (InterruptedException e) {
            LOGGER.error(e.getMessage());
        } finally {
            LOGGER.info("run: Code: " + code + ", Status: " + (result == null ? "NULL" : result.getStatus()) + ", In: "
                    + (System.currentTimeMillis() - start) + "ms");
            if (cx != null) {
                cx.close();
            }
        }
        return result;
    }

    public byte[] getFileContent(String fileLocation) {
        JSch jsch = new JSch();
        Session jschSession = null;

        try {
            jschSession = jsch.getSession(configuration.getSasUser().trim(), configuration.getSasHost().trim(), 22);
            jschSession.setConfig("StrictHostKeyChecking", "no");
            jschSession.setConfig("PreferredAuthentications", "publickey,keyboard-interactive,password");
            jschSession.setPassword(configuration.getSasPassword());
            jschSession.connect();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            // exec 'scp -f rfile' remotely
            String command = "scp -f " + fileLocation;
            Channel channel = jschSession.openChannel("exec");
            ((ChannelExec) channel).setCommand(command);

            // get I/O streams for remote scp
            OutputStream out = channel.getOutputStream();
            InputStream in = channel.getInputStream();

            channel.connect();

            byte[] buf = new byte[1024];

            // send '\0'
            buf[0] = 0;
            out.write(buf, 0, 1);
            out.flush();

            while (true) {
                int c = checkAck(in);
                if (c != 'C') {
                    break;
                }

                // read '0644 '
                in.read(buf, 0, 5);

                long filesize = 0L;
                while (true) {
                    if (in.read(buf, 0, 1) < 0) {
                        // error
                        break;
                    }
                    if (buf[0] == ' ')
                        break;
                    filesize = filesize * 10L + (long) (buf[0] - '0');
                }

                // read until the filename is found
                for (int i = 0;; i++) {
                    in.read(buf, i, 1);
                    if (buf[i] == (byte) 0x0a) {
                        break;
                    }
                }

                // send '\0'
                buf[0] = 0;
                out.write(buf, 0, 1);
                out.flush();

                // read a content of lfile

                int foo;
                while (true) {
                    if (buf.length < filesize)
                        foo = buf.length;
                    else
                        foo = (int) filesize;
                    foo = in.read(buf, 0, foo);
                    if (foo < 0) {
                        // error
                        break;
                    }
                    baos.write(buf, 0, foo);
                    filesize -= foo;
                    if (filesize == 0L)
                        break;
                }
                baos.flush();

                if (checkAck(in) != 0) {
                    throw new MebUncheckedException("export.sas.incompleteread.message");
                }

                // send '\0'
                buf[0] = 0;
                out.write(buf, 0, 1);
                out.flush();
            }

            return baos.toByteArray();
        } catch (IOException | JSchException e) {
            throw new MebUncheckedException("export.sas.incompleteread.message", e);
        } finally {
            if (jschSession != null)
                jschSession.disconnect();
        }
    }

    private static int checkAck(InputStream in) throws IOException {
        int b = in.read();
        // b may be 0 for success,
        // 1 for error,
        // 2 for fatal error,
        // -1
        if (b == 0)
            return b;
        if (b == -1)
            return b;

        if (b == 1 || b == 2) {
            StringBuilder sb = new StringBuilder();
            int c;
            do {
                c = in.read();
                sb.append((char) c);
            } while (c != '\n');
            if (b == 1) { // error
                LOGGER.info(sb.toString());
            }
            if (b == 2) { // fatal error
                LOGGER.info(sb.toString());
            }
        }
        return b;
    }

    private void handleFatalError() throws InterruptedException {
        if (fatalErrorDetected) {
            numberOfFatalErrorsDetected++;
            LOGGER.error("SAS fatal error detected, attempt #{}", numberOfFatalErrorsDetected);
            if (numberOfFatalErrorsDetected > 10) {
                Thread.sleep(numberOfFatalErrorsDetected * 100);
            }
            try {
                if (connectionFactoryInterface != null && connectionFactoryInterface.getAdminInterface() != null) {
                    connectionFactoryInterface.getAdminInterface().destroy();
                }
            } catch (ConnectionFactoryException e) {
                throw new SASException("SAS fatal error detected when trying to recover from fatal error: " + e.toString(), e);
            } finally {
                connectionFactoryInterface = null;
                fatalErrorDetected = false;
            }
        }
    }
}