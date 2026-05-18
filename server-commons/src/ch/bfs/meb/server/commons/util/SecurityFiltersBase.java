package ch.bfs.meb.server.commons.util;

import java.util.*;

import org.springframework.security.core.context.SecurityContextHolder;

import ch.bfs.meb.security.MebUser;
import ch.bfs.meb.security.idm.IIdmUserService;
import ch.bfs.meb.server.commons.codes.ICodegroupManager;
import ch.bfs.meb.server.commons.integration.dto.CodeGroup;
import ch.bfs.meb.server.commons.integration.dto.Filter;
import ch.bfs.meb.server.commons.integration.dto.Parameter;
import ch.bfs.meb.server.commons.integration.repository.IFilterCommonsRepository;
import ch.bfs.meb.util.CodegroupUtility;

public abstract class SecurityFiltersBase implements ISecurityFilters {
    protected ICodegroupManager _codegroupManager;
    protected IFilterCommonsRepository _filterRepository;
    protected IIdmUserService _idmService;

    protected final Map<String, DataHolder> _filtersForRole;
    protected List<Long> _allCantons = null;

    protected class DataHolder {
        protected String _actCantons = "";
        protected String _actUsername = "";
        protected final List<Filter> _actFilters = new ArrayList<Filter>();
        protected final List<String> _actTableNames = new ArrayList<String>();
        protected final List<Filter> _cantonFilters = new ArrayList<Filter>();
        protected final List<String> _cantonTableNames = new ArrayList<String>();
        protected final List<Filter> _configDeliveryFilters = new ArrayList<Filter>();
        protected final List<String> _configDeliveryTableNames = new ArrayList<String>();
    }

    public SecurityFiltersBase() {
        _filtersForRole = new HashMap<String, DataHolder>();
        for (String role : getRoles()) {
            _filtersForRole.put(role, new DataHolder());
        }
    }

    public void setCodegroupManager(ICodegroupManager codegroupManager) {
        _codegroupManager = codegroupManager;
    }

    public void setFilterRepository(IFilterCommonsRepository filterRepository) {
        _filterRepository = filterRepository;
    }

    public void setIdmService(IIdmUserService service) {
        _idmService = service;
    }

    /**
     * get roles in ascending order
     */
    protected abstract List<String> getRoles();

    protected abstract boolean isInRole(String role);

    /**
     * add information for one canton filter
     */
    protected void addCantonFilter(Long refObject, String source, String maxRole, String tableName) {
        Filter filter = new Filter();
        filter.setIsActive(true);
        filter.setIsDefault(true);
        filter.setRefObject(refObject);
        filter.setSource(source);
        Parameter parameter = new Parameter();
        parameter.setFilterId(filter.getFilterId());
        parameter.setUniqueName("%1");
        filter.getParameters().add(parameter);

        for (int i = 0; i < getRoles().size(); ++i) {
            _filtersForRole.get(getRoles().get(i))._cantonFilters.add(filter);
            _filtersForRole.get(getRoles().get(i))._cantonTableNames.add(" " + tableName);

            if (maxRole.equals(getRoles().get(i))) {
                break;
            }
        }
    }

    protected void addConfigDeliveryFilter(Long refObject, String source, String role, String tableName) {
        Filter filterRole = new Filter();
        filterRole.setIsActive(true);
        filterRole.setIsDefault(true);
        filterRole.setRefObject(refObject);
        filterRole.setSource(source);
        Parameter parameter = new Parameter();
        parameter.setFilterId(filterRole.getFilterId());
        parameter.setUniqueName("%1");
        filterRole.getParameters().add(parameter);
        parameter = new Parameter();
        parameter.setFilterId(filterRole.getFilterId());
        parameter.setUniqueName("%2");
        filterRole.getParameters().add(parameter);
        if (source.indexOf("%3") >= 0) {
            parameter = new Parameter();
            parameter.setFilterId(filterRole.getFilterId());
            parameter.setUniqueName("%3");
            filterRole.getParameters().add(parameter);
        }

        for (int i = 0; i < getRoles().size(); ++i) {
            if (role.equals(getRoles().get(i))) {
                _filtersForRole.get(getRoles().get(i))._configDeliveryFilters.add(filterRole);
                _filtersForRole.get(getRoles().get(i))._configDeliveryTableNames.add(" " + tableName);
                break;
            }
        }
    }

    /**
     * Cash filters for a specific list of cantons (other cantons in list --> refill)
     * and a specific user (other user --> refill)
     */
    protected DataHolder prepareData(DataHolder filters) {
        MebUser user = (MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String cantons = user.getCantonsAsString();
        if (!cantons.equals(filters._actCantons) || !user.getEmail().toLowerCase().equals(filters._actUsername.toLowerCase())) {
            filters._actCantons = cantons;
            filters._actUsername = user.getEmail();
            filters._actFilters.clear();
            filters._actTableNames.clear();

            for (Filter cantonFilter : filters._cantonFilters) {
                Filter filter = new Filter();
                filter.setIsActive(cantonFilter.getIsActive());
                filter.setIsDefault(cantonFilter.getIsDefault());
                filter.setRefObject(cantonFilter.getRefObject());
                filter.setSource(cantonFilter.getSource());
                Parameter cantonParameter = cantonFilter.getParameters().get(0);
                Parameter parameter = new Parameter();
                parameter.setFilterId(filter.getFilterId());
                parameter.setUniqueName(cantonParameter.getUniqueName());
                parameter.setDefaultValue(cantons);
                filter.getParameters().add(parameter);
                filters._actFilters.add(filter);
                filters._actTableNames.add(filters._cantonTableNames.get(filters._cantonFilters.indexOf(cantonFilter)));
            }

            String version = getActVersion().toString();

            for (Filter configDeliveryFilter : filters._configDeliveryFilters) {
                Filter filter = new Filter();
                filter.setIsActive(configDeliveryFilter.getIsActive());
                filter.setIsDefault(configDeliveryFilter.getIsDefault());
                filter.setRefObject(configDeliveryFilter.getRefObject());
                filter.setSource(configDeliveryFilter.getSource());
                Parameter configDeliveryParameter = configDeliveryFilter.getParameters().get(0);
                Parameter parameter = new Parameter();
                parameter.setFilterId(filter.getFilterId());
                parameter.setUniqueName(configDeliveryParameter.getUniqueName());
                parameter.setDefaultValue(user.getEmail().toLowerCase());
                filter.getParameters().add(parameter);
                configDeliveryParameter = configDeliveryFilter.getParameters().get(1);
                parameter = new Parameter();
                parameter.setFilterId(filter.getFilterId());
                parameter.setUniqueName(configDeliveryParameter.getUniqueName());
                parameter.setDefaultValue(version);
                filter.getParameters().add(parameter);
                if (configDeliveryFilter.getParameters().size() > 2) {
                    configDeliveryParameter = configDeliveryFilter.getParameters().get(2);
                    parameter = new Parameter();
                    parameter.setFilterId(filter.getFilterId());
                    parameter.setUniqueName(configDeliveryParameter.getUniqueName());
                    parameter.setDefaultValue(user.getEmail().toLowerCase());
                    filter.getParameters().add(parameter);
                }
                filters._actFilters.add(filter);
                filters._actTableNames.add(filters._configDeliveryTableNames.get(filters._configDeliveryFilters.indexOf(configDeliveryFilter)));
            }
        }
        return filters;
    }

    /**
     * Evaluate filters to be used for actual user
     */
    protected DataHolder getData() {
        for (int i = 1; i < getRoles().size(); ++i) {
            if (!isInRole(getRoles().get(i))) {
                return prepareData(_filtersForRole.get(getRoles().get(i - 1)));
            }
        }
        return prepareData(_filtersForRole.get(getRoles().get(getRoles().size() - 1)));
    }

    @Override
    public Filter getFilter(int index) {
        synchronized (_filtersForRole) {
            return getData()._actFilters.get(index);
        }
    }

    @Override
    public int getNrOfFilters() {
        synchronized (_filtersForRole) {
            return getData()._actFilters.size();
        }
    }

    @Override
    public String getTableName(Filter filter) {
        synchronized (_filtersForRole) {
            int id = getData()._actFilters.indexOf(filter);
            if (id >= 0) {
                return getData()._actTableNames.get(id);
            } else {
                return null;
            }
        }
    }

    protected List<Long> getAllCantons() {
        if (_codegroupManager.isInitialized()) {
            synchronized (this) {
                if (_allCantons == null) {
                    _allCantons = new ArrayList<Long>();
                    _allCantons.add(new Long(-1));
                    List<CodeGroup> allCantonts = _codegroupManager.getCodeGroupsByGroupIdAndLanguage(CodegroupUtility.CANTON, "de");

                    Set<Long> temp = new HashSet<Long>();
                    for (CodeGroup codeGroup : allCantonts) {
                        temp.add(codeGroup.getCode());
                    }

                    _allCantons.addAll(temp);
                    Collections.sort(_allCantons);
                }
            }

            return _allCantons;
        } else {
            List<Long> emptyCantons = new ArrayList<Long>();
            emptyCantons.add(new Long(-1));
            ;
            return emptyCantons;
        }

    }

    public List<Long> getFilterCantonsForActUser() {
        return getAllCantons();
    }

    protected abstract List<String> getEvUserRoles();

    public List<Long> getCantonsForUser(String userName) {
        for (String role : getEvUserRoles()) {
            if (_idmService.isUserInRole(userName, role)) {
                return getAllCantons();
            }
        }

        List<Long> cantonList = new ArrayList<Long>();
        String cantons = _idmService.getCantons(userName);
        for (String canton : cantons.split(",")) {
            try {
                cantonList.add(Long.parseLong(canton));
            } catch (NumberFormatException e) {
                // nothing to do, no cantons for user (misconfiguration)
            }
        }
        return cantonList;
    }

    public Long getActVersion() {
        return _filterRepository.getActVersion();
    }

    public Long getInitVersion() {
        return _filterRepository.getInitVersion();
    }
}
