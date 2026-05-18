package ch.bfs.meb.security.idm;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.mail.internet.InternetAddress;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import ch.bfs.meb.util.Canton;
import ch.bfs.meb.util.CodegroupUtility;
import ch.bfs.meb.util.SecurityConstants;
import ch.bfs.meb.util.StringUtils;
import ch.bfs.meb.web.ws.adminservice.*;
import ch.bfs.meb.web.ws.adminservice.User;

/**
 * Unit test for {@link NevisIdmUserService}
 */
@RunWith(MockitoJUnitRunner.class)
public class NevisIdmUserServiceTest {

    @Mock
    private INevisIdmUserServiceProvider nevisIdmUserServiceProvider;

    @InjectMocks
    private NevisIdmUserService nevisIdmUserService;

    @Test
    public void shouldGetOneCantonFromOneRole() {

        Canton canton = Canton.ZH;

        User user = new User();
        user.setEmail("nevis@idm.ch");
        Profile profile = new Profile();
        profile.setState(ProfileState.ACTIVE);
        Role cantonRole = new Role();
        cantonRole.setName(canton.getRoleName());
        profile.getRoles().add(cantonRole);
        user.getProfiles().add(profile);

        when(nevisIdmUserServiceProvider.getUsers(any(UserQuery.class))).thenReturn(Collections.singletonList(user));

        String cantons = nevisIdmUserService.getCantons(user.getEmail());
        assertThat("one canton is found", cantons, is(canton.getId().toString()));
    }

    @Test
    public void shouldGetOneCantonFromTwoRoles() {

        Canton canton = Canton.ZH;

        User user = new User();
        user.setEmail("nevis@idm.ch");

        Profile profile = new Profile();
        profile.setState(ProfileState.ACTIVE);
        user.getProfiles().add(profile);

        Role cantonRole = new Role();
        cantonRole.setName(canton.getRoleName());
        profile.getRoles().add(cantonRole);

        Role appRole = new Role();
        appRole.setName(SecurityConstants.ROLE_SBA_DV);
        profile.getRoles().add(appRole);

        when(nevisIdmUserServiceProvider.getUsers(any(UserQuery.class))).thenReturn(Collections.singletonList(user));

        String cantons = nevisIdmUserService.getCantons(user.getEmail());
        assertThat("one canton is found", cantons, is(canton.getId().toString()));
    }

    @Test
    public void shouldGetTwoCantonsFromOneProfile() {

        Canton canton1 = Canton.ZH;
        Canton canton2 = Canton.BE;

        User user = new User();
        user.setEmail("nevis@idm.ch");
        Profile profile = new Profile();
        profile.setState(ProfileState.ACTIVE);
        Role cantonRole1 = new Role();
        cantonRole1.setName(canton1.getRoleName());
        profile.getRoles().add(cantonRole1);
        Role cantonRole2 = new Role();
        cantonRole2.setName(canton2.getRoleName());
        profile.getRoles().add(cantonRole2);
        user.getProfiles().add(profile);

        when(nevisIdmUserServiceProvider.getUsers(any(UserQuery.class))).thenReturn(Collections.singletonList(user));

        String cantons = nevisIdmUserService.getCantons(user.getEmail());
        assertThat("two cantons are found", cantons, is(canton1.getId().toString() + "," + canton2.getId().toString()));
    }

    @Test
    public void shouldNotGetOneCantonFromRole() {

        Canton canton = Canton.ZH;

        User user = new User();
        user.setEmail("nevis@idm.ch");

        Profile profile = new Profile();
        profile.setState(ProfileState.DISABLED);
        user.getProfiles().add(profile);

        Role cantonRole = new Role();
        cantonRole.setName(canton.getRoleName());
        profile.getRoles().add(cantonRole);

        when(nevisIdmUserServiceProvider.getUsers(any(UserQuery.class))).thenReturn(Collections.singletonList(user));

        String cantons = nevisIdmUserService.getCantons(user.getEmail());
        assertThat("no canton is found", cantons, is(StringUtils.EMPTY_STRING));
    }

    @Test
    public void shouldGetTwoCantonsFromTwoProfiles() {

        Canton canton1 = Canton.ZH;
        Canton canton2 = Canton.BE;

        User user = new User();
        user.setEmail("nevis@idm.ch");

        Profile profile1 = new Profile();
        profile1.setState(ProfileState.ACTIVE);
        Role cantonRole1 = new Role();
        cantonRole1.setName(canton1.getRoleName());
        profile1.getRoles().add(cantonRole1);
        user.getProfiles().add(profile1);

        Profile profile2 = new Profile();
        profile2.setState(ProfileState.ACTIVE);
        Role cantonRole2 = new Role();
        cantonRole2.setName(canton2.getRoleName());
        profile2.getRoles().add(cantonRole2);
        user.getProfiles().add(profile2);

        when(nevisIdmUserServiceProvider.getUsers(any(UserQuery.class))).thenReturn(Collections.singletonList(user));

        String cantons = nevisIdmUserService.getCantons(user.getEmail());
        assertThat("two cantons are found", cantons, is(canton1.getId().toString() + "," + canton2.getId().toString()));
    }

    @Test
    public void shouldGetUserAndCanton() {

        User user = new User();
        user.setEmail("nevis@idm.ch");
        user.setState(UserState.ACTIVE);

        Profile profile = new Profile();
        profile.setState(ProfileState.ACTIVE);
        user.getProfiles().add(profile);

        Role cantonRole = new Role();
        cantonRole.setName(Canton.ZH.getRoleName());
        profile.getRoles().add(cantonRole);

        when(nevisIdmUserServiceProvider.getUsers(any(UserQuery.class))).thenReturn(Collections.singletonList(user));

        List<ch.bfs.meb.security.idm.User> users = nevisIdmUserService.getUsersForRole(SecurityConstants.ROLE_SBA_EA);
        assertThat("found one user", users.size(), is(1));
        assertThat("user is active", users.get(0).isActive(), is(true));
        assertThat("email is " + user.getEmail(), users.get(0).getUsername(), is(user.getEmail()));
        assertThat("found one canton", users.get(0).getCantons().size(), is(1));
        assertThat("canton is " + cantonRole.getName(), users.get(0).getCantons().get(0).getRoleName(), is(cantonRole.getName()));

    }

    @Test
    public void shouldNotGetUserCanton() {

        User user = new User();
        user.setEmail("nevis@idm.ch");
        user.setState(UserState.ACTIVE);

        Profile profile = new Profile();
        profile.setState(ProfileState.DISABLED);
        user.getProfiles().add(profile);

        Role cantonRole = new Role();
        cantonRole.setName(Canton.ZH.getRoleName());
        profile.getRoles().add(cantonRole);

        when(nevisIdmUserServiceProvider.getUsers(any(UserQuery.class))).thenReturn(Collections.singletonList(user));

        List<ch.bfs.meb.security.idm.User> users = nevisIdmUserService.getUsersForRole(SecurityConstants.ROLE_SBA_EA);
        assertThat("found one user", users.size(), is(1));
        assertThat("found no canton", users.get(0).getCantons().size(), is(0));

    }

    @Test
    public void shouldGetDvAddressFromOneProfile() {

        User user = new User();
        user.setEmail("nevis@idm.ch");
        user.setState(UserState.ACTIVE);

        Profile profile = new Profile();
        profile.setState(ProfileState.ACTIVE);
        user.getProfiles().add(profile);

        Role cantonRole = new Role();
        cantonRole.setName(Canton.ZH.getRoleName());
        profile.getRoles().add(cantonRole);

        when(nevisIdmUserServiceProvider.getUsers(any(UserQuery.class))).thenReturn(Collections.singletonList(user));

        List<InternetAddress> addresses = nevisIdmUserService.getDVMailAddresses(CodegroupUtility.MEB_APPLICATION_SBA, Canton.ZH.getId());
        assertThat("found one address", addresses.size(), is(1));
        assertThat("found no canton", addresses.get(0).getAddress(), is(user.getEmail()));

    }

    @Test
    public void shouldGetDvAddressFromTwoProfiles() {

        User user = new User();
        user.setEmail("nevis@idm.ch");
        user.setState(UserState.ACTIVE);

        Profile profile1 = new Profile();
        profile1.setState(ProfileState.ACTIVE);
        user.getProfiles().add(profile1);

        Role cantonRole1 = new Role();
        cantonRole1.setName(Canton.ZH.getRoleName());
        profile1.getRoles().add(cantonRole1);

        Profile profile2 = new Profile();
        profile2.setState(ProfileState.ACTIVE);
        user.getProfiles().add(profile2);

        Role cantonRole2 = new Role();
        cantonRole2.setName(Canton.BE.getRoleName());
        profile2.getRoles().add(cantonRole2);

        when(nevisIdmUserServiceProvider.getUsers(any(UserQuery.class))).thenReturn(Collections.singletonList(user));

        List<InternetAddress> addresses = nevisIdmUserService.getDVMailAddresses(CodegroupUtility.MEB_APPLICATION_SBA, Canton.ZH.getId());
        assertThat("found one address", addresses.size(), is(1));
        assertThat("address is " + user.getEmail(), addresses.get(0).getAddress(), is(user.getEmail()));

    }

    @Test
    public void shouldNotGetDvAddressWithCantonParameter() {

        User user = new User();
        user.setEmail("nevis@idm.ch");
        user.setState(UserState.ACTIVE);

        Profile profile = new Profile();
        profile.setState(ProfileState.ACTIVE);
        user.getProfiles().add(profile);

        Role cantonRole = new Role();
        cantonRole.setName(Canton.BE.getRoleName());
        profile.getRoles().add(cantonRole);

        when(nevisIdmUserServiceProvider.getUsers(any(UserQuery.class))).thenReturn(Collections.singletonList(user));

        List<InternetAddress> addresses = nevisIdmUserService.getDVMailAddresses(CodegroupUtility.MEB_APPLICATION_SBA, Canton.ZH.getId());
        assertThat("found no address", addresses.size(), is(0));

    }

    @Test
    public void shouldGetDvAddressWithoutCantonParameter() {

        User user = new User();
        user.setEmail("nevis@idm.ch");
        user.setState(UserState.ACTIVE);

        Profile profile = new Profile();
        profile.setState(ProfileState.ACTIVE);
        user.getProfiles().add(profile);

        when(nevisIdmUserServiceProvider.getUsers(any(UserQuery.class))).thenReturn(Collections.singletonList(user));

        List<InternetAddress> addresses = nevisIdmUserService.getDVMailAddresses(CodegroupUtility.MEB_APPLICATION_SBA, null);
        assertThat("found one address", addresses.size(), is(1));
        assertThat("address is " + user.getEmail(), addresses.get(0).getAddress(), is(user.getEmail()));

    }

    @Test
    public void shouldBeInRoleHierarchy() {

        User user = new User();
        user.setEmail("nevis@idm.ch");
        user.setState(UserState.ACTIVE);

        Profile profile = new Profile();
        profile.setState(ProfileState.ACTIVE);
        user.getProfiles().add(profile);

        when(nevisIdmUserServiceProvider.getUsers(any(UserQuery.class))).thenReturn(Collections.singletonList(user));

        boolean userInRole = nevisIdmUserService.isUserInRole(user.getEmail(), SecurityConstants.ROLE_SBA_EA, SecurityConstants.SBA_ROLE_HIERARCHY);
        assertThat("user is in role", userInRole, is(true));

    }

    @Test
    public void shouldNotBeInRoleHierarchy() {

        User user = new User();
        user.setEmail("nevis@idm.ch");
        user.setState(UserState.ACTIVE);

        Profile profile = new Profile();
        profile.setState(ProfileState.ACTIVE);
        user.getProfiles().add(profile);

        boolean userInRole = nevisIdmUserService.isUserInRole(user.getEmail(), SecurityConstants.ROLE_SBA_EA, SecurityConstants.SDL_ROLE_HIERARCHY);
        assertThat("user is not in role", userInRole, is(false));

    }

    @Test
    public void shouldGetAllUsers() {

        List<User> users1 = new ArrayList<>();
        for (int i = 0; i < NevisIdmUserService.MAX_NUM_RECORDS; i++) {
            users1.add(new User());
        }

        List<User> users2 = new ArrayList<>();
        int NUM_RECORDS = NevisIdmUserService.MAX_NUM_RECORDS - NevisIdmUserService.MAX_NUM_RECORDS / 4;
        for (int i = 0; i < NUM_RECORDS; i++) {
            users2.add(new User());
        }

        when(nevisIdmUserServiceProvider.getUsers(any(UserQuery.class))).thenReturn(users1).thenReturn(users2);

        List<ch.bfs.meb.security.idm.User> userInRole = nevisIdmUserService.getUsersForRole(SecurityConstants.ROLE_SBA_EA);
        assertThat("number of user is correct", userInRole.size(), is(users1.size() + users2.size()));

    }
}
