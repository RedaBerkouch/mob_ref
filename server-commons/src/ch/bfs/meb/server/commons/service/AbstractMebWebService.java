package ch.bfs.meb.server.commons.service;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import javax.annotation.Resource;
import javax.servlet.ServletContext;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * Base class for MEB webservices.
 */
public abstract class AbstractMebWebService<T extends Object> {

    /** Logging */
    private final static Logger LOG = LoggerFactory.getLogger(AbstractMebWebService.class);

    @Resource
    private WebServiceContext wsContext;

    /** Service bean */
    private volatile T service;

    /**
     * Returns the service implementation for the specified class T.
     * 
     * @return Service or <code>null</code>
     */
    protected T getService() {

        // Lazy initialisation with double checked locking
        if (service == null) {
            synchronized (this) {
                if (service == null) {
                    ServletContext servletContext = (ServletContext) wsContext.getMessageContext().get(MessageContext.SERVLET_CONTEXT);
                    WebApplicationContext webContext = WebApplicationContextUtils.getWebApplicationContext(servletContext);
                    Class<?> serviceClass = GenericUtil.INSTANCE.getGenericClass(this);
                    service = (T) webContext.getBean(serviceClass);
                    LOG.info("Webservice " + serviceClass.getName() + " initialised");
                }
            }
        }

        return service;
    }

    public enum GenericUtil {

        INSTANCE;

        public Class<?> getGenericClass(Object classObject) {
            Type t = classObject.getClass().getGenericSuperclass();
            return arg(t);
        }

        public Class<?> getGenericInterface(Object classObject) {
            Type t = classObject.getClass().getGenericInterfaces()[0];
            return arg(t);
        }

        private Class<?> arg(Type t) {
            return (Class<?>) ((ParameterizedType) t).getActualTypeArguments()[0];
        }
    }

}
