package ch.bfs.meb.ssp.server.service;

import javax.activation.DataHandler;
import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.xml.bind.annotation.XmlMimeType;
import javax.xml.ws.soap.MTOM;

import org.springframework.security.access.prepost.PreAuthorize;

import com.sun.xml.ws.developer.StreamingAttachment;

import ch.bfs.meb.server.commons.integration.dto.UploadResult;
import ch.bfs.meb.server.commons.service.AbstractMebWebService;
import ch.bfs.meb.server.commons.service.impl.IUploadService;
import ch.bfs.meb.util.SecurityConstants;

/*
 * Important: if changes are made, please consult the developer documentation, cf 35_MEB Entwicklerhandbuch.docx, ch "8.1.3 Änderungen an XXXUploadWebService")
 */

@MTOM
@StreamingAttachment(parseEagerly = true, memoryThreshold = 4000000L)
@WebService(serviceName = "SspUploadWebService", name = "SspUploadWebServicePortType")
public class SspUploadService extends AbstractMebWebService<IUploadService> {
    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SSP_DL + "')")
    public UploadResult uploadDelivery(@XmlMimeType("application/octet-stream") DataHandler data, String locale) {
        return getService().deliver(null, 0L, data, locale);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SSP_DL + "')")
    public UploadResult uploadWizardDelivery(String dlUser, Long version, @XmlMimeType("application/octet-stream") DataHandler data, String locale) {
        return getService().deliver(dlUser, version, data, locale);
    }
}