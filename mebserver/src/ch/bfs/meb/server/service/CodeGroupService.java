package ch.bfs.meb.server.service;

import javax.jws.WebMethod;
import javax.jws.WebService;

import org.springframework.security.access.prepost.PreAuthorize;

import ch.bfs.meb.server.commons.service.AbstractMebWebService;
import ch.bfs.meb.server.integration.dto.CodeGroupListResult;
import ch.bfs.meb.server.service.impl.ICodeGroupService;
import ch.bfs.meb.util.SecurityConstants;

/*
 * Important: if changes are made to this file, please copy the file mebserver/target/wsdl/CodeGroupWebService.wsdl,
 * which is built during the maven build of this module, to the folder mebwsclient/src/wsdl,
 * thus overwriting the file mebwsclient/src/wsdl/CodeGroupWebService.wsdl
 */

@WebService(serviceName = "CodeGroupWebService", name = "CodeGroupWebServicePortType")
public class CodeGroupService extends AbstractMebWebService<ICodeGroupService> {
    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_MEB_RO + "')")
    public CodeGroupListResult getCodesForGroup(String groupId, String language) {
        return getService().getCodesForGroup(groupId, language);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_MEB_RO + "')")
    public CodeGroupListResult getActualCodesForGroup(String groupId, String language) {
        return getService().getActualCodesForGroup(groupId, language);
    }
}