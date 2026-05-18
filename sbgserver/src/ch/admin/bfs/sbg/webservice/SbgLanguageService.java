package ch.admin.bfs.sbg.webservice;

import javax.jws.WebMethod;
import javax.jws.WebService;

import ch.admin.bfs.sbg.transfer.LocalizedCodeList;
import ch.bfs.meb.server.commons.service.AbstractMebWebService;

@WebService(serviceName = "SbgLanguageWebService", name = "SbgLanguageWebServicePortType")
public class SbgLanguageService extends AbstractMebWebService<ILanguageService> {
    @WebMethod
    public LocalizedCodeList getAllCodeByCodeGroupAndLocale(String codegroupId, String locale) {
        return getService().getAllCodeByCodeGroupAndLocale(codegroupId, locale);
    }
}