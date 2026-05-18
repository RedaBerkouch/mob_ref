package ch.bfs.meb.web.commons.util;

import ch.bfs.meb.configuration.IWebConfiguration;
import ch.bfs.meb.security.idm.IdmServiceException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.xml.ws.BindingProvider;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;

@Slf4j
public class WebserviceClientCertificateConfiguration {

    public static final String SSLSOCKET_FACTORY_PROPERTY = "com.sun.xml.ws.transport.https.client.SSLSocketFactory";

    public static void initClientCertificate(BindingProvider bindingProvider, IWebConfiguration configuration) {
        FileInputStream inputStream = null;
        try {

            if (StringUtils.isNotEmpty(configuration.getClientCertificateKeyStorePassword())) {
                // load keystore
                KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
                inputStream = new FileInputStream(configuration.getClientCertificateKeyStore());
                keyStore.load(inputStream, configuration.getClientCertificateKeyStorePassword().toCharArray());

                KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
                keyManagerFactory.init(keyStore, configuration.getClientCertificateKeyStorePassword().toCharArray());

                // initialize SSL context
                SSLContext context = SSLContext.getInstance("TLSv1.2");
                context.init(keyManagerFactory.getKeyManagers(), null, null);
                bindingProvider.getRequestContext().put(SSLSOCKET_FACTORY_PROPERTY, context.getSocketFactory());
                log.info("Initialized keystore: " + configuration.getClientCertificateKeyStore());
            } else {
                log.warn("no keystore path given, will use default ssl factory for jax-ws");
            }

        } catch (Exception e) {
            throw new IdmServiceException("idm.access.error", e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    log.warn("Cannot close input stream", e);
                }
            }
        }
    }
}
