package ch.admin.bfs.sbg.webservice;

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
 * Important: if changes are made, please consult the developer documentation,
 * cf 35_MEB Entwicklerhandbuch.docx, ch "8.1.3 Änderungen an XXXUploadWebService")
 */

@MTOM
@StreamingAttachment(parseEagerly = true, memoryThreshold = 4000000L)
@WebService(serviceName = "SbgUploadWebService", name = "SbgUploadWebServicePortType")
public class SbgUploadService extends AbstractMebWebService<IUploadService> {
    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SBG_DL + "')")
    public UploadResult uploadDelivery(@XmlMimeType("application/octet-stream") DataHandler data, String locale) {
        return getService().deliver(null, 0L, data, locale);
    }
}