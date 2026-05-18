package ch.bfs.meb.security.idm;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.mail.internet.InternetAddress;
import javax.management.Notification;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.util.Assert;

import ch.bfs.meb.configuration.IConfiguration;
import ch.bfs.meb.configuration.IConfigurationChangedListener;
import ch.bfs.meb.util.Canton;
import ch.bfs.meb.util.CodegroupUtility;
import ch.bfs.meb.util.SecurityConstants;
import ch.bfs.meb.util.StringUtils;
import ch.bfs.meb.web.ws.adminservice.*;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StopWatch;

/**
 * Service interface to the nevisIDM security system of BFS
 */
@Slf4j
@EnableCaching
public class NevisIdmUserService implements IIdmUserService, IConfigurationChangedListener {

    /**
     * Default values to use in user query
     */
    protected static final int MAX_NUM_RECORDS = 1000;
    private static final String CLIENT_EXT_ID = "1200";
    private static final String SORT_FIELD = "name";

    @Setter
    private INevisIdmUserServiceProvider nevisIdmUserServiceProvider;

    @Setter
    private IConfiguration configuration;

    @PostConstruct
    public void afterPropertiesSet() {
        Assert.notNull(configuration, "Configuration must be set");
        Assert.notNull(nevisIdmUserServiceProvider, "IDM serviceProvider provider must be set");
        // init webservice
        nevisIdmUserServiceProvider.init(configuration.getIdmServerURL(), configuration.getClientCertificateKeyStore(), configuration.getClientCertificateKeyStorePassword());
    }

    @Override
    public void configurationChanged(Notification notification) {
        nevisIdmUserServiceProvider.init(configuration.getIdmServerURL(), configuration.getClientCertificateKeyStore(), configuration.getClientCertificateKeyStorePassword());
    }

    @Override
    @Cacheable(value = "getCantons", cacheManager = "cacheManager")
    public String getCantons(String userEmail) {

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        log.debug("Get cantons for user: {}", userEmail);

        final String[] cantons = { StringUtils.EMPTY_STRING };

        UserQuery userQuery = createBaseQuery();
        userQuery.getUser().setEmail(userEmail);

        List<ch.bfs.meb.web.ws.adminservice.User> users = getUsers(userQuery);

        users.stream().findFirst().ifPresent(user -> {

            // only consider active profiles
            user.getProfiles().stream().filter(profile -> ProfileState.ACTIVE.equals(profile.getState())).forEach(profile -> {

                String prefix = cantons[0].length() == 0 ? StringUtils.EMPTY_STRING : ",";
                // get all cantons from appropriate roles
                String profileCantons = profile.getRoles().stream().filter(role -> Canton.isCantonRole(role.getName()))
                        .map(role -> Canton.getCanton(role.getName()).getId().toString()).collect(Collectors.joining(","));
                cantons[0] = cantons[0].concat(prefix.concat(profileCantons));
            });

        });

        if (log.isDebugEnabled()) {
            log.debug("Return cantons: " + cantons[0]);
        }

        stopWatch.stop();
        log.info("Get cantons for user {} took {} msec.", userEmail, stopWatch.getTotalTimeMillis());

        return cantons[0];
    }

    @Override
    @Cacheable(value = "getUser", cacheManager = "cacheManager")
    public User getUser(String userEmail) {

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        log.debug("Get user for email: {}", userEmail);
        final User[] result = { null };

        UserQuery userQuery = createBaseQuery();
        userQuery.getUser().setEmail(userEmail);

        List<ch.bfs.meb.web.ws.adminservice.User> users = getUsers(userQuery);

        users.stream().findFirst().ifPresent((ch.bfs.meb.web.ws.adminservice.User user) -> {
            result[0] = map(user);
        });

        stopWatch.stop();
        log.info("Get user for user {} took {} msec.", userEmail, stopWatch.getTotalTimeMillis());

        return result[0];
    }

    @Override
    @Cacheable(value = "getDVMailAddresses", cacheManager = "cacheManager")
    public List<InternetAddress> getDVMailAddresses(Long application, Long canton) {

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        log.debug("Get DV Mail addresses for application: {} and canton: {}", application, canton);

        List<InternetAddress> result = new ArrayList<>();

        String roleName = StringUtils.EMPTY_STRING;

        if (Long.valueOf(CodegroupUtility.MEB_APPLICATION_SDL).equals(application)) {
            roleName = SecurityConstants.ROLE_SDL_DV;
        } else if (Long.valueOf(CodegroupUtility.MEB_APPLICATION_SSP).equals(application)) {
            roleName = SecurityConstants.ROLE_SSP_DV;
        } else if (Long.valueOf(CodegroupUtility.MEB_APPLICATION_SBA).equals(application)) {
            roleName = SecurityConstants.ROLE_SBA_DV;
        }

        UserQuery userQuery = createBaseQuery();
        addRole(userQuery, roleName);
        // TODO check if canton role can be added

        List<ch.bfs.meb.web.ws.adminservice.User> users = getUsers(userQuery);

        users.forEach(user -> {
            final boolean[] cantonValid = { canton == null };

            // only check canton if one is given
            if (!cantonValid[0]) {
                user.getProfiles().stream().filter(profile -> ProfileState.ACTIVE.equals(profile.getState())).forEach(profile -> {
                    // do not override when already found
                    if (!cantonValid[0]) {
                        cantonValid[0] = profile.getRoles().stream()
                                .filter(role -> Canton.isCantonRole(role.getName()) && Canton.getCanton(role.getName()).getId().equals(canton)).findAny()
                                .isPresent();
                    }
                });
            }

            if (cantonValid[0]) {
                InternetAddress address = new InternetAddress();
                address.setAddress(user.getEmail());
                result.add(address);
            }
        });

        if (log.isDebugEnabled()) {
            log.debug("result size: {}", result.size());
            result.forEach(internetAddress -> log.debug(ToStringBuilder.reflectionToString(internetAddress)));
        }

        stopWatch.stop();
        log.info("Get DV Mail addresses for application: {} and canton: {} took {} msec.", application, canton, stopWatch.getTotalTimeMillis());

        return result;
    }

    @Override
    @Cacheable(value = "getEVMailAddresses", cacheManager = "cacheManager")
    public List<InternetAddress> getEVMailAddresses(Long application) {

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        log.debug("Get EV Mail addresses for application: {}", application);

        List<InternetAddress> result = new ArrayList<>();

        String roleName = StringUtils.EMPTY_STRING;
        if (Long.valueOf(CodegroupUtility.MEB_APPLICATION_SDL).equals(application)) {
            roleName = SecurityConstants.ROLE_SDL_EV;
        } else if (Long.valueOf(CodegroupUtility.MEB_APPLICATION_SSP).equals(application)) {
            roleName = SecurityConstants.ROLE_SSP_EV;
        } else if (Long.valueOf(CodegroupUtility.MEB_APPLICATION_SBA).equals(application)) {
            roleName = SecurityConstants.ROLE_SBA_EV;
        }

        List<User> users = getUsersForRole(roleName);
        users.forEach(user -> {
            InternetAddress address = new InternetAddress();
            address.setAddress(user.getUsername());
            result.add(address);
        });

        if (log.isDebugEnabled()) {
            log.debug("result size: {}", result.size());
            result.forEach(internetAddress -> log.debug(ToStringBuilder.reflectionToString(internetAddress)));
        }

        stopWatch.stop();
        log.info("Get EV Mail addresses for application: {} took {} msec.", application, stopWatch.getTotalTimeMillis());
        return result;
    }

    @Override
    @Cacheable(value = "getUsersForRole", cacheManager = "cacheManager")
    public List<User> getUsersForRole(String roleName) {

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        log.debug("Get users for role: {}", roleName);

        UserQuery userQuery = createBaseQuery();
        addRole(userQuery, roleName);

        List<ch.bfs.meb.web.ws.adminservice.User> users = getUsers(userQuery);
        stopWatch.stop();
        log.info("Get users for role: {} took {} msec.", roleName, stopWatch.getTotalTimeMillis());
        return map(users);
    }

    @Override
    @Cacheable(value = "isUserInRole", cacheManager = "cacheManager")
    public boolean isUserInRole(String userEmail, String roleName) {

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        log.debug("Is user: {} in role: {}", userEmail, roleName);

        UserQuery userQuery = createBaseQuery();
        userQuery.getUser().setEmail(userEmail);
        addRole(userQuery, roleName);

        List<ch.bfs.meb.web.ws.adminservice.User> users = getUsers(userQuery);

        boolean result = users.stream().findFirst().isPresent();

        log.debug("isUserInRole: {}", result);
        stopWatch.stop();
        log.info("Is user: {} in role: {} took {} msec.", userEmail, roleName, stopWatch.getTotalTimeMillis());

        return result;
    }

    @Override
    @Cacheable(value = "isUserInRoleHierarchy", cacheManager = "cacheManager")
    public boolean isUserInRole(String userEmail, String roleName, String[] roleHierarchy) {

        boolean result = false;

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        log.debug("Is user: {} in role: {} and roleHierachy: {}", userEmail, roleName, roleHierarchy);

        boolean isInRoleHierarchy = false;
        for (String role : roleHierarchy) {
            if (role.equals(roleName)) {
                isInRoleHierarchy = true;
            }
        }

        if (isInRoleHierarchy) {
            result = isUserInRole(userEmail, roleName);
        }

        log.debug("isUserInRole: {}", result);
        stopWatch.stop();
        log.info("Is user: {} in role: {} and roleHierachy: {} took {} msec.", userEmail, roleName, roleHierarchy, stopWatch.getTotalTimeMillis());

        return result;

    }

    /**
     * Maps the given users from idm.
     *
     * @param idmUsers list of {@link ch.bfs.meb.web.ws.adminservice.User}
     * @return list of {@link User} initialized with idm users information
     */
    private List<User> map(List<ch.bfs.meb.web.ws.adminservice.User> idmUsers) {

        List<User> result = new ArrayList<>();

        idmUsers.forEach(idmUser -> {
            result.add(map(idmUser));
        });

        if (log.isDebugEnabled()) {
            result.forEach(user -> log.debug("mapped user: " + ToStringBuilder.reflectionToString(user)));
        }

        return result;
    }

    /**
     * Maps the given user from idm.
     *
     * @param idmUser user from nevisIDM
     * @return mapped user
     */
    private User map(ch.bfs.meb.web.ws.adminservice.User idmUser) {

        User user = new User();
        user.setGuid(idmUser.getLoginId());
        user.setActive(UserState.ACTIVE.equals(idmUser.getState()));
        user.setUsername(idmUser.getEmail());

        user.setBusinessPhone(idmUser.getTelephone());
        user.setGivenName(idmUser.getFirstName());
        user.setSurname(idmUser.getName());

        List<Canton> cantons = new ArrayList<>();

        // get all active profiles
        idmUser.getProfiles().stream().filter(profile -> ProfileState.ACTIVE.equals(profile.getState())).forEach(profile -> {
            // get all canton roles
            profile.getRoles().stream().filter(role -> Canton.isCantonRole(role.getName())).forEach(role -> {
                cantons.add(Canton.getCanton(role.getName()));
            });
        });

        user.setCantons(cantons);

        return user;

    }

    /**
     * Creates an user query initialized with all base attributes (client id, detail level, app name, ...)
     * 
     * @return new {@link UserQuery}
     */
    private UserQuery createBaseQuery() {

        UserQuery userQuery = new UserQuery();
        userQuery.setClientExtId(CLIENT_EXT_ID);
        userQuery.setSkipRecords(0);
        userQuery.setNumRecords(MAX_NUM_RECORDS);
        userQuery.setSortByField(SORT_FIELD);
        userQuery.setSortOrder(SortOrder.ASC);

        // DetailLevel.MEDIUM as default
        DetailLevels detailLevels = new DetailLevels();
        detailLevels.setDefaultDetailLevel(DetailLevel.MEDIUM);
        userQuery.setDetailLevels(detailLevels);

        // only get MEB users with active profiles
        ch.bfs.meb.web.ws.adminservice.User user = new ch.bfs.meb.web.ws.adminservice.User();
        Profile profile = new Profile();
        profile.setState(ProfileState.ACTIVE);
        user.getProfiles().add(profile);
        userQuery.setUser(user);

        return userQuery;
    }

    /**
     * Adds a new role with the given name to the first profile of the user query (if it has one).
     * 
     * @param userQuery user query where the role should be added
     * @param roleName name of role
     * @return created role
     */
    private Role addRole(UserQuery userQuery, String roleName) {
        Role role = new Role();
        role.setName(roleName);
        role.setApplicationName(SecurityConstants.EIAM_APPLICATION_NAME_MEB);
        userQuery.getUser().getProfiles().stream().findFirst().ifPresent(profile -> profile.getRoles().add(role));
        return role;
    }

    /**
     * Executes given user query and performs multiple executions if necessary -> MAX_NUM_RECORDS are processed 
     * 
     * @param userQuery user query to execute
     * @return query result of all executions
     */
    private List<ch.bfs.meb.web.ws.adminservice.User> getUsers(UserQuery userQuery) {

        int resultSize = 0;
        List<ch.bfs.meb.web.ws.adminservice.User> users = new ArrayList<>();
        do {
            if (resultSize == MAX_NUM_RECORDS) {

                log.debug("max num records returned, load next users now ...");
                userQuery.setSkipRecords(userQuery.getSkipRecords() + MAX_NUM_RECORDS);
            }
            List<ch.bfs.meb.web.ws.adminservice.User> tmpUsers = nevisIdmUserServiceProvider.getUsers(userQuery);
            resultSize = tmpUsers.size();
            users.addAll(tmpUsers);
        } while (resultSize == MAX_NUM_RECORDS);

        return users;
    }

}
