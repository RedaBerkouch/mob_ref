package ch.bfs.meb.ssp.web.service;

import ch.bfs.meb.ssp.web.frontend.dto.CommonSearchRequest;
import ch.bfs.meb.web.commons.dhtmlx.table.WebFilter;
import ch.bfs.meb.web.commons.dhtmlx.table.WebFilterContext;
import ch.bfs.meb.web.commons.dhtmlx.table.WebParameter;
import ch.bfs.meb.web.commons.i18n.WebLocalizationManager;
import ch.bfs.meb.web.commons.util.IFilterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service("webFilterService")
public class WebFilterService {
    
    @Autowired
    private IFilterService filterService;
    @Autowired
    private WebLocalizationManager localizationManager;
    
    public WebFilterContext filterContextFromSearchRequest(CommonSearchRequest request, Long refObject) {
        List<WebFilter> filteredWebFilters = new ArrayList<>();
        
        // Jointure des filtres basée sur filterId
        if (!CollectionUtils.isEmpty(request.getWebFilters())) {
            // Récupérer tous les filtres disponibles pour cet objet de référence
            List<WebFilter> actualWebFilters = filterService.getFiltersForRefObject(refObject).getFilters();
            
            // Créer une Map pour un accès rapide aux filtres du body par filterId
            Map<Long, WebFilter> bodyFiltersMap = request.getWebFilters().stream()
                    .collect(Collectors.toMap(WebFilter::getFilterId, Function.identity()));
            
            // Ne garder que les filtres qui sont dans le body
            filteredWebFilters = actualWebFilters.stream()
                    .filter(actualFilter -> bodyFiltersMap.containsKey(actualFilter.getFilterId()))
                    .peek(actualFilter -> {
                        WebFilter bodyFilter = bodyFiltersMap.get(actualFilter.getFilterId());
                        actualFilter.setIsDefault(true);
                        // Créer une Map des paramètres du body pour accès rapide par parameterId
                        if (bodyFilter.getParameters() != null && !bodyFilter.getParameters().isEmpty()) {
                            Map<Long, WebParameter> bodyParamsMap = bodyFilter.getParameters().stream()
                                    .collect(Collectors.toMap(WebParameter::getParameterId, Function.identity()));
                            
                            // Surcharger les defaultValue des paramètres du filtre actuel
                            if (actualFilter.getParameters() != null) {
                                actualFilter.getParameters().forEach(actualParam -> {
                                    WebParameter bodyParam = bodyParamsMap.get(actualParam.getParameterId());
                                    if (bodyParam != null && bodyParam.getDefaultValue() != null) {
                                        actualParam.setDefaultValue(bodyParam.getDefaultValue());
                                    }
                                });
                            }
                        }
                        
                    })
                    .collect(Collectors.toList());
        }
        
        WebFilterContext filterContext = new WebFilterContext();
        filterContext.setFilter(filteredWebFilters);
        filterContext.setWhereFilter(request.getWhereFilters());
        filterContext.setLocale(localizationManager.getLocale().toString());
        return filterContext;
    }
}
