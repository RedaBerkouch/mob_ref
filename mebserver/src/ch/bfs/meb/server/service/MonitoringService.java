package ch.bfs.meb.server.service;

import javax.jws.WebMethod;
import javax.jws.WebService;

import org.springframework.security.access.prepost.PreAuthorize;

import ch.bfs.meb.server.commons.service.AbstractMebWebService;
import ch.bfs.meb.server.service.impl.IMonitoringService;
import ch.bfs.meb.util.SecurityConstants;

/*
 * Important: if changes are made to this file, please copy the file mebserver/target/wsdl/MonitoringWebService.wsdl,
 * which is built during the maven build of this module, to the folder mebwsclient/src/wsdl,
 * thus overwriting the file mebwsclient/src/wsdl/MonitoringWebService.wsdl
 */

@WebService(serviceName = "MonitoringWebService", name = "MonitoringWebServicePortType")
public class MonitoringService extends AbstractMebWebService<IMonitoringService> {
    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_MEB_RO + "')")
    public Boolean checkIdmService() {
        return getService().checkIdmService();
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_MEB_RO + "')")
    public Boolean checkSasService() {
        return getService().checkSasService();
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_MEB_RO + "')")
    public Boolean checkMetastatService() {
        return getService().checkMetastatService();
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_MEB_RO + "')")
    public Boolean checkBurService() {
        return getService().checkBurService();
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_MEB_RO + "')")
    public Boolean checkDatabase() {
        return getService().checkDatabase();
    }
}