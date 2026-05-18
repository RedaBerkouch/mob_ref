package ch.admin.bfs.sbg.webservice;

import javax.jws.WebMethod;
import javax.jws.WebService;

import ch.bfs.meb.server.commons.integration.dto.Filter;
import ch.bfs.meb.server.commons.integration.dto.FilterListResult;
import ch.bfs.meb.server.commons.integration.dto.FilterResult;
import ch.bfs.meb.server.commons.service.AbstractMebWebService;
import ch.bfs.meb.server.commons.service.impl.IFilterService;

@WebService(serviceName = "SbgFilterWebService", name = "SbgFilterWebServicePortType")
public class SbgFilterService extends AbstractMebWebService<IFilterService> {
    @WebMethod
    public FilterListResult getFilter() {
        return getService().getFilters();
    }

    @WebMethod
    public FilterListResult getFiltersForRefObjectAndNameDe(Long refObject, String nameDe) {
        return getService().getFiltersForRefObjectAndNameDe(refObject, nameDe);
    }

    @WebMethod
    public FilterListResult getFiltersForRefObject(Long refObject) {
        return getService().getFiltersForRefObject(refObject);
    }

    @WebMethod
    public FilterResult getFilterById(Long id) {
        return getService().getFilterById(id);
    }

    @WebMethod
    public FilterResult updateFilter(Filter filter) {
        return getService().updateFilter(filter);
    }

    @WebMethod
    public FilterResult insertFilter(Filter filter) {
        return getService().insertFilter(filter);
    }

    @WebMethod
    public FilterResult deleteFilter(Filter filter) {
        return getService().deleteFilter(filter);
    }
}